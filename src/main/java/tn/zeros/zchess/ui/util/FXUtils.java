package tn.zeros.zchess.ui.util;

import javafx.stage.Stage;
import javafx.stage.Window;

public class FXUtils {
    public static Window getRootWindow() {
        return Stage.getWindows().stream()
                .filter(Window::isShowing)
                .findFirst()
                .orElse(null);
    }
}