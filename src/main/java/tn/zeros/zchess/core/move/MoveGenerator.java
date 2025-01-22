package tn.zeros.zchess.core.move;

import tn.zeros.zchess.core.board.BitboardPosition;

import java.util.ArrayList;
import java.util.List;

public class MoveGenerator {
    private final BitboardPosition position;

    public MoveGenerator(BitboardPosition position) {
        this.position = position;
    }

    public List<Move> generateLegalMoves() {
        List<Move> moves = new ArrayList<>();
        // TODO Implementation for generating all legal moves
        return moves;
    }
}
