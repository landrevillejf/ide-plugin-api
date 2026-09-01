package com.protonmail.landrevillejf.ide.plugin.events;

import lombok.Getter;

/**
 * Event fired when a plugin adds a menu item.
 * <p>
 * This event contains information about the plugin that added the menu item.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
@Getter
public class PluginMenuAddedEvent {
    private final String pluginName;
    private final String pluginClass;

    /**
     * Creates a new plugin menu added event.
     *
     * @param pluginName the plugin name
     * @param pluginClass the plugin class
     */
    public PluginMenuAddedEvent(String pluginName, String pluginClass) {
        this.pluginName = pluginName;
        this.pluginClass = pluginClass;
    }
}
