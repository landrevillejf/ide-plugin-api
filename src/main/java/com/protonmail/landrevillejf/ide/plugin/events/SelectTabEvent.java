package com.protonmail.landrevillejf.ide.plugin.events;

import lombok.Getter;

@Getter
public class SelectTabEvent {
    private final String componentId;
    private final String pluginId;

    public SelectTabEvent(String componentId, String pluginId) {
        this.componentId = componentId;
        this.pluginId = pluginId;
    }
}