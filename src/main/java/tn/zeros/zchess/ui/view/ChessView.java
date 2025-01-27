package tn.zeros.zchess.ui.view;

import javafx.scene.Node;
import tn.zeros.zchess.core.model.Move;

import java.util.List;

public interface ChessView {
    void showError(String message);

    void clearHighlights();

    void refreshEntireBoard();

    void showPromotionDialog(boolean isWhite);

    void updateHighlights(List<Move> legalMoves, int kingInCheckSquare);

    Node getSquareNode(int square);
}