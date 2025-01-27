package tn.zeros.zchess.core.logic.generation;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.util.PrecomputedMoves;

public class PawnMoveGenerator {
    public static void generate(BoardState state, int from, MoveGenerator.MoveList moveList) {
        Piece pawn = state.getPieceAt(from);
        if (pawn == null || !pawn.isPawn()) return;

        boolean isWhite = pawn.isWhite();
        long allPieces = state.getAllPieces();
        long enemyPieces = state.getEnemyPieces(isWhite);
        int enPassantSquare = state.getEnPassantSquare();

        // Include en passant square in enemy pieces if valid
        if (enPassantSquare != -1) {
            enemyPieces |= 1L << enPassantSquare;
        }
        long possibleMoves = PrecomputedMoves.getPawnMoves(from, allPieces, enemyPieces, isWhite);

        // Convert bitmask to actual moves
        while (possibleMoves != 0) {
            int to = Long.numberOfTrailingZeros(possibleMoves);
            possibleMoves ^= 1L << to;

            Piece captured = getCapturedPiece(state, from, to, enPassantSquare, isWhite);
            boolean isEnPassant = (to == enPassantSquare);
            boolean isPromotion = isPromotionRank(to, isWhite);

            if (isPromotion) {
                addPromotionMoves(moveList, from, to, pawn, captured);
            } else {
                moveList.add(new Move(
                        from, to, pawn, captured,
                        false, false, isEnPassant, Piece.NONE
                ));
            }
        }
    }

    private static Piece getCapturedPiece(BoardState state, int from, int to, int enPassantSquare, boolean isWhite) {
        if (to == enPassantSquare) {
            int capturedSquare = enPassantSquare + (isWhite ? -8 : 8);
            return state.getPieceAt(capturedSquare);
        }
        return state.getPieceAt(to);
    }

    private static boolean isPromotionRank(int square, boolean isWhite) {
        return (isWhite && square >= 56) || (!isWhite && square <= 7);
    }

    private static void addPromotionMoves(MoveGenerator.MoveList moveList, int from, int to, Piece pawn, Piece captured) {
        Piece[] promotions = {
                pawn.isWhite() ? Piece.WHITE_QUEEN : Piece.BLACK_QUEEN,
                pawn.isWhite() ? Piece.WHITE_ROOK : Piece.BLACK_ROOK,
                pawn.isWhite() ? Piece.WHITE_BISHOP : Piece.BLACK_BISHOP,
                pawn.isWhite() ? Piece.WHITE_KNIGHT : Piece.BLACK_KNIGHT
        };

        for (Piece promotion : promotions) {
            moveList.add(new Move(
                    from, to, pawn, captured,
                    true, false, false, promotion
            ));
        }
    }
}
