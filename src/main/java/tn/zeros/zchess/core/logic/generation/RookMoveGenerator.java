package tn.zeros.zchess.core.logic.generation;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.util.PrecomputedMoves;

public class RookMoveGenerator {
    public static void generate(BoardState state, int from, MoveGenerator.MoveList moveList) {
        int rook = state.getPieceAt(from);

        if (rook == Piece.NONE || !Piece.isRook(rook)) return;

        boolean isWhite = Piece.isWhite(rook);
        long allPieces = state.getAllPieces();
        long friendlyPieces = state.getFriendlyPieces(isWhite);

        long possibleMoves = PrecomputedMoves.getMagicRookAttack(from, allPieces);
        possibleMoves &= ~friendlyPieces; // Filter out friendly blocks

        while (possibleMoves != 0) {
            int to = Long.numberOfTrailingZeros(possibleMoves);
            possibleMoves ^= 1L << to;

            int target = state.getPieceAt(to);
            int captured = Piece.isWhite(target) != isWhite ? target : Piece.NONE;

            moveList.add(new Move(
                    from, to, rook, captured,
                    false, false, false, Piece.NONE
            ));
        }
    }
}
