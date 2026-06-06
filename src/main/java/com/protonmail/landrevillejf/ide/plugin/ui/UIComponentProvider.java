package com.protonmail.landrevillejf.ide.plugin.ui;

import java.util.List;

/**
 * @author landrevillejf
 * @version 1.1.0
 */
@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface UIComponentProvider {

    /**
     * Returns a list of UI components that this plugin wants to contribute to the IDE.
     *
     * @return A list of {@link UIComponent} objects, or an empty list if no components.
     */
    List<UIComponent> provideComponents();

    /**
     * Called when a component is added to the IDE successfully.
     *
     * @param componentId The ID of the component that was added.
     */
    default void onComponentAdded(String componentId) {
        // Optional override
    }

    /**
     * Called when a component is removed from the IDE.
     *
     * @param componentId The ID of the component that was removed.
     */
    default void onComponentRemoved(String componentId) {
        // Optional override
    }

    /**
     * Returns whether this plugin wants to manage component lifecycle automatically.
     * If false, the IDE will handle showing/hiding components as the plugin is enabled/disabled.
     * If true, the plugin must handle this itself.
     *
     * @return {@code true} if the plugin manages lifecycle, {@code false} if IDE should manage it.
     */
    default boolean managesComponentLifecycle() {
        return false;
    }
}

