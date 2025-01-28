package tn.zeros.zchess.ui.view;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.ui.controller.ChessController;
import tn.zeros.zchess.ui.util.AssetLoader;
import tn.zeros.zchess.ui.util.EffectUtils;
import tn.zeros.zchess.ui.util.UIConstants;

public class SquareView extends StackPane {
    private final Circle legalMoveDot;
    private final Rectangle checkOverlay;
    private final Rectangle captureHighlight;
    private final Rectangle legalMoveHoverHighlight;
    private final Rectangle lastMoveHighlight;
    private final Rectangle selectedHighlight;

    private Piece currentPiece;
    private boolean isLegalMove;
    private boolean isCaptureMove;
    private boolean isLastMove;
    private boolean isHovered;

    public SquareView(Color color, ChessController controller, int squareIndex) {
        // Initialisation
        Rectangle background = new Rectangle(UIConstants.SQUARE_SIZE, UIConstants.SQUARE_SIZE, color);

        legalMoveDot = new Circle(UIConstants.SQUARE_SIZE * UIConstants.LEGAL_MOVE_DOT_RADIUS);
        checkOverlay = new Rectangle(UIConstants.SQUARE_SIZE, UIConstants.SQUARE_SIZE);
        captureHighlight = new Rectangle(UIConstants.SQUARE_SIZE, UIConstants.SQUARE_SIZE);
        legalMoveHoverHighlight = new Rectangle(UIConstants.SQUARE_SIZE, UIConstants.SQUARE_SIZE);
        lastMoveHighlight = new Rectangle(UIConstants.SQUARE_SIZE, UIConstants.SQUARE_SIZE);
        selectedHighlight = new Rectangle(UIConstants.SQUARE_SIZE, UIConstants.SQUARE_SIZE);

        legalMoveDot.setVisible(false);
        checkOverlay.setVisible(false);
        captureHighlight.setVisible(false);
        legalMoveHoverHighlight.setVisible(false);
        lastMoveHighlight.setVisible(false);
        selectedHighlight.setVisible(false);

        legalMoveDot.setFill(UIConstants.LEGAL_MOVE_DOT_COLOR);
        checkOverlay.setFill(EffectUtils.gradient);
        captureHighlight.setFill(UIConstants.CAPTURE_HIGHLIGHT);
        legalMoveHoverHighlight.setFill(UIConstants.LEGAL_MOVE_HOVER_COLOR);
        lastMoveHighlight.setFill(UIConstants.LAST_MOVE_COLOR);
        selectedHighlight.setFill(UIConstants.SELECTED_SQUARE_COLOR);

        getChildren().addAll(background, legalMoveDot, checkOverlay, captureHighlight, legalMoveHoverHighlight, lastMoveHighlight, selectedHighlight);

        // Events
        setOnMouseEntered(e -> {
            isHovered = true;
            if (isLegalMove) setLegalMoveHover(true);
        });

        setOnMouseExited(e -> {
            isHovered = false;
            setLegalMoveHover(false);
        });

        setOnMousePressed(e -> {
            if (e.getButton() != MouseButton.PRIMARY) return;
            controller.getInputHandler().handlePress(squareIndex);
            controller.getInputHandler().handleDrag(e.getSceneX(), e.getSceneY());
        });

        setOnMouseDragged(e -> {
            if (!e.isPrimaryButtonDown()) return;
            controller.getInputHandler().handleDrag(e.getSceneX(), e.getSceneY());
        });

        setOnMouseReleased(e -> {
            if (e.getButton() != MouseButton.PRIMARY) return;
            controller.getInputHandler().handleRelease(e.getSceneX(), e.getSceneY());
        });
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
        imageView.setPreserveRatio(true);
        getChildren().add(imageView);
    }

    private void addFallbackText() {
        Text text = new Text(String.valueOf(currentPiece.getSymbol()));
        text.setFont(UIConstants.PIECE_FONT);
        getChildren().add(text);
    }

    public Piece getPiece() {
        return currentPiece;
    }

    public void setPiece(Piece piece) {
        currentPiece = piece;
        refreshDisplay();
    }

    public void hideCurrentPiece() {
        getChildren().stream()
                .filter(node -> node instanceof ImageView || node instanceof Text)
                .forEach(node -> node.setVisible(false));
    }

    public void showCurrentPiece() {
        getChildren().stream()
                .filter(node -> node instanceof ImageView || node instanceof Text)
                .forEach(node -> node.setVisible(true));
    }

    public void setCheckOverlay(boolean inCheck) {
        checkOverlay.setVisible(inCheck);
    }

    public void setLegalMoveOverlay(boolean isLegal, boolean isCapture) {
        this.isLegalMove = isLegal;
        this.isCaptureMove = isCapture;

        legalMoveDot.setVisible(isLegal && !isCapture);
        captureHighlight.setVisible(isLegal && isCapture);

        if (isHovered) {
            setLegalMoveHover(isHovered);
        }

        if (isLegal && isCapture) {
            lastMoveHighlight.setVisible(false);
        }
    }

    public void setLastMove(boolean isLast) {
        this.isLastMove = isLast;

        if (isCaptureMove) {
            lastMoveHighlight.setVisible(false);
        } else {
            lastMoveHighlight.setVisible(isLast);
        }
    }

    public void setLegalMoveHover(boolean isHovered) {
        legalMoveHoverHighlight.setVisible(isHovered && isLegalMove);

        if (isHovered) {
            legalMoveDot.setVisible(false);
            captureHighlight.setVisible(false);
            lastMoveHighlight.setVisible(false);
        } else {
            setLegalMoveOverlay(isLegalMove, isCaptureMove);
            setLastMove(isLastMove);
        }
    }

    public void setSelected(boolean isSelected) {
        selectedHighlight.setVisible(isSelected);
    }
}
