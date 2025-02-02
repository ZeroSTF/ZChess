package tn.zeros.zchess.engine.models;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.engine.search.SearchService;

public class MiniMaxModel extends AbstractSearchModel {
    public MiniMaxModel(SearchService searchService, int maxDepth) {
        super(searchService, maxDepth);
    }

    @Override
    protected int performSearch(BoardState state) {
        return searchService.minimax(maxDepth, state);
    }
}