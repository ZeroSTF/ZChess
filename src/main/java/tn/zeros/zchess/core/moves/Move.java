package tn.zeros.zchess.core.moves;

public class Move {
    public static final int NORMAL = 0;
    public static final int CASTLING = 1;
    public static final int EN_PASSANT = 2;
    public static final int PROMOTION = 3;

    private final int fromSquare;
    private final int toSquare;
    private final int movedPiece;
    private final int flags;
    private int capturedPiece;

    public Move(int from, int to, int piece, int flags) {
        this.fromSquare = from;
        this.toSquare = to;
        this.movedPiece = piece;
        this.flags = flags;
    }

    // Getters and utility methods
    public int getFrom() { return fromSquare; }
    public int getTo() { return toSquare; }
    public int getPiece() { return movedPiece; }
    public int getFlags() { return flags; }
    public int getCapturedPiece() { return capturedPiece; }
    public void setCapturedPiece(int piece) { capturedPiece = piece; }

    @Override
    public String toString() {
        return String.format("%s%d-%s%d",
                (char) ('a' + (fromSquare % 8)), 8 - (fromSquare / 8),
                (char) ('a' + (toSquare % 8)), 8 - (toSquare / 8));
    }
}
