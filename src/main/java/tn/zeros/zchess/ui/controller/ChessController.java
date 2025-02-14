package tn.zeros.zchess.ui.controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.util.Duration;
import tn.zeros.zchess.core.logic.generation.LegalMoveFilter;
import tn.zeros.zchess.core.logic.generation.MoveGenerator;
import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.GameResult;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.service.FenService;
import tn.zeros.zchess.core.service.GameStateChecker;
import tn.zeros.zchess.core.util.ChessConstants;
import tn.zeros.zchess.engine.models.EngineModel;
import tn.zeros.zchess.ui.events.ClockEvent;
import tn.zeros.zchess.ui.events.EventBus;
import tn.zeros.zchess.ui.events.EventListener;
import tn.zeros.zchess.ui.matchmaker.GameManager;
import tn.zeros.zchess.ui.matchmaker.GameMode;
import tn.zeros.zchess.ui.models.GameStateModel;
import tn.zeros.zchess.ui.util.SoundManager;
import tn.zeros.zchess.ui.view.ChessBoardView;
import tn.zeros.zchess.ui.view.ChessView;

import java.util.Collections;
import java.util.List;

public class ChessController implements GameListener, EventListener {
    private final GameManager gameManager;
    private final InteractionState interactionState;
    private final GameStateModel gameState = new GameStateModel();
    private final Timeline chessClock = new Timeline();
    private final InputHandler inputHandler;
    private BoardState boardState;
    private ChessView view;

    public ChessController() {
        this.boardState = new BoardState();
        this.gameManager = new GameManager(boardState);
        this.gameManager.addListener(this);
        this.interactionState = new InteractionState();
        this.inputHandler = new InputHandler(this);
        EventBus.getInstance().register(this);
        setupClock();
    }

    public BoardState getBoardState() {
        return boardState;
    }

    public void registerView(ChessBoardView view) {
        this.view = view;
    }

    public void handlePieceSelection(int square) {
        int piece = boardState.getPieceAt(square);
        if (piece != Piece.NONE && Piece.isWhite(piece) == boardState.isWhiteToMove() && !gameManager.isGameOver()) {
            interactionState.setSelectedSquare(square);
            interactionState.clearCurrentLegalMoves();

            // Get legal moves for the selected piece using new move generator
            MoveGenerator.MoveList moveList = MoveGenerator.generateAllMoves(boardState, false);
            List<Integer> legalMoves = moveList.toList().stream()
                    .filter(move -> Move.getFrom(move) == square)
                    .toList();

            interactionState.getCurrentLegalMoves().addAll(legalMoves);

            int kingInCheck = LegalMoveFilter.getKingInCheckSquare(boardState, boardState.isWhiteToMove());
            view.updateHighlights(interactionState.getCurrentLegalMoves(), kingInCheck);
        } else {
            resetSelection();
        }
    }

    public void handleMoveExecution(int targetSquare) {
        int move = findMoveByTarget(targetSquare);

        if (move != Move.NULL_MOVE) {
            if (Move.isPromotion(move)) {
                interactionState.setPendingPromotionMove(move);
                view.showPromotionDialog(Piece.isWhite(Move.getPiece(move)));
            } else {
                resetSelection();
                gameManager.executeMove(move);
            }
        } else {
            handlePieceSelection(targetSquare);
        }
    }

    private int findMoveByTarget(int targetSquare) {
        return interactionState.getCurrentLegalMoves().stream()
                .filter(m -> Move.getTo(m) == targetSquare)
                .findFirst()
                .orElse(Move.NULL_MOVE);
    }

    public void completePromotion(int promotionPiece) {
        int pendingPromotionMove = interactionState.getPendingPromotionMove();
        if (pendingPromotionMove == Move.NULL_MOVE) return;
        int updatedPromotionMove = Move.updatePromotionPiece(pendingPromotionMove, promotionPiece);
        if (interactionState.getCurrentLegalMoves().contains(updatedPromotionMove)) {
            resetSelection();
            gameManager.executeMove(updatedPromotionMove);
        } else {
            this.getInputHandler().restoreSourcePiece();
        }

        interactionState.setPendingPromotionMove(Move.NULL_MOVE);
    }

    private void resetSelection() {
        int kingInCheck = LegalMoveFilter.getKingInCheckSquare(boardState, boardState.isWhiteToMove());
        interactionState.setSelectedSquare(-1);
        view.updateHighlights(Collections.emptyList(), kingInCheck);
        interactionState.clearCurrentLegalMoves();
    }

    private void playMoveSound(int move) {
        boolean inCheck = LegalMoveFilter.inCheck(boardState, boardState.isWhiteToMove());

        if (inCheck) {
            SoundManager.playMoveCheck();
        } else if (Move.isCastling(move)) {
            SoundManager.playCastle();
        } else if (Move.isPromotion(move)) {
            SoundManager.playPromotion();
        } else if (Move.getCapturedPiece(move) != Piece.NONE) {
            SoundManager.playCapture();
        } else {
            SoundManager.playMove();
        }
    }

    public void undo() {
        int move = gameManager.stateManager.undo();
        undoRedoRefresh(move);
    }

    public void redo() {
        int move = gameManager.stateManager.redo();
        undoRedoRefresh(move);
    }

    private void undoRedoRefresh(int move) {
        if (move != Move.NULL_MOVE) {
            view.refreshEntireBoard();
            interactionState.setLastMoveFrom(Move.getFrom(move));
            interactionState.setLastMoveTo(Move.getTo(move));
            interactionState.setSelectedSquare(-1);
            int kingInCheck = LegalMoveFilter.getKingInCheckSquare(boardState, boardState.isWhiteToMove());
            view.updateHighlights(Collections.emptyList(), kingInCheck);
        }
    }

    public String getCurrentFEN() {
        return FenService.generateFEN(boardState);
    }

    public void loadFEN(String fen) {
        try {
            BoardState newState = FenService.parseFEN(fen, boardState);
            resetState(newState);
            view.refreshEntireBoard();
        } catch (IllegalArgumentException e) {
            view.showError("Invalid FEN: " + e.getMessage());
        }
    }

    private void resetState(BoardState newState) {
        this.boardState = newState;
        gameManager.resetStateManager(newState);
    }

    public InteractionState getInteractionState() {
        return interactionState;
    }

    public ChessView getView() {
        return view;
    }

    public InputHandler getInputHandler() {
        return inputHandler;
    }

    @Override
    public void onMoveExecuted(int move, BoardState boardState) {
        interactionState.setLastMoveFrom(Move.getFrom(move));
        interactionState.setLastMoveTo(Move.getTo(move));
        gameManager.stateManager.clearRedo();

        int kingInCheck = LegalMoveFilter.getKingInCheckSquare(boardState, boardState.isWhiteToMove());
        view.refreshEntireBoard();
        view.updateHighlights(Collections.emptyList(), kingInCheck);
        playMoveSound(move);
    }

    public void startGame() {
        this.gameManager.startGame();
        chessClock.play();
    }

    public void setGameMode(GameMode mode, EngineModel whiteModel, EngineModel blackModel, boolean modelColor) {
        gameManager.setGameMode(mode, whiteModel, blackModel, modelColor);
    }

    public void flipBoard() {
        if (view instanceof ChessBoardView) {
            ((ChessBoardView) view).flipBoard();
        }
    }

    public void restartGame() {
        BoardState newState = FenService.parseFEN(ChessConstants.DEFAULT_FEN, boardState);
        resetState(newState);
        interactionState.clearAll();
        EventBus.getInstance().post(new ClockEvent(
                ClockEvent.Type.RESET,
                false,
                Duration.ZERO
        ));
        gameState.gameResultProperty().set(GameResult.ONGOING);
        gameManager.startGame();
        chessClock.play();
        view.refreshEntireBoard();
    }

    public GameStateModel getGameStateModel() {
        return gameState;
    }

    private void setupClock() {
        chessClock.setCycleCount(Timeline.INDEFINITE);
        chessClock.getKeyFrames().add(new KeyFrame(
                Duration.seconds(1),
                e -> EventBus.getInstance().post(new ClockEvent(
                        ClockEvent.Type.TICK,
                        boardState.isWhiteToMove(),
                        Duration.seconds(1)
                ))
        ));
    }

    private void checkClockExpiration(Duration remaining, boolean isWhite) {
        if (remaining.lessThanOrEqualTo(Duration.ZERO)) {
            Platform.runLater(() -> {
                chessClock.stop();
                GameResult result = isWhite ? GameResult.BLACK_WINS : GameResult.WHITE_WINS;
                gameState.gameResultProperty().set(result);
                showTimeExpiredResult(result);
            });
        }
    }

    private void showTimeExpiredResult(GameResult result) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Time Expired");
        alert.setHeaderText(result == GameResult.WHITE_WINS ?
                "White wins on time!" : "Black wins on time!");
        alert.show();
    }

    @Override
    public void onGameOver(BoardState boardState) {
        Platform.runLater(() -> {
            chessClock.stop();
            GameResult result = GameStateChecker.getGameResult(boardState);
            gameState.gameResultProperty().set(result);
        });
    }

    @Override
    public void onClockEvent(ClockEvent event) {
        Platform.runLater(() -> {
            switch (event.getType()) {
                case TICK -> handleClockTick(event);
                case INCREMENT -> handleTimeIncrement(event);
                case RESET -> handleClockReset();
            }
        });
    }

    private void handleClockTick(ClockEvent event) {
        if (event.isWhite()) {
            gameState.whiteTimeProperty().set(
                    gameState.whiteTimeProperty().get().subtract(event.getDuration())
            );
            checkClockExpiration(gameState.whiteTimeProperty().get(), true);
        } else {
            gameState.blackTimeProperty().set(
                    gameState.blackTimeProperty().get().subtract(event.getDuration())
            );
            checkClockExpiration(gameState.blackTimeProperty().get(), false);
        }
    }

    private void handleTimeIncrement(ClockEvent event) {
        if (event.isWhite()) {
            gameState.whiteTimeProperty().set(
                    gameState.whiteTimeProperty().get().add(event.getDuration())
            );
        } else {
            gameState.blackTimeProperty().set(
                    gameState.blackTimeProperty().get().add(event.getDuration())
            );
        }
    }

    private void handleClockReset() {
        gameState.resetClocks();
    }


}
