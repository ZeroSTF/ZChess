package tn.zeros.zchess.engine.search;

import tn.zeros.zchess.core.logic.generation.MoveGenerator;
import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.util.PrecomputedMoves;
import tn.zeros.zchess.engine.util.EvalUtils;
import tn.zeros.zchess.engine.util.SearchUtils;

public class MoveOrderingService {
    int[] moveScores;

    public MoveOrderingService() {
        moveScores = new int[SearchUtils.MAX_MOVES];
    }

    public void orderMoves(MoveGenerator.MoveList moveList, BoardState state) {
        int[] moves = moveList.moves;
        int size = moveList.size;
        for (int i = 0; i < size; i++) {
            int move = moves[i];
            int moveScoreGuess = 0;
            int movePieceType = Piece.getType(Move.getPiece(move));
            int capturedPieceType = Piece.getType(Move.getCapturedPiece(move));
            int toSquare = Move.getTo(move);

            // Prioritize capturing the most valuable piece with the least valuable piece
            if (capturedPieceType != Piece.NONE) {
                moveScoreGuess += 10 * EvalUtils.getPieceTypeValue(capturedPieceType) - EvalUtils.getPieceTypeValue(movePieceType);
            }

            // Prioritize promoting a pawn
            if (movePieceType == Piece.PAWN && Move.isPromotion(move)) {
                moveScoreGuess += EvalUtils.getPieceTypeValue(Piece.getType(Move.getPromotionPiece(move)));
            }

            // Penalize moving to a square that is attacked by an enemy pawn
            boolean isWhite = state.isWhiteToMove();
            long enemyPawns = state.getPieces(Piece.PAWN, isWhite ? Piece.BLACK : Piece.WHITE);
            if ((enemyPawns & PrecomputedMoves.getPawnAttacks(toSquare, isWhite)) != 0) {
                moveScoreGuess -= EvalUtils.getPieceTypeValue(movePieceType);
            }
            moveScores[i] = moveScoreGuess;
        }

        sort(moves, size);
    }

    private void sort(int[] moves, int size) {
        for (int i = 1; i < size; i++) {
            int currentMove = moves[i];
            int currentScore = moveScores[i];
            int j = i - 1;
            while (j >= 0 && moveScores[j] < currentScore) {
                moves[j + 1] = moves[j];
                moveScores[j + 1] = moveScores[j];
                j--;
            }
            moves[j + 1] = currentMove;
            moveScores[j + 1] = currentScore;
        }
    }
}
