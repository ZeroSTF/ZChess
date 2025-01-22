package tn.zeros.zchess.gui.views;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import tn.zeros.zchess.core.board.ChessBoard;

public class ChessBoardView extends Pane {
    private static final int SQUARE_SIZE = 60;
    private final ChessBoard board;


    public ChessBoardView(ChessBoard board) {
        this.board = board;
        initializeBoard();
        updateBoard();
    }

    private void initializeBoard() {
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                Rectangle square = new Rectangle(SQUARE_SIZE, SQUARE_SIZE);
                square.setFill((rank + file) % 2 == 0 ? Color.WHITE : Color.GRAY);
                square.setX(file * SQUARE_SIZE);
                square.setY(rank * SQUARE_SIZE);
                getChildren().add(square);
            }
        }
    }

    public void updateBoard() {
        // Clear existing pieces
        getChildren().removeIf(node -> node instanceof Text);

        // Add new pieces
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                int square = rank * 8 + file;
                int piece = board.getPieceAt(square);
                if (piece != -1) {
                    Text pieceText = new Text(getPieceSymbol(piece, board.getColorAt(square)));
                    pieceText.setX(file * SQUARE_SIZE + SQUARE_SIZE/4);
                    pieceText.setY(rank * SQUARE_SIZE + SQUARE_SIZE*3/4);
                    pieceText.setStyle("-fx-font-size: " + (SQUARE_SIZE/2) + "px;");
                    getChildren().add(pieceText);
                }
            }
        }
    }

    private String getPieceSymbol(int pieceType, int color) {
        String[] whiteSymbols = { "a", "a", "a", "a", "a", "a" };
        String[] blackSymbols = { "♟", "♞", "♝", "♜", "♛", "♚" };

        if (color == ChessBoard.WHITE) {
            return whiteSymbols[pieceType];
        } else {
            return blackSymbols[pieceType];
        }
    }
}
