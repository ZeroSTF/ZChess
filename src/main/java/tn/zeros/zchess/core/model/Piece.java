package tn.zeros.zchess.core.model;

public enum Piece {
    WHITE_PAWN(PieceType.PAWN, Color.WHITE),
    WHITE_KNIGHT(PieceType.KNIGHT, Color.WHITE),
    WHITE_BISHOP(PieceType.BISHOP, Color.WHITE),
    WHITE_ROOK(PieceType.ROOK, Color.WHITE),
    WHITE_QUEEN(PieceType.QUEEN, Color.WHITE),
    WHITE_KING(PieceType.KING, Color.WHITE),
    BLACK_PAWN(PieceType.PAWN, Color.BLACK),
    BLACK_KNIGHT(PieceType.KNIGHT, Color.BLACK),
    BLACK_BISHOP(PieceType.BISHOP, Color.BLACK),
    BLACK_ROOK(PieceType.ROOK, Color.BLACK),
    BLACK_QUEEN(PieceType.QUEEN, Color.BLACK),
    BLACK_KING(PieceType.KING, Color.BLACK),
    NONE(null, null);

    public final PieceType type;
    public final Color color;

    private Piece(PieceType type, Color color) {
        this.type = type;
        this.color = color;
    }

    public static Piece fromSymbol(char symbol) {
        return switch (symbol) {
            case 'P' -> WHITE_PAWN;
            case 'N' -> WHITE_KNIGHT;
            case 'B' -> WHITE_BISHOP;
            case 'R' -> WHITE_ROOK;
            case 'Q' -> WHITE_QUEEN;
            case 'K' -> WHITE_KING;
            case 'p' -> BLACK_PAWN;
            case 'n' -> BLACK_KNIGHT;
            case 'b' -> BLACK_BISHOP;
            case 'r' -> BLACK_ROOK;
            case 'q' -> BLACK_QUEEN;
            case 'k' -> BLACK_KING;
            default -> NONE;
        };
    }

    public char getSymbol() {
        return switch (this) {
            case WHITE_PAWN -> 'P';
            case WHITE_KNIGHT -> 'N';
            case WHITE_BISHOP -> 'B';
            case WHITE_ROOK -> 'R';
            case WHITE_QUEEN -> 'Q';
            case WHITE_KING -> 'K';
            case BLACK_PAWN -> 'p';
            case BLACK_KNIGHT -> 'n';
            case BLACK_BISHOP -> 'b';
            case BLACK_ROOK -> 'r';
            case BLACK_QUEEN -> 'q';
            case BLACK_KING -> 'k';
            default -> ' ';
        };
    }

    public boolean isWhite() {
        return color == Color.WHITE;
    }

    public boolean isPawn() {
        return type == PieceType.PAWN;
    }

    public boolean isRook() {
        return type == PieceType.ROOK;
    }

    public boolean isKing() {
        return type == PieceType.KING;
    }

    public boolean isKnight() {
        return type == PieceType.KNIGHT;
    }

    public boolean isBishop() {
        return type == PieceType.BISHOP;
    }

    public boolean isQueen() {
        return type == PieceType.QUEEN;
    }

    public enum PieceType {PAWN, KNIGHT, BISHOP, ROOK, QUEEN, KING}

    public enum Color {WHITE, BLACK}
}
