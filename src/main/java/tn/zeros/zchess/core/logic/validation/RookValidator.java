package tn.zeros.zchess.core.logic.validation;

import tn.zeros.zchess.core.util.Directions;

public class RookValidator extends SlidingPieceValidator {
    @Override
    protected int[] getDirections() {
        return Directions.ROOK;
    }

    @Override
    protected String getErrorMessage() {
        return "Rook must move straight and path must be clear";
    }
}