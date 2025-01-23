package tn.zeros.zchess.core.logic.generation;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;

import java.util.ArrayList;
import java.util.List;

public class MoveGenerator {
    private final BoardState position;

    public MoveGenerator(BoardState position) {
        this.position = position;
    }

    public List<Move> generateLegalMoves() {
        List<Move> moves = new ArrayList<>();
        // TODO Implementation for generating all legal moves
        return moves;
    }
}
