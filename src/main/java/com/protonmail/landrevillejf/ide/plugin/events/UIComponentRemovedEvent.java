package com.protonmail.landrevillejf.ide.plugin.events;

/**
 * Event published when a UI component provided by a plugin is removed from the IDE.
 *
 * <p>
 * This event is fired through the PluginEventBus when a component is unregistered
 * from the IDE, either by user action or when the plugin is disabled.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 */
public class UIComponentRemovedEvent extends BaseEvent {
    private final String componentId;
    private final String pluginId;
    private final String componentType;

    /**
     * Creates a new UI component removed event.
     *
     * @param source the source string (typically the IDE or plugin name)
     * @param componentId the ID of the component being removed
     * @param pluginId the ID of the plugin that provided the component
     * @param componentType the type of the component
     */
    public UIComponentRemovedEvent(String source, String componentId, String pluginId, String componentType) {
        super(source);
        this.componentId = componentId;
        this.pluginId = pluginId;
        this.componentType = componentType;
    }

    /**
     * Gets the ID of the component being removed.
     *
     * @return the component ID
     */
    public String getComponentId() {
        return componentId;
    }

    /**
     * Gets the ID of the plugin that provided the component.
     *
     * @return the plugin ID
     */
    public String getPluginId() {
        return pluginId;
    }

    /**
     * Gets the type of the component being removed.
     *
     * @return the component type
     */
    public String getComponentType() {
        return componentType;
    }
}

