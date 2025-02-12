package tn.zeros.zchess.ui.models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.util.Duration;
import tn.zeros.zchess.core.model.GameResult;

public class GameStateModel {
    private final ObjectProperty<Duration> whiteTime = new SimpleObjectProperty<>(Duration.ZERO);
    private final ObjectProperty<Duration> blackTime = new SimpleObjectProperty<>(Duration.ZERO);
    private final ObjectProperty<GameResult> gameResult = new SimpleObjectProperty<>(GameResult.ONGOING);
    private final BooleanProperty clockRunning = new SimpleBooleanProperty(false);

    // Getters and property methods
    public ObjectProperty<Duration> whiteTimeProperty() {
        return whiteTime;
    }

    public ObjectProperty<Duration> blackTimeProperty() {
        return blackTime;
    }

    public ObjectProperty<GameResult> gameResultProperty() {
        return gameResult;
    }

    public BooleanProperty clockRunningProperty() {
        return clockRunning;
    }
}