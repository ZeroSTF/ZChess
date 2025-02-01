package tn.zeros.zchess.ui.util;

import javafx.scene.paint.Color;

public class BoardGeometry {
    public static Color getSquareColor(int row, int col) {
        boolean isLight = (row + col) % 2 != 0;
        return isLight ?
                UIConstants.LIGHT_SQUARE_COLOR :
                UIConstants.DARK_SQUARE_COLOR;
    }

    public static int toSquareIndex(int row, int col) {
        return row * 8 + col;
    }

    public static int[] fromSquareIndex(int square) {
        return new int[]{square >> 3, square & 7};
    }
}