package tn.zeros.zchess.core.board;

public class ChessBoard {
    private long[] pieces;
    private long[] colors;

    public static final int WHITE = 0;
    public static final int BLACK = 1;

    // Piece indices
    public static final int PAWN = 0;
    public static final int KNIGHT = 1;
    public static final int BISHOP = 2;
    public static final int ROOK = 3;
    public static final int QUEEN = 4;
    public static final int KING = 5;

    public ChessBoard() {
        pieces = new long[6];
        colors = new long[2];
        initializeStartingPosition();
    }

    private void initializeStartingPosition() {
        // Initialize empty bitboards
        for(int i = 0; i < 6; i++) {
            pieces[i] = 0L;
        }
        colors[WHITE] = 0L;
        colors[BLACK] = 0L;

        // Set up pawns
        setRank(PAWN, WHITE, 6);  // White pawns on rank 2 (0-based index 6)
        setRank(PAWN, BLACK, 1);  // Black pawns on rank 7 (0-based index 1)

        // White pieces
        setPiece(ROOK, WHITE, 0, 0);
        setPiece(KNIGHT, WHITE, 1, 0);
        setPiece(BISHOP, WHITE, 2, 0);
        setPiece(QUEEN, WHITE, 3, 0);
        setPiece(KING, WHITE, 4, 0);
        setPiece(BISHOP, WHITE, 5, 0);
        setPiece(KNIGHT, WHITE, 6, 0);
        setPiece(ROOK, WHITE, 7, 0);

        // Black pieces
        setPiece(ROOK, BLACK, 0, 7);
        setPiece(KNIGHT, BLACK, 1, 7);
        setPiece(BISHOP, BLACK, 2, 7);
        setPiece(QUEEN, BLACK, 3, 7);
        setPiece(KING, BLACK, 4, 7);
        setPiece(BISHOP, BLACK, 5, 7);
        setPiece(KNIGHT, BLACK, 6, 7);
        setPiece(ROOK, BLACK, 7, 7);
    }

    private void setRank(int pieceType, int color, int rank) {
        for(int file = 0; file < 8; file++) {
            int square = rank * 8 + file;
            pieces[pieceType] = Bitboard.setBit(pieces[pieceType], square);
            colors[color] = Bitboard.setBit(colors[color], square);
        }
    }

    private void setPiece(int pieceType, int color, int file, int rank) {
        int square = rank * 8 + file;
        pieces[pieceType] = Bitboard.setBit(pieces[pieceType], square);
        colors[color] = Bitboard.setBit(colors[color], square);
    }

    public long getPieceBitboard(int pieceType) {
        return pieces[pieceType];
    }

    public long getColorBitboard(int color) {
        return colors[color];
    }

    public int getPieceAt(int square) {
        for (int piece = PAWN; piece <= KING; piece++) {
            if (Bitboard.isBitSet(pieces[piece], square)) {
                return piece;
            }
        }
        return -1;
    }

    public int getColorAt(int square) {
        if (Bitboard.isBitSet(colors[WHITE], square)) return WHITE;
        if (Bitboard.isBitSet(colors[BLACK], square)) return BLACK;
        return -1;
    }
}
