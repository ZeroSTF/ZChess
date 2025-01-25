package tn.zeros.zchess.ui.components;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import tn.zeros.zchess.core.model.Piece;
import tn.zeros.zchess.ui.controller.ChessController;
import tn.zeros.zchess.ui.util.AssetLoader;

import java.util.Optional;

public class PromotionDialog {
    private final Dialog<Piece> dialog;
    private final GridPane grid;
    private final ChessController controller;

    public PromotionDialog(ChessController controller) {
        this.controller = controller;
        dialog = new Dialog<>();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Pawn Promotion");
        dialog.setResultConverter(buttonType -> {
            // This will be null if dialog is closed without selection
            return null;
        });
        grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        dialog.getDialogPane().getButtonTypes().clear();
        dialog.getDialogPane().setContent(grid);
    }

    public void show(boolean isWhite) {
        grid.getChildren().clear();

        Piece[] options = isWhite ?
                new Piece[]{Piece.WHITE_QUEEN, Piece.WHITE_ROOK,
                        Piece.WHITE_BISHOP, Piece.WHITE_KNIGHT} :
                new Piece[]{Piece.BLACK_QUEEN, Piece.BLACK_ROOK,
                        Piece.BLACK_BISHOP, Piece.BLACK_KNIGHT};

        for (int i = 0; i < options.length; i++) {
            Piece piece = options[i];
            Button btn = createPromotionButton(piece);
            grid.add(btn, i, 0);
        }

        Optional<Piece> result = dialog.showAndWait();
        result.ifPresent(controller::completePromotion);
    }

    private Button createPromotionButton(Piece piece) {
        ImageView imageView = new ImageView(AssetLoader.getPieceImage(piece));
        imageView.setFitWidth(50);
        imageView.setFitHeight(50);

        Button btn = new Button();
        btn.setGraphic(imageView);
        btn.setOnAction(e -> {
            dialog.setResult(piece);
            dialog.close();
        });
        return btn;
    }
}