package tn.zeros.zchess.core.util;

import static tn.zeros.zchess.core.util.ChessConstants.DIRECTION_OFFSETS;

public class PrecomputedMoves {
    public static final long[] KNIGHT_MOVES = new long[64];
    public static final long[] KING_MOVES = new long[64];
    public static final long[][] RAY_MOVES = new long[64][8];

    static {
        initialize();
    }

    private static void initialize() {
        // Initialize knight moves
        int[] knightOffsets = {6, 10, 15, 17, -6, -10, -15, -17};
        for (int square = 0; square < 64; square++) {
            int rank = square / 8;
            int file = square % 8;

            for (int offset : knightOffsets) {
                int targetSquare = square + offset;
                int targetRank = targetSquare / 8;
                int targetFile = targetSquare % 8;

                if (targetSquare >= 0 && targetSquare < 64 &&
                        Math.abs(targetFile - file) + Math.abs(targetRank - rank) == 3) {
                    KNIGHT_MOVES[square] |= 1L << targetSquare;
                }
            }
        }

        // Initialize king moves
        int[] kingOffsets = {-9, -8, -7, -1, 1, 7, 8, 9};
        for (int square = 0; square < 64; square++) {
            int rank = square / 8;
            int file = square % 8;

            for (int offset : kingOffsets) {
                int targetSquare = square + offset;
                int targetRank = targetSquare / 8;
                int targetFile = targetSquare % 8;

                if (targetSquare >= 0 && targetSquare < 64 &&
                        Math.abs(targetFile - file) <= 1 &&
                        Math.abs(targetRank - rank) <= 1) {
                    KING_MOVES[square] |= 1L << targetSquare;
                }
            }
        }

        // Sliding pieces
        for (int square = 0; square < 64; square++) {
            int rank = square / 8;
            int file = square % 8;

            for (int dir = 0; dir < 8; dir++) {
                long ray = 0L;
                int current = square;

                while (true) {
                    int next = current + DIRECTION_OFFSETS[dir];
                    int nextRank = next / 8;
                    int nextFile = next % 8;

                    // Check boundaries
                    if (next < 0 || next >= 64) break;
                    if (Math.abs(nextFile - file) > 1) break;
                    if (Math.abs(nextRank - rank) > 1) break;

                    ray |= 1L << next;
                    current = next;
                    rank = nextRank;
                    file = nextFile;
                }

                RAY_MOVES[square][dir] = ray;
            }
        }
    }
}
