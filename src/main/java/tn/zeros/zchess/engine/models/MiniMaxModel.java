package tn.zeros.zchess.engine.models;

import tn.zeros.zchess.core.logic.generation.MoveGenerator;
import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.MoveUndoInfo;
import tn.zeros.zchess.core.service.MoveExecutor;
import tn.zeros.zchess.engine.search.SearchService;
import tn.zeros.zchess.engine.util.SearchMetrics;
import tn.zeros.zchess.engine.util.SearchResult;

public class MiniMaxModel implements EngineModel {
    private static final SearchService searchService = new SearchService();
    private static final int MAX_DEPTH = 3;

    @Override
    public int generateMove(BoardState boardState) {
        SearchMetrics metrics = SearchMetrics.getInstance();
        metrics.startSearch();
        if (!boardState.isGameOver()) {
            MoveGenerator.MoveList legalMoves = MoveGenerator.generateAllMoves(boardState);
            if (!legalMoves.isEmpty()) {
                int bestMove = -1;
                int bestEval = Integer.MIN_VALUE;

                for (int i = 0; i < legalMoves.size; i++) {
                    int move = legalMoves.moves[i];
                    MoveUndoInfo undoInfo = MoveExecutor.makeMove(boardState, move);
                    int eval = -searchService.minimax(MAX_DEPTH, boardState);
                    MoveExecutor.unmakeMove(boardState, undoInfo);

                    if (eval > bestEval) {
                        bestEval = eval;
                        bestMove = move;
                    }
                }
                SearchResult result = metrics.endSearch();
                System.out.printf("Search complete: %dms, %d positions%n",
                        result.timeMs(), result.positions());
                return bestMove;
            }
        }
        return -1;
    }
}
