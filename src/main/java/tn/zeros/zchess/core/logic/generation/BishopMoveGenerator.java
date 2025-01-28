package tn.zeros.zchess.core.logic.generation;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.util.PrecomputedMoves;

public class BishopMoveGenerator {
    public static void generate(BoardState state, int from, MoveGenerator.MoveList moveList) {
        int bishop = state.getPieceAt(from);

        if (bishop == Piece.NONE || !Piece.isBishop(bishop)) {
            return;
        }

        boolean isWhite = Piece.isWhite(bishop);
        long allPieces = state.getAllPieces();
        long friendlyPieces = state.getFriendlyPieces(isWhite);

        // Get magic bitboard attacks
        long possibleMoves = PrecomputedMoves.getMagicBishopAttack(from, allPieces);
        possibleMoves &= ~friendlyPieces; // Remove friendly blocking pieces

        // Convert bitmask to moves
        while (possibleMoves != 0) {
            int to = Long.numberOfTrailingZeros(possibleMoves);
            possibleMoves ^= 1L << to;

            int captured = Piece.isWhite(state.getPieceAt(to)) != isWhite ?
                    state.getPieceAt(to) :
                    Piece.NONE;

            moveList.add(new Move(
                    from, to, bishop, captured,
                    false, false, false, Piece.NONE
            ));
        }
    }
}
