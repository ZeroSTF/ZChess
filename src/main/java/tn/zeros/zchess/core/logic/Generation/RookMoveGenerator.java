package tn.zeros.zchess.core.logic.Generation;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.util.PrecomputedMoves;

import java.util.ArrayList;
import java.util.List;

public class RookMoveGenerator {
    public static List<Move> generate(BoardState state, int from) {
        List<Move> moves = new ArrayList<>();
        Piece rook = state.getPieceAt(from);

        if (rook == null || !rook.isRook()) {
            return moves;
        }

        boolean isWhite = rook.isWhite();
        long allPieces = state.getAllPieces();
        long friendlyPieces = state.getFriendlyPieces(isWhite);

        long possibleMoves = PrecomputedMoves.getMagicRookAttack(from, allPieces);
        possibleMoves &= ~friendlyPieces; // Filter out friendly blocks

        while (possibleMoves != 0) {
            int to = Long.numberOfTrailingZeros(possibleMoves);
            possibleMoves ^= 1L << to;

            Piece target = state.getPieceAt(to);
            Piece captured = target.isWhite() != isWhite ? target : Piece.NONE;

            moves.add(new Move(
                    from, to, rook, captured,
                    false, false, false, Piece.NONE
            ));
        }

        return moves;
    }
}
