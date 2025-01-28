package tn.zeros.zchess.core.model;

public final class Move {

    // Masks for extracting different parts of the move value
    public static final int FROM_MASK = 0x3F;  // 6 bits for from square
    public static final int TO_MASK = 0x3F << 6;  // 6 bits for to square
    public static final int PIECE_MASK = 0xF << 12;  // 4 bits for piece type
    public static final int CAPTURED_MASK = 0xF << 16;  // 4 bits for captured piece
    public static final int FLAGS_MASK = 0x7 << 20;  // 3 bits for flags
    public static final int PROMOTION_MASK = 0x1FF << 23;  // 9 bits for promotion piece type

    // Flag values
    public static final int FLAG_CASTLING = 1 << 20;
    public static final int FLAG_ENPASSANT = 1 << 21;
    public static final int FLAG_PROMOTION = 1 << 22;

    // Promotion piece types (can be adjusted as needed)
    public static final int PROMOTE_TO_QUEEN = 0b000000001;
    public static final int PROMOTE_TO_ROOK = 0b000000010;
    public static final int PROMOTE_TO_KNIGHT = 0b000000011;
    public static final int PROMOTE_TO_BISHOP = 0b000000100;

    // Constructor for creating a move
    public static int createMove(int fromSquare, int toSquare, int piece, int capturedPiece, int flags, int promotionPiece) {
        // Pack the move information into a single integer
        return (fromSquare & FROM_MASK)
                | ((toSquare << 6) & TO_MASK)
                | ((piece << 12) & PIECE_MASK)
                | ((capturedPiece << 16) & CAPTURED_MASK)
                | (flags & FLAGS_MASK)
                | ((promotionPiece << 23) & PROMOTION_MASK);
    }

    // Static method to compare moves
    public static boolean sameMove(int a, int b) {
        return a == b;
    }

    // Getters
    public static int getFrom(int move) {
        return move & FROM_MASK;
    }

    public static int getTo(int move) {
        return (move & TO_MASK) >> 6;
    }

    public static int getPiece(int move) {
        return (move & PIECE_MASK) >> 12;
    }

    public static int getCapturedPiece(int move) {
        return (move & CAPTURED_MASK) >> 16;
    }

    public static int getFlags(int move) {
        return (move & FLAGS_MASK) >> 20;
    }

    public static int getPromotionPiece(int move) {
        return (move & PROMOTION_MASK) >> 23;
    }

    public static boolean isPromotion(int move) {
        return (move & FLAG_PROMOTION) != 0;
    }

    public static boolean isCastling(int move) {
        return (move & FLAG_CASTLING) != 0;
    }

    public static boolean isEnPassant(int move) {
        return (move & FLAG_ENPASSANT) != 0;
    }

    public static int getMoveValue(int move) {
        return move;
    }
}
