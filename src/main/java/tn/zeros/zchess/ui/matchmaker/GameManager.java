package tn.zeros.zchess.ui.matchmaker;

import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.MoveUndoInfo;
import tn.zeros.zchess.core.service.MoveExecutor;
import tn.zeros.zchess.core.service.StateManager;
import tn.zeros.zchess.engine.models.EngineModel;
import tn.zeros.zchess.engine.models.MiniMaxModel;
import tn.zeros.zchess.ui.controller.GameListener;

import java.util.ArrayList;
import java.util.List;

public class GameManager {
    private final BoardState boardState;
    private final List<GameListener> listeners = new ArrayList<>();
    public StateManager stateManager;
    private EngineModel whiteModel;
    private EngineModel blackModel;
    private GameMode gameMode;
    private boolean gameInProgress;

    public GameManager(BoardState boardState) {
        this.boardState = boardState;
        this.stateManager = new StateManager(boardState);
        this.gameMode = GameMode.HUMAN_VS_MODEL;
        this.blackModel = new MiniMaxModel();
    }

    public void setGameMode(GameMode mode, EngineModel whiteModel, EngineModel blackModel) {
        this.gameMode = mode;
        this.whiteModel = whiteModel;
        this.blackModel = blackModel;
    }

    public void startGame() {
        gameInProgress = true;
        if (gameMode == GameMode.MODEL_VS_MODEL ||
                (gameMode == GameMode.HUMAN_VS_MODEL && !boardState.isWhiteToMove())) {
            playNextModelMove();
        }
    }

    public void executeMove(int move) {
        if (!gameInProgress) return;

        MoveUndoInfo undoInfo = MoveExecutor.makeMove(boardState, move);
        stateManager.saveState(undoInfo);
        notifyMoveExecuted(move);

        if (!isGameOver() && gameMode != GameMode.HUMAN_VS_HUMAN) {
            playNextModelMove();
        }
    }

    public void resetStateManager() {
        stateManager = new StateManager(boardState);
    }

    private void playNextModelMove() {
        if (gameMode == GameMode.HUMAN_VS_MODEL && boardState.isWhiteToMove()) return;

        EngineModel currentModel = boardState.isWhiteToMove() ? whiteModel : blackModel;
        int modelMove = currentModel.generateMove(boardState);

        if (modelMove != -1) {
            executeMove(modelMove);
        }
    }

    private boolean isGameOver() {
        return boardState.isGameOver();
    }

    public void addListener(GameListener listener) {
        listeners.add(listener);
    }

    private void notifyMoveExecuted(int move) {
        listeners.forEach(l -> l.onMoveExecuted(move, boardState));
    }

}