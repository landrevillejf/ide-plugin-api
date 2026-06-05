package com.protonmail.landrevillejf.swingide.plugin.events;

// Generic event listener
public interface EventListener<T extends Event> {
    void onEvent(T event);
}