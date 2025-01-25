package tn.zeros.zchess.core.logic.validation;

import tn.zeros.zchess.core.util.Directions;

public class QueenValidator extends SlidingPieceValidator {
    @Override
    protected int[] getDirections() {
        return Directions.QUEEN;
    }

    @Override
    protected String getErrorMessage() {
        return "Queen must move straight/diagonally and path must be clear";
    }
}