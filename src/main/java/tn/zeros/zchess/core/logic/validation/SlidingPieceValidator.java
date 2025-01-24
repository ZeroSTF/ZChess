package tn.zeros.zchess.core.logic.validation;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.util.ChessConstants;

public abstract class SlidingPieceValidator implements MoveValidator {

    @Override
    public ValidationResult validate(BoardState state, Move move) {
        int from = move.fromSquare();
        int to = move.toSquare();

        if (!isValidSquare(to)) return new ValidationResult(false, "Invalid destination");

        for (int dir : getDirections()) {
            if (isValidPath(from, to, dir) &&
                    !isPathBlocked(state, from, to, dir)) {
                return ValidationResult.VALID;
            }
        }
        return new ValidationResult(false, getErrorMessage());
    }

    private boolean isValidSquare(int square) {
        return square >= 0 && square < 64;
    }

    private boolean isValidPath(int from, int to, int direction) {
        int step = ChessConstants.DIRECTION_OFFSETS[direction];
        if (step == 0) return false;

        int delta = to - from;
        if (delta % step != 0) return false;

        return isWithinBoardBounds(from, to, direction);
    }

    private boolean isWithinBoardBounds(int from, int to, int direction) {
        int fromRank = from / 8;
        int fromFile = from % 8;
        int toRank = to / 8;
        int toFile = to % 8;

        return switch (direction) {
            case 1 -> (toFile > fromFile) && (toRank < fromRank); // NE
            case 3 -> (toFile > fromFile) && (toRank > fromRank); // SE
            case 5 -> (toFile < fromFile) && (toRank > fromRank); // SW
            case 7 -> (toFile < fromFile) && (toRank < fromRank); // NW
            case 0 -> (fromFile == toFile) && (toRank < fromRank); // N
            case 4 -> (fromFile == toFile) && (toRank > fromRank); // S
            case 2 -> (fromRank == toRank) && (toFile > fromFile); // E
            case 6 -> (fromRank == toRank) && (toFile < fromFile); // W
            default -> false;
        };
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