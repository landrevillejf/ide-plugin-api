package com.protonmail.landrevillejf.ide.plugin.events;

// Generic event listener
public interface EventListener<T extends Event> {
    void onEvent(T event);
}