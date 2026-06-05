package com.protonmail.landrevillejf.ide.plugin.events;

public class PluginDisabledEvent {
    private final String pluginName;

    public PluginDisabledEvent(String pluginName) {
        this.pluginName = pluginName;
    }

    public String getPluginName() {
        return pluginName;
    }
}
