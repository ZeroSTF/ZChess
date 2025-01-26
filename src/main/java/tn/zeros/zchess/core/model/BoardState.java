package tn.zeros.zchess.core.model;

import static tn.zeros.zchess.core.util.ChessConstants.*;

public class BoardState {
    private final long[] pieceBitboards; // Indexed by piece type
    private final long[] colorBitboards; // Indexed by color
    private final Piece[] pieceSquare = new Piece[64];
    private boolean whiteToMove;
    private int castlingRights;
    private int enPassantSquare;
    private int halfMoveClock;
    private int fullMoveNumber;

    public BoardState() {
        this.pieceBitboards = new long[6];
        this.colorBitboards = new long[2];
        initializeStartingPosition();
    }

    private void initializeStartingPosition() {
        // Clear all bitboards
        for (int i = 0; i < 6; i++) pieceBitboards[i] = 0L;
        colorBitboards[WHITE] = 0L;
        colorBitboards[BLACK] = 0L;

        // Set up white pieces
        setRank(PAWN, WHITE, 1);
        setPiece(ROOK, WHITE, 0, 0);
        setPiece(KNIGHT, WHITE, 1, 0);
        setPiece(BISHOP, WHITE, 2, 0);
        setPiece(QUEEN, WHITE, 3, 0);
        setPiece(KING, WHITE, 4, 0);
        setPiece(BISHOP, WHITE, 5, 0);
        setPiece(KNIGHT, WHITE, 6, 0);
        setPiece(ROOK, WHITE, 7, 0);

        // Set up black pieces
        setRank(PAWN, BLACK, 6);
        setPiece(ROOK, BLACK, 0, 7);
        setPiece(KNIGHT, BLACK, 1, 7);
        setPiece(BISHOP, BLACK, 2, 7);
        setPiece(QUEEN, BLACK, 3, 7);
        setPiece(KING, BLACK, 4, 7);
        setPiece(BISHOP, BLACK, 5, 7);
        setPiece(KNIGHT, BLACK, 6, 7);
        setPiece(ROOK, BLACK, 7, 7);

        whiteToMove = true;
        castlingRights = WHITE_KINGSIDE | WHITE_QUEENSIDE | BLACK_KINGSIDE | BLACK_QUEENSIDE;
        enPassantSquare = -1;
        halfMoveClock = 0;
        fullMoveNumber = 1;
    }

    private void setRank(int pieceType, int color, int rank) {
        for (int file = 0; file < 8; file++) {
            setPiece(pieceType, color, file, rank);
        }
    }

    private void setPiece(int pieceType, int color, int file, int rank) {
        int square = rank * 8 + file;
        pieceBitboards[pieceType] |= 1L << square;
        colorBitboards[color] |= 1L << square;
        pieceSquare[square] = Piece.values()[color * 6 + pieceType];
    }

    public Piece getPieceAt(int square) {
        return pieceSquare[square] != null ? pieceSquare[square] : Piece.NONE;
    }

    public long getAllPieces() {
        return colorBitboards[WHITE] | colorBitboards[BLACK];
    }

    public int getCastlingRights() {
        return castlingRights;
    }

    public void setCastlingRights(int rights) {
        this.castlingRights = rights;
    }

    public int getEnPassantSquare() {
        return enPassantSquare;
    }

    public void setEnPassantSquare(int enPassantSquare) {
        this.enPassantSquare = enPassantSquare;
    }

    public int getHalfMoveClock() {
        return halfMoveClock;
    }

    public void setHalfMoveClock(int halfMoveClock) {
        this.halfMoveClock = halfMoveClock;
    }

    public int getFullMoveNumber() {
        return fullMoveNumber;
    }

    public void setFullMoveNumber(int fullMoveNumber) {
        this.fullMoveNumber = fullMoveNumber;
    }

    public boolean isWhiteToMove() {
        return whiteToMove;
    }

    public void setWhiteToMove(boolean whiteToMove) {
        this.whiteToMove = whiteToMove;
    }

    public long getPieceBitboard(int pieceType) {
        return pieceBitboards[pieceType];
    }

    public long getColorBitboard(int color) {
        return colorBitboards[color];
    }

    public void movePiece(int from, int to, Piece piece) {
        int type = piece.ordinal() % 6;
        int color = piece.isWhite() ? WHITE : BLACK;
        long mask = 1L << from;

        // Remove from original square
        pieceBitboards[type] &= ~mask;
        colorBitboards[color] &= ~mask;

        // Add to new square
        mask = 1L << to;
        pieceBitboards[type] |= mask;
        colorBitboards[color] |= mask;

        pieceSquare[from] = Piece.NONE;
        pieceSquare[to] = piece;
    }

    public void removePiece(int square, Piece piece) {
        if (piece == Piece.NONE || piece == null) return;
        int type = piece.ordinal() % 6;
        int color = piece.isWhite() ? WHITE : BLACK;
        long mask = ~(1L << square);
        pieceBitboards[type] &= mask;
        colorBitboards[color] &= mask;

        pieceSquare[square] = Piece.NONE;
    }

    public void addPiece(int square, Piece piece) {
        int type = piece.ordinal() % 6;
        int color = piece.isWhite() ? WHITE : BLACK;
        pieceBitboards[type] |= 1L << square;
        colorBitboards[color] |= 1L << square;

        pieceSquare[square] = piece;
    }

}
