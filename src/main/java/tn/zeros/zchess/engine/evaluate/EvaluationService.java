package tn.zeros.zchess.engine.evaluate;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.engine.util.EvalVars;

public class EvaluationService {
    public static int evaluate(BoardState state) {
        int whiteMaterial = countMaterial(Piece.WHITE, state);
        int blackMaterial = countMaterial(Piece.BLACK, state);

        int eval = whiteMaterial - blackMaterial;

        int perspective = state.isWhiteToMove() ? 1 : -1;

        return perspective * eval;
    }

    static int countMaterial(int color, BoardState state) {
        int material = 0;
        material += Long.bitCount(state.getPieces(Piece.PAWN, color)) * EvalVars.PAWN_VALUE;
        material += Long.bitCount(state.getPieces(Piece.KNIGHT, color)) * EvalVars.KNIGHT_VALUE;
        material += Long.bitCount(state.getPieces(Piece.BISHOP, color)) * EvalVars.BISHOP_VALUE;
        material += Long.bitCount(state.getPieces(Piece.ROOK, color)) * EvalVars.ROOK_VALUE;
        material += Long.bitCount(state.getPieces(Piece.QUEEN, color)) * EvalVars.QUEEN_VALUE;
        return material;
    }
}
