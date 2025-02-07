package tn.zeros.zchess.engine.search;

import org.junit.jupiter.api.Test;
import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.service.FenService;

public class SearchTest {
    @Test
    public void testSearch() {
        SearchServiceV1 searchService = new SearchServiceV1(500);
        BoardState state = new BoardState();
        FenService.parseFEN("8/8/8/8/8/5K2/4R3/5k2 b - - 4 3", state);
        searchService.startSearch(state);
    }
}
