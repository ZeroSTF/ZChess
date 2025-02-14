package tn.zeros.zchess.engine.models;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.engine.search.SearchService;
import tn.zeros.zchess.engine.search.SearchServiceV1;

public class ModelV1 implements EngineModel {
    protected final SearchService searchService;

    public ModelV1(long searchTimeMs) {
        this.searchService = new SearchServiceV1(searchTimeMs);
    }

    @Override
    public int generateMove(BoardState boardState) {
        BoardState newState = boardState.clone();
        return searchService.startSearch(newState);
    }

    @Override
    public void reset() {
        searchService.clear();
    }

}