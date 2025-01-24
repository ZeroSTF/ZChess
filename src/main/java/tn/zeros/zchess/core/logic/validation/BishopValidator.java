package tn.zeros.zchess.core.logic.validation;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.util.ChessConstants;

public class BishopValidator implements MoveValidator {
    @Override
    public ValidationResult validate(BoardState state, Move move) {
        int from = move.getFromSquare();
        int to = move.getToSquare();
        int dr = (to/8 - from/8);
        int df = (to%8 - from%8);

        if (Math.abs(dr) != Math.abs(df)) {
            return new ValidationResult(false, "Bishop must move diagonally");
        }

        int direction = getDirection(from, to);
        if (direction == -1 || isPathBlocked(state, from, to, direction)) {
            return new ValidationResult(false, "Path is blocked");
        }
        return ValidationResult.VALID;
    }

    private int getDirection(int from, int to) {
        int dx = Integer.compare(to%8, from%8);
        int dy = Integer.compare(to/8, from/8);
        return switch (dx + dy * 3) {
            case 4 -> 1;  // NE
            case 2 -> 3;  // SE
            case -2 -> 5; // SW
            case -4 -> 7; // NW
            default -> -1;
        };
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