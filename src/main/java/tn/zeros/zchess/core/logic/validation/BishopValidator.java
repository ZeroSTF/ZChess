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
        int fromFile = from % 8;
        int fromRank = from / 8;
        int toFile = to % 8;
        int toRank = to / 8;

        int dx = Integer.compare(toFile, fromFile);
        int dy = Integer.compare(toRank, fromRank);

        // Ensure diagonal movement
        if (Math.abs(toFile - fromFile) != Math.abs(toRank - fromRank)) {
            return -1;
        }

        if (dx > 0) {
            return dy > 0 ? 3 : 1; // SE (3) or NE (1)
        } else {
            return dy > 0 ? 5 : 7; // SW (5) or NW (7)
        }
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