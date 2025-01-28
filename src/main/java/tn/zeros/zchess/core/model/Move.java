package tn.zeros.zchess.core.model;

public record Move(int fromSquare, int toSquare, int piece, int capturedPiece, boolean isPromotion, boolean isCastling,
                   boolean isEnPassant, int promotionPiece) {

    public Move(int fromSquare, int toSquare, int piece, int capturedPiece,
                boolean isPromotion, boolean isCastling, boolean isEnPassant, int promotionPiece) {
        this.fromSquare = fromSquare;
        this.toSquare = toSquare;
        this.piece = piece;
        this.capturedPiece = capturedPiece;
        this.isPromotion = isPromotion;
        this.isCastling = isCastling;
        this.isEnPassant = isEnPassant;
        this.promotionPiece = promotionPiece;
    }
}
