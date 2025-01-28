package tn.zeros.zchess.ui.controller;

import java.util.ArrayList;
import java.util.List;

public class InteractionState {
    private final List<Integer> currentLegalMoves = new ArrayList<>(32);
    private int selectedSquare = -1;
    private int pendingPromotionMove;
    private int lastMoveFrom = -1;
    private int lastMoveTo = -1;

    // Getters and setters
    public int getSelectedSquare() {
        return selectedSquare;
    }

    public void setSelectedSquare(int square) {
        this.selectedSquare = square;
    }

    public int getPendingPromotionMove() {
        return pendingPromotionMove;
    }

    public void setPendingPromotionMove(int move) {
        this.pendingPromotionMove = move;
    }

    public List<Integer> getCurrentLegalMoves() {
        return currentLegalMoves;
    }

    public void clearCurrentLegalMoves() {
        currentLegalMoves.clear();
    }

    public int getLastMoveFrom() {
        return lastMoveFrom;
    }

    public void setLastMoveFrom(int lastMoveFrom) {
        this.lastMoveFrom = lastMoveFrom;
    }

    public int getLastMoveTo() {
        return lastMoveTo;
    }

    public void setLastMoveTo(int lastMoveTo) {
        this.lastMoveTo = lastMoveTo;
    }
}