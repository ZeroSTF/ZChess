package tn.zeros.zchess.core.logic.validation;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.util.ChessConstants;

public class RookValidator implements MoveValidator {
    @Override
    public ValidationResult validate(BoardState state, Move move) {
        int from = move.getFromSquare();
        int to = move.getToSquare();

        if (from/8 != to/8 && from%8 != to%8) {
            return new ValidationResult(false, "Rook must move straight");
        }

        int direction = getDirection(from, to);
        if (direction == -1 || isPathBlocked(state, from, to, direction)) {
            return new ValidationResult(false, "Path is blocked");
        }
        return ValidationResult.VALID;
    }

    private int getDirection(int from, int to) {
        if (from/8 == to/8) return to%8 > from%8 ? 2 : 6; // E/W
        return to/8 > from/8 ? 4 : 0; // S/N
    }

    private boolean isPathBlocked(BoardState state, int from, int to, int direction) {
        int step = ChessConstants.DIRECTION_OFFSETS[direction];
        int current = from + step;

        while (current != to) {
            if (state.getPieceAt(current) != Piece.NONE) return true;
            current += step;
        }
        return false;
    }
}