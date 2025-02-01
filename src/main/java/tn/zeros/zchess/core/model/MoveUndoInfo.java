package tn.zeros.zchess.core.model;

public record MoveUndoInfo(
        int move,
        int previousCastlingRights,
        int previousEnPassantSquare,
        int previousHalfMoveClock,
        long zobristKey
) {
}