package tn.zeros.zchess.core.logic.validation;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.util.PrecomputedMoves;

public class PawnValidator implements MoveValidator {
    @Override
    public ValidationResult validate(BoardState state, Move move) {
        Piece pawn = move.piece();
        int from = move.fromSquare();
        int to = move.toSquare();

        if (!pawn.isPawn()) {
            return ValidationResult.valid();
        }

        long enemyPieces = state.getEnemyPieces(pawn.isWhite());
        if (to == state.getEnPassantSquare()) enemyPieces |= 1L << state.getEnPassantSquare();
        long precomputed = PrecomputedMoves.getPawnMoves(from, state.getAllPieces(), enemyPieces, pawn.isWhite());
        if ((precomputed & (1L << to)) != 0) {
            return ValidationResult.valid();
        }

        return new ValidationResult(false, "Invalid pawn move");
    }
}
