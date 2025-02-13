package tn.zeros.zchess.ui.controller;

import javafx.scene.image.ImageView;
import tn.zeros.zchess.core.model.Move;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.ui.util.AssetLoader;
import tn.zeros.zchess.ui.util.UIConstants;
import tn.zeros.zchess.ui.view.ChessBoardView;
import tn.zeros.zchess.ui.view.SquareView;

public class InputHandler {
    private final ChessController controller;
    private ImageView dragImage;
    private int startSquare = -1;
    private SquareView sourceSquare;

    public InputHandler(ChessController controller) {
        this.controller = controller;
    }

    public void handlePress(int square) {
        boolean isWhiteToMove = controller.getBoardState().isWhiteToMove();
        int piece = controller.getBoardState().getPieceAt(square);

        if (piece != Piece.NONE && Piece.isWhite(piece) == isWhiteToMove) {
            startSquare = square;
            ChessBoardView boardView = (ChessBoardView) controller.getView();
            sourceSquare = (SquareView) boardView.getSquareNode(square);
            sourceSquare.hideCurrentPiece();
            createDragImage(piece);
            controller.handlePieceSelection(square);
        } else {
            if (controller.getInteractionState().getSelectedSquare() == -1) {
                controller.handlePieceSelection(square);
            } else {
                controller.handleMoveExecution(square);
            }
        }
    }

    public void handleDrag(double x, double y) {
        if (dragImage != null) {
            ChessBoardView boardView = (ChessBoardView) controller.getView();
            javafx.geometry.Point2D boardPoint = boardView.sceneToLocal(x, y);
            dragImage.setTranslateX(boardPoint.getX() - (double) UIConstants.SQUARE_SIZE / 2);
            dragImage.setTranslateY(boardPoint.getY() - (double) UIConstants.SQUARE_SIZE / 2);
            boardView.handleDragHover(x, y);
        }
    }

    public void handleRelease(double x, double y) {
        if (startSquare != -1) {
            ChessBoardView boardView = (ChessBoardView) controller.getView();
            int targetSquare = boardView.getSquareFromSceneCoordinates(x, y);

            if (targetSquare != -1 && isValidMove(targetSquare)) {
                controller.handleMoveExecution(targetSquare);
            } else {
                sourceSquare.showCurrentPiece();
            }
            cleanup();
        }
    }

    private boolean isValidMove(int targetSquare) {
        return controller.getInteractionState().getCurrentLegalMoves()
                .stream()
                .anyMatch(move -> Move.getTo(move) == targetSquare);
    }

    private void createDragImage(int piece) {
        dragImage = new ImageView(AssetLoader.getPieceImage(piece));
        dragImage.setFitWidth(UIConstants.SQUARE_SIZE - 10);
        dragImage.setFitHeight(UIConstants.SQUARE_SIZE - 10);
        dragImage.setMouseTransparent(true);
        ChessBoardView boardView = (ChessBoardView) controller.getView();
        boardView.addDragImage(dragImage);
    }

    private void cleanup() {
        if (dragImage != null) {
            ChessBoardView boardView = (ChessBoardView) controller.getView();
            boardView.removeDragImage(dragImage);
            dragImage = null;
        }
        startSquare = -1;
    }

    public void restoreSourcePiece() {
        if (sourceSquare != null) {
            sourceSquare.showCurrentPiece();
            cleanup();
        }
    }
}