package tn.zeros.zchess.ui.components;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.ui.controller.ChessController;
import tn.zeros.zchess.ui.util.AssetLoader;
import tn.zeros.zchess.ui.util.FXUtils;

import java.util.Objects;
import java.util.Optional;

public class PromotionDialog {
    private final Dialog<Integer> dialog;
    private final GridPane grid;
    private final ChessController controller;
    private int selectedPiece;

    public PromotionDialog(ChessController controller) {
        this.controller = controller;
        dialog = new Dialog<>();
        Image dialogIcon = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/icons/app-icon-32x32.png")));
        dialog.initOwner(FXUtils.getRootWindow());
        Stage dialogStage = (Stage) dialog.getDialogPane().getScene().getWindow();
        dialogStage.getIcons().add(dialogIcon);

        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Pawn Promotion");
        dialog.setResultConverter(buttonType -> selectedPiece);

        grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        dialog.getDialogPane().getButtonTypes().clear();
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        dialog.getDialogPane().setContent(grid);

        Button cancelButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancelButton.setText("Cancel");

        // Styles
        dialog.getDialogPane().getStylesheets().addAll(
                Objects.requireNonNull(getClass().getResource("/css/promotion-dialog.css")).toExternalForm(),
                Objects.requireNonNull(getClass().getResource("/css/application.css")).toExternalForm(),
                Objects.requireNonNull(getClass().getResource("/css/colors.css")).toExternalForm()
        );
        dialog.getDialogPane().getStyleClass().add("promotion-dialog");
    }

    public void show(boolean isWhite) {
        grid.getChildren().clear();
        selectedPiece = Piece.NONE;

        int[] options = isWhite ?
                new int[]{Piece.makePiece(Piece.QUEEN, Piece.WHITE), Piece.makePiece(Piece.ROOK, Piece.WHITE),
                        Piece.makePiece(Piece.BISHOP, Piece.WHITE), Piece.makePiece(Piece.KNIGHT, Piece.WHITE)} :
                new int[]{Piece.makePiece(Piece.QUEEN, Piece.BLACK), Piece.makePiece(Piece.ROOK, Piece.BLACK),
                        Piece.makePiece(Piece.BISHOP, Piece.BLACK), Piece.makePiece(Piece.KNIGHT, Piece.BLACK)};

        for (int i = 0; i < options.length; i++) {
            int piece = options[i];
            Button btn = createPromotionButton(piece);
            grid.add(btn, i, 0);
        }

        dialog.setOnCloseRequest(event -> {
            if (selectedPiece == Piece.NONE) {
                controller.getInputHandler().restoreSourcePiece();
            }
        });

        Optional<Integer> result = dialog.showAndWait();
        result.ifPresent(controller::completePromotion);
    }

    private Button createPromotionButton(int piece) {
        ImageView imageView = new ImageView(AssetLoader.getPieceImage(piece));
        imageView.setFitWidth(50);
        imageView.setFitHeight(50);

        Button btn = new Button();
        btn.setGraphic(imageView);
        btn.setOnAction(e -> {
            selectedPiece = piece;
            dialog.setResult(piece);
            dialog.close();
        });

        // Styles
        btn.getStyleClass().add("promotion-button");
        imageView.getStyleClass().add("promotion-image");

        return btn;
    }
}