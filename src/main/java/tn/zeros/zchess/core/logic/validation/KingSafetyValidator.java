package tn.zeros.zchess.core.logic.validation;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.MoveUndoInfo;
import tn.zeros.zchess.core.service.MoveExecutor;
import tn.zeros.zchess.core.service.ThreatDetectionService;

public class KingSafetyValidator implements MoveValidator {

    @Override
    public ValidationResult validate(BoardState state, Move move) {
        MoveUndoInfo undoInfo = MoveExecutor.makeMove(state, move);
        boolean isWhite = move.piece().isWhite();
        boolean isInCheck = ThreatDetectionService.isInCheck(state, isWhite);
        MoveExecutor.unmakeMove(state, undoInfo);
        return isInCheck ?
                new ValidationResult(false, "King in check") :
                ValidationResult.VALID;
    }
}