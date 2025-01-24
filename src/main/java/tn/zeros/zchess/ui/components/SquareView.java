package tn.zeros.zchess.ui.components;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import tn.zeros.zchess.core.model.Piece;

import java.util.Objects;

public class SquareView extends StackPane {
    private final Rectangle background;
    private final Text pieceText;
    private final int row;
    private final int col;
    private Piece currentPiece;
    private final Color originalColor;

    public SquareView(int row, int col, Color color) {
        this.row = row;
        this.col = col;
        this.originalColor = color;

        background = new Rectangle(80, 80, color);
        pieceText = new Text();
        pieceText.setFont(Font.font("Arial", 40));

        getChildren().addAll(background, pieceText);

        setOnMouseClicked(e -> handleClick());
    }

    public void setPiece(Piece piece) {
        this.currentPiece = piece;
        updatePieceDisplay();
    }

    private void updatePieceDisplay() {
        getChildren().removeIf(node -> node instanceof ImageView);
        if (currentPiece != null && currentPiece != Piece.NONE) {
            try {
                Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream(
                        "/pieces/" + currentPiece.name() + ".png")));
                ImageView imageView = new ImageView(image);
                imageView.setFitWidth(60);
                imageView.setFitHeight(60);
                getChildren().add(imageView);
            } catch (Exception e) {
                pieceText.setText(String.valueOf(currentPiece.getSymbol()));
                pieceText.setFill(currentPiece.isWhite() ? Color.BLACK : Color.WHITE);
            }
        }
    }

    public void highlight(boolean isHighlighted) {
        background.setFill(isHighlighted ? Color.YELLOW : originalColor);
    }

    public int getRow() { return row; }
    public int getCol() { return col; }
    public Piece getPiece() { return currentPiece; }

    private void handleClick() {
    }
}
