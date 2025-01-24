package tn.zeros.zchess.core.logic.validation;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;

import java.util.EnumMap;
import java.util.Map;

public class PieceSpecificValidator implements MoveValidator{
    private final Map<Piece, MoveValidator> validators = new EnumMap<>(Piece.class);

    public PieceSpecificValidator() {
        validators.put(Piece.WHITE_PAWN, new PawnValidator());
        validators.put(Piece.BLACK_PAWN, new PawnValidator());
        validators.put(Piece.WHITE_KNIGHT, new KnightValidator());
        validators.put(Piece.BLACK_KNIGHT, new KnightValidator());
        validators.put(Piece.WHITE_BISHOP, new BishopValidator());
        validators.put(Piece.BLACK_BISHOP, new BishopValidator());
        validators.put(Piece.WHITE_ROOK, new RookValidator());
        validators.put(Piece.BLACK_ROOK, new RookValidator());
        validators.put(Piece.WHITE_QUEEN, new QueenValidator());
        validators.put(Piece.BLACK_QUEEN, new QueenValidator());
        validators.put(Piece.WHITE_KING, new KingValidator());
        validators.put(Piece.BLACK_KING, new KingValidator());
    }

    @Override
    public ValidationResult validate(BoardState state, Move move) {
        MoveValidator validator = validators.get(move.getPiece());
        if (validator == null) {
            return new ValidationResult(false, "Invalid piece type");
        }
        return validator.validate(state, move);
    }
}
