package tn.zeros.zchess.core.logic.generation;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.util.PrecomputedMoves;

public class KnightMoveGenerator {
    public static void generate(BoardState state, int from, MoveGenerator.MoveList moveList) {
        int knight = state.getPieceAt(from);

        if (knight == Piece.NONE || !Piece.isKnight(knight)) return;

        boolean isWhite = Piece.isWhite(knight);
        long friendlyPieces = state.getFriendlyPieces(isWhite);
        long possibleMoves = PrecomputedMoves.getKnightMoves(from, friendlyPieces);

        while (possibleMoves != 0) {
            int to = Long.numberOfTrailingZeros(possibleMoves);
            possibleMoves ^= 1L << to;

            int captured = Piece.isWhite(state.getPieceAt(to)) != isWhite ?
                    state.getPieceAt(to) :
                    Piece.NONE;

            moveList.add(new Move(
                    from, to, knight, captured,
                    false, false, false, Piece.NONE
            ));
        }
    }
}
