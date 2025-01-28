package tn.zeros.zchess.engine.models;

import tn.zeros.zchess.core.model.BoardState;

public interface EngineModel {
    int generateMove(BoardState boardState);
}