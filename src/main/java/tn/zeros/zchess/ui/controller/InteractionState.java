package tn.zeros.zchess.ui.controller;

import tn.zeros.zchess.core.model.Move;

import java.util.ArrayList;
import java.util.List;

public class InteractionState {
    private final List<Move> currentLegalMoves = new ArrayList<>(32);
    private int selectedSquare = -1;
    private Move pendingPromotionMove;

    // Getters and setters
    public int getSelectedSquare() {
        return selectedSquare;
    }

    public void setSelectedSquare(int square) {
        this.selectedSquare = square;
    }

    public Move getPendingPromotionMove() {
        return pendingPromotionMove;
    }

    public void setPendingPromotionMove(Move move) {
        this.pendingPromotionMove = move;
    }

    public List<Move> getCurrentLegalMoves() {
        return currentLegalMoves;
    }

    public void clearCurrentLegalMoves() {
        currentLegalMoves.clear();
    }
}