package tn.zeros.zchess.engine.models;

import tn.zeros.zchess.core.logic.generation.MoveGenerator;
import tn.zeros.zchess.core.model.BoardState;

import java.util.List;
import java.util.Random;

public class RandomMoveModel implements EngineModel {
    private static final Random random = new Random();

    public int generateMove(BoardState boardState) {
        if (!boardState.isGameOver()) { // Check if the game is not over
            List<Integer> legalMoves = MoveGenerator.generateAllMoves(boardState);
            if (!legalMoves.isEmpty()) {
                return legalMoves.get(random.nextInt(legalMoves.size()));
            }
        }
        return -1;
    }
}
