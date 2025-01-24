package tn.zeros.zchess.core.model;

import java.util.Arrays;

public class GameState {
    private final long[] pieceBitboards;
    private final long[] colorBitboards;
    private final int castlingRights;
    private final int enPassantSquare;
    private final int halfMoveClock;
    private final int fullMoveNumber;
    private final boolean whiteToMove;

    public GameState(long[] pieces, long[] colors, int castling,
                     int epSquare, int halfMove, int fullMove, boolean whiteTurn) {
        this.pieceBitboards = Arrays.copyOf(pieces, pieces.length);
        this.colorBitboards = Arrays.copyOf(colors, colors.length);
        this.castlingRights = castling;
        this.enPassantSquare = epSquare;
        this.halfMoveClock = halfMove;
        this.fullMoveNumber = fullMove;
        this.whiteToMove = whiteTurn;
    }

    public void restore(BoardState target) {
        System.arraycopy(this.pieceBitboards, 0, target.getPieceBitboards(), 0, 6);
        System.arraycopy(this.colorBitboards, 0, target.getColorBitboards(), 0, 2);
        target.setCastlingRights(this.castlingRights);
        target.setEnPassantSquare(this.enPassantSquare);
        target.setHalfMoveClock(this.halfMoveClock);
        target.setFullMoveNumber(this.fullMoveNumber);
        target.setWhiteToMove(this.whiteToMove);
    }
}
