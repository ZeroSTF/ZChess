package tn.zeros.zchess.ui.controller;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;

public interface GameListener {
    void onMoveExecuted(Move move, BoardState boardState);
}