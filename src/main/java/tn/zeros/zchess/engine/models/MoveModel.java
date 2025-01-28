package tn.zeros.zchess.engine.models;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;

public interface MoveModel {
    Move generateMove(BoardState boardState);
}