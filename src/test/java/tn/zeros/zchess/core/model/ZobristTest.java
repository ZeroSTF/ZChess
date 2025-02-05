package tn.zeros.zchess.core.model;

import org.junit.jupiter.api.Test;
import tn.zeros.zchess.core.service.FenService;

public class ZobristTest {
    @Test
    public void testZobristKey() {
        BoardState state1 = new BoardState();
        FenService.parseFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", state1);
        long key1 = state1.getZobristKey();

        BoardState state2 = new BoardState();
        FenService.parseFEN("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 40", state2);
        long key2 = state2.getZobristKey();

        assert key1 == key2;
    }
}
