package tn.zeros.zchess.ui.controller;

import tn.zeros.zchess.core.logic.generation.*;
import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.service.FenService;
import tn.zeros.zchess.ui.matchmaker.GameManager;
import tn.zeros.zchess.ui.util.SoundManager;
import tn.zeros.zchess.ui.view.ChessBoardView;
import tn.zeros.zchess.ui.view.ChessView;

import java.util.Collections;

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
        interactionState.getCurrentLegalMoves().addAll(LegalMoveFilter.filterLegalMoves(boardState, moveList.toList()));
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
        int move = findMoveByTarget(targetSquare);

        if (move != -1) {
            if (Move.isPromotion(move)) {
                interactionState.setPendingPromotionMove(move);
                view.showPromotionDialog(Piece.isWhite(Move.getPiece(move)));
            } else {
                gameManager.executeMove(move);
            }
            resetSelection();
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
        if (move != -1) {
            view.refreshEntireBoard();
            interactionState.setLastMoveFrom(Move.getFrom(move));
            interactionState.setLastMoveTo(Move.getTo(move));
            interactionState.setSelectedSquare(-1);
            int kingInCheck = LegalMoveFilter.getKingInCheckSquare(boardState, boardState.isWhiteToMove());
            view.updateHighlights(Collections.emptyList(), kingInCheck);
        }
    }

    public void redo() {
        int move = gameManager.stateManager.redo();
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
            BoardState newState = FenService.parseFEN(fen);
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
}
