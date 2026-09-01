package com.protonmail.landrevillejf.ide.plugin.events;

import java.time.LocalDateTime;

/**
 * Abstract base class for events providing common timestamp and source functionality.
 * <p>
 * This class implements the Event interface and provides a concrete implementation
 * for timestamp and source tracking. Specific event types should extend this class.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
public abstract class BaseEvent implements Event {
    private final LocalDateTime timestamp;
    private final String source;

    /**
     * Creates a new base event.
     *
     * @param source the source of this event
     */
    public BaseEvent(String source) {
        this.timestamp = LocalDateTime.now();
        this.source = source;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public String getSource() {
        return source;
    }
}