package com.protonmail.landrevillejf.ide.plugin.events;

/**
 * Event fired when a plugin is unloaded.
 * <p>
 * This event is published after a plugin has been successfully unloaded
 * from memory.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
public class PluginUnloadedEvent extends BaseEvent {
    private final String pluginId;
    private final String pluginName;

    /**
     * Creates a new plugin unloaded event.
     *
     * @param source the source of this event
     * @param pluginId the plugin identifier
     * @param pluginName the plugin name
     */
    public PluginUnloadedEvent(String source, String pluginId, String pluginName) {
        super(source);
        this.pluginId = pluginId;
        this.pluginName = pluginName;
    }

    /**
     * Returns the plugin identifier.
     *
     * @return the plugin ID
     */
    public String getPluginId() {
        return pluginId;
    }

    /**
     * Returns the plugin name.
     *
     * @return the plugin name
     */
    public String getPluginName() {
        return pluginName;
    }
}