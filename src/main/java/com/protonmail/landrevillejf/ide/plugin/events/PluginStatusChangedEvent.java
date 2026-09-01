package com.protonmail.landrevillejf.ide.plugin.events;

import com.protonmail.landrevillejf.ide.plugin.PluginStatus;

/**
 * Event fired when a plugin's status changes.
 * <p>
 * This event is published whenever a plugin transitions from one status
 * to another (e.g., from DISABLED to ENABLED).
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
public class PluginStatusChangedEvent {
    private final String pluginName;
    private final PluginStatus status;

    /**
     * Creates a new plugin status changed event.
     *
     * @param pluginName the name of the plugin
     * @param status the new status
     */
    public PluginStatusChangedEvent(String pluginName, PluginStatus status) {
        this.pluginName = pluginName;
        this.status = status;
    }

    /**
     * Returns the plugin name.
     *
     * @return the plugin name
     */
    public String getPluginName() {
        return pluginName;
    }

    /**
     * Returns the new status.
     *
     * @return the plugin status
     */
    public PluginStatus getStatus() {
        return status;
    }
}