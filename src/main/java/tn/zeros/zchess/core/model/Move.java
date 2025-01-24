package tn.zeros.zchess.core.model;

public class Move {
    private final int fromSquare;
    private final int toSquare;
    private final Piece piece;
    private final Piece capturedPiece;
    private final boolean isPromotion;
    private final boolean isCastling;
    private final boolean isEnPassant;
    private final Piece promotionPiece;

    public Move(int fromSquare, int toSquare, Piece piece, Piece capturedPiece,
                boolean isPromotion, boolean isCastling, boolean isEnPassant, Piece promotionPiece) {
        this.fromSquare = fromSquare;
        this.toSquare = toSquare;
        this.piece = piece;
        this.capturedPiece = capturedPiece != null ? capturedPiece : Piece.NONE;
        this.isPromotion = isPromotion;
        this.isCastling = isCastling;
        this.isEnPassant = isEnPassant;
        this.promotionPiece = promotionPiece;
    }

    // Getters
    public int getFromSquare() { return fromSquare; }
    public int getToSquare() { return toSquare; }
    public Piece getPiece() { return piece; }
    public Piece getCapturedPiece() { return capturedPiece; }
    public boolean isPromotion() { return isPromotion; }
    public boolean isCastling() { return isCastling; }
    public boolean isEnPassant() { return isEnPassant; }
    public Piece getPromotionPiece() { return promotionPiece; }
}
