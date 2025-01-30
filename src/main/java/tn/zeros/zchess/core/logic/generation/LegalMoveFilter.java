package tn.zeros.zchess.core.logic.generation;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.util.PrecomputedMoves;

public class LegalMoveFilter {
    static boolean isSquareAttacked(BoardState state, int square, boolean byWhite) {
        return getAttackersBitboard(state, square, byWhite) != 0;
    }

    public static boolean inCheck(BoardState state, boolean whiteKing) {
        int kingSquare = state.getKingSquare(whiteKing);
        return isSquareAttacked(state, kingSquare, !whiteKing);
    }

    public static long getAttackersBitboard(BoardState state, int square, boolean byWhite) {
        int color = byWhite ? Piece.WHITE : Piece.BLACK;
        long allPieces = state.getAllPieces();
        long attackers = 0;

        // Pawn attacks
        attackers |= state.getPieces(Piece.PAWN, color) &
                PrecomputedMoves.getPawnAttacks(square, !byWhite);

        // Knight attacks
        attackers |= state.getPieces(Piece.KNIGHT, color) &
                PrecomputedMoves.getKnightMoves(square, 0L);

        // Bishop/Queen attacks
        long bishopAttacks = PrecomputedMoves.getMagicBishopAttack(square, allPieces);
        attackers |= (state.getPieces(Piece.BISHOP, color) |
                state.getPieces(Piece.QUEEN, color)) & bishopAttacks;

        // Rook/Queen attacks
        long rookAttacks = PrecomputedMoves.getMagicRookAttack(square, allPieces);
        attackers |= (state.getPieces(Piece.ROOK, color) |
                state.getPieces(Piece.QUEEN, color)) & rookAttacks;

        // King attacks
        attackers |= state.getPieces(Piece.KING, color) &
                PrecomputedMoves.getKingMoves(square, 0L);

        return attackers;
    }


    // For controller
    public static int getKingInCheckSquare(BoardState state, boolean whiteKing) {
        int kingSquare = state.getKingSquare(whiteKing);
        return isSquareAttacked(state, kingSquare, !whiteKing) ?
                kingSquare :
                -1;
    }
}
