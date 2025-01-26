package tn.zeros.zchess.core.model;

public enum Piece {
    WHITE_PAWN('P', true), WHITE_KNIGHT('N', true), WHITE_BISHOP('B', true), WHITE_ROOK('R', true), WHITE_QUEEN('Q', true), WHITE_KING('K', true),
    BLACK_PAWN('p', false), BLACK_KNIGHT('n', false), BLACK_BISHOP('b', false), BLACK_ROOK('r', false), BLACK_QUEEN('q', false), BLACK_KING('k', false),
    NONE(' ', false);

    private final char symbol;
    private final boolean isWhite;

    Piece(char symbol, boolean isWhite) {
        this.symbol = symbol;
        this.isWhite = isWhite;
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
        return symbol;
    }

    public boolean isWhite() {
        return isWhite;
    }

    public boolean isPawn() {
        return this == WHITE_PAWN || this == BLACK_PAWN;
    }

    public boolean isRook() {
        return this == WHITE_ROOK || this == BLACK_ROOK;
    }

    public boolean isKing() {
        return this == WHITE_KING || this == BLACK_KING;
    }

    public boolean isKnight() {
        return this == WHITE_KNIGHT || this == BLACK_KNIGHT;
    }

    public boolean isBishop() {
        return this == WHITE_BISHOP || this == BLACK_BISHOP;
    }

    public boolean isQueen() {
        return this == WHITE_QUEEN || this == BLACK_QUEEN;
    }
}
