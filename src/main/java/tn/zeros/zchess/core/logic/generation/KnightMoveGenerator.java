package tn.zeros.zchess.core.logic.generation;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.util.PrecomputedMoves;

public class KnightMoveGenerator {
    public static void generate(BoardState state, int from, MoveGenerator.MoveList moveList, long pinned, long checkingRay) {
        // Pinned knights can't move
        if ((1L << from & pinned) != 0) {
            return;
        }

        long possibleMoves = PrecomputedMoves.getKnightMoves(from, state.getFriendlyPieces(state.isWhiteToMove()));

        // Filter moves by checking ray if in check
        if (checkingRay != -1L) {
            possibleMoves &= checkingRay;
        }

        while (possibleMoves != 0) {
            int to = Long.numberOfTrailingZeros(possibleMoves);
            int captured = state.getPieceAt(to);
            moveList.add(Move.createMove(from, to, state.getPieceAt(from), captured, 0, Piece.NONE));
            possibleMoves &= possibleMoves - 1;
        }
    }
}
