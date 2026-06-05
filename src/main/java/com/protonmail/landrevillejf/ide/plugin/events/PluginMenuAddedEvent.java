package com.protonmail.landrevillejf.ide.plugin.events;

import lombok.Getter;

@Getter
public class PluginMenuAddedEvent {
    private final String pluginName;
    private final String pluginClass;

    public PluginMenuAddedEvent(String pluginName, String pluginClass) {
        this.pluginName = pluginName;
        this.pluginClass = pluginClass;
    }
}
