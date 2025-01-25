package tn.zeros.zchess.core.model;

public enum Piece {
    WHITE_PAWN('P'), WHITE_KNIGHT('N'), WHITE_BISHOP('B'), WHITE_ROOK('R'), WHITE_QUEEN('Q'), WHITE_KING('K'),
    BLACK_PAWN('p'), BLACK_KNIGHT('n'), BLACK_BISHOP('b'), BLACK_ROOK('r'), BLACK_QUEEN('q'), BLACK_KING('k'),
    NONE(' ');

    private final char symbol;

    Piece(char symbol) {
        this.symbol = symbol;
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
        return this.name().startsWith("WHITE");
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
}
