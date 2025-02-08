package tn.zeros.zchess.ui.matchmaker;

import javafx.application.Platform;
import javafx.concurrent.Task;
import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.MoveUndoInfo;
import tn.zeros.zchess.core.service.GameStateChecker;
import tn.zeros.zchess.core.service.MoveExecutor;
import tn.zeros.zchess.core.service.StateManager;
import tn.zeros.zchess.engine.models.EngineModel;
import tn.zeros.zchess.engine.models.ModelV1;
import tn.zeros.zchess.ui.controller.GameListener;

import java.util.ArrayList;
import java.util.List;

import static tn.zeros.zchess.ui.util.UIConstants.DEFAULT_SEARCH_TIME_MS;

public class GameManager {
    private final BoardState boardState;
    private final List<GameListener> listeners = new ArrayList<>();
    public StateManager stateManager;
    private EngineModel whiteModel;
    private EngineModel blackModel;
    private GameMode gameMode;
    private boolean gameInProgress;
    private boolean modelColor;

    public GameManager(BoardState boardState) {
        this.boardState = boardState;
        this.stateManager = new StateManager(boardState);
        // Default game mode
        this.gameMode = GameMode.HUMAN_VS_MODEL;
        this.modelColor = false;
        this.whiteModel = new ModelV1(DEFAULT_SEARCH_TIME_MS);
        this.blackModel = new ModelV1(DEFAULT_SEARCH_TIME_MS);
    }

    public void setGameMode(GameMode mode, EngineModel whiteModel, EngineModel blackModel, boolean modelColor) {
        this.gameMode = mode;
        this.whiteModel = whiteModel;
        this.blackModel = blackModel;
        this.modelColor = modelColor;
        checkForModelMove();
    }

    public void startGame() {
        gameInProgress = true;
        checkForModelMove();
    }

    public void executeMove(int move) {
        if (!gameInProgress || isGameOver()) return;

        MoveUndoInfo undoInfo = MoveExecutor.makeMove(boardState, move);
        stateManager.saveState(undoInfo);
        notifyMoveExecuted(move);

        checkForModelMove();
    }

    public void resetStateManager(BoardState newState) {
        stateManager = new StateManager(newState);
        whiteModel.reset();
        blackModel.reset();
        newState.clearPositionCounts();
    }

    public void checkForModelMove() {
        if (!gameInProgress || isGameOver()) return;
        if (shouldModelPlay()) {
            runModelMoveTask();
        }
    }

    private void runModelMoveTask() {
        Task<Integer> task = new Task<>() {
            @Override
            protected Integer call() {
                EngineModel currentModel = boardState.isWhiteToMove() ? whiteModel : blackModel;
                // This call might be time-consuming.
                return currentModel.generateMove(boardState);
            }

            @Override
            protected void succeeded() {
                int bestMove = getValue();
                // Update the GUI (and game state) on the JavaFX Application Thread.
                Platform.runLater(() -> executeMove(bestMove));
            }

            @Override
            protected void failed() {
                getException().printStackTrace();
            }
        };

        Thread thread = new Thread(task);
        thread.setDaemon(true); // Ensure the thread doesn't block application exit.
        thread.start();
    }


    private boolean shouldModelPlay() {
        return gameMode == GameMode.MODEL_VS_MODEL ||
                (gameMode == GameMode.HUMAN_VS_MODEL && boardState.isWhiteToMove() == modelColor);
    }

    private boolean isGameOver() {
        return GameStateChecker.isGameOver(boardState);
    }

    public void addListener(GameListener listener) {
        listeners.add(listener);
    }

    private void notifyMoveExecuted(int move) {
        listeners.forEach(l -> l.onMoveExecuted(move, boardState));
    }

}