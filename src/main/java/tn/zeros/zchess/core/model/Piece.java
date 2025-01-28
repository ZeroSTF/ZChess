package tn.zeros.zchess.core.model;

public class Piece {
    public static final int PIECE_MASK = 0x7;
    public static final int COLOR_MASK = 0x8;

    // Pieces (3 bits)
    public static final int PAWN = 0;
    public static final int KNIGHT = 1;
    public static final int BISHOP = 2;
    public static final int ROOK = 3;
    public static final int QUEEN = 4;
    public static final int KING = 5;
    public static final int NONE = 6;


    // Colors (1 bit)
    public static final int WHITE = 0;
    public static final int BLACK = 8;

    public static int getType(int piece) {
        return piece & PIECE_MASK;
    }

    public static int getColor(int piece) {
        return piece & COLOR_MASK;
    }

    public static int makePiece(int type, int color) {
        return type | color;
    }

    public static char getSymbol(int piece) {
        int type = getType(piece);
        boolean isBlack = getColor(piece) == BLACK;
        return switch (type) {
            case PAWN -> isBlack ? 'p' : 'P';
            case KNIGHT -> isBlack ? 'n' : 'N';
            case BISHOP -> isBlack ? 'b' : 'B';
            case ROOK -> isBlack ? 'r' : 'R';
            case QUEEN -> isBlack ? 'q' : 'Q';
            case KING -> isBlack ? 'k' : 'K';
            default -> ' ';
        };
    }

    public static String getName(int piece) {
        int type = getType(piece);
        boolean isBlack = getColor(piece) == BLACK;
        return switch (type) {
            case PAWN -> isBlack ? "BLACK_PAWN" : "WHITE_PAWN";
            case KNIGHT -> isBlack ? "BLACK_KNIGHT" : "WHITE_KNIGHT";
            case BISHOP -> isBlack ? "BLACK_BISHOP" : "WHITE_BISHOP";
            case ROOK -> isBlack ? "BLACK_ROOK" : "WHITE_ROOK";
            case QUEEN -> isBlack ? "BLACK_QUEEN" : "WHITE_QUEEN";
            case KING -> isBlack ? "BLACK_KING" : "WHITE_KING";
            default -> " ";
        };
    }

    public static int fromSymbol(char symbol) {
        return switch (symbol) {
            case 'P' -> makePiece(PAWN, WHITE);
            case 'N' -> makePiece(KNIGHT, WHITE);
            case 'B' -> makePiece(BISHOP, WHITE);
            case 'R' -> makePiece(ROOK, WHITE);
            case 'Q' -> makePiece(QUEEN, WHITE);
            case 'K' -> makePiece(KING, WHITE);
            case 'p' -> makePiece(PAWN, BLACK);
            case 'n' -> makePiece(KNIGHT, BLACK);
            case 'b' -> makePiece(BISHOP, BLACK);
            case 'r' -> makePiece(ROOK, BLACK);
            case 'q' -> makePiece(QUEEN, BLACK);
            case 'k' -> makePiece(KING, BLACK);
            default -> 0;
        };
    }

    public static boolean isWhite(int piece) {
        return getColor(piece) == WHITE;
    }

    public static boolean isPawn(int piece) {
        return getType(piece) == PAWN;
    }

    public static boolean isKnight(int piece) {
        return getType(piece) == KNIGHT;
    }

    public static boolean isBishop(int piece) {
        return getType(piece) == BISHOP;
    }

    public static boolean isRook(int piece) {
        return getType(piece) == ROOK;
    }

    public static boolean isQueen(int piece) {
        return getType(piece) == QUEEN;
    }

    public static boolean isKing(int piece) {
        return getType(piece) == KING;
    }
}
