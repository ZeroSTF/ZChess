package tn.zeros.zchess.core.util;

import static tn.zeros.zchess.core.util.ChessConstants.DIRECTION_OFFSETS;

public class PrecomputedMoves {
    public static final long[] KNIGHT_MOVES = new long[64];
    public static final long[] KING_MOVES = new long[64];
    public static final long[][] RAY_MOVES = new long[64][8];
    public static final long[] WHITE_PAWN_ATTACKS = new long[64];
    public static final long[] BLACK_PAWN_ATTACKS = new long[64];

    // Magic Bitboard Data
    public static final long[][] BISHOP_ATTACKS = new long[64][];
    public static final long[][] ROOK_ATTACKS = new long[64][];

    public static final int[] BISHOP_SHIFTS = new int[64];
    public static final int[] ROOK_SHIFTS = new int[64];

    public static final long[] BISHOP_MASKS = new long[64];
    public static final long[] ROOK_MASKS = new long[64];

    static {
        initializeMagicBitboards();
        initializeKnightMoves();
        initializeKingMoves();
        initializeSlidingPieces();
        initializePawnAttacks();
    }

    private static void initializeMagicBitboards() {
        // Initialize masks and shifts
        for (int square = 0; square < 64; square++) {
            BISHOP_MASKS[square] = calculateBishopMask(square);
            ROOK_MASKS[square] = calculateRookMask(square);

            BISHOP_SHIFTS[square] = 64 - Long.bitCount(BISHOP_MASKS[square]);
            ROOK_SHIFTS[square] = 64 - Long.bitCount(ROOK_MASKS[square]);
        }

        // Initialize attack tables
        initializeAttackTables(BISHOP_ATTACKS, ChessConstants.BISHOP_MAGICS, BISHOP_MASKS, BISHOP_SHIFTS, true);
        initializeAttackTables(ROOK_ATTACKS, ChessConstants.ROOK_MAGICS, ROOK_MASKS, ROOK_SHIFTS, false);
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

    // Mask calculation
    private static long calculateBishopMask(int square) {
        long attacks = 0L;
        int r = square / 8;
        int f = square % 8;

        for (int i = 1; i < 7; i++) {
            if (r + i < 7 && f + i < 7) attacks |= 1L << ((r + i) * 8 + (f + i));
            if (r + i < 7 && f - i > 0) attacks |= 1L << ((r + i) * 8 + (f - i));
            if (r - i > 0 && f + i < 7) attacks |= 1L << ((r - i) * 8 + (f + i));
            if (r - i > 0 && f - i > 0) attacks |= 1L << ((r - i) * 8 + (f - i));
        }
        return attacks;
    }

    private static long calculateRookMask(int square) {
        long attacks = 0L;
        int r = square / 8;
        int f = square % 8;

        for (int i = r + 1; i < 7; i++) attacks |= 1L << (i * 8 + f); // Up
        for (int i = r - 1; i > 0; i--) attacks |= 1L << (i * 8 + f); // Down
        for (int i = f + 1; i < 7; i++) attacks |= 1L << (r * 8 + i); // Right
        for (int i = f - 1; i > 0; i--) attacks |= 1L << (r * 8 + i); // Left

        return attacks;
    }

    private static void initializeAttackTables(long[][] attackTable, long[] magics, long[] masks, int[] shifts, boolean isBishop) {
        for (int square = 0; square < 64; square++) {
            int numBits = Long.bitCount(masks[square]);
            int size = 1 << (64 - shifts[square]);
            attackTable[square] = new long[size];

            long mask = masks[square];
            long magic = magics[square];

            long occ = 0L;
            do {
                // Calculate index
                long index = (occ & mask) * magic;
                index >>>= shifts[square];

                // Generate attacks
                attackTable[square][(int) index] = isBishop ?
                        generateBishopAttacks(square, occ) :
                        generateRookAttacks(square, occ);

                // Next occupancy
                occ = (occ - mask) & mask;
            } while (occ != 0);
        }
    }

    public static long generateBishopAttacks(int square, long occupancy) {
        long attacks = 0L;
        int r = square / 8;
        int f = square % 8;

        // Northeast
        for (int nr = r + 1, nf = f + 1; nr < 8 && nf < 8; nr++, nf++) {
            attacks |= 1L << (nr * 8 + nf);
            if ((occupancy & (1L << (nr * 8 + nf))) != 0) break;
        }

        // Northwest
        for (int nr = r + 1, nf = f - 1; nr < 8 && nf >= 0; nr++, nf--) {
            attacks |= 1L << (nr * 8 + nf);
            if ((occupancy & (1L << (nr * 8 + nf))) != 0) break;
        }

        // Southeast
        for (int nr = r - 1, nf = f + 1; nr >= 0 && nf < 8; nr--, nf++) {
            attacks |= 1L << (nr * 8 + nf);
            if ((occupancy & (1L << (nr * 8 + nf))) != 0) break;
        }

        // Southwest
        for (int nr = r - 1, nf = f - 1; nr >= 0 && nf >= 0; nr--, nf--) {
            attacks |= 1L << (nr * 8 + nf);
            if ((occupancy & (1L << (nr * 8 + nf))) != 0) break;
        }

        return attacks;
    }

    public static long generateRookAttacks(int square, long occupancy) {
        long attacks = 0L;
        int r = square / 8;
        int f = square % 8;

        // North
        for (int nr = r + 1; nr < 8; nr++) {
            attacks |= 1L << (nr * 8 + f);
            if ((occupancy & (1L << (nr * 8 + f))) != 0) break;
        }

        // South
        for (int nr = r - 1; nr >= 0; nr--) {
            attacks |= 1L << (nr * 8 + f);
            if ((occupancy & (1L << (nr * 8 + f))) != 0) break;
        }

        // East
        for (int nf = f + 1; nf < 8; nf++) {
            attacks |= 1L << (r * 8 + nf);
            if ((occupancy & (1L << (r * 8 + nf))) != 0) break;
        }

        // West
        for (int nf = f - 1; nf >= 0; nf--) {
            attacks |= 1L << (r * 8 + nf);
            if ((occupancy & (1L << (r * 8 + nf))) != 0) break;
        }

        return attacks;
    }
}
