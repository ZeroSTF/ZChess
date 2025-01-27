package tn.zeros.zchess.ui.view;

import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.ui.components.PromotionDialog;
import tn.zeros.zchess.ui.controller.ChessController;
import tn.zeros.zchess.ui.util.BoardGeometry;

import java.util.List;

import static tn.zeros.zchess.ui.util.UIConstants.FILES;
import static tn.zeros.zchess.ui.util.UIConstants.RANKS;

public class ChessBoardView extends GridPane implements ChessView {
    private final SquareView[][] squares = new SquareView[8][8];
    private final ChessController controller;
    private final PromotionDialog promotionDialog;

    public ChessBoardView(ChessController controller) {
        this.promotionDialog = new PromotionDialog(controller);
        this.controller = controller;
        this.controller.registerView(this);
        initializeBoard();
        refreshEntireBoard();
    }

    private void initializeBoard() {
        addRankLabels();
        addFileLabels();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                SquareView square = createSquare(row, col);
                squares[row][col] = square;
                add(square, col, 7 - row); // Flip for visual board
            }
        }
    }

    private SquareView createSquare(int row, int col) {
        Color color = BoardGeometry.getSquareColor(row, col);
        int squareIndex = BoardGeometry.toSquareIndex(row, col);
        return new SquareView(color, controller, squareIndex);
    }


    @Override
    public void showError(String message) {
        Tooltip errorTip = new Tooltip(message);
        errorTip.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
        errorTip.show(getScene().getWindow());
        PauseTransition pause = new PauseTransition(Duration.seconds(1));
        pause.setOnFinished(event -> errorTip.hide());
        pause.play();
    }

    public void clearHighlights() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                squares[row][col].highlight(false);
            }
        }
    }

    @Override
    public void refreshEntireBoard() {
        BoardState state = controller.getBoardState();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int square = row * 8 + col;
                squares[row][col].setPiece(state.getPieceAt(square));
                squares[row][col].highlight(false);
            }
        }
    }

    @Override
    public void showPromotionDialog(boolean isWhite) {
        promotionDialog.show(isWhite);
    }

    @Override
    public void updateHighlights(List<Integer> legalSquares) {
        clearHighlights();
        for (int square : legalSquares) {
            int row = square / 8;
            int col = square % 8;
            squares[row][col].highlight(true);
        }
    }

    private void addRankLabels() {
        for (int row = 0; row < 8; row++) {
            Label rightLabel = createLabel(RANKS[row]);
            add(rightLabel, 9, row);
        }
    }

    private void addFileLabels() {
        for (int col = 0; col < 8; col++) {
            Label fileLabel = createLabel(FILES[col]);
            add(fileLabel, col, 8);
            GridPane.setHalignment(fileLabel, javafx.geometry.HPos.CENTER);
        }
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        label.setPadding(new Insets(5));
        return label;
    }

}
