package com.protonmail.landrevillejf.ide.plugin.events;

import lombok.Getter;

/**
 * Event fired when a tab is selected in the IDE.
 * <p>
 * This event contains information about the component and plugin
 * associated with the selected tab.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
@Getter
public class SelectTabEvent {
    private final String componentId;
    private final String pluginId;

    /**
     * Creates a new select tab event.
     *
     * @param componentId the component identifier of the selected tab
     * @param pluginId    the plugin identifier that owns the tab
     */
    public SelectTabEvent(String componentId, String pluginId) {
        this.componentId = componentId;
        this.pluginId = pluginId;
    }
}