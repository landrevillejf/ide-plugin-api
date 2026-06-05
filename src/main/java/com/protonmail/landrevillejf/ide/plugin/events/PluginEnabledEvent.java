package com.protonmail.landrevillejf.ide.plugin.events;

public class PluginEnabledEvent {
    private final String pluginName;

    public PluginEnabledEvent(String pluginName) {
        this.pluginName = pluginName;
    }

    public String getPluginName() {
        return pluginName;
    }
}