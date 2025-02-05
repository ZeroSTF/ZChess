package tn.zeros.zchess.engine.search;

import tn.zeros.zchess.core.logic.generation.LegalMoveFilter;
import tn.zeros.zchess.core.logic.generation.MoveGenerator;
import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.MoveUndoInfo;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.service.MoveExecutor;
import tn.zeros.zchess.engine.evaluate.EvaluationService;
import tn.zeros.zchess.engine.util.*;

public class SearchService {
    private final MoveOrderingService moveOrderingService = new MoveOrderingService();
    private final TranspositionTable transpositionTable = new TranspositionTable(1 << 21);

    public int alphaBetaPrune(int depth, int alpha, int beta, BoardState state, int currentPly, long searchEndTime) {
        SearchMetrics.getInstance().incrementNodes();

        if (System.currentTimeMillis() >= searchEndTime) {
            SearchMetrics.getInstance().incrementTimeouts();
            return SearchUtils.TIMEOUT_VALUE;
        }
        // Check transposition table
        final TranspositionTable.Entry ttEntry = getTranspositionEntry(state, currentPly);
        if (ttEntry != null && ttEntry.depth >= depth) {
            SearchMetrics.getInstance().incrementTranspositionHits();
            final int score = ttEntry.score;

            // Exact score can be returned immediately
            if (ttEntry.type == TTEntryType.EXACT) {
                return score;
            }

            // Bounds check
            if (ttEntry.type == TTEntryType.LOWER_BOUND && score >= beta) {
                return score;
            }
            if (ttEntry.type == TTEntryType.UPPER_BOUND && score <= alpha) {
                return score;
            }
        }

        if (depth == 0) {
            SearchMetrics.getInstance().incrementTerminalEvals();
            return quiescenceSearch(alpha, beta, state, currentPly, searchEndTime);
        }

        MoveGenerator.MoveList moves = MoveGenerator.generateAllMoves(state, false);
        if (moves.isEmpty()) {
            if (LegalMoveFilter.inCheck(state, state.isWhiteToMove())) {
                SearchMetrics.getInstance().incrementCheckmates();
                return -(SearchUtils.CHECKMATE_EVAL - currentPly);
            }
            return 0;
        }


        // If we have a TT entry, and it's a best move, swap it with the first move
        if (ttEntry != null && ttEntry.bestMove != Move.NULL_MOVE) {
            int ttBestMove = ttEntry.bestMove;
            for (int i = 0; i < moves.size; i++) {
                if (moves.moves[i] == ttBestMove) {
                    // Swap with the first move
                    int temp = moves.moves[0];
                    moves.moves[0] = moves.moves[i];
                    moves.moves[i] = temp;
                    break;
                }
            }
        }
        moveOrderingService.orderMoves(moves, state);

        int originalAlpha = alpha;
        int bestMove = Move.NULL_MOVE;
        int bestScore = SearchUtils.MIN_EVAL;

        for (int i = 0; i < moves.size; i++) {
            int move = moves.moves[i];
            MoveUndoInfo undoInfo = MoveExecutor.makeMove(state, move);
            int eval = -alphaBetaPrune(depth - 1, -beta, -alpha, state, currentPly + 1, searchEndTime);
            MoveExecutor.unmakeMove(state, undoInfo);

            if (eval > bestScore) {
                bestScore = eval;
                bestMove = move;
                alpha = Math.max(alpha, eval);
            }

            if (alpha >= beta) {
                storeTranspositionEntry(state, depth, beta, TTEntryType.LOWER_BOUND,
                        bestMove, currentPly);
                return beta;
            }
        }

        // Store position in transposition table
        TTEntryType entryType;
        int entryScore;
        if (bestScore <= originalAlpha) {
            entryType = TTEntryType.UPPER_BOUND;
            entryScore = originalAlpha;  // Use alpha, not bestScore
        } else if (bestScore >= beta) {
            entryType = TTEntryType.LOWER_BOUND;
            entryScore = beta;  // Use beta, not bestScore
        } else {
            entryType = TTEntryType.EXACT;
            entryScore = bestScore;
        }

        storeTranspositionEntry(state, depth, entryScore, entryType,
                bestMove, currentPly);

        return bestScore;
    }

    private int quiescenceSearch(int alpha, int beta, BoardState state, int currentPly, long searchEndTime) {
        SearchMetrics.getInstance().incrementQNodes();

        if (System.currentTimeMillis() >= searchEndTime) {
            return SearchUtils.TIMEOUT_VALUE;
        }

        // Keep the TT handling from your original code
        final TranspositionTable.Entry ttEntry = getTranspositionEntry(state, currentPly);
        if (ttEntry != null) {
            SearchMetrics.getInstance().incrementTranspositionHits();
            final int score = ttEntry.score;

            if (ttEntry.type == TTEntryType.EXACT) {
                return score;
            }
            if (ttEntry.type == TTEntryType.LOWER_BOUND) {
                alpha = Math.max(alpha, score);
            }
            if (ttEntry.type == TTEntryType.UPPER_BOUND) {
                beta = Math.min(beta, score);
            }
            if (alpha >= beta) {
                return score;  // Return the actual score, not beta or alpha
            }
        }

        int standPat = EvaluationService.evaluate(state);
        SearchMetrics.getInstance().incrementNonTerminalEvals();
        boolean inCheck = LegalMoveFilter.inCheck(state, state.isWhiteToMove());

        // Stand-pat cutoff and update
        if (!inCheck) {
            if (standPat >= beta) {
                storeTranspositionEntry(state, 0, beta, TTEntryType.LOWER_BOUND,
                        Move.NULL_MOVE, currentPly);
                return beta;
            }
            if (standPat > alpha) {
                alpha = standPat;
            }
        }

        // Generate captures (or all moves if in check)
        boolean capturesOnly = !inCheck;
        MoveGenerator.MoveList moves = MoveGenerator.generateAllMoves(state, capturesOnly);

        if (moves.isEmpty()) {
            int finalScore = inCheck ? -SearchUtils.CHECKMATE_EVAL : standPat;
            // Store terminal position
            storeTranspositionEntry(state, 0, finalScore, TTEntryType.EXACT,
                    Move.NULL_MOVE, currentPly);
            return finalScore;
        }

        // Order moves
        moveOrderingService.orderMoves(moves, state);

        int originalAlpha = alpha;
        int bestMove = Move.NULL_MOVE;
        int bestScore = standPat;

        // Use a more conservative margin for delta pruning
        int deltaMargin = EvalUtils.getPieceTypeValue(Piece.PAWN) + 200;

        for (int i = 0; i < moves.size; i++) {
            int move = moves.moves[i];
            int capturedPiece = Move.getCapturedPiece(move);
            int capturedValue = EvalUtils.getPieceTypeValue(capturedPiece);

            // More conservative delta pruning
            if (!inCheck && capturedValue != EvalUtils.NONE_VALUE) {
                if (capturedValue + deltaMargin + standPat <= alpha) {
                    continue;
                }
            }

            MoveUndoInfo undoInfo = MoveExecutor.makeMove(state, move);
            int eval = -quiescenceSearch(-beta, -alpha, state, currentPly + 1, searchEndTime);
            MoveExecutor.unmakeMove(state, undoInfo);

            if (eval > bestScore) {
                bestScore = eval;
                bestMove = move;
                alpha = Math.max(alpha, eval);
            }

            if (alpha >= beta) {
                // Store beta cutoff
                storeTranspositionEntry(state, 0, beta, TTEntryType.LOWER_BOUND,
                        bestMove, currentPly);
                return beta;
            }
        }

        // Store position in transposition table
        TTEntryType entryType;
        int entryScore;
        if (bestScore <= originalAlpha) {
            entryType = TTEntryType.UPPER_BOUND;
            entryScore = originalAlpha;
        } else if (bestScore >= beta) {
            entryType = TTEntryType.LOWER_BOUND;
            entryScore = beta;
        } else {
            entryType = TTEntryType.EXACT;
            entryScore = bestScore;
        }

        storeTranspositionEntry(state, 0, entryScore, entryType,
                bestMove, currentPly);

        return bestScore;
    }

    // TT helper methods =============================================
    public TranspositionTable.Entry getTranspositionEntry(BoardState state, int currentPly) {
        final long positionKey = state.getZobristKey();
        final TranspositionTable.Entry entry = transpositionTable.get(positionKey);

        if (entry != null) {
            // Always unadjust mate scores when retrieving from TT
            entry.score = SearchUtils.unadjustMateScore(entry.score, currentPly);
        }
        return entry;
    }

    public void storeTranspositionEntry(BoardState state, int depth, int score, TTEntryType type, int bestMove, int currentPly) {
        if (SearchUtils.isTimeout(score)) return;
        final long positionKey = state.getZobristKey();
        final int adjustedScore = SearchUtils.adjustMateScore(score, currentPly);
        transpositionTable.put(positionKey, depth, adjustedScore, type, bestMove, currentPly);
    }

    public void clearTT() {
        transpositionTable.clear();
    }
}
