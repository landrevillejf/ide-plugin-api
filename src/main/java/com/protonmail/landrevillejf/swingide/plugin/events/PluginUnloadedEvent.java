package com.protonmail.landrevillejf.swingide.plugin.events;

public class PluginUnloadedEvent extends BaseEvent {
    private final String pluginId;
    private final String pluginName;

    public PluginUnloadedEvent(String source, String pluginId, String pluginName) {
        super(source);
        this.pluginId = pluginId;
        this.pluginName = pluginName;
    }

    public String getPluginId() {
        return pluginId;
    }

    public String getPluginName() {
        return pluginName;
    }
}