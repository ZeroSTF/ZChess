package tn.zeros.zchess.core.util;

public class ChessConstants {
    // Piece Types (indexes for bitboard arrays)
    public static final int PAWN = 0;
    public static final int KNIGHT = 1;
    public static final int BISHOP = 2;
    public static final int ROOK = 3;
    public static final int QUEEN = 4;
    public static final int KING = 5;

    // Colors
    public static final int WHITE = 0;
    public static final int BLACK = 1;

    // Castling Rights
    public static final int WHITE_KINGSIDE = 0b0001;
    public static final int WHITE_QUEENSIDE = 0b0010;
    public static final int BLACK_KINGSIDE = 0b0100;
    public static final int BLACK_QUEENSIDE = 0b1000;

    // Board Geometry
    public static final int BOARD_SIZE = 8;
    public static final int NUM_SQUARES = 64;
    public static final long FIRST_RANK = 0xFFL;
    public static final long LAST_RANK = 0xFF00000000000000L;
    public static final long FILE_A = 0x0101010101010101L;
    public static final long FILE_H = 0x8080808080808080L;

    // FEN Constants
    public static final String DEFAULT_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
    public static final String FEN_DELIMITER = " ";
    public static final String FEN_WHITE_ACTIVE = "w";
    public static final String FEN_BLACK_ACTIVE = "b";

    // Direction Offsets (N, NE, E, SE, S, SW, W, NW)
    public static final int[] DIRECTION_OFFSETS = {8, 9, 1, -7, -8, -9, -1, 7};

    // Special Square Markers
    public static final int NO_EN_PASSANT = -1;
    public static final int MAX_GAME_LENGTH = 600; // Max ply count

    // Bitboard printing
    public static void printBitboard(long bitboard) {
        for (int rank = 7; rank >= 0; rank--) {
            for (int file = 0; file < 8; file++) {
                int square = rank * 8 + file;
                System.out.print(((bitboard >> square) & 1) == 1 ? "1 " : ". ");
            }
            System.out.println();
        }
        System.out.println();
    }

}
