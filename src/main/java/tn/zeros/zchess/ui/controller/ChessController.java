package tn.zeros.zchess.ui.controller;

import tn.zeros.zchess.core.logic.validation.*;
import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.service.*;
import tn.zeros.zchess.ui.util.SoundManager;
import tn.zeros.zchess.ui.view.ChessBoardView;
import tn.zeros.zchess.ui.view.ChessView;

import java.util.ArrayList;
import java.util.List;

public class ChessController {
    private BoardState boardState;
    private StateManager stateManager;
    private final MoveValidator moveValidator;
    private ChessView view;
    private int selectedSquare = -1;
    private Move pendingPromotionMove;

    public ChessController() {
        this.boardState = new BoardState();
        this.stateManager = new StateManager(boardState);
        this.moveValidator = new CompositeMoveValidator();
    }

    public BoardState getBoardState() {
        return boardState;
    }

    public void registerView(ChessBoardView view) {
        this.view = view;
    }

    public void handleSquareClick(int square) {
        if (selectedSquare == -1) {
            handlePieceSelection(square);
        } else {
            handleMoveExecution(square);
        }
    }

    private void handlePieceSelection(int square) {
        Piece piece = boardState.getPieceAt(square);
        if (piece != Piece.NONE && piece.isWhite() == boardState.isWhiteToMove()) {
            selectedSquare = square;
            view.highlightLegalMoves(getLegalMoves(square));
        }
    }

    private void handleMoveExecution(int targetSquare) {
        Move move = createMove(selectedSquare, targetSquare);
        ValidationResult result = moveValidator.validate(boardState, move);

        if (result.isValid()) {
            if (move.isPromotion()) {
                pendingPromotionMove = move;
                view.showPromotionDialog(move.piece().isWhite());
            } else {
                commitMove(move);
            }
        }  else {
            view.showError(result.getMessage());
        }

        view.clearHighlights();
        selectedSquare = -1;
    }

    private List<Integer> getLegalMoves(int square) {
        List<Integer> legalMoves = new ArrayList<>();
        for (int target = 0; target < 64; target++) {
            Move testMove = createMove(square, target);
            if (moveValidator.validate(boardState, testMove).isValid()) {
                legalMoves.add(target);
            }
        }
        return legalMoves;
    }

    private Move createMove(int from, int to) {
        Piece piece = boardState.getPieceAt(from);
        Piece captured = boardState.getPieceAt(to);
        boolean isEnPassant = checkEnPassant(piece, to);
        boolean isCastling = checkCastling(piece, from, to);
        boolean isPromotion = piece.isPawn() &&
                ((piece.isWhite() && (to / 8 == 7)) ||
                        (!piece.isWhite() && (to / 8 == 0)));

        if (isEnPassant) {
            int capturedSquare = to + (piece.isWhite() ? -8 : 8);
            captured = boardState.getPieceAt(capturedSquare);
        }

        return new Move(
                from, to, piece, captured,
                isPromotion, isCastling, isEnPassant, isPromotion ? Piece.NONE : null
        );
    }

    public void completePromotion(Piece promotionPiece) {
        if (pendingPromotionMove == null) return;

        Move completedMove = new Move(
                pendingPromotionMove.fromSquare(),
                pendingPromotionMove.toSquare(),
                pendingPromotionMove.piece(),
                pendingPromotionMove.capturedPiece(),
                true,
                pendingPromotionMove.isCastling(),
                pendingPromotionMove.isEnPassant(),
                promotionPiece
        );

        if (moveValidator.validate(boardState, completedMove).isValid()) {
            commitMove(completedMove);
        }

        pendingPromotionMove = null;
    }

    private void commitMove(Move move) {
        playMoveSound(move);
        stateManager.saveState();
        MoveExecutor.executeMove(boardState, move);
        boardState.setWhiteToMove(!boardState.isWhiteToMove());
        view.updateBoard(move);
        stateManager.clearRedo();
    }

    private void playMoveSound(Move move) {
        boolean inCheck = ThreatDetectionService.isInCheck(boardState, boardState.isWhiteToMove());

        if (inCheck) {
            SoundManager.playMoveCheck();
        } else if (move.isCastling()) {
            SoundManager.playCastle();
        } else if (move.isPromotion()) {
            SoundManager.playPromotion();
        } else if (move.capturedPiece() != Piece.NONE){
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

}
