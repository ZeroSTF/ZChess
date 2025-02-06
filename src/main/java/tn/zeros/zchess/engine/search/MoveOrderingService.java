package tn.zeros.zchess.engine.search;

import tn.zeros.zchess.core.logic.generation.MoveGenerator;
import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.util.PrecomputedMoves;
import tn.zeros.zchess.engine.evaluate.EvalUtils;

import java.util.Arrays;

public class MoveOrderingService {
    private static final int CAPTURE_BONUS = 1000000;
    private static final int PROMOTION_BONUS = 800000;
    private static final int ATTACKED_PENALTY = -200000;
    private static final int KILLER_MOVE_BONUS = 900000;
    private static final int HISTORY_SCORE_BONUS_FACTOR = 100;
    private final int[] moveScores;
    private final int[][] historyScores;
    private final int[] killerMoves;

    public MoveOrderingService() {
        moveScores = new int[SearchUtils.MAX_MOVES];
        historyScores = new int[7][64];
        killerMoves = new int[SearchUtils.MAX_DEPTH];
        clearKillerMoves();
    }

    public void clearKillerMoves() {
        Arrays.fill(killerMoves, Move.NULL_MOVE);
    }

    public void updateKillerMoves(int move, int ply) {
        if (Move.getCapturedPiece(move) != Piece.NONE)
            return; // Don't store captures as killer moves (MVV-LVA already handles them well)
        if (killerMoves[ply] != move) { // Avoid storing duplicates
            killerMoves[ply] = move;
        }
    }

    public void updateHistoryScores(int move, int depth) {
        int pieceType = Piece.getType(Move.getPiece(move));
        int toSquare = Move.getTo(move);
        historyScores[pieceType][toSquare] += depth * HISTORY_SCORE_BONUS_FACTOR; // Bonus scaled by depth
    }

    public void clearHistoryScores() {
        for (int i = 0; i < historyScores.length; i++) {
            Arrays.fill(historyScores[i], 0);
        }
    }

    public void orderMoves(MoveGenerator.MoveList moveList, BoardState state, int currentPly, int ttMove) {
        Arrays.fill(moveScores, 0, moveList.size, 0);
        int[] moves = moveList.moves;
        int size = moveList.size;
        int killerMove = killerMoves[currentPly];

        for (int i = 0; i < size; i++) {
            int move = moves[i];
            int movePieceType = Piece.getType(Move.getPiece(move));
            int capturedPieceType = Piece.getType(Move.getCapturedPiece(move));
            int toSquare = Move.getTo(move);
            int moveScore = 0;

            // Transposition Table move bonus (highest priority)
            if (move == ttMove && ttMove != Move.NULL_MOVE) {
                moveScore += CAPTURE_BONUS + PROMOTION_BONUS + KILLER_MOVE_BONUS + 10000000; // Very high bonus
            } else { // Only apply other heuristics if it's not the TT move (TT move is already best guess)

                // MVV-LVA scoring for captures
                if (capturedPieceType != Piece.NONE) {
                    moveScore += CAPTURE_BONUS + (10 * EvalUtils.getPieceTypeValue(capturedPieceType) - EvalUtils.getPieceTypeValue(movePieceType));
                }

                // Killer move bonus
                if (move == killerMove && killerMove != Move.NULL_MOVE) {
                    moveScore += KILLER_MOVE_BONUS;
                }

                // History Heuristic bonus
                moveScore += historyScores[movePieceType][toSquare] / HISTORY_SCORE_BONUS_FACTOR; // Scale down history score

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
