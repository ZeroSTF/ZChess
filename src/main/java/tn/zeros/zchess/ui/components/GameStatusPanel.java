package tn.zeros.zchess.ui.components;

import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import tn.zeros.zchess.core.model.GameResult;
import tn.zeros.zchess.ui.models.GameStateModel;

import java.util.Objects;

public class GameStatusPanel extends VBox {
    private final GameStateModel gameState;
    private final Label whiteClock;
    private final Label blackClock;
    private final Label gameResultLabel;

    public GameStatusPanel(GameStateModel gameState, Runnable restartHandler) {
        this.gameState = gameState;

        // Create UI elements
        whiteClock = createClockLabel(true);
        blackClock = createClockLabel(false);
        gameResultLabel = new Label();
        Button restartBtn = new Button("New Game");

        // Layout setup
        getChildren().addAll(
                createPlayerClockSection(whiteClock, "White"),
                createPlayerClockSection(blackClock, "Black"),
                gameResultLabel,
                restartBtn
        );
        setAlignment(Pos.TOP_CENTER);

        // Bindings
        bindProperties();
        restartBtn.setOnAction(e -> restartHandler.run());

        // Styles
        this.getStylesheets().addAll(
                Objects.requireNonNull(getClass().getResource("/css/application.css")).toExternalForm(),
                Objects.requireNonNull(getClass().getResource("/css/game-status-panel.css")).toExternalForm());
        this.getStyleClass().add("game-status-panel");
        gameResultLabel.getStyleClass().add("result-label");
        restartBtn.getStyleClass().add("control-button");
    }

    private void bindProperties() {
        whiteClock.textProperty().bind(Bindings.createStringBinding(() -> {
            Duration time = gameState.whiteTimeProperty().get();
            updateClockStyle(whiteClock, time);
            return formatDuration(time);
        }, gameState.whiteTimeProperty()));

        blackClock.textProperty().bind(Bindings.createStringBinding(() -> {
            Duration time = gameState.blackTimeProperty().get();
            updateClockStyle(blackClock, time);
            return formatDuration(time);
        }, gameState.blackTimeProperty()));

        gameResultLabel.textProperty().bind(Bindings.createStringBinding(() ->
                        gameState.gameResultProperty().get() == GameResult.ONGOING ? "" :
                                getResultText(gameState.gameResultProperty().get()),
                gameState.gameResultProperty()
        ));
    }

    private void updateClockStyle(Label clock, Duration remaining) {
        clock.getStyleClass().removeAll("time-warning", "time-critical");

        if (remaining.lessThanOrEqualTo(Duration.seconds(30))) {
            clock.getStyleClass().add("time-warning");
        }
        if (remaining.lessThanOrEqualTo(Duration.seconds(10))) {
            clock.getStyleClass().add("time-critical");
        }
    }

    private String formatDuration(Duration duration) {
        Duration effective = duration.greaterThan(Duration.ZERO) ? duration : Duration.ZERO;
        return String.format("%02d:%02d",
                (int) effective.toMinutes(),
                (int) effective.toSeconds() % 60
        );
    }

    private String getResultText(GameResult result) {
        return switch (result) {
            case WHITE_WINS -> "White Wins by Checkmate!";
            case BLACK_WINS -> "Black Wins by Checkmate!";
            case WHITE_TIMEOUT -> "Black Wins by Timeout!";
            case BLACK_TIMEOUT -> "White Wins by Timeout!";
            case STALEMATE -> "Draw by Stalemate!";
            case THREEFOLD_REPETITION -> "Draw by Repetition!";
            case FIFTY_MOVE_RULE -> "Draw by 50-Move Rule!";
            case INSUFFICIENT_MATERIAL -> "Draw by Insufficient Material!";
            default -> "";
        };
    }

    private Node createPlayerClockSection(Label clockLabel, String playerName) {
        VBox container = new VBox(5);
        container.getStyleClass().add("clock-container");

        Label title = new Label(playerName);
        title.getStyleClass().add("player-title");

        clockLabel.getStyleClass().add("clock-label");
        clockLabel.getStyleClass().add(playerName.equals("White") ? "white-clock" : "black-clock");

        container.getChildren().addAll(title, clockLabel);
        return container;
    }

    private Label createClockLabel(boolean isWhite) {
        Label clock = new Label("00:00");
        clock.getStyleClass().add("clock-label");
        clock.getStyleClass().add(isWhite ? "white-clock" : "black-clock");
        clock.setMinWidth(120);
        return clock;
    }
}