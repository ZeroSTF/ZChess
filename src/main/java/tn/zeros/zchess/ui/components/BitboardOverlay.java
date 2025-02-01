package tn.zeros.zchess.ui.components;

import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import tn.zeros.zchess.ui.util.UIConstants;

public class BitboardOverlay extends GridPane {
    private static final Color HIGHLIGHT_COLOR = Color.rgb(53, 139, 159, 0.8); // Light blue with transparency
    private final Rectangle[][] overlaySquares = new Rectangle[8][8];

    public BitboardOverlay() {
        setMouseTransparent(true);
        initialize();
    }

    private void initialize() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Rectangle rect = createOverlayRectangle();
                overlaySquares[row][col] = rect;
                add(rect, col, row);
            }
        }
        configureGridConstraints();
    }

    private Rectangle createOverlayRectangle() {
        Rectangle rect = new Rectangle(UIConstants.SQUARE_SIZE, UIConstants.SQUARE_SIZE);
        rect.setFill(Color.TRANSPARENT);
        rect.setMouseTransparent(true); // Allow mouse events to pass through
        return rect;
    }

    private void configureGridConstraints() {
        for (int i = 0; i < 8; i++) {
            getColumnConstraints().add(createSquareColumnConstraints());
            getRowConstraints().add(createSquareRowConstraints());
        }
    }

    public void updateBitboard(long bitboard) {
        for (int s = 0; s < 64; s++) {
            int overlayRow = 7 - (s >> 3);
            int overlayCol = s & 7;
            Rectangle rect = overlaySquares[overlayRow][overlayCol];
            boolean isSet = (bitboard & (1L << s)) != 0;
            rect.setFill(isSet ? HIGHLIGHT_COLOR : Color.TRANSPARENT);
        }
    }

    private ColumnConstraints createSquareColumnConstraints() {
        return new ColumnConstraints(UIConstants.SQUARE_SIZE, UIConstants.SQUARE_SIZE, UIConstants.SQUARE_SIZE);
    }

    private RowConstraints createSquareRowConstraints() {
        return new RowConstraints(UIConstants.SQUARE_SIZE, UIConstants.SQUARE_SIZE, UIConstants.SQUARE_SIZE);
    }
}