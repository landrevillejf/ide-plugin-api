package com.protonmail.landrevillejf.ide.plugin.events;

import java.time.LocalDateTime;

public abstract class BaseEvent implements Event {
    private final LocalDateTime timestamp;
    private final String source;

    public BaseEvent(String source) {
        this.timestamp = LocalDateTime.now();
        this.source = source;
    }

    @Override
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String getSource() {
        return source;
    }
}