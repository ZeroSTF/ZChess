package tn.zeros.zchess.ui.view;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.ui.controller.ChessController;
import tn.zeros.zchess.ui.util.AssetLoader;
import tn.zeros.zchess.ui.util.UIConstants;

public class SquareView extends StackPane {
    private final Rectangle background;
    private final Color originalColor;
    private Piece currentPiece;

    public SquareView(Color color, ChessController controller, int squareIndex) {
        setOnMousePressed(e -> {
            controller.handleSquareInteraction(squareIndex);
            controller.getInputHandler().handleDrag(e.getSceneX(), e.getSceneY());
        });

        setOnMouseDragged(e -> controller.getInputHandler().handleDrag(e.getSceneX(), e.getSceneY()));

        setOnMouseReleased(e -> controller.getInputHandler().handleRelease(e.getSceneX(), e.getSceneY()));

        this.originalColor = color;
        background = new Rectangle(
                UIConstants.SQUARE_SIZE,
                UIConstants.SQUARE_SIZE,
                color
        );
        getChildren().add(background);
    }

    private void refreshDisplay() {
        getChildren().removeIf(node ->
                node instanceof ImageView || node instanceof Text
        );

        if (currentPiece != Piece.NONE) {
            Image image = AssetLoader.getPieceImage(currentPiece);
            if (image != null) {
                addPieceImage(image);
            } else {
                addFallbackText();
            }
        }
    }

    private void addPieceImage(Image image) {
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(UIConstants.SQUARE_SIZE - 10);
        imageView.setFitHeight(UIConstants.SQUARE_SIZE - 10);
        imageView.setSmooth(true);
        imageView.setPreserveRatio(true);
        imageView.setCache(true);
        getChildren().add(imageView);
    }

    private void addFallbackText() {
        Text text = new Text(String.valueOf(currentPiece.getSymbol()));
        text.setFont(UIConstants.PIECE_FONT);
        getChildren().add(text);
    }

    public void highlight(boolean highlight) {
        background.setFill(highlight ?
                UIConstants.HIGHLIGHT_COLOR :
                originalColor
        );
    }

    public Piece getPiece() {
        return currentPiece;
    }

    public void setPiece(Piece piece) {
        currentPiece = piece;
        refreshDisplay();
    }

    public void hideCurrentPiece() {
        getChildren().stream()
                .filter(node -> node instanceof ImageView || node instanceof Text)
                .forEach(node -> node.setVisible(false));
    }

    public void showCurrentPiece() {
        getChildren().stream()
                .filter(node -> node instanceof ImageView || node instanceof Text)
                .forEach(node -> node.setVisible(true));
    }
}
