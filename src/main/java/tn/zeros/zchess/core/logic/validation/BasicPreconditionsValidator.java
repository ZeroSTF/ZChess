package tn.zeros.zchess.core.logic.validation;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;

public class BasicPreconditionsValidator implements MoveValidator {
    @Override
    public ValidationResult validate(BoardState state, Move move) {
        if (move.piece() == Piece.NONE) {
            return new ValidationResult(false, "No piece at source");
        }

        if (move.piece().isWhite() != state.isWhiteToMove()) {
            return new ValidationResult(false, "Wrong turn");
        }

        if (move.capturedPiece() != Piece.NONE &&
                move.capturedPiece().isWhite() == move.piece().isWhite()) {
            return new ValidationResult(false, "Cannot capture own piece");
        }

        return ValidationResult.VALID;
    }
}
