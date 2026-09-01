package com.protonmail.landrevillejf.ide.plugin.events;

/**
 * Event fired when a plugin is enabled.
 * <p>
 * This event is published after a plugin has been successfully enabled.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
public class PluginEnabledEvent {
    private final String pluginName;

    /**
     * Creates a new plugin enabled event.
     *
     * @param pluginName the name of the plugin that was enabled
     */
    public PluginEnabledEvent(String pluginName) {
        this.pluginName = pluginName;
    }

    /**
     * Returns the name of the plugin that was enabled.
     *
     * @return the plugin name
     */
    public String getPluginName() {
        return pluginName;
    }
}