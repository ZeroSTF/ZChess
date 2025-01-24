package tn.zeros.zchess.ui.view;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import tn.zeros.zchess.core.model.BoardState;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.ui.util.BoardGeometry;
import tn.zeros.zchess.ui.controllers.ChessController;

import java.util.List;

import static tn.zeros.zchess.ui.util.UIConstants.FILES;
import static tn.zeros.zchess.ui.util.UIConstants.RANKS;

public class ChessBoardView extends GridPane implements ChessView {
    private final SquareView[][] squares = new SquareView[8][8];
    private final ChessController controller;

    public ChessBoardView(ChessController controller) {
        this.controller = controller;
        this.controller.registerView(this);
        initializeBoard();
        refreshEntireBoard();
    }

    private void initializeBoard() {
        addRankLabels();
        addFileLabels();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                SquareView square = createSquare(row, col);
                squares[row][col] = square;
                add(square, col, 7 - row); // Flip for visual board
            }
        }
    }

    private SquareView createSquare(int row, int col) {
        Color color = BoardGeometry.getSquareColor(row, col);
        SquareView square = new SquareView(color);

        square.setOnMouseClicked(e -> {
            int squareIndex = BoardGeometry.toSquareIndex(row, col);
            controller.handleSquareClick(squareIndex);
        });

        return square;
    }

    public void updateBoard(Move move) {
        // Update source square
        int fromRow = move.getFromSquare() / 8;
        int fromCol = move.getFromSquare() % 8;
        squares[fromRow][fromCol].setPiece(Piece.NONE);

        // Update destination square
        int toRow = move.getToSquare() / 8;
        int toCol = move.getToSquare() % 8;
        squares[toRow][toCol].setPiece(move.getPiece());

        // Handle special moves
        if (move.isEnPassant()) {
            handleEnPassantUI(move);
        } else if (move.isCastling()) {
            handleCastlingUI(move);
        }
    }

    private void handleEnPassantUI(Move move) {
        int capturedSquare = move.getToSquare() +
                (move.getPiece().isWhite() ? -8 : 8);
        int[] coords = BoardGeometry.fromSquareIndex(capturedSquare);
        squares[coords[0]][coords[1]].setPiece(Piece.NONE);
    }

    private void handleCastlingUI(Move move) {
        int[] rookPositions = calculateRookMove(move);
        updateRookPosition(rookPositions[0], rookPositions[1]);
    }

    private int[] calculateRookMove(Move move) {
        int from = move.getFromSquare();
        int to = move.getToSquare();
        boolean kingside = to > from;

        int rookFrom = kingside ? from + 3 : from - 4;
        int rookTo = kingside ? to - 1 : to + 1;

        return new int[]{rookFrom, rookTo};
    }

    private void updateRookPosition(int from, int to) {
        int[] fromCoords = BoardGeometry.fromSquareIndex(from);
        int[] toCoords = BoardGeometry.fromSquareIndex(to);

        Piece rook = squares[fromCoords[0]][fromCoords[1]].getPiece();
        squares[fromCoords[0]][fromCoords[1]].setPiece(Piece.NONE);
        squares[toCoords[0]][toCoords[1]].setPiece(rook);
    }

    @Override
    public void showError(String message) {
        Tooltip errorTip = new Tooltip(message);
        errorTip.setStyle("-fx-background-color: #ff4444; -fx-text-fill: white;");
        errorTip.show(getScene().getWindow());
    }

    public void highlightLegalMoves(List<Integer> legalSquares) {
        clearHighlights();
        for (int square : legalSquares) {
            int row = square / 8;
            int col = square % 8;
            squares[row][col].highlight(true);
        }
    }

    public void clearHighlights() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                squares[row][col].highlight(false);
            }
        }
    }

    @Override
    public void refreshEntireBoard() {
        BoardState state = controller.getBoardState();
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int square = row * 8 + col;
                squares[row][col].setPiece(state.getPieceAt(square));
                squares[row][col].highlight(false);
            }
        }
    }

    @Override
    public void showPromotionDialog(boolean isWhite) {
        // TODO
    }

    private void addRankLabels() {
        for (int row = 0; row < 8; row++) {
            Label rightLabel = createLabel(RANKS[row], true);
            add(rightLabel, 9, row);
        }
    }

    private void addFileLabels() {
        for (int col = 0; col < 8; col++) {
            Label fileLabel = createLabel(FILES[col], false);
            add(fileLabel, col, 8);
            GridPane.setHalignment(fileLabel, javafx.geometry.HPos.CENTER);
        }
    }

    private Label createLabel(String text, boolean isRank) {
        Label label = new Label(text);
        label.setStyle("-fx-font-size: 14; -fx-font-weight: bold;");
        label.setPadding(new Insets(5));
        return label;
    }
}
