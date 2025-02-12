package tn.zeros.zchess.ui.events;

import java.util.HashSet;
import java.util.Set;

public class EventBus {
    private static final EventBus instance = new EventBus();
    private final Set<EventListener> listeners = new HashSet<>();

    private EventBus() {
    }

    public static EventBus getInstance() {
        return instance;
    }

    public void register(EventListener listener) {
        listeners.add(listener);
    }

    public void unregister(EventListener listener) {
        listeners.remove(listener);
    }

    public void post(ClockEvent event) {
        listeners.forEach(listener -> listener.onClockEvent(event));
    }
}