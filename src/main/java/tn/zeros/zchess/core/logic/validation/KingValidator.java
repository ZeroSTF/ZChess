package tn.zeros.zchess.core.logic.validation;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.util.PrecomputedMoves;

public class KingValidator implements MoveValidator {
    @Override
    public ValidationResult validate(BoardState state, Move move) {
        if (move.isCastling()) return ValidationResult.VALID;

        int from = move.getFromSquare();
        int to = move.getToSquare();

        if ((PrecomputedMoves.KING_MOVES[from] & (1L << to)) != 0) {
            return ValidationResult.VALID;
        }
        return new ValidationResult(false, "Invalid king move");
    }
}