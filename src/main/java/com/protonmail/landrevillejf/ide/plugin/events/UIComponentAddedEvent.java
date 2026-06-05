package com.protonmail.landrevillejf.ide.plugin.events;

import com.protonmail.landrevillejf.ide.plugin.ui.UIComponent;

/**
 * Event published when a plugin requests to add a UI component to the IDE.
 *
 * <p>
 * This event is fired through the PluginEventBus when a plugin wants to register
 * UI components with the IDE.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 */
public class UIComponentAddedEvent extends BaseEvent {
    private final UIComponent component;
    private final String pluginId;
    private boolean handled = false;

    /**
     * Creates a new UI component added event.
     *
     * @param source the source string (typically the plugin name or ID)
     * @param component the component being added
     * @param pluginId the ID of the plugin adding the component
     */
    public UIComponentAddedEvent(String source, UIComponent component, String pluginId) {
        super(source);
        this.component = component;
        this.pluginId = pluginId;
    }

    /**
     * Gets the component being added.
     *
     * @return the component
     */
    public UIComponent getComponent() {
        return component;
    }

    /**
     * Gets the ID of the plugin adding this component.
     *
     * @return the plugin ID
     */
    public String getPluginId() {
        return pluginId;
    }

    /**
     * Marks this event as handled by the IDE.
     *
     * @param handled {@code true} if the IDE successfully handled the request
     */
    public void setHandled(boolean handled) {
        this.handled = handled;
    }

    /**
     * Checks if this event was handled by the IDE.
     *
     * @return {@code true} if handled, {@code false} otherwise
     */
    public boolean isHandled() {
        return handled;
    }
}

