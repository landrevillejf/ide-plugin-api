package com.protonmail.landrevillejf.ide.plugin.events;

/**
 * Event fired when the application is closing.
 * <p>
 * This event is fired before the application shuts down, allowing plugins
 * to perform cleanup operations.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
public class ApplicationClosingEvent extends BaseEvent {
    /**
     * Creates a new application closing event.
     *
     * @param source the source of this event
     */
    public ApplicationClosingEvent(String source) {
        super(source);
    }
}