package tn.zeros.zchess.ui.controller;

import tn.zeros.zchess.core.logic.Generation.*;
import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.MoveUndoInfo;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.core.service.FenService;
import tn.zeros.zchess.core.service.MoveExecutor;
import tn.zeros.zchess.core.service.StateManager;
import tn.zeros.zchess.core.service.ThreatDetectionService;
import tn.zeros.zchess.ui.util.SoundManager;
import tn.zeros.zchess.ui.view.ChessBoardView;
import tn.zeros.zchess.ui.view.ChessView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ChessController {
    private BoardState boardState;
    private StateManager stateManager;
    private ChessView view;
    private int selectedSquare = -1;
    private Move pendingPromotionMove;
    private List<Move> currentLegalMoves = new ArrayList<>(32);

    public ChessController() {
        this.boardState = new BoardState();
        this.stateManager = new StateManager(boardState);
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
            currentLegalMoves.clear();
            generateLegalMoves(square, piece);
            view.highlightLegalMoves(extractTargetSquares());
        }
    }

    private void generateLegalMoves(int square, Piece piece) {
        List<Move> pseudoLegal = getPseudoLegalMoves(square, piece);
        currentLegalMoves = LegalMoveFilter.filterLegalMoves(boardState, pseudoLegal);
    }

    private List<Move> getPseudoLegalMoves(int square, Piece piece) {
        if (piece.isPawn()) return PawnMoveGenerator.generate(boardState, square);
        if (piece.isKnight()) return KnightMoveGenerator.generate(boardState, square);
        if (piece.isBishop()) return BishopMoveGenerator.generate(boardState, square);
        if (piece.isRook()) return RookMoveGenerator.generate(boardState, square);
        if (piece.isQueen()) return QueenMoveGenerator.generate(boardState, square);
        if (piece.isKing()) return KingMoveGenerator.generate(boardState, square);
        return List.of();
    }

    private List<Integer> extractTargetSquares() {
        return currentLegalMoves.stream()
                .map(Move::toSquare)
                .collect(Collectors.toList());
    }

    private void handleMoveExecution(int targetSquare) {
        Move move = findMoveByTarget(targetSquare);

        if (move != null) {
            handleMove(move);
        } else {
            view.showError("Illegal move");
        }
        resetSelection();
    }

    private Move findMoveByTarget(int targetSquare) {
        return currentLegalMoves.stream()
                .filter(m -> m.toSquare() == targetSquare)
                .findFirst()
                .orElse(null);
    }

    private void handleMove(Move move) {
        if (move.isPromotion()) {
            pendingPromotionMove = move;
            view.showPromotionDialog(move.piece().isWhite());
        } else {
            commitMove(move);
        }
    }

    public void completePromotion(Piece promotionPiece) {
        if (pendingPromotionMove == null) return;

        // Find the exact promotion move from legal options
        currentLegalMoves.stream()
                .filter(m -> m.isPromotion() && m.promotionPiece() == promotionPiece)
                .findFirst().ifPresent(this::commitMove);

        pendingPromotionMove = null;
    }

    private void commitMove(Move move) {
        MoveUndoInfo undoInfo = MoveExecutor.makeMove(boardState, move);
        stateManager.saveState(undoInfo);
        view.refreshEntireBoard();
        playMoveSound(move);
        stateManager.clearRedo();
    }

    private void resetSelection() {
        view.clearHighlights();
        selectedSquare = -1;
        currentLegalMoves.clear();
    }

    private void playMoveSound(Move move) {
        boolean inCheck = ThreatDetectionService.isInCheck(boardState, boardState.isWhiteToMove());

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

}
