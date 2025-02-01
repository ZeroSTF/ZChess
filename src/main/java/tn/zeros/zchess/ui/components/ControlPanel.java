package tn.zeros.zchess.ui.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import tn.zeros.zchess.ui.controller.ChessController;

import java.util.Optional;

public class ControlPanel extends HBox {

    public ControlPanel(ChessController controller, SettingsPanel settingsPanel) {
        // Buttons
        Button undoButton = new Button("Undo (Ctrl+Z)");
        Button redoButton = new Button("Redo (Ctrl+Y)");
        Button fenButton = new Button("Get FEN");
        Button setFenButton = new Button("Set FEN");
        Button settingsButton = new Button("Settings ▶");

        // Button actions
        undoButton.setOnAction(e -> controller.undo());
        redoButton.setOnAction(e -> controller.redo());
        fenButton.setOnAction(e -> showFEN(controller));
        setFenButton.setOnAction(e -> setFEN(controller));
        settingsButton.setOnAction(e -> {
            boolean visible = !settingsPanel.isVisible();
            settingsPanel.setVisible(visible);
            settingsButton.setText(visible ? "Settings ◀" : "Settings ▶");
        });

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
        this.getChildren().addAll(undoButton, redoButton, fenButton, setFenButton, settingsButton);
    }

    private void showFEN(ChessController controller) {
        String fen = controller.getCurrentFEN();

        TextArea textArea = new TextArea(fen);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefSize(400, 60);

        Button copyButton = new Button("Copy to Clipboard");
        copyButton.setOnAction(e -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(fen);
            clipboard.setContent(content);
        });

        HBox container = new HBox(10, textArea, copyButton);
        container.setAlignment(Pos.CENTER);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Current Position FEN");
        alert.setHeaderText("Select and copy the FEN string:");
        alert.getDialogPane().setContent(container);

        alert.showAndWait();
    }

    private void setFEN(ChessController controller) {
        TextArea textArea = new TextArea();
        textArea.setPromptText("Paste FEN here");
        textArea.setWrapText(true);

        Alert dialog = new Alert(Alert.AlertType.CONFIRMATION);
        dialog.setTitle("Set Position from FEN");
        dialog.setHeaderText("Enter FEN string:");
        dialog.getDialogPane().setContent(textArea);
        dialog.getDialogPane().setPrefSize(400, 200);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            String fen = textArea.getText().trim();
            try {
                controller.loadFEN(fen);
            } catch (IllegalArgumentException ex) {
                showErrorAlert("Invalid FEN", ex.getMessage());
            }
        }
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}