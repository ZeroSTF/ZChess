package tn.zeros.zchess.core.util;

import org.junit.jupiter.api.Test;

public class PrecomputedMovesTest {

    @Test
    void testRayMoves() {
        System.out.println("Sliding Moves (Queen) from H6:");
        int testSquare = 47;
        for (int dir = 0; dir < 8; dir++) {
            System.out.println("Direction " + dir + ":");
            ChessConstants.printBitboard(PrecomputedMoves.RAY_MOVES[testSquare][dir]);
        }
    }

    @Test
    void testKnightMoves() {
        System.out.println("Knight Moves from H4:");
        int testSquare = 31;
        ChessConstants.printBitboard(PrecomputedMoves.KNIGHT_MOVES[testSquare]);
    }
}
