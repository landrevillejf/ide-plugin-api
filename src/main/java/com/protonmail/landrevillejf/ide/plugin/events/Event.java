package com.protonmail.landrevillejf.ide.plugin.events;

import java.time.LocalDateTime;

/**
 * Base interface for all events in the plugin system.
 * <p>
 * All events must implement this interface to be compatible with the
 * plugin event bus. Events carry a timestamp and source identifier.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
public interface Event {
    /**
     * Returns the timestamp when this event occurred.
     * <p>
     * Default implementation returns the current time.
     * </p>
     *
     * @return the event timestamp
     */
    default LocalDateTime getTimestamp() {
        return LocalDateTime.now();
    }

    /**
     * Returns the source of this event.
     * <p>
     * The source is typically the plugin or component that published the event.
     * </p>
     *
     * @return the event source identifier
     */
    String getSource();
}