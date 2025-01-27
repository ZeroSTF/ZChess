package tn.zeros.zchess.ui.view;

import tn.zeros.zchess.core.model.Move;

import java.util.List;

public interface ChessView {
    void highlightLegalMoves(List<Integer> legalSquares);

    void showError(String message);

    void clearHighlights();

    void refreshEntireBoard();

    void showPromotionDialog(boolean isWhite);

    @Deprecated
    void updateBoard(Move move);
}