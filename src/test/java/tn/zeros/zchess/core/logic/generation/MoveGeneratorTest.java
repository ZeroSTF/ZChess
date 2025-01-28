package tn.zeros.zchess.core.logic.generation;

import org.junit.jupiter.api.Test;
import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Piece;

public class MoveGeneratorTest {
    @Test
    void testPawnMoves() {
        System.out.println("Pawn Moves from D2:");
        int testSquare = 11;
        BoardState testState = new BoardState();
        testState.setRank(Piece.PAWN, Piece.WHITE, 1);
        //System.out.println(PawnMoveGenerator.generate(testState, testSquare));
    }
}
