package tn.zeros.zchess.core.logic.validation;

import tn.zeros.zchess.core.util.Directions;

public class BishopValidator extends SlidingPieceValidator {
    @Override
    protected int[] getDirections() {
        return Directions.BISHOP;
    }

    @Override
    protected String getErrorMessage() {
        return "Bishop must move diagonally and path must be clear";
    }
}