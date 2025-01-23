package tn.zeros.zchess.ui.components;

import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import tn.zeros.zchess.core.board.BitboardPosition;
import tn.zeros.zchess.core.move.Move;
import tn.zeros.zchess.core.move.MoveValidator;
import tn.zeros.zchess.core.piece.Piece;

public class ChessBoardView extends GridPane {
    private static final int SQUARE_SIZE = 80;
    private final SquareView[][] squares = new SquareView[8][8];
    private final BitboardPosition position;
    private SquareView selectedSquare = null;

    public ChessBoardView(BitboardPosition position) {
        this.position = position;
        initializeBoard();
    }

    private void initializeBoard() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Color squareColor = (row + col) % 2 == 0 ? Color.TEAL : Color.BEIGE;
                SquareView square = new SquareView(row, col, squareColor);
                squares[row][col] = square;
                add(square, col, 7 - row); // Flip board to show white at bottom

                final int finalRow = row;
                final int finalCol = col;
                square.setOnMouseClicked(e -> handleSquareClick(squares[finalRow][finalCol]));
            }
        }
        setupInitialPosition();
    }

    private void setupInitialPosition() {
        // Set up pawns
        for (int col = 0; col < 8; col++) {
            squares[1][col].setPiece(Piece.WHITE_PAWN);
            squares[6][col].setPiece(Piece.BLACK_PAWN);
        }

        // Set up other pieces
        setupPiece(0, 0, Piece.WHITE_ROOK);
        setupPiece(0, 1, Piece.WHITE_KNIGHT);
        setupPiece(0, 2, Piece.WHITE_BISHOP);
        setupPiece(0, 3, Piece.WHITE_QUEEN);
        setupPiece(0, 4, Piece.WHITE_KING);
        setupPiece(0, 5, Piece.WHITE_BISHOP);
        setupPiece(0, 6, Piece.WHITE_KNIGHT);
        setupPiece(0, 7, Piece.WHITE_ROOK);

        setupPiece(7, 0, Piece.BLACK_ROOK);
        setupPiece(7, 1, Piece.BLACK_KNIGHT);
        setupPiece(7, 2, Piece.BLACK_BISHOP);
        setupPiece(7, 3, Piece.BLACK_QUEEN);
        setupPiece(7, 4, Piece.BLACK_KING);
        setupPiece(7, 5, Piece.BLACK_BISHOP);
        setupPiece(7, 6, Piece.BLACK_KNIGHT);
        setupPiece(7, 7, Piece.BLACK_ROOK);
    }

    private void setupPiece(int row, int col, Piece piece) {
        squares[row][col].setPiece(piece);
    }

    private void handleSquareClick(SquareView square) {
        MoveValidator validator = new MoveValidator(position);

        if (selectedSquare == null) {
            Piece piece = square.getPiece();
            if (piece != Piece.NONE && piece !=null && piece.isWhite() == position.isWhiteToMove()) {
                selectedSquare = square;
                highlightLegalMoves(square);
            }
        } else {
            int fromSquare = selectedSquare.getRow() * 8 + selectedSquare.getCol();
            int toSquare = square.getRow() * 8 + square.getCol();

            if (validator.isLegalMove(fromSquare, toSquare)) {
                Move move = new Move(fromSquare, toSquare,
                        selectedSquare.getPiece(),
                        square.getPiece(),
                        false, false, false, null);

                position.makeMove(move);

                // Update UI
                square.setPiece(selectedSquare.getPiece());
                selectedSquare.setPiece(null);
            }

            clearHighlights();
            selectedSquare = null;
        }
    }

    private void highlightLegalMoves(SquareView square) {
        int fromSquare = square.getRow() * 8 + square.getCol();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int toSquare = row * 8 + col;
                if (new MoveValidator(position).isLegalMove(fromSquare, toSquare)) {
                    squares[row][col].highlight(true);
                }
            }
        }
    }

    private void clearHighlights() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                squares[row][col].highlight(false);
            }
        }
    }
}
