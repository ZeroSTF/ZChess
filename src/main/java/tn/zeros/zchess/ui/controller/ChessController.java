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

    public void handlePieceSelection(int square) {
        int piece = boardState.getPieceAt(square);
        if (piece != Piece.NONE && Piece.isWhite(piece) == boardState.isWhiteToMove()) {
            interactionState.setSelectedSquare(square);
            interactionState.clearCurrentLegalMoves();
            generateLegalMoves(square, piece);
            int kingInCheck = LegalMoveFilter.getKingInCheckSquare(boardState, boardState.isWhiteToMove());
            view.updateHighlights(interactionState.getCurrentLegalMoves(), kingInCheck);
        } else {
            resetSelection();
        }
    }

    private void generateLegalMoves(int square, int piece) {
        // Get thread-local MoveList and reuse it
        MoveGenerator.MoveList moveList = MoveGenerator.getThreadLocalMoveList();
        moveList.clear();

        getPseudoLegalMoves(square, piece, moveList);
        interactionState.getCurrentLegalMoves().clear();
        interactionState.getCurrentLegalMoves().addAll(
                LegalMoveFilter.filterLegalMoves(boardState, moveList.toList())
        );
    }

    private void getPseudoLegalMoves(int square, int piece, MoveGenerator.MoveList moveList) {
        if (Piece.isPawn(piece)) PawnMoveGenerator.generate(boardState, square, moveList);
        else if (Piece.isKnight(piece)) KnightMoveGenerator.generate(boardState, square, moveList);
        else if (Piece.isBishop(piece)) BishopMoveGenerator.generate(boardState, square, moveList);
        else if (Piece.isRook(piece)) RookMoveGenerator.generate(boardState, square, moveList);
        else if (Piece.isQueen(piece)) QueenMoveGenerator.generate(boardState, square, moveList);
        else if (Piece.isKing(piece)) KingMoveGenerator.generate(boardState, square, moveList);
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
            view.showPromotionDialog(Piece.isWhite(move.piece()));
        } else {
            commitMove(move);
        }
    }

    public void completePromotion(int promotionPiece) {
        if (interactionState.getPendingPromotionMove() == null) return;

        // Find the exact promotion move from legal options
        interactionState.getCurrentLegalMoves().stream()
                .filter(m -> m.isPromotion() && m.promotionPiece() == promotionPiece)
                .findFirst().ifPresent(this::commitMove);

        interactionState.setPendingPromotionMove(null);
    }

    private void commitMove(Move move) {
        interactionState.setLastMoveFrom(move.fromSquare());
        interactionState.setLastMoveTo(move.toSquare());
        interactionState.setSelectedSquare(-1);

        MoveUndoInfo undoInfo = MoveExecutor.makeMove(boardState, move);
        stateManager.saveState(undoInfo);
        int kingInCheck = LegalMoveFilter.getKingInCheckSquare(boardState, boardState.isWhiteToMove());
        view.refreshEntireBoard();
        view.updateHighlights(Collections.emptyList(), kingInCheck);
        playMoveSound(move);
        stateManager.clearRedo();
    }

    private void resetSelection() {
        int kingInCheck = LegalMoveFilter.getKingInCheckSquare(boardState, boardState.isWhiteToMove());
        interactionState.setSelectedSquare(-1);
        view.updateHighlights(Collections.emptyList(), kingInCheck);
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

    public void undo() {
        Move move = stateManager.undo();
        if (move != null) {
            view.refreshEntireBoard();
            interactionState.setLastMoveFrom(move.fromSquare());
            interactionState.setLastMoveTo(move.toSquare());
            interactionState.setSelectedSquare(-1);
            int kingInCheck = LegalMoveFilter.getKingInCheckSquare(boardState, boardState.isWhiteToMove());
            view.updateHighlights(Collections.emptyList(), kingInCheck);
        }
    }

    public void redo() {
        Move move = stateManager.redo();
        if (move != null) {
            view.refreshEntireBoard();
            interactionState.setLastMoveFrom(move.fromSquare());
            interactionState.setLastMoveTo(move.toSquare());
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

    public void updateHighlights() {
        int kingInCheck = LegalMoveFilter.getKingInCheckSquare(boardState, boardState.isWhiteToMove());
        view.updateHighlights(interactionState.getCurrentLegalMoves(), kingInCheck);
    }

}
