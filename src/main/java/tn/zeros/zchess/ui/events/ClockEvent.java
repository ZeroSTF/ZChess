package tn.zeros.zchess.ui.events;


import javafx.util.Duration;

public class ClockEvent {
    private final Type type;
    private final Duration duration;
    private final boolean isWhite;
    public ClockEvent(Type type, boolean isWhite, Duration duration) {
        this.type = type;
        this.isWhite = isWhite;
        this.duration = duration;
    }

    // Getters
    public Type getType() {
        return type;
    }

    public Duration getDuration() {
        return duration;
    }

    public boolean isWhite() {
        return isWhite;
    }

    public enum Type {
        TICK,
        RESET,
        INCREMENT
    }
}