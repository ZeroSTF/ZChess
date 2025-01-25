package tn.zeros.zchess.core.model;

public record MoveUndoInfo(
        Move move,
        int previousCastlingRights,
        int previousEnPassantSquare,
        int previousHalfMoveClock
) {
}