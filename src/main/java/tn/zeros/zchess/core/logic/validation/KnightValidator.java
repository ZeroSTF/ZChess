package tn.zeros.zchess.core.logic.validation;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.util.PrecomputedMoves;

public class KnightValidator implements MoveValidator {
    @Override
    public ValidationResult validate(BoardState state, Move move) {
        int from = move.getFromSquare();
        int to = move.getToSquare();

        if ((PrecomputedMoves.KNIGHT_MOVES[from] & (1L << to)) == 0) {
            return new ValidationResult(false, "Invalid knight move");
        }
        return ValidationResult.VALID;
    }
}