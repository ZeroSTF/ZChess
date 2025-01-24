package tn.zeros.zchess.ui.view;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.ui.util.AssetLoader;
import tn.zeros.zchess.ui.util.UIConstants;

public class SquareView extends StackPane {
    private final Rectangle background;
    private final Color originalColor;
    private Piece currentPiece;

    public SquareView(Color color) {
        this.originalColor = color;
        background = new Rectangle(
                UIConstants.SQUARE_SIZE,
                UIConstants.SQUARE_SIZE,
                color
        );
        getChildren().add(background);
    }

    public void setPiece(Piece piece) {
        currentPiece = piece;
        refreshDisplay();
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

    public Piece getPiece() { return currentPiece; }

}
