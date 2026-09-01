package com.protonmail.landrevillejf.ide.plugin.events;

import lombok.Getter;

/**
 * Event fired when a plugin removes a menu item.
 * <p>
 * This event contains information about the plugin that removed the menu item.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
@Getter
public class PluginMenuRemovedEvent {
    private final String pluginName;
    private final String pluginClass;

    /**
     * Creates a new plugin menu removed event.
     *
     * @param pluginName the plugin name
     * @param pluginClass the plugin class
     */
    public PluginMenuRemovedEvent(String pluginName, String pluginClass) {
        this.pluginName = pluginName;
        this.pluginClass = pluginClass;
    }
}