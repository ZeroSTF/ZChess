package tn.zeros.zchess.ui.components;

import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;

public class ErrorDisplay {
    private static final String ERROR_STYLE =
            "-fx-background-color: #ff4444; -fx-text-fill: white;";
    private final Tooltip tooltip = new Tooltip();

    public void showError(Pane parent, String message) {
        tooltip.setText(message);
        tooltip.setStyle(ERROR_STYLE);
        tooltip.show(parent.getScene().getWindow());
    }

    public void hideError() {
        tooltip.hide();
    }
}