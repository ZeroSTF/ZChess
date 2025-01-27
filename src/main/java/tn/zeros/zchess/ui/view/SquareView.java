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
            controller.getInputHandler().handlePress(squareIndex);
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

    public void highlightWithColor(boolean highlight, Color overlayColor) {
        if (highlight) {
            Color blendedColor = blendColors(originalColor, overlayColor);
            background.setFill(blendedColor);
        } else {
            background.setFill(originalColor);
        }
    }

    private Color blendColors(Color base, Color overlay) {
        double overlayAlpha = overlay.getOpacity();
        // Calculate blended color components
        double r = (1 - overlayAlpha) * base.getRed() + overlayAlpha * overlay.getRed();
        double g = (1 - overlayAlpha) * base.getGreen() + overlayAlpha * overlay.getGreen();
        double b = (1 - overlayAlpha) * base.getBlue() + overlayAlpha * overlay.getBlue();
        return new Color(r, g, b, 1.0);
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
