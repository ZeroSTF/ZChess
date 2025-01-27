package tn.zeros.zchess.ui.controller;

import tn.zeros.zchess.core.logic.generation.*;
import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.MoveUndoInfo;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.service.FenService;
import tn.zeros.zchess.core.service.MoveExecutor;
import tn.zeros.zchess.core.service.StateManager;
import tn.zeros.zchess.ui.util.SoundManager;
import tn.zeros.zchess.ui.view.ChessBoardView;
import tn.zeros.zchess.ui.view.ChessView;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ChessController {
    private final InteractionState interactionState;
    private final InputHandler inputHandler;
    private BoardState boardState;
    private StateManager stateManager;
    private ChessView view;

    public ChessController() {
        this.boardState = new BoardState();
        this.stateManager = new StateManager(boardState);
        this.interactionState = new InteractionState();
        this.inputHandler = new InputHandler(this);
    }

    public BoardState getBoardState() {
        return boardState;
    }

    public void registerView(ChessBoardView view) {
        this.view = view;
    }

    public void handleSquareInteraction(int square) {
        inputHandler.handlePress(square);
    }

    public void handlePieceSelection(int square) {
        Piece piece = boardState.getPieceAt(square);
        if (piece != Piece.NONE && piece.isWhite() == boardState.isWhiteToMove()) {
            interactionState.setSelectedSquare(square);
            interactionState.clearCurrentLegalMoves();
            generateLegalMoves(square, piece);
            view.updateHighlights(extractTargetSquares());
        } else {
            resetSelection();
        }
    }

    private void generateLegalMoves(int square, Piece piece) {
        // Get thread-local MoveList and reuse it
        MoveGenerator.MoveList moveList = MoveGenerator.getThreadLocalMoveList();
        moveList.clear();

        getPseudoLegalMoves(square, piece, moveList);
        interactionState.getCurrentLegalMoves().clear();
        interactionState.getCurrentLegalMoves().addAll(
                LegalMoveFilter.filterLegalMoves(boardState, moveList.toList())
        );
    }

    private void getPseudoLegalMoves(int square, Piece piece, MoveGenerator.MoveList moveList) {
        if (piece.isPawn()) PawnMoveGenerator.generate(boardState, square, moveList);
        else if (piece.isKnight()) KnightMoveGenerator.generate(boardState, square, moveList);
        else if (piece.isBishop()) BishopMoveGenerator.generate(boardState, square, moveList);
        else if (piece.isRook()) RookMoveGenerator.generate(boardState, square, moveList);
        else if (piece.isQueen()) QueenMoveGenerator.generate(boardState, square, moveList);
        else if (piece.isKing()) KingMoveGenerator.generate(boardState, square, moveList);
    }

    private List<Integer> extractTargetSquares() {
        return interactionState.getCurrentLegalMoves().stream()
                .map(Move::toSquare)
                .collect(Collectors.toList());
    }

    public void handleMoveExecution(int targetSquare) {
        Move move = findMoveByTarget(targetSquare);

        if (move != null) {
            handleMove(move);
            resetSelection();
        } else {
            handlePieceSelection(targetSquare);
        }

    }

    private Move findMoveByTarget(int targetSquare) {
        return interactionState.getCurrentLegalMoves().stream()
                .filter(m -> m.toSquare() == targetSquare)
                .findFirst()
                .orElse(null);
    }

    void handleMove(Move move) {
        if (move.isPromotion()) {
            interactionState.setPendingPromotionMove(move);
            view.showPromotionDialog(move.piece().isWhite());
        } else {
            commitMove(move);
        }
    }

    public void completePromotion(Piece promotionPiece) {
        if (interactionState.getPendingPromotionMove() == null) return;

        // Find the exact promotion move from legal options
        interactionState.getCurrentLegalMoves().stream()
                .filter(m -> m.isPromotion() && m.promotionPiece() == promotionPiece)
                .findFirst().ifPresent(this::commitMove);

        interactionState.setPendingPromotionMove(null);
    }

    private void commitMove(Move move) {
        MoveUndoInfo undoInfo = MoveExecutor.makeMove(boardState, move);
        stateManager.saveState(undoInfo);
        view.refreshEntireBoard();
        playMoveSound(move);
        stateManager.clearRedo();
    }

    private void resetSelection() {
        view.updateHighlights(Collections.emptyList());
        interactionState.setSelectedSquare(-1);
        interactionState.clearCurrentLegalMoves();
    }

    private void playMoveSound(Move move) {
        boolean inCheck = LegalMoveFilter.inCheck(boardState, boardState.isWhiteToMove());

        if (inCheck) {
            SoundManager.playMoveCheck();
        } else if (move.isCastling()) {
            SoundManager.playCastle();
        } else if (move.isPromotion()) {
            SoundManager.playPromotion();
        } else if (move.capturedPiece() != Piece.NONE) {
            SoundManager.playCapture();
        } else {
            SoundManager.playMove();
        }
    }

    private boolean checkEnPassant(Piece piece, int toSquare) {
        return piece.isPawn() && toSquare == boardState.getEnPassantSquare();
    }

    private boolean checkCastling(Piece piece, int from, int to) {
        if (!piece.isKing()) return false;
        int delta = Math.abs(from - to);
        return delta == 2 && (from == 4 || from == 60);
    }

    public void undo() {
        if (stateManager.undo()) {
            view.refreshEntireBoard();
        }
    }

    public void redo() {
        if (stateManager.redo()) {
            view.refreshEntireBoard();
        }
    }

    public String getCurrentFEN() {
        return FenService.generateFEN(boardState);
    }

    public void loadFEN(String fen) {
        try {
            BoardState newState = FenService.parseFEN(fen);
            resetState(newState);
            view.refreshEntireBoard();
        } catch (IllegalArgumentException e) {
            view.showError("Invalid FEN: " + e.getMessage());
        }
    }

    private void resetState(BoardState newState) {
        this.boardState = newState;
        this.stateManager = new StateManager(newState);
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

}
