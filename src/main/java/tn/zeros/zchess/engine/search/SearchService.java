package tn.zeros.zchess.engine.search;

import tn.zeros.zchess.core.logic.generation.LegalMoveFilter;
import tn.zeros.zchess.core.logic.generation.MoveGenerator;
import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.MoveUndoInfo;
import tn.zeros.zchess.core.service.MoveExecutor;
import tn.zeros.zchess.engine.evaluate.EvaluationService;
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
        if (depth == 0) return evaluateTerminalNode(state);

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
}
