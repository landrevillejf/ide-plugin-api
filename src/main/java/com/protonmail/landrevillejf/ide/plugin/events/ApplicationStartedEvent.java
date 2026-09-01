package com.protonmail.landrevillejf.ide.plugin.events;

/**
 * Event fired when the application has started.
 * <p>
 * This event contains information about the application version and
 * the time it took to start up.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
public class ApplicationStartedEvent extends BaseEvent {
    private final String version;
    private final long startupTime;

    /**
     * Creates a new application started event.
     *
     * @param source the source of this event
     * @param version the application version
     * @param startupTime the startup time in milliseconds
     */
    public ApplicationStartedEvent(String source, String version, long startupTime) {
        super(source);
        this.version = version;
        this.startupTime = startupTime;
    }

    /**
     * Returns the application version.
     *
     * @return the version string
     */
    public String getVersion() {
        return version;
    }

    /**
     * Returns the startup time in milliseconds.
     *
     * @return the startup time
     */
    public long getStartupTime() {
        return startupTime;
    }
}