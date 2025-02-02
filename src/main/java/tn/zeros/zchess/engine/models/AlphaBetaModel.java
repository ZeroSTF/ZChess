package tn.zeros.zchess.engine.models;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.engine.search.SearchService;
import tn.zeros.zchess.engine.util.SearchUtils;

public class AlphaBetaModel extends AbstractSearchModel {
    public AlphaBetaModel(SearchService searchService, int maxDepth) {
        super(searchService, maxDepth);
    }

    @Override
    protected int performSearch(BoardState state) {
        return searchService.alphaBetaPrune(maxDepth, SearchUtils.MIN_EVAL, SearchUtils.MAX_EVAL, state, false);
    }
}