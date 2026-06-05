package com.protonmail.landrevillejf.swingide.plugin.events;

import java.time.LocalDateTime;

// Base interface for all events
public interface Event {
    // Marker interface
    default LocalDateTime getTimestamp() {
        return LocalDateTime.now();
    }
    String getSource();
}