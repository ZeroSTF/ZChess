package tn.zeros.zchess.ui.matchmaker;

import javafx.animation.PauseTransition;
import javafx.util.Duration;
import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.MoveUndoInfo;
import tn.zeros.zchess.core.service.MoveExecutor;
import tn.zeros.zchess.core.service.StateManager;
import tn.zeros.zchess.engine.models.EngineModel;
import tn.zeros.zchess.engine.models.OrderedAlphaBetaModel;
import tn.zeros.zchess.engine.search.SearchService;
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
    private boolean modelColor;

    public GameManager(BoardState boardState) {
        this.boardState = boardState;
        this.stateManager = new StateManager(boardState);
        // Default game mode
        this.gameMode = GameMode.HUMAN_VS_MODEL;
        this.modelColor = false;
        this.blackModel = new OrderedAlphaBetaModel(new SearchService(), 4);
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

    public void resetStateManager() {
        stateManager = new StateManager(boardState);
    }

    private void playNextModelMove() {
        PauseTransition pause = new PauseTransition(Duration.millis(500));
        pause.setOnFinished(event -> {
            EngineModel currentModel = boardState.isWhiteToMove() ? whiteModel : blackModel;
            int modelMove = currentModel.generateMove(boardState);

            if (modelMove != -1) {
                executeMove(modelMove);
            }
        });
        pause.play();
    }

    public void checkForModelMove() {
        if (!gameInProgress || isGameOver()) return;

        if (shouldModelPlay()) {
            playNextModelMove();
        }
    }

    private boolean shouldModelPlay() {
        return gameMode == GameMode.MODEL_VS_MODEL ||
                (gameMode == GameMode.HUMAN_VS_MODEL && boardState.isWhiteToMove() == modelColor);
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