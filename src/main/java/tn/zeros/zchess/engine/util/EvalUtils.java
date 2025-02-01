package tn.zeros.zchess.engine.util;

public class EvalUtils {
    public static final int PAWN_VALUE = 100;
    public static final int KNIGHT_VALUE = 300;
    public static final int BISHOP_VALUE = 300;
    public static final int ROOK_VALUE = 500;
    public static final int QUEEN_VALUE = 900;
    public static final int KING_VALUE = 10000;

    public static final float ENDGAME_MATERIAL_START = ROOK_VALUE * 2 + BISHOP_VALUE + KNIGHT_VALUE;

    private static final int[] PIECE_VALUES = {
            PAWN_VALUE, // 0: pawn
            KNIGHT_VALUE,    // 1: knight
            BISHOP_VALUE,  // 2: bishop
            ROOK_VALUE,  // 3: rook
            QUEEN_VALUE,    // 4: queen
            KING_VALUE,   // 5: queen
            0     // 6: none
    };

    public static int getPieceTypeValue(int pieceType) {
        if (pieceType >= 0 && pieceType < 7) {
            return PIECE_VALUES[pieceType];
        }
        return 0;
    }
}
