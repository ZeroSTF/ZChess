package tn.zeros.zchess.engine.search;

import tn.zeros.zchess.core.logic.generation.MoveGenerator;
import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.util.PrecomputedMoves;
import tn.zeros.zchess.engine.util.EvalUtils;
import tn.zeros.zchess.engine.util.SearchUtils;

import java.util.Arrays;

public class MoveOrderingService {
    private static final int CAPTURE_BONUS = 1000000;
    private static final int PROMOTION_BONUS = 800000;
    private static final int ATTACKED_PENALTY = -200000;
    private final int[] moveScores;

    public MoveOrderingService() {
        moveScores = new int[SearchUtils.MAX_MOVES];
    }

    public void orderMoves(MoveGenerator.MoveList moveList, BoardState state) {
        Arrays.fill(moveScores, 0, moveList.size, 0);
        int[] moves = moveList.moves;
        int size = moveList.size;

        for (int i = 0; i < size; i++) {
            int move = moves[i];
            int movePieceType = Piece.getType(Move.getPiece(move));
            int capturedPieceType = Piece.getType(Move.getCapturedPiece(move));
            int toSquare = Move.getTo(move);
            int moveScore = 0;

            // MVV-LVA scoring for captures
            if (capturedPieceType != Piece.NONE) {
                moveScore += CAPTURE_BONUS + (10 * EvalUtils.getPieceTypeValue(capturedPieceType) - EvalUtils.getPieceTypeValue(movePieceType));
            }

            // Prioritize promoting a pawn
            if (Move.isPromotion(move)) {
                moveScore += PROMOTION_BONUS + EvalUtils.getPieceTypeValue(Piece.getType(Move.getPromotionPiece(move)));
            }


            // Attacked square penalty
            boolean isWhite = state.isWhiteToMove();
            long enemyPawns = state.getPieces(Piece.PAWN, isWhite ? Piece.BLACK : Piece.WHITE);
            if ((PrecomputedMoves.getPawnAttacks(toSquare, isWhite) & enemyPawns) != 0) {
                moveScore += ATTACKED_PENALTY;
            }
            moveScores[i] = moveScore;
        }

        sort(moves, moveScores, size);
    }

    private void sort(int[] moves, int[] scores, int size) {
        for (int i = 1; i < size; i++) {
            int currentMove = moves[i];
            int currentScore = scores[i];
            int j = i - 1;

            while (j >= 0 && scores[j] < currentScore) {
                moves[j + 1] = moves[j];
                scores[j + 1] = scores[j];
                j--;
            }

            moves[j + 1] = currentMove;
            scores[j + 1] = currentScore;
        }
    }
}
