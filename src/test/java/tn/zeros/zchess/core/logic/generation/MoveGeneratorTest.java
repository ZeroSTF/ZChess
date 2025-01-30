package tn.zeros.zchess.core.logic.generation;

import org.junit.jupiter.api.Test;
import tn.zeros.zchess.core.service.FenService;
import tn.zeros.zchess.core.util.ChessConstants;

public class MoveGeneratorTest {
    @Test
    void testPawnMoves() {
        MoveGenerator.generateAllMoves(FenService.parseFEN(ChessConstants.DEFAULT_FEN));
    }
}
