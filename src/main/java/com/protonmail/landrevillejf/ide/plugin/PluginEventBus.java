package com.protonmail.landrevillejf.ide.plugin;

import com.protonmail.landrevillejf.ide.plugin.events.Event;
import com.protonmail.landrevillejf.ide.plugin.events.EventListener;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Event bus for plugin-specific event publishing and subscription.
 * <p>
 * This class provides a thread-safe mechanism for plugins to publish events
 * and subscribe to events from other plugins. It uses type-safe event handling
 * with generics.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
public class PluginEventBus {
    private final Map<Class<? extends Event>, List<EventListener<?>>> listeners =
            new ConcurrentHashMap<>();

    /**
     * Subscribes a listener to a specific event type.
     *
     * @param <T> the event type
     * @param eventType the class of the event to subscribe to
     * @param listener the listener to handle events
     */
    public <T extends Event> void subscribe(Class<T> eventType, EventListener<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                .add(listener);
    }

    /**
     * Unsubscribes a listener from a specific event type.
     *
     * @param <T> the event type
     * @param eventType the class of the event to unsubscribe from
     * @param listener the listener to remove
     */
    public <T extends Event> void unsubscribe(Class<T> eventType, EventListener<T> listener) {
        List<EventListener<?>> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            eventListeners.remove(listener);
        }
    }

    /**
     * Publishes an event to all subscribed listeners.
     *
     * @param <T> the event type
     * @param event the event to publish
     */
    @SuppressWarnings("unchecked")
    public <T extends Event> void publish(T event) {
        List<EventListener<?>> eventListeners = listeners.get(event.getClass());
        if (eventListeners != null) {
            for (EventListener<?> listener : eventListeners) {
                // Safe cast because we only add listeners for the correct type
                EventListener<T> typedListener = (EventListener<T>) listener;
                typedListener.onEvent(event);
            }
        }
    }

    /**
     * Checks if there are any subscribers for a specific event type.
     *
     * @param eventType the class of the event to check
     * @return {@code true} if there are subscribers, {@code false} otherwise
     */
    public boolean hasSubscribers(Class<? extends Event> eventType) {
        List<EventListener<?>> eventListeners = listeners.get(eventType);
        return eventListeners != null && !eventListeners.isEmpty();
    }

    /**
     * Clears all listeners for all event types.
     */
    public void clear() {
        listeners.clear();
    }

    /**
     * Clears all listeners for a specific event type.
     *
     * @param eventType the class of the event to clear listeners for
     */
    public void clear(Class<? extends Event> eventType) {
        listeners.remove(eventType);
    }
}
