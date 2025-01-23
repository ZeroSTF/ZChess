package tn.zeros.zchess.ui.components;

import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.logic.MoveValidator;
import tn.zeros.zchess.core.piece.Piece;

public class ChessBoardView extends GridPane {
    private static final int SQUARE_SIZE = 80;
    private final SquareView[][] squares = new SquareView[8][8];
    private final BoardState position;
    private SquareView selectedSquare = null;

    public ChessBoardView(BoardState position) {
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

            Piece piece = selectedSquare.getPiece();
            Move move;
            // Check if the move is an en passant capture
            boolean isEnPassant = false;
            if (piece.isPawn() && position.getEnPassantSquare() == toSquare) {
                isEnPassant = true;
                // Get the actual captured pawn's position (behind the target square)
                int capturedSquare = toSquare + (piece.isWhite() ? -8 : 8);
                Piece capturedPiece = position.getPieceAt(capturedSquare);
                move = new Move(fromSquare, toSquare, piece, capturedPiece, false, false, true, null);
            } else {
                move = new Move(fromSquare, toSquare, piece, square.getPiece(), false, false, false, null);
            }


            if (validator.isLegalMove(fromSquare, toSquare)) {
                position.makeMove(move);

                // Update UI
                square.setPiece(piece);
                selectedSquare.setPiece(null);

                if (isEnPassant) {
                    // Remove the captured pawn from the board
                    int capturedSquare = toSquare + (piece.isWhite() ? -8 : 8);
                    squares[capturedSquare / 8][capturedSquare % 8].setPiece(Piece.NONE);
                }
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
