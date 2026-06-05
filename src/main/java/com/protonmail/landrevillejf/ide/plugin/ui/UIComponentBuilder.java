package com.protonmail.landrevillejf.ide.plugin.ui;

import com.protonmail.landrevillejf.ide.plugin.PluginContext;
import com.protonmail.landrevillejf.ide.plugin.events.SelectTabEvent;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

 /**
 * @author landrevillejf
 * @version 1.1.0
 */
@Slf4j
public class UIComponentBuilder {
    private final PluginContext context;
    private final String pluginId;
    private final List<UIComponent> components = new ArrayList<>();

    /**
     * Creates a new UI component builder.
     *
     * @param context the plugin context
     * @param pluginId the ID of the plugin using this builder
     */
    public UIComponentBuilder(PluginContext context, String pluginId) {
        if (context == null) {
            throw new IllegalArgumentException("context cannot be null");
        }
        if (pluginId == null || pluginId.trim().isEmpty()) {
            throw new IllegalArgumentException("pluginId cannot be null or empty");
        }

        this.context = context;
        this.pluginId = pluginId;
    }

    // ==================== ADD TAB METHODS ====================

    /**
     * Adds an IDE tab component.
     *
     * @param componentId unique identifier for this component
     * @param title display title for the tab
     * @param component the Swing component to display
     * @return this builder for chaining
     */
    public UIComponentBuilder addTab(String componentId, String title, JComponent component) {
        return addTab(componentId, title, component, (String) null);
    }

    /**
     * Adds an IDE tab component with an icon path.
     *
     * @param componentId unique identifier for this component
     * @param title display title for the tab
     * @param component the Swing component to display
     * @param iconPath path to the icon resource
     * @return this builder for chaining
     */
    public UIComponentBuilder addTab(String componentId, String title, JComponent component, String iconPath) {
        return addComponent(
                componentId,
                UIComponent.ComponentType.IDE_TAB,
                title,
                component,
                iconPath
        );
    }

    /**
     * Adds an IDE tab component with an Icon.
     *
     * @param componentId unique identifier for this component
     * @param title display title for the tab
     * @param component the Swing component to display
     * @param icon the Icon for the tab
     * @return this builder for chaining
     */
    public UIComponentBuilder addTab(String componentId, String title, JComponent component, Icon icon) {
        return addComponent(
                componentId,
                UIComponent.ComponentType.IDE_TAB,
                title,
                component,
                icon
        );
    }

    // ==================== ADD BOTTOM PANEL METHODS ====================

    /**
     * Adds a bottom panel component.
     *
     * @param componentId unique identifier for this component
     * @param title display title for the panel
     * @param component the Swing component to display
     * @return this builder for chaining
     */
    public UIComponentBuilder addBottomPanel(String componentId, String title, JComponent component) {
        return addBottomPanel(componentId, title, component, (String) null);
    }

    /**
     * Adds a bottom panel component with an icon path.
     *
     * @param componentId unique identifier for this component
     * @param title display title for the panel
     * @param component the Swing component to display
     * @param iconPath path to the icon resource
     * @return this builder for chaining
     */
    public UIComponentBuilder addBottomPanel(String componentId, String title, JComponent component, String iconPath) {
        return addComponent(
                componentId,
                UIComponent.ComponentType.BOTTOM_PANEL,
                title,
                component,
                iconPath
        );
    }

    /**
     * Adds a bottom panel component with an Icon.
     *
     * @param componentId unique identifier for this component
     * @param title display title for the panel
     * @param component the Swing component to display
     * @param icon the Icon for the panel
     * @return this builder for chaining
     */
    public UIComponentBuilder addBottomPanel(String componentId, String title, JComponent component, Icon icon) {
        return addComponent(
                componentId,
                UIComponent.ComponentType.BOTTOM_PANEL,
                title,
                component,
                icon
        );
    }

    // ==================== ADD SIDEBAR METHODS ====================

    /**
     * Adds a left sidebar panel component.
     *
     * @param componentId unique identifier for this component
     * @param title display title for the panel
     * @param component the Swing component to display
     * @return this builder for chaining
     */
    public UIComponentBuilder addLeftSidebar(String componentId, String title, JComponent component) {
        return addComponent(
                componentId,
                UIComponent.ComponentType.LEFT_SIDEBAR,
                title,
                component,
                (String) null
        );
    }

    /**
     * Adds a left sidebar panel component with an Icon.
     *
     * @param componentId unique identifier for this component
     * @param title display title for the panel
     * @param component the Swing component to display
     * @param icon the Icon for the panel
     * @return this builder for chaining
     */
    public UIComponentBuilder addLeftSidebar(String componentId, String title, JComponent component, Icon icon) {
        return addComponent(
                componentId,
                UIComponent.ComponentType.LEFT_SIDEBAR,
                title,
                component,
                icon
        );
    }

    /**
     * Adds a right sidebar panel component.
     *
     * @param componentId unique identifier for this component
     * @param title display title for the panel
     * @param component the Swing component to display
     * @return this builder for chaining
     */
    public UIComponentBuilder addRightSidebar(String componentId, String title, JComponent component) {
        return addComponent(
                componentId,
                UIComponent.ComponentType.RIGHT_SIDEBAR,
                title,
                component,
                (String) null
        );
    }

    /**
     * Adds a right sidebar panel component with an Icon.
     *
     * @param componentId unique identifier for this component
     * @param title display title for the panel
     * @param component the Swing component to display
     * @param icon the Icon for the panel
     * @return this builder for chaining
     */
    public UIComponentBuilder addRightSidebar(String componentId, String title, JComponent component, Icon icon) {
        return addComponent(
                componentId,
                UIComponent.ComponentType.RIGHT_SIDEBAR,
                title,
                component,
                icon
        );
    }

    // ==================== ADD DOCKABLE PANEL METHODS ====================

    /**
     * Adds a dockable panel (tool window) component.
     *
     * @param componentId unique identifier for this component
     * @param title display title for the panel
     * @param component the Swing component to display
     * @return this builder for chaining
     */
    public UIComponentBuilder addDockablePanel(String componentId, String title, JComponent component) {
        return addComponent(
                componentId,
                UIComponent.ComponentType.DOCKABLE_PANEL,
                title,
                component,
                (String) null
        );
    }

    /**
     * Adds a dockable panel (tool window) component with an Icon.
     *
     * @param componentId unique identifier for this component
     * @param title display title for the panel
     * @param component the Swing component to display
     * @param icon the Icon for the panel
     * @return this builder for chaining
     */
    public UIComponentBuilder addDockablePanel(String componentId, String title, JComponent component, Icon icon) {
        return addComponent(
                componentId,
                UIComponent.ComponentType.DOCKABLE_PANEL,
                title,
                component,
                icon
        );
    }

    // ==================== ADD STATUS BAR METHODS ====================

    /**
     * Adds a status bar component.
     *
     * @param componentId unique identifier for this component
     * @param component the Swing component to display in the status bar
     * @return this builder for chaining
     */
    public UIComponentBuilder addStatusBarComponent(String componentId, JComponent component) {
        return addComponent(
                componentId,
                UIComponent.ComponentType.STATUS_BAR_COMPONENT,
                "",
                component,
                (String) null
        );
    }

    /**
     * Adds a status bar component with an Icon.
     *
     * @param componentId unique identifier for this component
     * @param component the Swing component to display in the status bar
     * @param icon the Icon for the component
     * @return this builder for chaining
     */
    public UIComponentBuilder addStatusBarComponent(String componentId, JComponent component, Icon icon) {
        return addComponent(
                componentId,
                UIComponent.ComponentType.STATUS_BAR_COMPONENT,
                "",
                component,
                icon
        );
    }

    // ==================== ADD COMPONENT METHODS (CORE) ====================

    /**
     * Adds a generic component with full control over parameters using icon path.
     *
     * @param componentId unique identifier for this component
     * @param type the type of component
     * @param title display title
     * @param component the Swing component
     * @param iconPath optional path to icon resource
     * @return this builder for chaining
     */
    public UIComponentBuilder addComponent(
            String componentId,
            UIComponent.ComponentType type,
            String title,
            JComponent component,
            String iconPath
    ) {
        return addComponent(componentId, type, title, component, iconPath, Integer.MAX_VALUE, true);
    }

    /**
     * Adds a generic component with full control over all parameters using icon path.
     *
     * @param componentId unique identifier for this component
     * @param type the type of component
     * @param title display title
     * @param component the Swing component
     * @param iconPath optional path to icon resource
     * @param order display order (lower numbers appear first)
     * @param removable whether the user can remove this component
     * @return this builder for chaining
     */
    public UIComponentBuilder addComponent(
            String componentId,
            UIComponent.ComponentType type,
            String title,
            JComponent component,
            String iconPath,
            int order,
            boolean removable
    ) {
        try {
            UIComponent uiComponent = new UIComponent(
                    componentId,
                    type,
                    title,
                    component,
                    iconPath,
                    order,
                    removable
            );
            components.add(uiComponent);
            log.debug("Added component to builder: {}", componentId);
        } catch (IllegalArgumentException e) {
            log.error("Failed to add component: {}", componentId, e);
            throw e;
        }
        return this;
    }

    /**
     * Adds a generic component with full control over parameters using Icon.
     *
     * @param componentId unique identifier for this component
     * @param type the type of component
     * @param title display title
     * @param component the Swing component
     * @param icon the Icon for the component
     * @return this builder for chaining
     */
    public UIComponentBuilder addComponent(
            String componentId,
            UIComponent.ComponentType type,
            String title,
            JComponent component,
            Icon icon
    ) {
        return addComponent(componentId, type, title, component, icon, Integer.MAX_VALUE, true);
    }

    /**
     * Adds a generic component with full control over all parameters using Icon.
     *
     * @param componentId unique identifier for this component
     * @param type the type of component
     * @param title display title
     * @param component the Swing component
     * @param icon the Icon for the component
     * @param order display order (lower numbers appear first)
     * @param removable whether the user can remove this component
     * @return this builder for chaining
     */
    public UIComponentBuilder addComponent(
            String componentId,
            UIComponent.ComponentType type,
            String title,
            JComponent component,
            Icon icon,
            int order,
            boolean removable
    ) {
        try {
            UIComponent uiComponent = new UIComponent(
                    componentId,
                    type,
                    title,
                    component,
                    icon,
                    order,
                    removable
            );
            components.add(uiComponent);
            log.debug("Added component to builder: {}", componentId);
        } catch (IllegalArgumentException e) {
            log.error("Failed to add component: {}", componentId, e);
            throw e;
        }
        return this;
    }

    // ==================== REGISTRATION METHODS ====================

    /**
     * Registers all components that have been added to this builder.
     *
     * @return the number of components successfully registered
     */
    public int registerAll() {
        ComponentRegistry registry = this.context.getComponentRegistry();
        int registered = 0;

        for(UIComponent component : this.components) {
            try {
                // Ajouter le clientProperty au composant AVANT l'enregistrement
                JComponent jComponent = component.getComponent();
                if (jComponent != null) {
                    jComponent.putClientProperty("ui_component_id", component.getComponentId());
                }

                registry.registerComponent(component, this.pluginId);
                log.info("Registered component: {} of type {}", component.getComponentId(), component.getType());
                ++registered;
            } catch (IllegalArgumentException e) {
                log.warn("Failed to register component: {}", component.getComponentId(), e);
            }
        }

        return registered;
    }

    /**
     * Registers a single component.
     *
     * @param component the component to register
     * @return {@code true} if registration was successful
     */
    public boolean register(UIComponent component) {
        try {
            context.getComponentRegistry().registerComponent(component, pluginId);
            log.info("Registered component: {} of type {}",
                    component.getComponentId(), component.getType());
            return true;
        } catch (IllegalArgumentException e) {
            log.warn("Failed to register component: {}",
                    component.getComponentId(), e);
            return false;
        }
    }

    /**
     * Unregisters all components that were added through this builder.
     *
     * @return the number of components successfully unregistered
     */
    public int unregisterAll() {
        ComponentRegistry registry = context.getComponentRegistry();
        int unregistered = 0;

        for (UIComponent component : components) {
            if (registry.unregisterComponent(component.getComponentId(), pluginId)) {
                log.info("Unregistered component: {}", component.getComponentId());
                unregistered++;
            }
        }

        return unregistered;
    }

    /**
     * Gets the list of components that have been added to this builder.
     *
     * @return unmodifiable list of components
     */
    public List<UIComponent> getComponents() {
        return new ArrayList<>(components);
    }

    /**
     * Clears all components from the builder.
     *
     * @return this builder for chaining
     */
    public UIComponentBuilder clear() {
        components.clear();
        return this;
    }

    /**
     * Selects (activates) a tab in the main IDE tab pane by its component ID.
     * This method publishes a SelectTabEvent that the MainWindow will handle.
     *
     * @param componentId the ID of the component whose tab should be selected
     * @return this builder for chaining
     */
    public UIComponentBuilder selectTab(String componentId) {
        try {
            // Essayer d'abord via l'EventBus
            com.protonmail.landrevillejf.swingide.core.bus.EventBus eventBus = null;
            try {
                eventBus = context.getService(com.protonmail.landrevillejf.swingide.core.bus.EventBus.class);
            } catch (Exception e) {
                log.debug("EventBus not available via service", e);
            }

            if (eventBus != null) {
                SelectTabEvent event = new SelectTabEvent(componentId, pluginId);
                eventBus.publish(event);
                log.info("Published SelectTabEvent for component: {}", componentId);
            } else {
                log.info("EventBus not available, tab '{}' registered but not auto-selected", componentId);
                // Ne pas lancer d'exception, juste logguer
            }
        } catch (Exception e) {
            log.warn("Could not select tab '{}': {}", componentId, e.getMessage());
        }
        return this;
    }

    /**
     * Unregisters a specific component by its ID.
     *
     * @param componentId the ID of the component to unregister
     * @return true if the component was unregistered, false otherwise
     */
    public boolean unregisterComponent(String componentId) {
        ComponentRegistry registry = context.getComponentRegistry();
        boolean unregistered = registry.unregisterComponent(componentId, pluginId);
        if (unregistered) {
            // Also remove from our internal list if present
            components.removeIf(comp -> comp.getComponentId().equals(componentId));
            log.info("Unregistered component: {}", componentId);
        } else {
            log.warn("Failed to unregister component: {}", componentId);
        }
        return unregistered;
    }
}