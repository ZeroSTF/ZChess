package tn.zeros.zchess.core.piece;

public enum Piece {
    WHITE_PAWN('P'), WHITE_KNIGHT('N'), WHITE_BISHOP('B'), WHITE_ROOK('R'), WHITE_QUEEN('Q'), WHITE_KING('K'),
    BLACK_PAWN('p'), BLACK_KNIGHT('n'), BLACK_BISHOP('b'), BLACK_ROOK('r'), BLACK_QUEEN('q'), BLACK_KING('k'),
    NONE(' ');

    private final char symbol;

    Piece(char symbol) {
        this.symbol = symbol;
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

    public boolean isKnight() {
        return this == WHITE_KNIGHT || this == BLACK_KNIGHT;
    }

    public boolean isBishop() {
        return this == WHITE_BISHOP || this == BLACK_BISHOP;
    }

    public boolean isRook() {
        return this == WHITE_ROOK || this == BLACK_ROOK;
    }

    public boolean isQueen() {
        return this == WHITE_QUEEN || this == BLACK_QUEEN;
    }

    public boolean isKing() {
        return this == WHITE_KING || this == BLACK_KING;
    }
}
