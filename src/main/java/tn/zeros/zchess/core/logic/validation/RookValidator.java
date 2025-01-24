package tn.zeros.zchess.core.logic.validation;

public class RookValidator extends SlidingPieceValidator {
    @Override
    protected int[] getDirections() {
        return new int[]{0, 2, 4, 6}; // N, E, S, W
    }

    @Override
    protected String getErrorMessage() {
        return "Rook must move straight and path must be clear";
    }
}