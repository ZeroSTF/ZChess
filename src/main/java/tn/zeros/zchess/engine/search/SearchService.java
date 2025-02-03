package tn.zeros.zchess.engine.search;

import tn.zeros.zchess.core.logic.generation.LegalMoveFilter;
import tn.zeros.zchess.core.logic.generation.MoveGenerator;
import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.MoveUndoInfo;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.service.MoveExecutor;
import tn.zeros.zchess.engine.evaluate.EvaluationService;
import tn.zeros.zchess.engine.util.EvalUtils;
import tn.zeros.zchess.engine.util.SearchMetrics;
import tn.zeros.zchess.engine.util.SearchUtils;
import tn.zeros.zchess.engine.util.TranspositionTable;

public class SearchService {
    private final MoveOrderingService moveOrderingService = new MoveOrderingService();
    private final TranspositionTable transpositionTable = new TranspositionTable(1 << 20);

    private int evaluateTerminalNode(BoardState state) {
        SearchMetrics.getInstance().incrementPositions();
        return EvaluationService.evaluate(state);
    }

    private int handleLeafNode(BoardState state) {
        if (LegalMoveFilter.inCheck(state, state.isWhiteToMove())) {
            return SearchUtils.MIN_EVAL;
        }
        return 0;
    }

    public int minimax(int depth, BoardState state) {
        if (depth == 0) return evaluateTerminalNode(state);

        MoveGenerator.MoveList moves = MoveGenerator.generateAllMoves(state, true);
        if (moves.isEmpty()) return handleLeafNode(state);

        int bestEval = SearchUtils.MIN_EVAL;
        for (int i = 0; i < moves.size; i++) {
            int move = moves.moves[i];
            MoveUndoInfo undoInfo = MoveExecutor.makeMove(state, move);
            bestEval = Math.max(bestEval, -minimax(depth - 1, state));
            MoveExecutor.unmakeMove(state, undoInfo);
        }
        return bestEval;
    }

    public int alphaBetaPrune(int depth, int alpha, int beta, BoardState state, boolean useOrdering) {
        if (depth == 0) return quiescenceSearch(alpha, beta, state, useOrdering);

        MoveGenerator.MoveList moves = MoveGenerator.generateAllMoves(state, false);
        if (moves.isEmpty()) return handleLeafNode(state);

        if (useOrdering) {
            moveOrderingService.orderMoves(moves, state);
        }

        for (int i = 0; i < moves.size; i++) {
            int move = moves.moves[i];
            MoveUndoInfo undoInfo = MoveExecutor.makeMove(state, move);
            int eval = -alphaBetaPrune(depth - 1, -beta, -alpha, state, useOrdering);
            MoveExecutor.unmakeMove(state, undoInfo);

            if (eval >= beta) return beta;
            alpha = Math.max(alpha, eval);
        }
        return alpha;
    }

    private int quiescenceSearch(int alpha, int beta, BoardState state, boolean useOrdering) {
        SearchMetrics.getInstance().incrementPositions();

        int standPat = EvaluationService.evaluate(state);
        boolean inCheck = LegalMoveFilter.inCheck(state, state.isWhiteToMove());

        // Stand-pat cutoff and update
        if (!inCheck) {
            if (standPat >= beta) {
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
            return inCheck ? SearchUtils.MIN_EVAL : standPat;
        }

        // Order moves (MVV-LVA in captures)
        if (useOrdering) {
            moveOrderingService.orderMoves(moves, state);
        }

        int deltaMargin = EvalUtils.getPieceTypeValue(Piece.PAWN);

        for (int i = 0; i < moves.size; i++) {
            int move = moves.moves[i];
            int capturedPiece = Move.getCapturedPiece(move);
            int capturedValue = EvalUtils.getPieceTypeValue(capturedPiece);

            // Delta pruning: skip moves that can't improve alpha
            if (!inCheck) {
                if (capturedValue == Piece.NONE) continue;
                if (standPat + capturedValue + deltaMargin <= alpha) {
                    continue;
                }
            }

            MoveUndoInfo undoInfo = MoveExecutor.makeMove(state, move);
            int eval = -quiescenceSearch(-beta, -alpha, state, useOrdering);
            MoveExecutor.unmakeMove(state, undoInfo);

            if (eval >= beta) {
                return beta;
            }
            if (eval > alpha) {
                alpha = eval;
            }
        }

        return alpha;
    }
}
