package tn.zeros.zchess.ui.controller;

import tn.zeros.zchess.core.model.BoardState;

public interface GameListener {
    void onMoveExecuted(int move, BoardState boardState);

    void onGameOver(BoardState boardState);
}