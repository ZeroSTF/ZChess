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
    private final Rectangle background;
    private final Circle legalMoveDot;
    private final Color originalColor;
    private final Rectangle checkOverlay;
    private final Rectangle captureHighlight;
    private Piece currentPiece;
    private boolean isLegalMove;
    private boolean isCaptureMove;

    public SquareView(Color color, ChessController controller, int squareIndex) {
        // Initialisation
        this.originalColor = color;

        background = new Rectangle(UIConstants.SQUARE_SIZE, UIConstants.SQUARE_SIZE, color);
        legalMoveDot = new Circle(UIConstants.SQUARE_SIZE * UIConstants.LEGAL_MOVE_DOT_RADIUS, UIConstants.LEGAL_MOVE_DOT_COLOR);
        checkOverlay = new Rectangle(UIConstants.SQUARE_SIZE, UIConstants.SQUARE_SIZE);
        captureHighlight = new Rectangle(UIConstants.SQUARE_SIZE, UIConstants.SQUARE_SIZE);

        legalMoveDot.setVisible(false);
        checkOverlay.setVisible(false);
        captureHighlight.setVisible(false);
        checkOverlay.setFill(EffectUtils.gradient);
        captureHighlight.setFill(UIConstants.CAPTURE_HIGHLIGHT);


        getChildren().addAll(background, legalMoveDot, captureHighlight, checkOverlay);

        // Events
        setOnMouseEntered(e -> {
            if (isLegalMove) highlightWithColor(true, UIConstants.LEGAL_MOVE_HOVER_COLOR);
        });

        setOnMouseExited(e -> {
            setLegalMove(isLegalMove, isCaptureMove);
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

    public void setLegalMove(boolean isLegal, boolean isCapture) {
        this.isLegalMove = isLegal;
        this.isCaptureMove = isCapture;

        legalMoveDot.setVisible(isLegal && !isCapture);
        captureHighlight.setVisible(isLegal && isCapture);
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

    public void highlightWithColor(boolean highlight, Color overlayColor) {
        if (highlight) {
            Color blendedColor = blendColors(originalColor, overlayColor);
            background.setFill(blendedColor);
            if (isLegalMove) {
                legalMoveDot.setVisible(false);
                captureHighlight.setVisible(false);
            }
        } else {
            background.setFill(originalColor);
            if (isLegalMove) {
                legalMoveDot.setVisible(!isCaptureMove);
                captureHighlight.setVisible(isCaptureMove);
            }
        }
    }

    private Color blendColors(Color base, Color overlay) {
        double overlayAlpha = overlay.getOpacity();
        // Calculate blended color components
        double r = (1 - overlayAlpha) * base.getRed() + overlayAlpha * overlay.getRed();
        double g = (1 - overlayAlpha) * base.getGreen() + overlayAlpha * overlay.getGreen();
        double b = (1 - overlayAlpha) * base.getBlue() + overlayAlpha * overlay.getBlue();
        return new Color(r, g, b, 1.0);
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

    public void setCheck(boolean inCheck) {
        checkOverlay.setVisible(inCheck);
    }
}
