package tn.zeros.zchess.core.model;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static tn.zeros.zchess.core.util.ChessConstants.*;

public class BoardState {
    private final long[] pieceBitboards = new long[6]; // Indexed by piece type
    private final long[] colorBitboards = new long[2]; // Indexed by color
    private final int[] pieceSquare = new int[64];
    private final Map<Long, Integer> positionCounts = new ConcurrentHashMap<>();
    private boolean whiteToMove;
    private int castlingRights;
    private int enPassantSquare;
    private int halfMoveClock;
    private int fullMoveNumber;

    private long zobristKey;

    public BoardState() {
        initializeStartingPosition();
    }

    private void initializeStartingPosition() {
        clearPositionCounts();
        zobristKey = 0L;
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

        toggleCastling(castlingRights);
        toggleSideToMove();
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
        togglePiece(pieceType, colorIndex, square);
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
        toggleCastling(this.castlingRights);
        this.castlingRights = rights;
        toggleCastling(this.castlingRights);
    }

    public int getEnPassantSquare() {
        return enPassantSquare;
    }

    public void setEnPassantSquare(int square) {
        // Toggle old file
        if (enPassantSquare != -1) {
            int oldFile = enPassantSquare % 8;
            toggleEnPassant(oldFile);
        }
        // Toggle new file
        this.enPassantSquare = square;
        if (square != -1) {
            int newFile = square % 8;
            toggleEnPassant(newFile);
        }
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
        if (this.whiteToMove != whiteToMove) {
            toggleSideToMove();
            this.whiteToMove = whiteToMove;
        }
    }

    public void movePiece(int from, int to, int piece) {
        final long combinedMask = (1L << from) | (1L << to);
        final int type = piece & 0x7;
        final int colorIndex = getColorIndex(Piece.getColor(piece));

        togglePiece(type, colorIndex, from);
        togglePiece(type, colorIndex, to);
        pieceBitboards[type] ^= combinedMask;
        colorBitboards[colorIndex] ^= combinedMask;
        pieceSquare[from] = Piece.NONE;
        pieceSquare[to] = piece;
    }

    public void removePiece(int square, int piece) {
        if (piece == Piece.NONE) return;
        final long mask = 1L << square;
        final int type = piece & 0x7;
        final int colorIndex = getColorIndex(Piece.getColor(piece));

        togglePiece(type, colorIndex, square);
        pieceBitboards[type] &= ~mask;
        colorBitboards[colorIndex] &= ~mask;
        pieceSquare[square] = Piece.NONE;
    }

    public void addPiece(int square, int piece) {
        final long mask = 1L << square;
        final int type = piece & 0x7;
        final int colorIndex = getColorIndex(Piece.getColor(piece));

        togglePiece(type, colorIndex, square);
        pieceBitboards[type] |= mask;
        colorBitboards[colorIndex] |= mask;
        pieceSquare[square] = piece;
    }

    public Map<Long, Integer> getPositionCounts() {
        return positionCounts;
    }

    public int getKingSquare(boolean white) {
        return Long.numberOfTrailingZeros(pieceBitboards[Piece.KING] & colorBitboards[getColorIndex(white ? Piece.WHITE : Piece.BLACK)]);
    }

    private int getColorIndex(int color) {
        return color >> 3;
    }

    public void clearPositionCounts() {
        this.positionCounts.clear();
    }

    @Override
    public BoardState clone() {
        BoardState cloned = new BoardState();

        System.arraycopy(this.pieceBitboards, 0, cloned.pieceBitboards, 0, this.pieceBitboards.length);
        System.arraycopy(this.colorBitboards, 0, cloned.colorBitboards, 0, this.colorBitboards.length);

        System.arraycopy(this.pieceSquare, 0, cloned.pieceSquare, 0, this.pieceSquare.length);

        cloned.positionCounts.clear();
        cloned.positionCounts.putAll(this.positionCounts);

        cloned.whiteToMove = this.whiteToMove;
        cloned.castlingRights = this.castlingRights;
        cloned.enPassantSquare = this.enPassantSquare;
        cloned.halfMoveClock = this.halfMoveClock;
        cloned.fullMoveNumber = this.fullMoveNumber;
        cloned.zobristKey = this.zobristKey;

        return cloned;
    }

    // Zobrist Hashing
    public long getZobristKey() {
        return zobristKey;
    }

    public void setZobristKey(long zobristKey) {
        this.zobristKey = zobristKey;
    }

    private void togglePiece(int pieceType, int color, int square) {
        int pieceIdx = Zobrist.pieceIndex(pieceType, color);
        zobristKey ^= Zobrist.PIECES[pieceIdx][square];
    }

    private void toggleEnPassant(int file) {
        if (file != -1) zobristKey ^= Zobrist.EN_PASSANT[file];
    }

    private void toggleCastling(int rights) {
        zobristKey ^= Zobrist.CASTLING[rights];
    }

    private void toggleSideToMove() {
        zobristKey ^= Zobrist.SIDE_TO_MOVE;
    }
}
