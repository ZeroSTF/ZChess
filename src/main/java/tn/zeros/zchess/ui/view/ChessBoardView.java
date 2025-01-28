package tn.zeros.zchess.ui.view;

import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.ui.components.PromotionDialog;
import tn.zeros.zchess.ui.controller.ChessController;
import tn.zeros.zchess.ui.util.BoardGeometry;
import tn.zeros.zchess.ui.util.UIConstants;

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

    @Override
    public void refreshEntireBoard() {
        BoardState state = controller.getBoardState();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int square = row * 8 + col;
                squares[row][col].setPiece(state.getPieceAt(square));
            }
        }
    }

    @Override
    public void showPromotionDialog(boolean isWhite) {
        promotionDialog.show(isWhite);
    }

    @Override
    public void updateHighlights(List<Move> legalMoves, int kingInCheckSquare) {
        clearHighlights();
        highlightLastMove();
        highlightSelectedSquare();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                squares[row][col].setCheckOverlay(false);
            }
        }

        // Process legal moves
        for (Move move : legalMoves) {
            int row = move.toSquare() / 8;
            int col = move.toSquare() % 8;
            boolean isCapture = move.capturedPiece() != Piece.NONE;

            squares[row][col].setLegalMoveOverlay(true, isCapture);
        }

        // Set check highlight
        if (kingInCheckSquare != -1) {
            int row = kingInCheckSquare / 8;
            int col = kingInCheckSquare % 8;
            squares[row][col].setCheckOverlay(true);
        }
    }

    private void highlightLastMove() {
        int lastMoveFrom = controller.getInteractionState().getLastMoveFrom();
        int lastMoveTo = controller.getInteractionState().getLastMoveTo();

        if (lastMoveFrom != -1) {
            squares[lastMoveFrom / 8][lastMoveFrom % 8]
                    .setLastMove(true);
        }
        if (lastMoveTo != -1) {
            squares[lastMoveTo / 8][lastMoveTo % 8]
                    .setLastMove(true);
        }
    }

    private void highlightSelectedSquare() {
        int selectedSquare = controller.getInteractionState().getSelectedSquare();
        if (selectedSquare != -1) {
            squares[selectedSquare / 8][selectedSquare % 8].setSelected(true);
        }
    }

    public void clearHighlights() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                squares[row][col].setLegalMoveOverlay(false, false);
                squares[row][col].setSelected(false);
                squares[row][col].setLastMove(false);
            }
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

    public void addDragImage(ImageView image) {
        getChildren().add(image);
    }

    public void removeDragImage(ImageView image) {
        getChildren().remove(image);
    }

    @Override
    public Node getSquareNode(int square) {
        int row = square / 8;
        int col = square % 8;
        return squares[row][col];
    }

    public int getSquareFromSceneCoordinates(double sceneX, double sceneY) {
        Point2D boardPoint = sceneToLocal(sceneX, sceneY);
        int col = (int) (boardPoint.getX() / UIConstants.SQUARE_SIZE);
        int row = 7 - (int) (boardPoint.getY() / UIConstants.SQUARE_SIZE);

        if (row >= 0 && row < 8 && col >= 0 && col < 8) {
            return row * 8 + col;
        }
        return -1;
    }
}
