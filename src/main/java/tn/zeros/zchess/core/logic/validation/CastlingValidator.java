package tn.zeros.zchess.core.logic.validation;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.service.CastlingService;
import tn.zeros.zchess.core.service.ThreatDetectionService;

public class CastlingValidator implements MoveValidator {
    @Override
    public ValidationResult validate(BoardState state, Move move) {
        if (!move.isCastling()) {
            return ValidationResult.valid();
        }

        Piece king = move.piece();
        int from = move.fromSquare();
        int to = move.toSquare();

        // 1. Verify castling rights
        if (!CastlingService.canCastle(state, from, to)) {
            return new ValidationResult(false, "Castling rights no longer available");
        }

        // 2. Check path between king and rook
        int direction = (to > from) ? 1 : -1;
        int current = from;
        int rookFrom = (direction == 1) ? from + 3 : from - 4;

        // Verify rook exists
        Piece expectedRook = king.isWhite() ? Piece.WHITE_ROOK : Piece.BLACK_ROOK;
        if (state.getPieceAt(rookFrom) != expectedRook) {
            return new ValidationResult(false, "Rook missing from castling position");
        }

        // 3. Check king is not in check
        if (ThreatDetectionService.isSquareAttacked(state, from, !king.isWhite())) {
            return new ValidationResult(false, "King would be in check");
        }

        // 4. Check squares between are empty
        current += direction;
        while (current != rookFrom) {
            if (state.getPieceAt(current) != Piece.NONE) {
                return new ValidationResult(false, "Pieces between king and rook");
            }
            current += direction;
        }

        // 5. Check king doesn't move through check
        current = from;
        for (int i = 0; i < 2; i++) {
            current += direction;
            if (ThreatDetectionService.isSquareAttacked(state, current, !king.isWhite())) {
                return new ValidationResult(false, "King would move through check");
            }
        }

        return ValidationResult.valid();
    }
}
