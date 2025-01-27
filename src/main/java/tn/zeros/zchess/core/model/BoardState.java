package tn.zeros.zchess.core.model;

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
    private final Piece[] pieceSquare = new Piece[64];
    private boolean whiteToMove;
    private int castlingRights;
    private int enPassantSquare;
    private int halfMoveClock;
    private int fullMoveNumber;

    public BoardState() {
        initializeStartingPosition();
    }

    private void initializeStartingPosition() {
        setRank(PAWN, WHITE, 1);
        setBackRank(WHITE, 0);
        setRank(PAWN, BLACK, 6);
        setBackRank(BLACK, 7);

        whiteToMove = true;
        castlingRights = WHITE_KINGSIDE | WHITE_QUEENSIDE | BLACK_KINGSIDE | BLACK_QUEENSIDE;
        enPassantSquare = -1;
        halfMoveClock = 0;
        fullMoveNumber = 1;
    }

    private void setBackRank(int color, int rank) {
        setPiece(ROOK, color, 0, rank);
        setPiece(KNIGHT, color, 1, rank);
        setPiece(BISHOP, color, 2, rank);
        setPiece(QUEEN, color, 3, rank);
        setPiece(KING, color, 4, rank);
        setPiece(BISHOP, color, 5, rank);
        setPiece(KNIGHT, color, 6, rank);
        setPiece(ROOK, color, 7, rank);
    }

    public void setRank(int pieceType, int color, int rank) {
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

    public long getFriendlyPieces(boolean isWhite) {
        return colorBitboards[isWhite ? WHITE : BLACK];
    }

    public long getEnemyPieces(boolean isWhite) {
        return colorBitboards[isWhite ? BLACK : WHITE];
    }

    public long getPiecesOfType(int pieceType) {
        return pieceBitboards[pieceType];
    }

    public long getPieces(int pieceType, int color) {
        return pieceBitboards[pieceType] & colorBitboards[color];
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

    public void movePiece(int from, int to, Piece piece) {
        final long fromMask = SQUARE_MASKS[from];
        final long toMask = SQUARE_MASKS[to];
        final int type = piece.type.ordinal();
        final int color = piece.color.ordinal();

        // Update bitboards using precomputed masks
        pieceBitboards[type] ^= fromMask | toMask;
        colorBitboards[color] ^= fromMask | toMask;

        // Update pieceSquare array
        pieceSquare[from] = null;
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

    public int getKingSquare(boolean white) {
        return Long.numberOfTrailingZeros(getFriendlyPieces(white) & getPieceBitboard(KING));
    }
}
