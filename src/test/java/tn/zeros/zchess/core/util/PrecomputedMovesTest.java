package tn.zeros.zchess.core.util;

import org.junit.jupiter.api.Test;

public class PrecomputedMovesTest {
    private static final int testSquare = 28; // E4 (for example)

    @Test
    void testRayMoves() {
        System.out.println("Sliding Moves (Queen) from E4:");
        for (int dir = 0; dir < 8; dir++) {
            System.out.println("Direction " + dir + ":");
            ChessConstants.printBitboard(PrecomputedMoves.RAY_MOVES[testSquare][dir]);
        }
    }
}
