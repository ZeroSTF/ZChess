package tn.zeros.zchess.core.logic.validation;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;

public class BasicPreconditionsValidator implements MoveValidator {
    @Override
    public ValidationResult validate(BoardState state, Move move) {
        if (move.getPiece() == Piece.NONE) {
            return new ValidationResult(false, "No piece at source");
        }

        if (move.getPiece().isWhite() != state.isWhiteToMove()) {
            return new ValidationResult(false, "Wrong turn");
        }

        if (move.getCapturedPiece() != Piece.NONE &&
                move.getCapturedPiece().isWhite() == move.getPiece().isWhite()) {
            return new ValidationResult(false, "Cannot capture own piece");
        }

        return ValidationResult.VALID;
    }
}
