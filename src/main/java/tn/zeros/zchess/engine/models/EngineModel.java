package tn.zeros.zchess.engine.models;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;

public interface EngineModel {
    Move generateMove(BoardState boardState);
}