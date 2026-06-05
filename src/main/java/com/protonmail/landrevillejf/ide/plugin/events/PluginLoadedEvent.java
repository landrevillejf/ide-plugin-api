package com.protonmail.landrevillejf.ide.plugin.events;

public class PluginLoadedEvent extends BaseEvent {
    private final String pluginId;
    private final String pluginName;
    private final String pluginVersion;

    public PluginLoadedEvent(String source, String pluginId, String pluginName, String pluginVersion) {
        super(source);
        this.pluginId = pluginId;
        this.pluginName = pluginName;
        this.pluginVersion = pluginVersion;
    }

    public String getPluginId() {
        return pluginId;
    }

    public String getPluginName() {
        return pluginName;
    }

    public String getPluginVersion() {
        return pluginVersion;
    }
}


