package tn.zeros.zchess.core.board;

public class Bitboard {
    public static final long FILE_A = 0x0101010101010101L;
    public static final long RANK_1 = 0xFFL;

    // Basic bit operations
    public static long setBit(long board, int square) {
        return board | (1L << square);
    }

    public static long clearBit(long board, int square) {
        return board & ~(1L << square);
    }

    public static boolean isBitSet(long board, int square) {
        return (board & (1L << square)) != 0;
    }

    // Bitboard manipulation
    public static long northOne(long b) { return b << 8; }
    public static long southOne(long b) { return b >>> 8; }
    public static long eastOne(long b)  { return (b << 1) & ~FILE_A; }
    public static long westOne(long b)  { return (b >>> 1) & ~FILE_A; }

    // Population count
    public static int countBits(long b) {
        return Long.bitCount(b);
    }

    // Visualization for debugging
    public static String toString(long board) {
        StringBuilder sb = new StringBuilder();
        for (int rank = 7; rank >= 0; rank--) {
            for (int file = 0; file < 8; file++) {
                int square = rank * 8 + file;
                sb.append(isBitSet(board, square) ? "1 " : "0 ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}