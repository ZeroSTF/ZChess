package tn.zeros.zchess.core.logic.validation;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;

public interface MoveValidator {
     ValidationResult validate(BoardState state, Move move);
}
