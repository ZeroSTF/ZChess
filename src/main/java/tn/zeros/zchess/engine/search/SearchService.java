package tn.zeros.zchess.engine.search;

import tn.zeros.zchess.core.logic.generation.LegalMoveFilter;
import tn.zeros.zchess.core.logic.generation.MoveGenerator;
import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.MoveUndoInfo;
import tn.zeros.zchess.core.service.MoveExecutor;
import tn.zeros.zchess.engine.evaluate.EvaluationService;

import java.util.List;

public class SearchService {
    private final BoardState state;

    public SearchService(BoardState state) {
        this.state = state;
    }

    public int minimax(int depth) {
        if (depth == 0) return EvaluationService.evaluate(state);

        List<Integer> moves = MoveGenerator.generateAllMoves(state);

        if (moves.isEmpty()) {
            if (LegalMoveFilter.inCheck(state, state.isWhiteToMove()))
                return Integer.MIN_VALUE;
            return 0;
        }

        int bestEval = Integer.MIN_VALUE;

        for (int move : moves) {
            MoveUndoInfo undoInfo = MoveExecutor.makeMove(state, move);
            int eval = -minimax(depth - 1);
            bestEval = Math.max(bestEval, eval);
            MoveExecutor.unmakeMove(state, undoInfo);
        }

        return bestEval;
    }
}
