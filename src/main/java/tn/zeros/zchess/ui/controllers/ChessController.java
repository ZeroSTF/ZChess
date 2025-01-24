package tn.zeros.zchess.ui.controllers;

import tn.zeros.zchess.core.logic.validation.MoveValidator;
import tn.zeros.zchess.core.logic.validation.*;
import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.service.FenService;
import tn.zeros.zchess.core.service.MoveExecutor;
import tn.zeros.zchess.core.service.StateManager;
import tn.zeros.zchess.ui.view.ChessBoardView;

import java.util.ArrayList;
import java.util.List;

public class ChessController {
    private final BoardState boardState;
    private final StateManager stateManager;
    private final MoveValidator moveValidator;
    private ChessBoardView view;
    private int selectedSquare = -1;

    public ChessController(BoardState boardState) {
        this.boardState = boardState;
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
            executeMove(move);
            view.updateBoard(move);
        } else {
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

        if (isEnPassant) {
            int capturedSquare = to + (piece.isWhite() ? -8 : 8);
            captured = boardState.getPieceAt(capturedSquare);
        }

        return new Move(
                from, to, piece, captured,
                false, isCastling, isEnPassant, null
        );
    }

    private void executeMove(Move move) {
        stateManager.saveState();
        MoveExecutor.executeMove(boardState, move);
        boardState.setWhiteToMove(!boardState.isWhiteToMove());
    }

    private boolean checkEnPassant(Piece piece, int toSquare) {
        return piece.isPawn() && toSquare == boardState.getEnPassantSquare();
    }

    private boolean checkCastling(Piece piece, int from, int to) {
        return piece.isKing() && Math.abs(from - to) == 2;
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

}
