package tn.zeros.zchess.core.logic.validation;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.util.PrecomputedMoves;

public class BishopValidator implements MoveValidator {
    @Override
    public ValidationResult validate(BoardState state, Move move) {
        int from = move.fromSquare();
        int to = move.toSquare();
        long precomputed = PrecomputedMoves.getMagicBishopAttack(from, state.getAllPieces());

        if ((precomputed & (1L << to)) != 0) return ValidationResult.VALID;
        return new ValidationResult(false, "Invalid rook move");
    }
}