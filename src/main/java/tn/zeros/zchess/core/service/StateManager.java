package tn.zeros.zchess.core.service;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.MoveUndoInfo;

import java.util.Stack;

public class StateManager {
    private final BoardState boardState;
    private final Stack<MoveUndoInfo> undoStack = new Stack<>();
    private final Stack<MoveUndoInfo> redoStack = new Stack<>();

    public StateManager(BoardState boardState) {
        this.boardState = boardState;
    }

    public void saveState(MoveUndoInfo undoInfo) {
        //MoveUndoInfo undoInfo = MoveExecutor.makeMove(boardState, move);
        undoStack.push(undoInfo);
    }

    public boolean undo() {
        if (undoStack.isEmpty()) return false;

        MoveUndoInfo undoInfo = undoStack.pop();
        MoveExecutor.unmakeMove(boardState, undoInfo);
        redoStack.push(undoInfo);
        return true;
    }

    public boolean redo() {
        if (redoStack.isEmpty()) return false;

        MoveUndoInfo redoInfo = redoStack.pop();
        saveState(redoInfo);
        return true;
    }

    public void clearRedo() {
        redoStack.clear();
    }
}
