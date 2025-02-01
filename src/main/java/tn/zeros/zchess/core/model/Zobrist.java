package tn.zeros.zchess.core.model;

import java.util.Random;

public class Zobrist {
    public static final long[][] PIECES = new long[12][64]; // 6 types * 2 colors
    public static final long[] EN_PASSANT = new long[8]; // Files
    public static final long[] CASTLING = new long[16]; // 4 bits
    public static final long SIDE_TO_MOVE;

    static {
        Random rand = new Random(0x12345678); // Fixed seed for reproducibility
        for (int i = 0; i < PIECES.length; i++) {
            for (int j = 0; j < PIECES[i].length; j++) {
                PIECES[i][j] = rand.nextLong();
            }
        }
        for (int i = 0; i < EN_PASSANT.length; i++) {
            EN_PASSANT[i] = rand.nextLong();
        }
        for (int i = 0; i < CASTLING.length; i++) {
            CASTLING[i] = rand.nextLong();
        }
        SIDE_TO_MOVE = rand.nextLong();
    }

    public static int pieceIndex(int pieceType, int color) {
        return pieceType + 6 * color;
    }
}