package tn.zeros.zchess.engine.evaluate;

import org.junit.jupiter.api.Test;
import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.service.FenService;

public class EvaluationTest {
    @Test
    public void testEvaluate() {
        BoardState state = new BoardState();
        FenService.parseFEN("8/8/8/8/8/5K2/4R3/7k w - - 0 1", state);
        int evaluation = EvaluationService.evaluate(state);
        System.out.println("Evaluation: " + evaluation);
    }
}
