package tn.zeros.zchess.core.model;

import java.util.Arrays;

import static tn.zeros.zchess.core.util.ChessConstants.*;

public class BoardState {
    private static final long[] SQUARE_MASKS = new long[64];

    static {
        for (int i = 0; i < 64; i++) {
            SQUARE_MASKS[i] = 1L << i;
        }
    }

    private final long[] pieceBitboards = new long[6]; // Indexed by piece type
    private final long[] colorBitboards = new long[2]; // Indexed by color
    private final int[] pieceSquare = new int[64];
    private boolean whiteToMove;
    private int castlingRights;
    private int enPassantSquare;
    private int halfMoveClock;
    private int fullMoveNumber;

    public BoardState() {
        initializeStartingPosition();
    }

    private void initializeStartingPosition() {
        Arrays.fill(pieceSquare, Piece.NONE);
        setRank(Piece.PAWN, Piece.WHITE, 1);
        setBackRank(Piece.WHITE, 0);
        setRank(Piece.PAWN, Piece.BLACK, 6);
        setBackRank(Piece.BLACK, 7);

        whiteToMove = true;
        castlingRights = WHITE_KINGSIDE | WHITE_QUEENSIDE | BLACK_KINGSIDE | BLACK_QUEENSIDE;
        enPassantSquare = -1;
        halfMoveClock = 0;
        fullMoveNumber = 1;
    }

    private void setBackRank(int color, int rank) {
        setPiece(Piece.ROOK, color, 0, rank);
        setPiece(Piece.KNIGHT, color, 1, rank);
        setPiece(Piece.BISHOP, color, 2, rank);
        setPiece(Piece.QUEEN, color, 3, rank);
        setPiece(Piece.KING, color, 4, rank);
        setPiece(Piece.BISHOP, color, 5, rank);
        setPiece(Piece.KNIGHT, color, 6, rank);
        setPiece(Piece.ROOK, color, 7, rank);
    }

    public void setRank(int pieceType, int color, int rank) {
        for (int file = 0; file < 8; file++) {
            setPiece(pieceType, color, file, rank);
        }
    }

    private void setPiece(int pieceType, int color, int file, int rank) {
        int square = rank * 8 + file;
        int colorIndex = getColorIndex(color);
        pieceBitboards[pieceType] |= 1L << square;
        colorBitboards[colorIndex] |= 1L << square;
        pieceSquare[square] = Piece.makePiece(pieceType, color);
    }

    public int getPieceAt(int square) {
        return pieceSquare[square];
    }

    public long getAllPieces() {
        return colorBitboards[getColorIndex(Piece.WHITE)] | colorBitboards[getColorIndex(Piece.BLACK)];
    }

    public long getFriendlyPieces(boolean isWhite) {
        return colorBitboards[getColorIndex(isWhite ? Piece.WHITE : Piece.BLACK)];
    }

    public long getEnemyPieces(boolean isWhite) {
        return colorBitboards[getColorIndex(isWhite ? Piece.BLACK : Piece.WHITE)];
    }

    public long getPiecesOfType(int pieceType) {
        return pieceBitboards[pieceType];
    }

    public long getPieces(int pieceType, int color) {
        return pieceBitboards[pieceType] & colorBitboards[getColorIndex(color)];
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

    public void movePiece(int from, int to, int piece) {
        final long fromMask = SQUARE_MASKS[from];
        final long toMask = SQUARE_MASKS[to];
        final int type = Piece.getType(piece);
        final int color = Piece.getColor(piece);
        final int colorIndex = getColorIndex(color);

        pieceBitboards[type] ^= fromMask | toMask;
        colorBitboards[colorIndex] ^= fromMask | toMask;

        pieceSquare[from] = Piece.NONE;
        pieceSquare[to] = piece;
    }

    public void removePiece(int square, int piece) {
        if (piece == Piece.NONE) return;

        final long mask = SQUARE_MASKS[square];
        final int type = Piece.getType(piece);
        final int color = Piece.getColor(piece);
        final int colorIndex = getColorIndex(color);

        pieceBitboards[type] &= ~mask;
        colorBitboards[colorIndex] &= ~mask;
        pieceSquare[square] = Piece.NONE;
    }

    public void addPiece(int square, int piece) {
        final long mask = SQUARE_MASKS[square];
        final int type = Piece.getType(piece);
        final int color = Piece.getColor(piece);
        final int colorIndex = getColorIndex(color);

        pieceBitboards[type] |= mask;
        colorBitboards[colorIndex] |= mask;
        pieceSquare[square] = piece;
    }

    public int getKingSquare(boolean white) {
        return Long.numberOfTrailingZeros(pieceBitboards[Piece.KING] & colorBitboards[getColorIndex(white ? Piece.WHITE : Piece.BLACK)]);
    }

    private int getColorIndex(int color) {
        return color == Piece.WHITE ? 0 : 1;
    }
}
