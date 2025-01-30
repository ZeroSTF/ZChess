package tn.zeros.zchess.core.logic.generation;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.util.PrecomputedMoves;

public class QueenMoveGenerator {
    public static void generate(BoardState state, int from, MoveGenerator.MoveList moveList, long pinned, long checkingRay) {
        int queen = state.getPieceAt(from);
        boolean isWhite = Piece.isWhite(queen);
        long fromBit = 1L << from;

        // Get all possible queen moves (bishop + rook moves)
        long moves = PrecomputedMoves.getMagicBishopAttack(from, state.getAllPieces()) |
                PrecomputedMoves.getMagicRookAttack(from, state.getAllPieces());
        moves &= ~state.getFriendlyPieces(isWhite);

        // If pinned, restrict to pin ray
        if ((fromBit & pinned) != 0) {
            long pinRay = MoveGenerator.calculatePinRay(from, state.getKingSquare(isWhite), state);
            moves &= pinRay;
        }

        // If in check, only moves that block or capture checker
        if (checkingRay != -1L) {
            moves &= checkingRay;
        }

        while (moves != 0) {
            int to = Long.numberOfTrailingZeros(moves);
            int captured = state.getPieceAt(to);
            moveList.add(Move.createMove(from, to, queen, captured, 0, Piece.NONE));
            moves &= moves - 1;
        }
    }
}
