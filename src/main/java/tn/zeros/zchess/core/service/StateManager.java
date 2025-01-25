package tn.zeros.zchess.core.service;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.GameState;

import java.util.Stack;

public class StateManager {
    private final BoardState boardState;
    private final Stack<GameState> undoStack = new Stack<>();
    private final Stack<GameState> redoStack = new Stack<>();

    public StateManager(BoardState boardState) {
        this.boardState = boardState;
    }

    public void saveState() {
        GameState state = new GameState(
                boardState.getPieceBitboards().clone(),
                boardState.getColorBitboards().clone(),
                boardState.getCastlingRights(),
                boardState.getEnPassantSquare(),
                boardState.getHalfMoveClock(),
                boardState.getFullMoveNumber(),
                boardState.isWhiteToMove()
        );
        undoStack.push(state);
    }

    public boolean undo() {
        if (undoStack.isEmpty()) return false;

        GameState current = new GameState(
                boardState.getPieceBitboards().clone(),
                boardState.getColorBitboards().clone(),
                boardState.getCastlingRights(),
                boardState.getEnPassantSquare(),
                boardState.getHalfMoveClock(),
                boardState.getFullMoveNumber(),
                boardState.isWhiteToMove()
        );
        redoStack.push(current);

        GameState previous = undoStack.pop();
        previous.restore(boardState);
        return true;
    }

    public boolean redo() {
        if (redoStack.isEmpty()) return false;

        GameState future = redoStack.pop();
        saveState();
        future.restore(boardState);
        return true;
    }

    public void clearRedo(){
        redoStack.clear();
    }
}
