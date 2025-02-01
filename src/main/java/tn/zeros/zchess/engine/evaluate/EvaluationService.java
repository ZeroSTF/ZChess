package tn.zeros.zchess.engine.evaluate;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.engine.util.EvalUtils;

public class EvaluationService {
    private static int forceKingIntoCorner(int friendlyKingSquare, int enemyKingSquare, float endgameWeight) {
        int enemyKingRank = enemyKingSquare / 8;
        int enemyKingFile = enemyKingSquare % 8;
        return 0;
    }

    public static int evaluate(BoardState state) {
        int whiteMaterial = countMaterial(Piece.WHITE, state);
        int blackMaterial = countMaterial(Piece.BLACK, state);

        int eval = whiteMaterial - blackMaterial;

        int perspective = state.isWhiteToMove() ? 1 : -1;

        return eval * perspective;
    }

    private static int countMaterial(int color, BoardState state) {
        int material = 0;
        material += Long.bitCount(state.getPieces(Piece.PAWN, color)) * EvalUtils.PAWN_VALUE;
        material += Long.bitCount(state.getPieces(Piece.KNIGHT, color)) * EvalUtils.KNIGHT_VALUE;
        material += Long.bitCount(state.getPieces(Piece.BISHOP, color)) * EvalUtils.BISHOP_VALUE;
        material += Long.bitCount(state.getPieces(Piece.ROOK, color)) * EvalUtils.ROOK_VALUE;
        material += Long.bitCount(state.getPieces(Piece.QUEEN, color)) * EvalUtils.QUEEN_VALUE;
        return material;
    }

}
