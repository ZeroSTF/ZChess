package tn.zeros.zchess.ui.matchmaker;

import tn.zeros.zchess.core.logic.generation.LegalMoveFilter;
import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.service.MoveExecutor;
import tn.zeros.zchess.engine.models.EngineModel;

public class EngineTester {
    private static final int NUM_GAMES = 100;
    private static final int MAX_MOVES = 200;
    private static final boolean ALTERNATE_COLORS = true;

    public static void testVersions(EngineModel versionA, EngineModel versionB) {
        int winsA = 0, winsB = 0, draws = 0;

        for (int i = 0; i < NUM_GAMES; i++) {
            boolean aIsWhite = !ALTERNATE_COLORS || i % 2 == 0;
            GameResult result = playGame(versionA, versionB, aIsWhite);

            if (result == GameResult.WHITE_WINS) winsA += aIsWhite ? 1 : 0;
            else if (result == GameResult.BLACK_WINS) winsB += aIsWhite ? 0 : 1;
            else draws++;
        }

        System.out.printf("Version A wins: %d | Version B wins: %d | Draws: %d%n", winsA, winsB, draws);
        printConfidence(winsA, winsB, draws, NUM_GAMES);
    }

    private static GameResult playGame(EngineModel whiteEngine, EngineModel blackEngine, boolean useOpeningBook) {
        BoardState state = new BoardState();

        for (int moveCount = 0; moveCount < MAX_MOVES; moveCount++) {
            EngineModel currentEngine = state.isWhiteToMove() ? whiteEngine : blackEngine;
            int move = currentEngine.generateMove(state);

            if (move == Move.NULL_MOVE) { // Game over
                if (LegalMoveFilter.inCheck(state, state.isWhiteToMove())) {
                    return state.isWhiteToMove() ? GameResult.BLACK_WINS : GameResult.WHITE_WINS;
                }
                return GameResult.DRAW;
            }

            MoveExecutor.makeMove(state, move);
        }
        return GameResult.DRAW;
    }

    private static void printConfidence(int winsA, int winsB, int draws, int totalGames) {
        double winRateA = (winsA + 0.5 * draws) / totalGames;
        double winRateB = (winsB + 0.5 * draws) / totalGames;

        double eloDifference = 400 * Math.log10(winRateA / winRateB);
        double stdError = 1 / Math.sqrt(totalGames * (winRateA * (1 - winRateA) + winRateB * (1 - winRateB)));

        System.out.printf("Elo difference: %.1f ± %.1f (95%% confidence)%n",
                eloDifference, 1.96 * stdError * 400);
    }

    enum GameResult {WHITE_WINS, BLACK_WINS, DRAW}
}