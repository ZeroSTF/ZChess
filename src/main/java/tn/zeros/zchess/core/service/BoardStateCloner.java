package tn.zeros.zchess.core.service;

import tn.zeros.zchess.core.model.BoardState;

public class BoardStateCloner {
    public static BoardState clone(BoardState original) {
        BoardState clone = new BoardState();

        System.arraycopy(original.getPieceBitboards(), 0, clone.getPieceBitboards(), 0, 6);
        System.arraycopy(original.getColorBitboards(), 0, clone.getColorBitboards(), 0, 2);
        clone.setWhiteToMove(original.isWhiteToMove());
        clone.setCastlingRights(original.getCastlingRights());
        clone.setEnPassantSquare(original.getEnPassantSquare());
        clone.setHalfMoveClock(original.getHalfMoveClock());
        clone.setFullMoveNumber(original.getFullMoveNumber());

        return clone;
    }
}