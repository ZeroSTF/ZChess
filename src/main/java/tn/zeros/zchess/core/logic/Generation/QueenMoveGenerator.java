package tn.zeros.zchess.core.logic.Generation;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.util.PrecomputedMoves;

import java.util.ArrayList;
import java.util.List;

public class QueenMoveGenerator {
    protected static List<Move> generate(BoardState state, int from) {
        List<Move> moves = new ArrayList<>();
        Piece queen = state.getPieceAt(from);

        if (queen == null || !queen.isQueen()) {
            return moves;
        }

        boolean isWhite = queen.isWhite();
        long allPieces = state.getAllPieces();
        long friendlyPieces = state.getFriendlyPieces(isWhite);

        // Combine bishop and rook attacks
        long possibleMoves = PrecomputedMoves.getMagicBishopAttack(from, allPieces)
                | PrecomputedMoves.getMagicRookAttack(from, allPieces);

        possibleMoves &= ~friendlyPieces; // Filter out friendly blocks

        while (possibleMoves != 0) {
            int to = Long.numberOfTrailingZeros(possibleMoves);
            possibleMoves ^= 1L << to;

            Piece target = state.getPieceAt(to);
            Piece captured = target.isWhite() != isWhite ? target : Piece.NONE;

            moves.add(new Move(
                    from, to, queen, captured,
                    false, false, false, Piece.NONE
            ));
        }

        return moves;
    }
}
