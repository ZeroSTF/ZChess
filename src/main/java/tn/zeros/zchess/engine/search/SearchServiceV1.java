package tn.zeros.zchess.engine.search;

import tn.zeros.zchess.core.logic.generation.LegalMoveFilter;
import tn.zeros.zchess.core.logic.generation.MoveGenerator;
import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.MoveUndoInfo;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.service.GameStateChecker;
import tn.zeros.zchess.core.service.MoveExecutor;
import tn.zeros.zchess.engine.evaluate.EvalUtils;
import tn.zeros.zchess.engine.evaluate.EvaluationService;

public class SearchServiceV1 implements SearchService {
    private static final int MAX_DEPTH = SearchUtils.MAX_DEPTH;

    private final MoveOrderingService moveOrderingService = new MoveOrderingService();
    private final TranspositionTable transpositionTable = new TranspositionTable(1 << 26);
    private final SearchMetrics metrics;
    private final SearchLogger logger;

    private final long searchTimeMs;
    private long searchEndTime;
    private boolean searchCancelled;

    private int bestMoveThisIteration;
    private int bestEvalThisIteration;
    private boolean hasSearchedAtLeastOneMove;

    public SearchServiceV1(long searchTimeMs) {
        this.searchTimeMs = searchTimeMs;
        this.metrics = new SearchMetrics();
        this.logger = new SearchLogger(metrics, transpositionTable);
        SearchDebugConfig.getInstance()
                .enableMetrics(false)
                .enableIterationLogging(false)
                .enableVerboseLogging(false)
                .enableFinalSummary(false);

    }

    @Override
    public int startSearch(BoardState boardState) {
        int bestMove = Move.NULL_MOVE;
        int bestEval = SearchUtils.MIN_EVAL;
        searchEndTime = System.currentTimeMillis() + searchTimeMs;
        searchCancelled = false;

        bestMoveThisIteration = Move.NULL_MOVE;
        bestEvalThisIteration = SearchUtils.MIN_EVAL;

        // Iterative deepening loop
        for (int searchDepth = 1; searchDepth <= MAX_DEPTH; searchDepth++) {
            metrics.setCurrentDepth(searchDepth);
            hasSearchedAtLeastOneMove = false;

            alphaBetaPrune(searchDepth, SearchUtils.MIN_EVAL, SearchUtils.MAX_EVAL, boardState, 0);

            if (isSearchCancelled()) {
                if (hasSearchedAtLeastOneMove) {
                    bestMove = bestMoveThisIteration;
                    bestEval = bestEvalThisIteration;
                    metrics.setBestMove(bestMove);
                    metrics.setBestEval(bestEval);
                    logger.logIterationResults();
                }
                break;
            } else {
                bestMove = bestMoveThisIteration;
                bestEval = bestEvalThisIteration;
                metrics.setBestMove(bestMove);
                metrics.setBestEval(bestEval);

                if (SearchUtils.isMateScore(bestEval)) {
                    break;
                }
            }

            logger.logIterationResults();
        }
        logger.logFinalSummary();
        searchCancelled = false;
        return bestMove != Move.NULL_MOVE ? bestMove : getFallbackMove(boardState);
    }

    @Override
    public int alphaBetaPrune(int depth, int alpha, int beta, BoardState state, int currentPly) {
        checkSearchTimeout();
        if (isSearchCancelled()) {
            return SearchUtils.TIMEOUT_VALUE;
        }

        metrics.incrementNodes();

        if (currentPly > 0) {
            if (isDrawishPosition(state)) return 0;

            // Mate Distance Pruning
            alpha = Math.max(alpha, -SearchUtils.CHECKMATE_EVAL + currentPly);
            beta = Math.min(beta, SearchUtils.CHECKMATE_EVAL - currentPly);
            if (alpha >= beta) {
                return alpha;
            }
        }

        // 7.2. Transposition Table Lookup
        final TranspositionTable.Entry ttEntry = getTranspositionEntry(state, currentPly);
        final int ttScore = lookupEntryEval(ttEntry, depth, alpha, beta);
        if (ttScore != SearchUtils.LOOKUP_FAILED) {
            metrics.incrementTTHits();
            if (currentPly == 0) {
                bestMoveThisIteration = ttEntry.bestMove;
                bestEvalThisIteration = ttScore;
                hasSearchedAtLeastOneMove = true;
            }
            return ttScore;
        }

        if (depth == 0) {
            return quiescenceSearch(alpha, beta, state, currentPly);
        }

        MoveGenerator.MoveList moves = generateLegalMoves(state);
        if (moves.isEmpty()) {
            if (LegalMoveFilter.inCheck(state, state.isWhiteToMove())) {
                return -SearchUtils.CHECKMATE_EVAL;
            }
            return 0;
        }

        int ttBestMove = (ttEntry != null) ? ttEntry.bestMove : Move.NULL_MOVE; // Get TT best move for ordering
        moveOrderingService.orderMoves(moves, state, currentPly, ttBestMove); // Order moves for efficiency

        int originalAlpha = alpha; // Store original alpha for TT entry type
        int bestMove = Move.NULL_MOVE;  // Initialize best move for this node
        int bestScore = SearchUtils.MIN_EVAL; // Initialize best score for this node

        for (int i = 0; i < moves.size; i++) { // Iterate through all moves
            int move = moves.moves[i];
            MoveUndoInfo undoInfo = MoveExecutor.makeMove(state, move); // Make move
            int eval = -alphaBetaPrune(depth - 1, -beta, -alpha, state, currentPly + 1); // Recursive alpha-beta call
            MoveExecutor.unmakeMove(state, undoInfo); // Unmake move (restore state)

            checkSearchTimeout();
            if (isSearchCancelled()) {
                return SearchUtils.TIMEOUT_VALUE;
            }

            // 7.5.2. Update Best Move and Score
            if (eval > bestScore) {
                bestScore = eval;   // Update best score if we found a better one
                bestMove = move;     // Update best move

                if (currentPly == 0) { // Update root move for iterative deepening
                    bestMoveThisIteration = bestMove;
                    bestEvalThisIteration = bestScore;
                    hasSearchedAtLeastOneMove = true;
                }
                alpha = Math.max(alpha, eval); // Update alpha (lower bound)
            }

            // 7.5.3. Beta Cutoff
            if (alpha >= beta) {
                storeTranspositionEntry(state, depth, beta, TTEntryType.LOWER_BOUND, bestMove, currentPly); // Store TT entry (lower bound)
                moveOrderingService.updateKillerMoves(move, currentPly); // Update killer move heuristic
                return beta; // Beta cutoff - return beta value
            }
        }

        // 7.6. Transposition Table Store (after move loop)
        TTEntryType entryType;
        int entryScore;
        if (bestScore <= originalAlpha) {
            entryType = TTEntryType.UPPER_BOUND;   // Fail-low score
            entryScore = originalAlpha;          // Store original alpha as score
        } else if (bestScore >= beta) {
            entryType = TTEntryType.LOWER_BOUND;   // Fail-high score
            entryScore = beta;                 // Store beta as score
        } else {
            entryType = TTEntryType.EXACT;       // Exact score
            entryScore = bestScore;              // Store best score
        }
        storeTranspositionEntry(state, depth, entryScore, entryType, bestMove, currentPly); // Store TT entry

        // 7.7. History Heuristic Update
        if (bestScore > originalAlpha) { // If best move improved alpha (or caused cutoff - good move)
            moveOrderingService.updateHistoryScores(bestMove, depth); // Update history heuristic
        }

        return bestScore; // Return best score from this node
    }

    // 8. Quiescence Search (quiescenceSearch)
    private int quiescenceSearch(int alpha, int beta, BoardState state, int currentPly) {
        checkSearchTimeout();
        if (isSearchCancelled()) {
            return SearchUtils.TIMEOUT_VALUE;
        }

        // 8.2. Transposition Table Lookup
        final TranspositionTable.Entry ttEntry = getTranspositionEntry(state, currentPly); // Retrieve TT entry
        final int ttScore = lookupEntryEval(ttEntry, 0, alpha, beta); // Depth 0 for quiescence entries
        if (ttScore != SearchUtils.LOOKUP_FAILED) {
            metrics.incrementTTHits(); // Increment TT hit count
            if (currentPly == 0) { // Store best move for root in iterative deepening (though likely NULL in quiescence)
                bestMoveThisIteration = ttEntry.bestMove;
                bestEvalThisIteration = ttScore;
                hasSearchedAtLeastOneMove = true;
            }
            return ttScore; // Return TT score if lookup is successful and valid
        }

        // 8.3. Stand-Pat Evaluation
        int standPat = evaluateBoard(state); // Evaluate the current static position
        boolean inCheck = LegalMoveFilter.inCheck(state, state.isWhiteToMove()); // Check if side to move is in check

        // 8.4. Stand-Pat Cutoff (if not in check)
        if (!inCheck) {
            if (standPat >= beta) {
                storeTranspositionEntry(state, 0, beta, TTEntryType.LOWER_BOUND, Move.NULL_MOVE, currentPly); // Store TT entry (lower bound)
                return beta; // Beta cutoff based on stand-pat
            }
            alpha = Math.max(alpha, standPat); // Update alpha if stand-pat improves it
        }

        // 8.5. Move Generation (Captures or all moves if in check)
        MoveGenerator.MoveList moves = generateQuiescenceMoves(state, inCheck); // Generate only captures (or all if in check)

        // 8.6. Check for No Legal Quiescence Moves
        if (moves.isEmpty()) {
            int finalScore = inCheck ? -SearchUtils.CHECKMATE_EVAL : standPat; // Checkmate if in check, otherwise stand-pat
            storeTranspositionEntry(state, 0, finalScore, TTEntryType.EXACT, Move.NULL_MOVE, currentPly); // Store TT entry (exact score for terminal node)
            return finalScore; // Return final score for quiescence search leaf node
        }

        int ttBestMove = (ttEntry != null) ? ttEntry.bestMove : Move.NULL_MOVE; // Get TT best move for ordering
        moveOrderingService.orderMoves(moves, state, currentPly, ttBestMove); // Order moves (captures)

        int originalAlpha = alpha; // Store original alpha for TT entry type
        int bestMove = Move.NULL_MOVE; // Initialize best move for quiescence node
        int bestScore = standPat;      // Initialize best score to stand-pat


        // 8.7. Quiescence Move Loop (Iterate through captures)
        // Use a more conservative margin for delta pruning
        int deltaMargin = EvalUtils.getPieceTypeValue(Piece.PAWN) + 200;

        for (int i = 0; i < moves.size; i++) {

            int move = moves.moves[i];
            int capturedPiece = Move.getCapturedPiece(move);
            int capturedValue = EvalUtils.getPieceTypeValue(capturedPiece);

            // 8.7.2. Delta Pruning (more conservative)
            if (!inCheck && capturedValue != EvalUtils.NONE_VALUE) {
                if (capturedValue + deltaMargin + standPat <= alpha) {
                    continue; // Delta pruning cutoff
                }
            }

            // 8.7.3. Recursive Quiescence Search Call
            MoveUndoInfo undoInfo = MoveExecutor.makeMove(state, move); // Make capture move
            int eval = -quiescenceSearch(-beta, -alpha, state, currentPly + 1); // Recursive quiescence search
            MoveExecutor.unmakeMove(state, undoInfo); // Unmake capture move

            // 8.7.5. Update Best Score and Move
            if (eval > bestScore) {
                bestScore = eval;  // Update best score
                bestMove = move;    // Update best move
                alpha = Math.max(alpha, eval); // Update alpha
            }

            // 8.7.6. Beta Cutoff
            if (alpha >= beta) {
                storeTranspositionEntry(state, 0, beta, TTEntryType.LOWER_BOUND, bestMove, currentPly); // Store TT entry (lower bound)
                moveOrderingService.updateKillerMoves(move, currentPly); // Update killer move heuristic
                return beta; // Beta cutoff - return beta value
            }
        }

        // 8.8. Transposition Table Store (after move loop)
        TTEntryType entryType;
        int entryScore;
        if (bestScore <= originalAlpha) {
            entryType = TTEntryType.UPPER_BOUND;   // Fail-low score
            entryScore = originalAlpha;          // Store original alpha as score
        } else if (bestScore >= beta) {
            entryType = TTEntryType.LOWER_BOUND;   // Fail-high score
            entryScore = beta;                 // Store beta as score
        } else {
            entryType = TTEntryType.EXACT;       // Exact score
            entryScore = bestScore;              // Store best score
        }
        storeTranspositionEntry(state, 0, entryScore, entryType, bestMove, currentPly); // Store TT entry

        return bestScore; // Return best score from quiescence node
    }

    // 9. Move Generation and Ordering Helpers
    private MoveGenerator.MoveList generateLegalMoves(BoardState state) { // Renamed for clarity
        return MoveGenerator.generateAllMoves(state, false);
    }

    private MoveGenerator.MoveList generateQuiescenceMoves(BoardState state, boolean inCheck) { // Clarified purpose
        return MoveGenerator.generateAllMoves(state, !inCheck); // Captures only if not in check
    }


    // 10. Transposition Table Helper Methods (TT Access)
    private TranspositionTable.Entry getTranspositionEntry(BoardState state, int currentPly) {
        final long positionKey = state.getZobristKey(); // Get Zobrist key for the current position
        final TranspositionTable.Entry entry = transpositionTable.get(positionKey); // Lookup entry in TT

        if (entry != null) {
            // Always unadjust mate scores when retrieving from TT to get correct ply distance to mate
            entry.score = SearchUtils.unadjustMateScore(entry.score, currentPly);
        }
        return entry; // Return the TT entry (or null if not found)
    }

    private void storeTranspositionEntry(BoardState state, int depth, int score, TTEntryType type, int bestMove, int currentPly) {
        if (SearchDebugConfig.getInstance().isVerboseLogging()) {
            if (depth > 2)
                System.out.printf("Storing TT entry - depth: %d, move: %s%n", depth, Move.toAlgebraic(bestMove));
        }

        if (SearchUtils.isTimeout(score)) return;

        final long positionKey = state.getZobristKey();
        final int adjustedScore = SearchUtils.adjustMateScore(score, currentPly);

        // Only store if we have a valid move
        if (bestMove != Move.NULL_MOVE) {
            transpositionTable.put(positionKey, depth, adjustedScore, type, bestMove, currentPly);
        }
    }

    private int lookupEntryEval(TranspositionTable.Entry ttEntry, int depth, int alpha, int beta) {
        if (ttEntry != null && ttEntry.depth >= depth) {
            final int score = ttEntry.score;

            if (ttEntry.type == TTEntryType.EXACT) {
                return score;
            }

            if (ttEntry.type == TTEntryType.LOWER_BOUND && score >= beta) {
                return score;
            }
            if (ttEntry.type == TTEntryType.UPPER_BOUND && score <= alpha) {
                return score;
            }
        }
        return SearchUtils.LOOKUP_FAILED;
    }

    @Override
    public void clear() {
        transpositionTable.clear();
        moveOrderingService.clearKillerMoves();
        moveOrderingService.clearHistoryScores();
    }

    private void checkSearchTimeout() {
        if (System.currentTimeMillis() >= searchEndTime) searchCancelled = true;
    }

    private boolean isSearchCancelled() {
        return searchCancelled;
    }


    private int evaluateBoard(BoardState state) {
        return EvaluationService.evaluate(state);
    }

    private int getFallbackMove(BoardState boardState) {
        MoveGenerator.MoveList moves = generateLegalMoves(boardState);
        return moves.size > 0 ? moves.moves[0] : Move.NULL_MOVE;
    }


    private boolean isDrawishPosition(BoardState state) {
        return GameStateChecker.isFiftyMoveRule(state) ||
                GameStateChecker.isThreefoldRepetition(state) ||
                GameStateChecker.isInsufficientMaterial(state);
    }

}