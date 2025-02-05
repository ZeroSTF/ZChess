package tn.zeros.zchess.core.model;

import tn.zeros.zchess.core.service.FenService;

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

    // Null move
    public static final int NULL_MOVE = -1;

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

    public static int createCastling(int fromSquare, int toSquare, int piece) {
        return createMove(fromSquare, toSquare, piece, Piece.NONE, FLAG_CASTLING, Piece.NONE);
    }

    public static int getRookFrom(int move) {
        if (!isCastling(move)) return -1;
        int kingFrom = getFrom(move);
        int kingTo = getTo(move);

        // Determine if it's kingside or queenside castling based on king's movement
        if (kingTo > kingFrom) {  // Kingside castling
            return kingFrom + 3;  // Rook starts 3 squares right of king
        } else {  // Queenside castling
            return kingFrom - 4;  // Rook starts 4 squares left of king
        }
    }

    public static int getRookTo(int move) {
        if (!isCastling(move)) return -1;
        int kingFrom = getFrom(move);
        int kingTo = getTo(move);

        // Determine rook's destination based on king's movement
        if (kingTo > kingFrom) {  // Kingside castling
            return kingTo - 1;    // Rook ends up 1 square left of king's final position
        } else {  // Queenside castling
            return kingTo + 1;    // Rook ends up 1 square right of king's final position
        }
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

    public static int updatePromotionPiece(int move, int promotionPiece) {
        move &= ~PROMOTION_MASK;
        move |= (promotionPiece << 23) & PROMOTION_MASK;
        return move;
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

    public static String toAlgebraic(int move) {
        int from = getFrom(move);
        int to = getTo(move);
        int piece = getPiece(move);
        int captured = getCapturedPiece(move);
        int promotion = getPromotionPiece(move);

        String fromSquare = FenService.squareToAlgebraic(from);
        String toSquare = FenService.squareToAlgebraic(to);

        // Basic algebraic notation
        if (Piece.isPawn(piece)) {
            if (captured != Piece.NONE) {
                return fromSquare.charAt(0) + "x" + toSquare;
            }
            return toSquare + (promotion != Piece.NONE ? "=" + Piece.getSymbol(promotion) : "");
        }

        return Piece.getSymbol(piece) +
                (captured != Piece.NONE ? "x" : "") +
                toSquare;
    }
}
