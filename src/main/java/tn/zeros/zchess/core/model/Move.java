package tn.zeros.zchess.core.model;

public record Move(int fromSquare, int toSquare, Piece piece, Piece capturedPiece, boolean isPromotion, boolean isCastling, boolean isEnPassant, Piece promotionPiece) {

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
}
