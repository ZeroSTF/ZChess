package tn.zeros.zchess.core.logic.generation;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.util.PrecomputedMoves;

public class KnightMoveGenerator {
    public static void generate(BoardState state, int from, MoveGenerator.MoveList moveList) {
        Piece knight = state.getPieceAt(from);

        if (knight == null || !knight.isKnight()) return;

        boolean isWhite = knight.isWhite();
        long friendlyPieces = state.getFriendlyPieces(isWhite);
        long possibleMoves = PrecomputedMoves.getKnightMoves(from, friendlyPieces);

        while (possibleMoves != 0) {
            int to = Long.numberOfTrailingZeros(possibleMoves);
            possibleMoves ^= 1L << to;

            Piece captured = state.getPieceAt(to).isWhite() != isWhite ?
                    state.getPieceAt(to) :
                    Piece.NONE;

            moveList.add(new Move(
                    from, to, knight, captured,
                    false, false, false, Piece.NONE
            ));
        }
    }
}
