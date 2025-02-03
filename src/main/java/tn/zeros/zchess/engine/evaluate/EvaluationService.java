package tn.zeros.zchess.engine.evaluate;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.engine.util.EvalUtils;

public class EvaluationService {
    public static int evaluate(BoardState state) {
        int whiteEval = 0;
        int blackEval = 0;

        int whiteMaterial = countMaterial(Piece.WHITE, state);
        int blackMaterial = countMaterial(Piece.BLACK, state);
        int whiteMaterialWithoutPawns = whiteMaterial - Long.bitCount(state.getPieces(Piece.PAWN, Piece.WHITE)) * EvalUtils.PAWN_VALUE;
        int blackMaterialWithoutPawns = blackMaterial - Long.bitCount(state.getPieces(Piece.PAWN, Piece.BLACK)) * EvalUtils.PAWN_VALUE;

        // Evaluate material
        whiteEval += whiteMaterial;
        blackEval += blackMaterial;

        // Evaluate king move up in endgame
        float whiteEndgameWeight = getEndgameWeight(whiteMaterialWithoutPawns);
        float blackEndgameWeight = getEndgameWeight(blackMaterialWithoutPawns);
        int whiteKingSquare = state.getKingSquare(true);
        int blackKingSquare = state.getKingSquare(false);
        whiteEval += forceKingIntoCorner(whiteKingSquare, blackKingSquare, whiteEndgameWeight);
        blackEval += forceKingIntoCorner(blackKingSquare, whiteKingSquare, blackEndgameWeight);

        // Calculate final evaluation
        int eval = whiteEval - blackEval;
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

    private static int forceKingIntoCorner(int friendlyKingSquare, int enemyKingSquare, float endgameWeight) {
        int evaluation = 0;

        // Force enemy king into edge or corner
        int enemyKingRank = enemyKingSquare >> 3;
        int enemyKingFile = enemyKingSquare & 7;

        int opponentKingDistanceFromCenterRank = Math.max(3 - enemyKingRank, enemyKingRank - 4);
        int opponentKingDistanceFromCenterFile = Math.max(3 - enemyKingFile, enemyKingFile - 4);

        int opponentKingDistanceFromCenter = opponentKingDistanceFromCenterRank + opponentKingDistanceFromCenterFile;
        evaluation += (int) (opponentKingDistanceFromCenter * endgameWeight);

        // Incentivise friendly king to get closer to enemy king
        int friendlyKingRank = friendlyKingSquare >> 3;
        int friendlyKingFile = friendlyKingSquare & 7;

        int distanceBetweenKings = Math.abs(friendlyKingRank - enemyKingRank) + Math.abs(friendlyKingFile - enemyKingFile);
        evaluation += 14 - distanceBetweenKings;
        return (int) (evaluation * 10 * endgameWeight);
    }

    private static float getEndgameWeight(int materialCountWithoutPawns) {
        final float multiplier = 1 / EvalUtils.ENDGAME_MATERIAL_START;
        return 1 - Math.min(1, materialCountWithoutPawns * multiplier);
    }

}
