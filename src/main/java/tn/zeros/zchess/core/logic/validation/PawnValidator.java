package tn.zeros.zchess.core.logic.validation;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;

public class PawnValidator implements MoveValidator {
    @Override
    public ValidationResult validate(BoardState state, Move move) {
        Piece pawn = move.piece();
        int from = move.fromSquare();
        int to = move.toSquare();

        if (!pawn.isPawn()) {
            return ValidationResult.valid();
        }

        int fromRank = from / 8;
        int fromFile = from % 8;
        int toRank = to / 8;
        int toFile = to % 8;
        int direction = pawn.isWhite() ? 1 : -1;

        // Basic forward move validation
        if (fromFile == toFile) {
            if (toRank == fromRank + direction) {
                if (state.getPieceAt(to) != Piece.NONE) {
                    return new ValidationResult(false, "Pawn cannot move forward to occupied square");
                }
                return ValidationResult.valid();
            }

            // Two-square move
            if (toRank == fromRank + 2 * direction) {
                if ((pawn.isWhite() && fromRank != 1) || (!pawn.isWhite() && fromRank != 6)) {
                    return new ValidationResult(false, "Pawn can only move two squares from initial position");
                }

                int intermediate = from + 8 * direction;
                if (state.getPieceAt(intermediate) != Piece.NONE ||
                        state.getPieceAt(to) != Piece.NONE) {
                    return new ValidationResult(false, "Pawn path is blocked");
                }
                return ValidationResult.valid();
            }
        }

        // Capture validation
        if (Math.abs(fromFile - toFile) == 1 && toRank == fromRank + direction) {
            if (state.getPieceAt(to) != Piece.NONE) {
                return ValidationResult.valid();
            }

            // En passant validation
            if (to == state.getEnPassantSquare()) {
                int capturedPawnSquare = to + (pawn.isWhite() ? -8 : 8);
                if (state.getPieceAt(capturedPawnSquare).isPawn()) {
                    return ValidationResult.valid();
                }
            }
            return new ValidationResult(false, "Invalid pawn capture");
        }

        return new ValidationResult(false, "Invalid pawn move");
    }
}
