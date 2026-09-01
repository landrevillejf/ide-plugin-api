package com.protonmail.landrevillejf.ide.plugin.events;

import lombok.Getter;

/**
 * Event fired when a plugin is loaded.
 * <p>
 * This event is published after a plugin has been successfully loaded
 * from its JAR file but before it is initialized.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
@Getter
public class PluginLoadedEvent extends BaseEvent {
    private final String pluginId;
    private final String pluginName;
    private final String pluginVersion;

    /**
     * Creates a new plugin loaded event.
     *
     * @param source the source of this event
     * @param pluginId the plugin identifier
     * @param pluginName the plugin name
     * @param pluginVersion the plugin version
     */
    public PluginLoadedEvent(String source, String pluginId, String pluginName, String pluginVersion) {
        super(source);
        this.pluginId = pluginId;
        this.pluginName = pluginName;
        this.pluginVersion = pluginVersion;
    }

}


