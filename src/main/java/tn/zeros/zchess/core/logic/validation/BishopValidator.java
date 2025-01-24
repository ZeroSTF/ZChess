package tn.zeros.zchess.core.logic.validation;

public class BishopValidator extends SlidingPieceValidator {
    @Override
    protected int[] getDirections() {
        return new int[]{1, 3, 5, 7}; // NE, SE, SW, NW
    }

    @Override
    protected String getErrorMessage() {
        return "Bishop must move diagonally and path must be clear";
    }
}