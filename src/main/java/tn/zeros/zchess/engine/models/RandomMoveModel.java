package tn.zeros.zchess.engine.models;

import tn.zeros.zchess.core.logic.generation.MoveGenerator;
import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;

import java.util.List;
import java.util.Random;

public class RandomMoveModel implements MoveModel {
    private static final Random random = new Random();

    public Move generateMove(BoardState boardState) {
        if (!boardState.isGameOver()) { // Check if the game is not over
            List<Move> legalMoves = MoveGenerator.generateAllMoves(boardState);
            if (!legalMoves.isEmpty()) {
                Move move = legalMoves.get(random.nextInt(legalMoves.size()));
                return move;
            }
        }
        return null;
    }
}
