package tn.zeros.zchess.core.logic.validation;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;

public class QueenValidator implements MoveValidator {
    private final BishopValidator bishopValidator = new BishopValidator();
    private final RookValidator rookValidator = new RookValidator();

    @Override
    public ValidationResult validate(BoardState state, Move move) {
        ValidationResult bishopResult = bishopValidator.validate(state, move);
        ValidationResult rookResult = rookValidator.validate(state, move);

        if (!bishopResult.isValid() && !rookResult.isValid()) {
            return new ValidationResult(false, "Queen must move straight or diagonally");
        }
        return ValidationResult.VALID;
    }
}