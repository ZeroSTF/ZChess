package tn.zeros.zchess.ui.models;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.util.Duration;
import tn.zeros.zchess.core.model.GameResult;
import tn.zeros.zchess.ui.util.UIConstants;

public class GameStateModel {
    private final ObjectProperty<Duration> whiteTime = new SimpleObjectProperty<>(UIConstants.INITIAL_TIME);
    private final ObjectProperty<Duration> blackTime = new SimpleObjectProperty<>(UIConstants.INITIAL_TIME);
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

    public void resetClocks() {
        whiteTime.set(UIConstants.INITIAL_TIME);
        blackTime.set(UIConstants.INITIAL_TIME);
    }

    public void applyTimeIncrement(boolean isWhite) {
        if (isWhite) {
            whiteTime.set(whiteTime.get().add(UIConstants.TIME_INCREMENT));
        } else {
            blackTime.set(blackTime.get().add(UIConstants.TIME_INCREMENT));
        }
    }
}