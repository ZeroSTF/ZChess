package tn.zeros.zchess.core.util;

import org.junit.jupiter.api.Test;

public class MagicBitboardTest {
    public static long getBishopAttacks(int square, long occupancy) {
        long masked = occupancy & PrecomputedMoves.BISHOP_MASKS[square];
        long magic = ChessConstants.BISHOP_MAGICS[square];
        int index = (int) ((masked * magic) >>> PrecomputedMoves.BISHOP_SHIFTS[square]);
        return PrecomputedMoves.BISHOP_ATTACKS[square][index];
    }

    @Test
    void testMagicBitboards() {
        // Test rook on a1 (square 0) with no blockers
        testRookAttacks(0, 0L);

        // Test bishop on e4 (square 28) with central blockers
        testBishopAttacks(28, 1L << 35 | 1L << 19);

        // Test edge case: rook on h8 (square 63) with edge blockers
        testRookAttacks(63, 1L << 55 | 1L << 62);
    }

    private void testRookAttacks(int square, long blockers) {
        System.out.println("Rook at " + squareName(square));
        System.out.println("Blockers:");
        ChessConstants.printBitboard(blockers);
        System.out.println("Generated attacks:");
        ChessConstants.printBitboard(PrecomputedMoves.generateRookAttacks(square, blockers));
    }

    private void testBishopAttacks(int square, long blockers) {
        System.out.println("Bishop at " + squareName(square));
        System.out.println("Blockers:");
        ChessConstants.printBitboard(blockers);
        System.out.println("Generated attacks:");
        ChessConstants.printBitboard(PrecomputedMoves.generateBishopAttacks(square, blockers));
    }

    public String squareName(int square) {
        char file = (char) ('a' + (square % 8));
        int rank = (square / 8) + 1;
        return "" + file + rank;
    }

}
