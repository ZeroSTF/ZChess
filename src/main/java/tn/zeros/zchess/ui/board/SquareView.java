package tn.zeros.zchess.ui.board;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import tn.zeros.zchess.core.piece.Piece;

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
        if (currentPiece == null || currentPiece == Piece.NONE) {
            pieceText.setText("");
        } else {
            pieceText.setText(String.valueOf(currentPiece.getSymbol()));
            pieceText.setFill(currentPiece.isWhite() ? Color.BLACK : Color.WHITE);
        }
    }

    public void highlight(boolean isHighlighted) {
        background.setFill(isHighlighted ? Color.YELLOW : originalColor);
    }

    public int getRow() { return row; }
    public int getCol() { return col; }
    public Piece getPiece() { return currentPiece; }

    private void handleClick() {
        // Will be implemented for move handling
        System.out.println("Clicked square: " + row + ", " + col);
    }
}
