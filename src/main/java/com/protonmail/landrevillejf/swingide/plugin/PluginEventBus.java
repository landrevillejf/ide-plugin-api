package com.protonmail.landrevillejf.swingide.plugin;

import com.protonmail.landrevillejf.swingide.plugin.events.Event;
import com.protonmail.landrevillejf.swingide.plugin.events.EventListener;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class PluginEventBus {
    private final Map<Class<? extends Event>, List<EventListener<?>>> listeners =
            new ConcurrentHashMap<>();

    // Subscribe to a specific event type
    public <T extends Event> void subscribe(Class<T> eventType, EventListener<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                .add(listener);
    }

    // Unsubscribe from an event type
    public <T extends Event> void unsubscribe(Class<T> eventType, EventListener<T> listener) {
        List<EventListener<?>> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            eventListeners.remove(listener);
        }
    }

    // Publish an event to all subscribers
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

    // Check if there are any listeners for an event type
    public boolean hasSubscribers(Class<? extends Event> eventType) {
        List<EventListener<?>> eventListeners = listeners.get(eventType);
        return eventListeners != null && !eventListeners.isEmpty();
    }

    // Clear all listeners
    public void clear() {
        listeners.clear();
    }

    // Clear listeners for a specific event type
    public void clear(Class<? extends Event> eventType) {
        listeners.remove(eventType);
    }
}
