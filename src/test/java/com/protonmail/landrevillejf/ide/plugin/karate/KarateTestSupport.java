package com.protonmail.landrevillejf.ide.plugin.karate;

import com.protonmail.landrevillejf.ide.plugin.events.Event;
import com.protonmail.landrevillejf.ide.plugin.events.EventListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Java interop helper used by Karate feature files.
 * <p>
 * Provides deterministic, reflection-friendly entry points so Karate
 * scenarios can subscribe to the plugin event bus without relying on
 * JS-to-functional-interface conversion.
 * </p>
 */
public final class KarateTestSupport {

    private static final List<Event> RECEIVED = new CopyOnWriteArrayList<>();

    private KarateTestSupport() {
        // Utility class
    }

    /**
     * Creates a listener that records every received event.
     *
     * @param <T> the event type
     * @return a recording event listener
     */
    public static <T extends Event> EventListener<T> recordingListener() {
        return event -> RECEIVED.add(event);
    }

    /**
     * Clears all recorded events.
     */
    public static void clear() {
        RECEIVED.clear();
    }

    /**
     * Returns the number of events recorded so far.
     *
     * @return the recorded event count
     */
    public static int receivedCount() {
        return RECEIVED.size();
    }

    /**
     * Returns the most recently recorded event, or {@code null} if none.
     *
     * @return the last recorded event
     */
    public static Event lastEvent() {
        return RECEIVED.isEmpty() ? null : RECEIVED.get(RECEIVED.size() - 1);
    }

    /**
     * Returns the simple class name of the most recently recorded event.
     *
     * @return the last event type name, or {@code "none"}
     */
    public static String lastEventType() {
        Event event = lastEvent();
        return event == null ? "none" : event.getClass().getSimpleName();
    }
}
