package tn.zeros.zchess.engine.util;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.service.FenService;
import tn.zeros.zchess.engine.models.EngineModel;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TestHarness {
    public static void runTestSuite(EngineModel model, Path epdPath) throws IOException {
        List<String> epdLines = Files.readAllLines(epdPath);
        int total = epdLines.size();
        int solved = 0;

        for (String line : epdLines) {
            TestPosition position = TestPosition.fromEpd(line);
            BoardState testState = new BoardState();
            FenService.parseFEN(position.fen(), testState);

            int move = model.generateMove(testState);

            if (isMoveCorrect(position, move)) {
                solved++;
                System.out.printf("✅ %s: Correct%n", position.id());
            } else {
                System.out.printf("❌ %s: Failed%n", position.id());
            }
        }

        System.out.printf("%nTest Suite Results: %d/%d (%.1f%%)%n",
                solved, total, (100.0 * solved) / total);
    }

    private static boolean isMoveCorrect(TestPosition position, int move) {
        if (move == Move.NULL_MOVE) return false;
        String engineMove = Move.toAlgebraic(move)
                .toLowerCase();
        return position.correctMoves().stream()
                .map(correct -> correct.toLowerCase().replace("+", "").replace("#", ""))
                .anyMatch(engineMove::equals);
    }

}