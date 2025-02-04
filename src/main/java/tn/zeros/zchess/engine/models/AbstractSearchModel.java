package tn.zeros.zchess.engine.models;

import tn.zeros.zchess.core.logic.generation.MoveGenerator;
import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.MoveUndoInfo;
import tn.zeros.zchess.core.service.MoveExecutor;
import tn.zeros.zchess.engine.search.SearchService;
import tn.zeros.zchess.engine.util.SearchMetrics;
import tn.zeros.zchess.engine.util.SearchUtils;

public abstract class AbstractSearchModel implements EngineModel {
    protected final SearchService searchService;
    protected final int maxDepth;

    protected AbstractSearchModel(SearchService searchService, int maxDepth) {
        this.searchService = searchService;
        this.maxDepth = maxDepth;
    }

    @Override
    public int generateMove(BoardState boardState) {
        SearchMetrics metrics = SearchMetrics.getInstance();
        metrics.startSearch();
        if (boardState.isGameOver()) return -1;

        MoveGenerator.MoveList legalMoves = MoveGenerator.generateAllMoves(boardState, false);
        if (legalMoves.isEmpty()) return -1;

        int bestMove = -1;
        int bestEval = SearchUtils.MIN_EVAL;

        for (int i = 0; i < legalMoves.size; i++) {
            int move = legalMoves.moves[i];
            MoveUndoInfo undoInfo = MoveExecutor.makeMove(boardState, move);
            int eval = -performSearch(boardState);
            MoveExecutor.unmakeMove(boardState, undoInfo);

            if (eval > bestEval) {
                bestEval = eval;
                bestMove = move;
            }
        }

        SearchMetrics.SearchResult result = metrics.endSearch();
        result.logPerformance();
        logSearchResult(result);
        return bestMove;
    }

    protected abstract int performSearch(BoardState state);

    private void logSearchResult(SearchMetrics.SearchResult result) {
        System.out.printf("Search complete: %dms, %d positions%n",
                result.timeMs(), result.positions());
    }
}