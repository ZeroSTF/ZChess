package tn.zeros.zchess.engine.models;

import tn.zeros.zchess.core.logic.generation.MoveGenerator;
import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.service.GameStateChecker;

import java.util.Random;

public class RandomMoveModel implements EngineModel {
    private static final Random random = new Random();

    public int generateMove(BoardState boardState) {
        if (!GameStateChecker.isGameOver(boardState)) {
            MoveGenerator.MoveList legalMoves = MoveGenerator.generateAllMoves(boardState, false);
            if (!legalMoves.isEmpty()) {
                return legalMoves.moves[random.nextInt(legalMoves.size)];
            }
        }
        return Move.NULL_MOVE;
    }

    @Override
    public void reset() {
    }
}
