package tn.zeros.zchess.ui.controller;

import tn.zeros.zchess.core.logic.generation.LegalMoveFilter;
import tn.zeros.zchess.core.logic.generation.MoveGenerator;
import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.service.FenService;
import tn.zeros.zchess.engine.models.EngineModel;
import tn.zeros.zchess.ui.matchmaker.GameManager;
import tn.zeros.zchess.ui.matchmaker.GameMode;
import tn.zeros.zchess.ui.util.SoundManager;
import tn.zeros.zchess.ui.view.ChessBoardView;
import tn.zeros.zchess.ui.view.ChessView;

import java.util.Collections;
import java.util.List;

public class ChessController implements GameListener {
    private final GameManager gameManager;
    private final InteractionState interactionState;
    private final InputHandler inputHandler;
    private BoardState boardState;
    private ChessView view;

    public ChessController() {
        this.boardState = new BoardState();
        this.gameManager = new GameManager(boardState);
        this.gameManager.addListener(this);
        this.interactionState = new InteractionState();
        this.inputHandler = new InputHandler(this);
    }

    public BoardState getBoardState() {
        return boardState;
    }

    public void registerView(ChessBoardView view) {
        this.view = view;
    }

    public void handlePieceSelection(int square) {
        int piece = boardState.getPieceAt(square);
        if (piece != Piece.NONE && Piece.isWhite(piece) == boardState.isWhiteToMove()) {
            interactionState.setSelectedSquare(square);
            interactionState.clearCurrentLegalMoves();

            // Get legal moves for the selected piece using new move generator
            MoveGenerator.MoveList moveList = MoveGenerator.generateAllMoves(boardState);
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

        if (move != -1) {
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
                .orElse(-1);
    }

    public void completePromotion(int promotionPiece) {
        int pendingPromotionMove = interactionState.getPendingPromotionMove();
        if (pendingPromotionMove == -1) return;
        int updatedPromotionMove = Move.updatePromotionPiece(pendingPromotionMove, promotionPiece);
        if (interactionState.getCurrentLegalMoves().contains(updatedPromotionMove)) {
            resetSelection();
            gameManager.executeMove(updatedPromotionMove);
        } else {
            this.getInputHandler().restoreSourcePiece();
        }

        interactionState.setPendingPromotionMove(-1);
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
        if (move != -1) {
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
        gameManager.resetStateManager();
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
    }

    public void setGameMode(GameMode mode, EngineModel whiteModel, EngineModel blackModel, boolean modelColor) {
        gameManager.setGameMode(mode, whiteModel, blackModel, modelColor);
    }
}
