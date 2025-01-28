package tn.zeros.zchess.core.logic.generation;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.util.PrecomputedMoves;

public class QueenMoveGenerator {
    public static void generate(BoardState state, int from, MoveGenerator.MoveList moveList) {
        int queen = state.getPieceAt(from);

        if (queen == Piece.NONE || !Piece.isQueen(queen)) return;

        boolean isWhite = Piece.isWhite(queen);
        long allPieces = state.getAllPieces();
        long friendlyPieces = state.getFriendlyPieces(isWhite);

        // Combine bishop and rook attacks
        long possibleMoves = PrecomputedMoves.getMagicBishopAttack(from, allPieces) | PrecomputedMoves.getMagicRookAttack(from, allPieces);

        possibleMoves &= ~friendlyPieces; // Filter out friendly blocks

        while (possibleMoves != 0) {
            int to = Long.numberOfTrailingZeros(possibleMoves);
            possibleMoves ^= 1L << to;

            int target = state.getPieceAt(to);
            int captured = Piece.isWhite(target) != isWhite ? target : Piece.NONE;

            moveList.add(new Move(
                    from, to, queen, captured,
                    false, false, false, Piece.NONE
            ));
        }
    }
}
