package tn.zeros.zchess.ui.controller;

import javafx.scene.image.ImageView;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.ui.util.AssetLoader;
import tn.zeros.zchess.ui.util.UIConstants;
import tn.zeros.zchess.ui.view.ChessBoardView;
import tn.zeros.zchess.ui.view.SquareView;

public class DragInputHandler implements InputHandler {
    private final ChessController controller;
    private ImageView dragImage;
    private int startSquare = -1;
    private SquareView sourceSquare;

    public DragInputHandler(ChessController controller) {
        this.controller = controller;
    }

    @Override
    public void handlePress(int square) {
        Piece piece = controller.getBoardState().getPieceAt(square);
        if (piece != Piece.NONE && piece.isWhite() == controller.getBoardState().isWhiteToMove()) {
            startSquare = square;
            ChessBoardView boardView = (ChessBoardView) controller.getView();
            sourceSquare = (SquareView) boardView.getSquareNode(square);
            sourceSquare.hideCurrentPiece();
            createDragImage(piece);
            controller.handlePieceSelection(square);
        } else if (piece == Piece.NONE) {
            if (controller.getInteractionState().getSelectedSquare() == -1) {
                controller.handlePieceSelection(square);
            } else {
                controller.handleMoveExecution(square);
            }

        }
    }

    @Override
    public void handleDrag(double x, double y) {
        if (dragImage != null) {
            ChessBoardView boardView = (ChessBoardView) controller.getView();
            javafx.geometry.Point2D boardPoint = boardView.sceneToLocal(x, y);
            dragImage.setTranslateX(boardPoint.getX() - (double) UIConstants.SQUARE_SIZE / 2);
            dragImage.setTranslateY(boardPoint.getY() - (double) UIConstants.SQUARE_SIZE / 2);
        }
    }

    @Override
    public void handleRelease(double x, double y) {
        if (startSquare != -1) {
            ChessBoardView boardView = (ChessBoardView) controller.getView();
            int targetSquare = boardView.getSquareFromSceneCoordinates(x, y);
            if (targetSquare != -1 && controller.getInteractionState().getCurrentLegalMoves()
                    .stream()
                    .anyMatch(move -> move.toSquare() == targetSquare)) {
                controller.handleMoveExecution(targetSquare);
            } else {
                sourceSquare.showCurrentPiece();
            }
            cleanup();
        }
    }

    private void createDragImage(Piece piece) {
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
}