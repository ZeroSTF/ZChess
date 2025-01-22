package tn.zeros.zchess.core.board;

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
        this.pieceBitboards = pieces.clone();
        this.colorBitboards = colors.clone();
        this.castlingRights = castling;
        this.enPassantSquare = epSquare;
        this.halfMoveClock = halfMove;
        this.fullMoveNumber = fullMove;
        this.whiteToMove = whiteTurn;
    }

    public void restore(BitboardPosition pos) {
        System.arraycopy(pieceBitboards, 0, pos.pieceBitboards, 0, 6);
        System.arraycopy(colorBitboards, 0, pos.colorBitboards, 0, 2);
        pos.castlingRights = castlingRights;
        pos.enPassantSquare = enPassantSquare;
        pos.halfMoveClock = halfMoveClock;
        pos.fullMoveNumber = fullMoveNumber;
        pos.whiteToMove = whiteToMove;
    }
}
