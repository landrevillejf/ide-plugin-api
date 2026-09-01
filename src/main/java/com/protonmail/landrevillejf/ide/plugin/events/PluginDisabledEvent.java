package com.protonmail.landrevillejf.ide.plugin.events;

/**
 * Event fired when a plugin is disabled.
 * <p>
 * This event is published after a plugin has been successfully disabled.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
public class PluginDisabledEvent {
    private final String pluginName;

    /**
     * Creates a new plugin disabled event.
     *
     * @param pluginName the name of the plugin that was disabled
     */
    public PluginDisabledEvent(String pluginName) {
        this.pluginName = pluginName;
    }

    /**
     * Returns the name of the plugin that was disabled.
     *
     * @return the plugin name
     */
    public String getPluginName() {
        return pluginName;
    }
}
