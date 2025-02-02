package tn.zeros.zchess.core.util;

import org.junit.jupiter.api.Test;

public class MagicBitboardTest {
    @Test
    void testMagicBitboards() {
        // Rooks
        testRookAttacks(0, 0L);              // a1, no blockers
        testRookAttacks(63, 1L << 55 | 1L << 62); // h8 with edge blockers
        testRookAttacks(28, 1L << 20 | 1L << 27); // e4 with central blockers

        // Bishops
        testBishopAttacks(28, 1L << 35 | 1L << 19); // e4 with central blockers
        testBishopAttacks(0, 1L << 9 | 1L << 18);   // a1 with diagonal blockers
        testBishopAttacks(63, 1L << 54 | 1L << 45); // h8 with diagonal blockers

        // Knights

    }

    private void testRookAttacks(int square, long blockers) {
        System.out.println("Rook at " + ChessConstants.squareName(square));
        System.out.println("Blockers:");
        ChessConstants.printBitboard(blockers);
        System.out.println("Generated attacks:");
        ChessConstants.printBitboard(PrecomputedMoves.getMagicRookAttack(square, blockers));
    }

    private void testBishopAttacks(int square, long blockers) {
        System.out.println("Bishop at " + ChessConstants.squareName(square));
        System.out.println("Blockers:");
        ChessConstants.printBitboard(blockers);
        System.out.println("Generated attacks:");
        ChessConstants.printBitboard(PrecomputedMoves.getMagicBishopAttack(square, blockers));
    }

}
