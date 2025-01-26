package tn.zeros.zchess.core.logic.Generation;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.util.PrecomputedMoves;

import java.util.ArrayList;
import java.util.List;

public class KnightMoveGenerator {
    public static List<Move> generate(BoardState state, int from) {
        List<Move> moves = new ArrayList<>();
        Piece knight = state.getPieceAt(from);

        if (knight == null || !knight.isKnight()) {
            return moves;
        }

        boolean isWhite = knight.isWhite();
        long friendlyPieces = state.getFriendlyPieces(isWhite);
        long possibleMoves = PrecomputedMoves.getKnightMoves(from, friendlyPieces);

        while (possibleMoves != 0) {
            int to = Long.numberOfTrailingZeros(possibleMoves);
            possibleMoves ^= 1L << to;

            Piece captured = state.getPieceAt(to).isWhite() != isWhite ?
                    state.getPieceAt(to) :
                    Piece.NONE;

            moves.add(new Move(
                    from, to, knight, captured,
                    false, false, false, Piece.NONE
            ));
        }

        return moves;
    }
}
