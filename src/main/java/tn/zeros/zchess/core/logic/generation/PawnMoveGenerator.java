package tn.zeros.zchess.core.logic.generation;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.util.PrecomputedMoves;

public class PawnMoveGenerator {
    public static void generate(BoardState state, int from, MoveGenerator.MoveList moveList) {
        int pawn = state.getPieceAt(from);
        if (pawn == Piece.NONE || !Piece.isPawn(pawn)) return;

        boolean isWhite = Piece.isWhite(pawn);
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

            int captured = getCapturedPiece(state, from, to, enPassantSquare, isWhite);
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

    private static int getCapturedPiece(BoardState state, int from, int to, int enPassantSquare, boolean isWhite) {
        if (to == enPassantSquare) {
            int capturedSquare = enPassantSquare + (isWhite ? -8 : 8);
            return state.getPieceAt(capturedSquare);
        }
        return state.getPieceAt(to);
    }

    private static boolean isPromotionRank(int square, boolean isWhite) {
        return (isWhite && square >= 56) || (!isWhite && square <= 7);
    }

    private static void addPromotionMoves(MoveGenerator.MoveList moveList, int from, int to, int pawn, int captured) {
        int[] promotions = {
                Piece.isWhite(pawn) ? Piece.makePiece(4, 0) : Piece.makePiece(4, 1),
                Piece.isWhite(pawn) ? Piece.makePiece(3, 0) : Piece.makePiece(3, 1),
                Piece.isWhite(pawn) ? Piece.makePiece(2, 0) : Piece.makePiece(2, 1),
                Piece.isWhite(pawn) ? Piece.makePiece(1, 0) : Piece.makePiece(1, 1)
        };

        for (int promotion : promotions) {
            moveList.add(new Move(
                    from, to, pawn, captured,
                    true, false, false, promotion
            ));
        }
    }
}
