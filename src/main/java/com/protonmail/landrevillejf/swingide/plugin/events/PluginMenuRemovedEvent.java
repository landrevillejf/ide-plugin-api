package com.protonmail.landrevillejf.swingide.plugin.events;

import lombok.Getter;

@Getter
public class PluginMenuRemovedEvent {
    private final String pluginName;
    private final String pluginClass;

    public PluginMenuRemovedEvent(String pluginName, String pluginClass) {
        this.pluginName = pluginName;
        this.pluginClass = pluginClass;
    }
}