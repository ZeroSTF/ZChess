package tn.zeros.zchess.engine.search;

import tn.zeros.zchess.core.logic.generation.LegalMoveFilter;
import tn.zeros.zchess.core.logic.generation.MoveGenerator;
import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.MoveUndoInfo;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.service.MoveExecutor;
import tn.zeros.zchess.engine.evaluate.EvaluationService;
import tn.zeros.zchess.engine.util.EvalUtils;
import tn.zeros.zchess.engine.util.SearchMetrics;

public class SearchService {
    public int minimax(int depth, BoardState state) {
        if (depth == 0) {
            SearchMetrics.getInstance().incrementPositions();
            return EvaluationService.evaluate(state);
        }
        ;

        MoveGenerator.MoveList moves = MoveGenerator.generateAllMoves(state);

        if (moves.isEmpty()) {
            if (LegalMoveFilter.inCheck(state, state.isWhiteToMove()))
                return Integer.MIN_VALUE;
            return 0;
        }

        int bestEval = Integer.MIN_VALUE;

        for (int i = 0; i < moves.size; i++) {
            int move = moves.moves[i];
            MoveUndoInfo undoInfo = MoveExecutor.makeMove(state, move);
            int eval = -minimax(depth - 1, state);
            bestEval = Math.max(bestEval, eval);
            MoveExecutor.unmakeMove(state, undoInfo);
        }

        return bestEval;
    }

    public int alphaBetaPrune(int depth, int alpha, int beta, BoardState state) {
        if (depth == 0) {
            SearchMetrics.getInstance().incrementPositions();
            return EvaluationService.evaluate(state);
        }
        ;

        MoveGenerator.MoveList moves = MoveGenerator.generateAllMoves(state);

        if (moves.isEmpty()) {
            if (LegalMoveFilter.inCheck(state, state.isWhiteToMove()))
                return Integer.MIN_VALUE;
            return 0;
        }

        for (int i = 0; i < moves.size; i++) {
            int move = moves.moves[i];
            MoveUndoInfo undoInfo = MoveExecutor.makeMove(state, move);
            int eval = -alphaBetaPrune(depth - 1, -beta, -alpha, state);
            MoveExecutor.unmakeMove(state, undoInfo);
            if (eval >= beta) {
                return beta;
            }
            alpha = Math.max(alpha, eval);
        }

        return alpha;
    }

    // Helper methods
    private void orderMoves(int[] moves) {
        for (int move : moves) {
            int moveScoreGuess = 0;
            int movePieceType = Piece.getType(Move.getPiece(move));
            int capturedPieceType = Piece.getType(Move.getCapturedPiece(move));

            // Prioritize capturing the most valuable piece with the least valuable piece
            if (capturedPieceType != Piece.NONE) {
                moveScoreGuess += 10 * EvalUtils.getPieceTypeValue(capturedPieceType) - EvalUtils.getPieceTypeValue(movePieceType);
            }

            // Prioritize promoting a pawn
            if (movePieceType == Piece.PAWN && Move.isPromotion(move)) {
                moveScoreGuess += 100;
            }

            // Penalize moving to a square that is attacked by an enemy pawn
            // TODO add board attack maps by color and piece type
        }
    }
}
