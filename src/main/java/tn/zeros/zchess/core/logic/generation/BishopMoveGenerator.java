package tn.zeros.zchess.core.logic.generation;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.util.PrecomputedMoves;

import java.util.ArrayList;
import java.util.List;

public class BishopMoveGenerator {
    public static List<Move> generate(BoardState state, int from) {
        List<Move> moves = new ArrayList<>();
        Piece bishop = state.getPieceAt(from);

        if (bishop == null || !bishop.isBishop()) {
            return moves;
        }

        boolean isWhite = bishop.isWhite();
        long allPieces = state.getAllPieces();
        long friendlyPieces = state.getFriendlyPieces(isWhite);

        // Get magic bitboard attacks
        long possibleMoves = PrecomputedMoves.getMagicBishopAttack(from, allPieces);
        possibleMoves &= ~friendlyPieces; // Remove friendly blocking pieces

        // Convert bitmask to moves
        while (possibleMoves != 0) {
            int to = Long.numberOfTrailingZeros(possibleMoves);
            possibleMoves ^= 1L << to;

            Piece captured = state.getPieceAt(to).isWhite() != isWhite ?
                    state.getPieceAt(to) :
                    Piece.NONE;

            moves.add(new Move(
                    from, to, bishop, captured,
                    false, false, false, Piece.NONE
            ));
        }

        return moves;
    }
}
