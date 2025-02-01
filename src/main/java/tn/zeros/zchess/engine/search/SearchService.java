package tn.zeros.zchess.engine.search;

import tn.zeros.zchess.core.logic.generation.LegalMoveFilter;
import tn.zeros.zchess.core.logic.generation.MoveGenerator;
import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.MoveUndoInfo;
import tn.zeros.zchess.core.service.MoveExecutor;
import tn.zeros.zchess.engine.evaluate.EvaluationService;
import tn.zeros.zchess.engine.util.SearchMetrics;
import tn.zeros.zchess.engine.util.SearchUtils;

public class SearchService {
    private final MoveOrderingService moveOrderingService = new MoveOrderingService();

    public int minimax(int depth, BoardState state) {
        if (depth == 0) {
            SearchMetrics.getInstance().incrementPositions();
            return EvaluationService.evaluate(state);
        }


        MoveGenerator.MoveList moves = MoveGenerator.generateAllMoves(state);

        if (moves.isEmpty()) {
            if (LegalMoveFilter.inCheck(state, state.isWhiteToMove()))
                return SearchUtils.MIN_EVAL;
            return 0;
        }

        int bestEval = SearchUtils.MIN_EVAL;

        for (int i = 0; i < moves.size; i++) {
            int move = moves.moves[i];
            MoveUndoInfo undoInfo = MoveExecutor.makeMove(state, move);
            int eval = -minimax(depth - 1, state);
            bestEval = Math.max(bestEval, eval);
            MoveExecutor.unmakeMove(state, undoInfo);
        }

        return bestEval;
    }

    public int alphaBetaPrune(int depth, int alpha, int beta, BoardState state) {
        if (depth == 0) {
            SearchMetrics.getInstance().incrementPositions();
            return EvaluationService.evaluate(state);
        }

        MoveGenerator.MoveList moves = MoveGenerator.generateAllMoves(state);

        if (moves.isEmpty()) {
            if (LegalMoveFilter.inCheck(state, state.isWhiteToMove()))
                return SearchUtils.MIN_EVAL;
            return 0;
        }

        for (int i = 0; i < moves.size; i++) {
            int move = moves.moves[i];
            MoveUndoInfo undoInfo = MoveExecutor.makeMove(state, move);
            int eval = -alphaBetaPrune(depth - 1, -beta, -alpha, state);
            MoveExecutor.unmakeMove(state, undoInfo);
            if (eval >= beta) {
                return beta;
            }
            alpha = Math.max(alpha, eval);
        }

        return alpha;
    }

    public int orderedAlphaBetaPrune(int depth, int alpha, int beta, BoardState state) {
        if (depth == 0) {
            SearchMetrics.getInstance().incrementPositions();
            return EvaluationService.evaluate(state);
        }
        MoveGenerator.MoveList moves = MoveGenerator.generateAllMoves(state);

        if (moves.isEmpty()) {
            if (LegalMoveFilter.inCheck(state, state.isWhiteToMove())) {
                return SearchUtils.MIN_EVAL;
            }
            return 0;
        }

        moveOrderingService.orderMoves(moves, state);

        for (int i = 0; i < moves.size; i++) {
            int move = moves.moves[i];
            MoveUndoInfo undoInfo = MoveExecutor.makeMove(state, move);
            int eval = -orderedAlphaBetaPrune(depth - 1, -beta, -alpha, state);
            MoveExecutor.unmakeMove(state, undoInfo);

            if (eval >= beta) {
                return beta;
            }
            alpha = Math.max(alpha, eval);
        }

        return alpha;
    }

}
