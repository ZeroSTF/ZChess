package tn.zeros.zchess.core.logic.validation;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;

import java.util.Arrays;
import java.util.List;

public class CompositeMoveValidator implements MoveValidator {
    private final List<MoveValidator> validators = Arrays.asList(
            new BasicPreconditionsValidator(),
            new PieceSpecificValidator(),
            new CastlingValidator(),
            new KingSafetyValidator()
    );

    @Override
    public ValidationResult validate(BoardState state, Move move) {
        for (MoveValidator validator : validators) {
            ValidationResult result = validator.validate(state, move);
            if (!result.isValid()) return result;
        }
        return ValidationResult.VALID;
    }
}
