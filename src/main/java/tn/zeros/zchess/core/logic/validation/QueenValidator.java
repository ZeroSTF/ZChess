package tn.zeros.zchess.core.logic.validation;

public class QueenValidator extends SlidingPieceValidator {
    @Override
    protected int[] getDirections() {
        return new int[]{0, 1, 2, 3, 4, 5, 6, 7}; // All directions
    }

    @Override
    protected String getErrorMessage() {
        return "Queen must move straight/diagonally and path must be clear";
    }
}