package com.protonmail.landrevillejf.ide.plugin.events;

public class ApplicationStartedEvent extends BaseEvent {
    private final String version;
    private final long startupTime;

    public ApplicationStartedEvent(String source, String version, long startupTime) {
        super(source);
        this.version = version;
        this.startupTime = startupTime;
    }

    public String getVersion() {
        return version;
    }

    public long getStartupTime() {
        return startupTime;
    }
}