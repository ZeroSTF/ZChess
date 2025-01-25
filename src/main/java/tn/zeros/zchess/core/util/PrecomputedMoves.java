package tn.zeros.zchess.core.util;

import static tn.zeros.zchess.core.util.ChessConstants.DIRECTION_OFFSETS;

public class PrecomputedMoves {
    public static final long[] KNIGHT_MOVES = new long[64];
    public static final long[] KING_MOVES = new long[64];
    public static final long[][] RAY_MOVES = new long[64][8];
    public static final long[] WHITE_PAWN_ATTACKS = new long[64];
    public static final long[] BLACK_PAWN_ATTACKS = new long[64];

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
            for (int dir = 0; dir < 8; dir++) {
                long ray = 0L;
                int current = square;

                while (true) {
                    int next = current + DIRECTION_OFFSETS[dir];

                    // Check if the move is still within the board
                    if (next < 0 || next >= 64) break;

                    // Check for edge crossing (horizontal and diagonal directions)
                    if ((dir == 1 || dir == 2 || dir == 3) && (current % 8 == 7)) break; // Crossing FILE_H
                    if ((dir == 5 || dir == 6 || dir == 7) && (current % 8 == 0)) break; // Crossing FILE_A

                    ray |= 1L << next;
                    current = next;

                    // Stop if reaching the edge of the board in a specific direction
                    if ((dir == 0 && next < 8) ||       // N (first rank)
                            (dir == 4 && next >= 56)) {     // S (last rank)
                        break;
                    }
                }

                RAY_MOVES[square][dir] = ray;
            }
        }

        // Pawn attacks
        for (int square = 0; square < 64; square++) {
            int rank = square / 8;
            int file = square % 8;

            // White pawns attack northeast and northwest
            if (rank < 7) { // Prevent overflow from top rank
                if (file > 0) WHITE_PAWN_ATTACKS[square] |= 1L << (square + 7);
                if (file < 7) WHITE_PAWN_ATTACKS[square] |= 1L << (square + 9);
            }

            // Black pawns attack southeast and southwest
            if (rank > 0) { // Prevent underflow from bottom rank
                if (file > 0) BLACK_PAWN_ATTACKS[square] |= 1L << (square - 9);
                if (file < 7) BLACK_PAWN_ATTACKS[square] |= 1L << (square - 7);
            }
        }

    }
}
