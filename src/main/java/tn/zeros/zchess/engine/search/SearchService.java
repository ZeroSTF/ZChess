package tn.zeros.zchess.engine.search;

import tn.zeros.zchess.core.model.BoardState;

public interface SearchService {
    int startSearch(BoardState boardState);

    int alphaBetaPrune(int depth, int alpha, int beta, BoardState state, int currentPly);

    void clear();
}
