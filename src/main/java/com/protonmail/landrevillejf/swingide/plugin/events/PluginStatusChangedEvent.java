package com.protonmail.landrevillejf.swingide.plugin.events;

import com.protonmail.landrevillejf.swingide.plugin.PluginStatus;

public class PluginStatusChangedEvent {
    private final String pluginName;
    private final PluginStatus status;

    public PluginStatusChangedEvent(String pluginName, PluginStatus status) {
        this.pluginName = pluginName;
        this.status = status;
    }

    public String getPluginName() {
        return pluginName;
    }

    public PluginStatus getStatus() {
        return status;
    }
}