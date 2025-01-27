package tn.zeros.zchess.ui.view;

import java.util.List;

public interface ChessView {
    void showError(String message);

    void clearHighlights();

    void refreshEntireBoard();

    void showPromotionDialog(boolean isWhite);

    void updateHighlights(List<Integer> legalSquares);
}