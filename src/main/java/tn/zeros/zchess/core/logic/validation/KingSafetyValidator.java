package tn.zeros.zchess.core.logic.validation;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.service.BoardStateCloner;
import tn.zeros.zchess.core.service.MoveExecutor;
import tn.zeros.zchess.core.service.ThreatDetectionService;

public class KingSafetyValidator implements MoveValidator {

    @Override
    public ValidationResult validate(BoardState state, Move move) {
        BoardState testState = BoardStateCloner.clone(state);
        MoveExecutor.executeMove(testState, move);

        boolean isWhite = move.piece().isWhite();
        return ThreatDetectionService.isInCheck(testState, isWhite) ?
                new ValidationResult(false, "King in check") :
                ValidationResult.VALID;
    }
}