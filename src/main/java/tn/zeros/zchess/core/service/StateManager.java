package tn.zeros.zchess.core.service;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
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
        undoStack.push(undoInfo);
    }

    public Move undo() {
        if (undoStack.isEmpty()) return null;

        MoveUndoInfo undoInfo = undoStack.pop();
        Move move = MoveExecutor.unmakeMove(boardState, undoInfo);
        redoStack.push(undoInfo);
        return move;
    }

    public Move redo() {
        if (redoStack.isEmpty()) return null;

        MoveUndoInfo redoInfo = redoStack.pop();
        saveState(redoInfo);
        MoveUndoInfo undoInfo = MoveExecutor.makeMove(boardState, redoInfo.move());
        return undoInfo.move();
    }

    public void clearRedo() {
        redoStack.clear();
    }
}
