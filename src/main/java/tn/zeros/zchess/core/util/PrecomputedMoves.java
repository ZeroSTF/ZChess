package tn.zeros.zchess.core.util;

import static tn.zeros.zchess.core.util.ChessConstants.DIRECTION_OFFSETS;

public class PrecomputedMoves {
    public static final long[] KNIGHT_MOVES = new long[64];
    public static final long[] KING_MOVES = new long[64];
    public static final long[][] RAY_MOVES = new long[64][8];
    public static final long[] WHITE_PAWN_ATTACKS = new long[64];
    public static final long[] BLACK_PAWN_ATTACKS = new long[64];

    static {
        initializeKnightMoves();
        initializeKingMoves();
        initializeSlidingPieces();
        initializePawnAttacks();
    }

    private static void initializeKnightMoves() {
        int[] knightOffsets = {6, 10, 15, 17, -6, -10, -15, -17};

        for (int square = 0; square < 64; square++) {
            for (int offset : knightOffsets) {
                int targetSquare = square + offset;
                if (isWithinBounds(targetSquare) &&
                        Math.abs((targetSquare % 8) - (square % 8)) + Math.abs((targetSquare / 8) - (square / 8)) == 3) {
                    KNIGHT_MOVES[square] |= 1L << targetSquare;
                }
            }
        }
    }

    private static void initializeKingMoves() {
        int[] kingOffsets = {-9, -8, -7, -1, 1, 7, 8, 9};

        for (int square = 0; square < 64; square++) {
            for (int offset : kingOffsets) {
                int targetSquare = square + offset;
                if (isWithinBounds(targetSquare) && isAdjacent(square, targetSquare)) {
                    KING_MOVES[square] |= 1L << targetSquare;
                }
            }
        }
    }

    private static void initializeSlidingPieces() {
        for (int square = 0; square < 64; square++) {
            for (int dir = 0; dir < 8; dir++) {
                long ray = 0L;
                int current = square;

                while (true) {
                    int next = current + DIRECTION_OFFSETS[dir];

                    // Validate bounds and edge crossing
                    if (!isWithinBounds(next) ||
                            (dir == 1 || dir == 2 || dir == 3) && (current % 8 == 7) || // Crossing FILE_H
                            (dir == 5 || dir == 6 || dir == 7) && (current % 8 == 0)) { // Crossing FILE_A
                        break;
                    }

                    ray |= 1L << next;
                    current = next;

                    // Stop if reaching the edge of the board in a specific direction
                    if ((dir == 0 && next < 8) ||       // N (first rank)
                            (dir == 4 && next >= 56)) {    // S (last rank)
                        break;
                    }
                }

                RAY_MOVES[square][dir] = ray;
            }
        }
    }

    private static void initializePawnAttacks() {
        for (int square = 0; square < 64; square++) {
            int rank = square / 8;
            int file = square % 8;

            // White pawn attacks
            if (rank < 7) {
                if (file > 0 && isWithinBounds(square + 7)) WHITE_PAWN_ATTACKS[square] |= 1L << (square + 7);
                if (file < 7 && isWithinBounds(square + 9)) WHITE_PAWN_ATTACKS[square] |= 1L << (square + 9);
            }

            // Black pawn attacks
            if (rank > 0) {
                if (file > 0 && isWithinBounds(square - 9)) BLACK_PAWN_ATTACKS[square] |= 1L << (square - 9);
                if (file < 7 && isWithinBounds(square - 7)) BLACK_PAWN_ATTACKS[square] |= 1L << (square - 7);
            }
        }
    }

    // Helper methods
    private static boolean isWithinBounds(int targetSquare) {
        return targetSquare >= 0 && targetSquare < 64;
    }

    private static boolean isAdjacent(int currentSquare, int targetSquare) {
        int currentRank = currentSquare / 8;
        int currentFile = currentSquare % 8;
        int targetRank = targetSquare / 8;
        int targetFile = targetSquare % 8;

        return Math.abs(currentFile - targetFile) <= 1 && Math.abs(currentRank - targetRank) <= 1;
    }
}
