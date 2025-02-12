package tn.zeros.zchess.ui.view;

import javafx.animation.PauseTransition;
import javafx.geometry.Bounds;
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
import tn.zeros.zchess.ui.components.BitboardOverlay;
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
    private final BitboardOverlay bitboardOverlay;
    private SquareView lastHoveredSquare = null;
    private boolean isFlipped = false;

    public ChessBoardView(ChessController controller) {
        this.promotionDialog = new PromotionDialog(controller);
        this.controller = controller;
        this.controller.registerView(this);
        this.bitboardOverlay = new BitboardOverlay();
        initializeBoard();
        addBitboardOverlay();
        refreshEntireBoard();
    }

    private void initializeBoard() {
        getChildren().clear();
        addRankLabels();
        addFileLabels();

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                SquareView square = createSquare(row, col);
                squares[row][col] = square;
                int displayRow = isFlipped ? row : 7 - row;
                add(square, col, displayRow);
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
        clearHighlights();
    }

    @Override
    public void showPromotionDialog(boolean isWhite) {
        promotionDialog.show(isWhite);
    }

    @Override
    public void updateHighlights(List<Integer> legalMoves, int kingInCheckSquare) {
        clearHighlights();
        highlightLastMove();
        highlightSelectedSquare();

        // Remove check overlays
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                squares[row][col].setCheckOverlay(false);
            }
        }

        // Set legal moves overlay
        for (int move : legalMoves) {
            int to = Move.getTo(move);
            int row = to >> 3;
            int col = to & 7;
            boolean isCapture = Move.getCapturedPiece(move) != Piece.NONE;

            squares[row][col].setLegalMoveOverlay(true, isCapture);
        }

        // Set check overlay
        if (kingInCheckSquare != -1) {
            int row = kingInCheckSquare >> 3;
            int col = kingInCheckSquare & 7;
            squares[row][col].setCheckOverlay(true);
        }

        // Update bitboard overlay
        /*BoardState state = controller.getBoardState();
        if (controller.getInteractionState().getSelectedSquare() != -1) {
            updateBitboardOverlay(LegalMoveFilter.getAttackersBitboard(state, controller.getInteractionState().getSelectedSquare(), !state.isWhiteToMove()));
        } else {
            clearBitboardOverlay();
        }*/
    }

    private void highlightLastMove() {
        int lastMoveFrom = controller.getInteractionState().getLastMoveFrom();
        int lastMoveTo = controller.getInteractionState().getLastMoveTo();

        if (lastMoveFrom != -1) {
            squares[lastMoveFrom >> 3][lastMoveFrom & 7]
                    .setLastMove(true);
        }
        if (lastMoveTo != -1) {
            squares[lastMoveTo >> 3][lastMoveTo & 7]
                    .setLastMove(true);
        }
    }

    private void highlightSelectedSquare() {
        int selectedSquare = controller.getInteractionState().getSelectedSquare();
        if (selectedSquare != -1) {
            squares[selectedSquare >> 3][selectedSquare & 7].setSelected(true);
        }
    }

    public void clearHighlights() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                squares[row][col].setLegalMoveOverlay(false, false);
                squares[row][col].setSelected(false);
                squares[row][col].setLastMove(false);
                squares[row][col].setCheckOverlay(false);
            }
        }
    }

    private void addRankLabels() {
        for (int row = 0; row < 8; row++) {
            Label rightLabel = createLabel(RANKS[row]);
            add(rightLabel, 9, isFlipped ? 7 - row : row);
        }
    }

    private void addFileLabels() {
        for (int col = 0; col < 8; col++) {
            Label fileLabel = createLabel(FILES[col]);
            add(fileLabel, isFlipped ? 7 - col : col, 8);
            GridPane.setHalignment(fileLabel, javafx.geometry.HPos.CENTER);
        }
    }

    private Label createLabel(String text) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 14; -fx-font-weight: bold; -fx-text-fill: #000000;");
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
        int row = square >> 3;
        int col = square & 7;
        return squares[row][col];
    }

    public int getSquareFromSceneCoordinates(double sceneX, double sceneY) {
        Point2D boardPoint = sceneToLocal(sceneX, sceneY);
        int displayCol = (int) (boardPoint.getX() / UIConstants.SQUARE_SIZE);
        int displayRow = (int) (boardPoint.getY() / UIConstants.SQUARE_SIZE);

        // Convert to logical coordinates
        int logicalRow = isFlipped ? displayRow : 7 - displayRow;
        int logicalCol = isFlipped ? 7 - displayCol : displayCol;

        return logicalRow * 8 + logicalCol;
    }

    public void flipBoard() {
        isFlipped = !isFlipped;
        refreshEntireBoard();
        rebuildLabels();
        rearrangeSquares();
    }

    private void rebuildLabels() {
        // Clear existing labels
        getChildren().removeIf(node -> node instanceof Label);
        addRankLabels();
        addFileLabels();
    }

    private void rearrangeSquares() {
        getChildren().removeIf(node -> node instanceof SquareView);
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int displayRow = isFlipped ? row : 7 - row;
                int displayCol = isFlipped ? 7 - col : col;

                add(squares[row][col], displayCol, displayRow);
            }
        }
    }

    public void handleDragHover(double x, double y) {
        if (lastHoveredSquare != null) {
            lastHoveredSquare.handleHover(false);
        }

        Point2D boardPoint = sceneToLocal(x, y);
        for (Node node : getChildren()) {
            if (node instanceof SquareView square) {
                Bounds bounds = square.getBoundsInParent();
                if (bounds.contains(boardPoint)) {
                    square.handleHover(true);
                    lastHoveredSquare = square;
                    return;
                }
            }
        }
    }

    private void addBitboardOverlay() {
        add(bitboardOverlay, 0, 0, 8, 8);
    }

    public void updateBitboardOverlay(long bitboard) {
        bitboardOverlay.updateBitboard(bitboard);
    }

    public void clearBitboardOverlay() {
        bitboardOverlay.updateBitboard(0L);
    }
}
