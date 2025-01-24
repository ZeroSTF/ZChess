package tn.zeros.zchess.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import tn.zeros.zchess.ui.controllers.ChessController;

public class ControlPanel extends HBox {
    public ControlPanel(ChessController controller) {
        // Buttons
        Button undoButton = new Button("Undo (Ctrl+Z)");
        Button redoButton = new Button("Redo (Ctrl+Y)");

        // Button actions
        undoButton.setOnAction(e -> controller.undo());
        redoButton.setOnAction(e -> controller.redo());

        // Keyboard shortcuts
        this.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (e.isControlDown()) {
                if (e.getCode() == KeyCode.Z) {
                    controller.undo();
                    e.consume();
                } else if (e.getCode() == KeyCode.Y) {
                    controller.redo();
                    e.consume();
                }
            }
        });

        // Styling
        this.setSpacing(10);
        this.setPadding(new Insets(10));
        this.setAlignment(Pos.CENTER);
        this.getChildren().addAll(undoButton, redoButton);
    }
}