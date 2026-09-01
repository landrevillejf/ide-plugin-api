package com.protonmail.landrevillejf.ide.plugin.events;

/**
 * Generic listener interface for handling events.
 * <p>
 * Implementations of this interface can be registered with the event bus
 * to receive notifications of specific event types.
 * </p>
 *
 * @param <T> the event type this listener handles
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
public interface EventListener<T extends Event> {
    /**
     * Called when an event of the appropriate type is published.
     *
     * @param event the event that was published
     */
    void onEvent(T event);
}