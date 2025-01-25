package tn.zeros.zchess.core.logic.validation;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.util.ChessConstants;
import tn.zeros.zchess.core.util.PrecomputedMoves;

public abstract class SlidingPieceValidator implements MoveValidator {

    @Override
    public ValidationResult validate(BoardState state, Move move) {
        int from = move.fromSquare();
        int to = move.toSquare();

        if (!isValidSquare(to)) return new ValidationResult(false, "Invalid destination");

        for (int dir : getDirections()) {
            if (isPathValid(from, to, dir) &&
                    !isPathBlocked(state, from, to, dir)) {
                return ValidationResult.VALID;
            }
        }
        return new ValidationResult(false, getErrorMessage());
    }

    private boolean isValidSquare(int square) {
        return square >= 0 && square < 64;
    }

    private boolean isPathValid(int from, int to, int direction) {
        long ray = PrecomputedMoves.RAY_MOVES[from][direction];
        return (ray & (1L << to)) != 0;
    }

    private boolean isPathBlocked(BoardState state, int from, int to, int direction) {
        int step = ChessConstants.DIRECTION_OFFSETS[direction];
        int current = from + step;

        while (current != to) {
            if (!isValidSquare(current)) return true;
            if (state.getPieceAt(current) != Piece.NONE) return true;

            // Prevent file wrapping
            int prevFile = (current - step) % 8;
            int currFile = current % 8;
            if (Math.abs(currFile - prevFile) > 1) return true;

            current += step;
        }
        return !isValidSquare(to);
    }

    protected abstract int[] getDirections();

    protected abstract String getErrorMessage();
}