package tn.zeros.zchess.engine.util;

import tn.zeros.zchess.core.model.Piece;

public class EvalUtils {
    public static final int PAWN_VALUE = 100;
    public static final int KNIGHT_VALUE = 300;
    public static final int BISHOP_VALUE = 300;
    public static final int ROOK_VALUE = 500;
    public static final int QUEEN_VALUE = 900;
    public static final int KING_VALUE = 10000;

    public static int getPieceTypeValue(int pieceType) {
        return switch (pieceType) {
            case Piece.PAWN -> PAWN_VALUE;
            case Piece.KNIGHT -> KNIGHT_VALUE;
            case Piece.BISHOP -> BISHOP_VALUE;
            case Piece.ROOK -> ROOK_VALUE;
            case Piece.QUEEN -> QUEEN_VALUE;
            case Piece.KING -> KING_VALUE;
            default -> 0;
        };
    }
}
