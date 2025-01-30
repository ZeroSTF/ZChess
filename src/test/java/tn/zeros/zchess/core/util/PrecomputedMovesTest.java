package tn.zeros.zchess.core.util;

import org.junit.jupiter.api.Test;

public class PrecomputedMovesTest {

    @Test
    void testKnightMoves() {
        System.out.println("Knight Moves from H4:");
        int testSquare = 31;
        //ChessConstants.printBitboard(PrecomputedMoves.KNIGHT_MOVES[testSquare]);
    }

    @Test
    void testPawnAttacks() {
        System.out.println("Pawn Attacks from D7:");
        int testSquare = 51;
        //ChessConstants.printBitboard(PrecomputedMoves.WHITE_PAWN_ATTACKS[testSquare]);
    }
}
