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
    public UIComponentBuilder(final PluginContext context, final String pluginId) {
        if (context == null) {
            throw new IllegalArgumentException("context cannot be null");
        }
        if (pluginId == null || pluginId.isBlank()) {
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
    public UIComponentBuilder addTab(final String componentId, final String title, final JComponent component) {
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
    public UIComponentBuilder addTab(final String componentId, final String title, final JComponent component, final String iconPath) {
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
    public UIComponentBuilder addTab(final String componentId, final String title, final JComponent component, final Icon icon) {
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
    public UIComponentBuilder addBottomPanel(final String componentId, final String title, final JComponent component) {
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
    public UIComponentBuilder addBottomPanel(final String componentId, final String title, final JComponent component, final String iconPath) {
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
    public UIComponentBuilder addBottomPanel(final String componentId, final String title, final JComponent component, final Icon icon) {
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
    public UIComponentBuilder addLeftSidebar(final String componentId, final String title, final JComponent component) {
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
    public UIComponentBuilder addLeftSidebar(final String componentId, final String title, final JComponent component, final Icon icon) {
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
    public UIComponentBuilder addRightSidebar(final String componentId, final String title, final JComponent component) {
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
    public UIComponentBuilder addRightSidebar(final String componentId, final String title, final JComponent component, final Icon icon) {
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
    public UIComponentBuilder addDockablePanel(final String componentId, final String title, final JComponent component) {
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
    public UIComponentBuilder addDockablePanel(final String componentId, final String title, final JComponent component, final Icon icon) {
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
    public UIComponentBuilder addStatusBarComponent(final String componentId, final JComponent component) {
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
    public UIComponentBuilder addStatusBarComponent(final String componentId, final JComponent component, final Icon icon) {
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
            final String componentId,
            final UIComponent.ComponentType type,
            final String title,
            final JComponent component,
            final String iconPath
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
            final String componentId,
            final UIComponent.ComponentType type,
            final String title,
            final JComponent component,
            final String iconPath,
            final int order,
            final boolean removable
    ) {
        try {
            final UIComponent uiComponent = new UIComponent(
                    componentId,
                    type,
                    title,
                    component,
                    iconPath,
                    order,
                    removable
            );
            components.add(uiComponent);
            if (log.isDebugEnabled()) {
                log.debug("Added component to builder: {}", componentId);
            }
        } catch (IllegalArgumentException e) {
            if (log.isErrorEnabled()) {
                log.error("Failed to add component: {}", componentId, e);
            }
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
            final String componentId,
            final UIComponent.ComponentType type,
            final String title,
            final JComponent component,
            final Icon icon
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
            final String componentId,
            final UIComponent.ComponentType type,
            final String title,
            final JComponent component,
            final Icon icon,
            final int order,
            final boolean removable
    ) {
        try {
            final UIComponent uiComponent = new UIComponent(
                    componentId,
                    type,
                    title,
                    component,
                    icon,
                    order,
                    removable
            );
            components.add(uiComponent);
            if (log.isDebugEnabled()) {
                log.debug("Added component to builder: {}", componentId);
            }
        } catch (IllegalArgumentException e) {
            if (log.isErrorEnabled()) {
                log.error("Failed to add component: {}", componentId, e);
            }
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
        final ComponentRegistry registry = this.context.getComponentRegistry();
        int registered = 0;

        for (final UIComponent component : this.components) {
            // Ajouter le clientProperty au composant AVANT l'enregistrement
            final JComponent jComponent = component.getComponent();
            if (jComponent != null) {
                jComponent.putClientProperty("ui_component_id", component.getComponentId());
            }

            final boolean success = registerComponent(registry, component);
            if (success) {
                ++registered;
            }
        }

        return registered;
    }

    /**
     * Register a single component.
     */
    private boolean registerComponent(final ComponentRegistry registry, final UIComponent component) {
        try {
            registry.registerComponent(component, this.pluginId);
            if (log.isInfoEnabled()) {
                log.info("Registered component: {} of type {}", component.getComponentId(), component.getType());
            }
            return true;
        } catch (IllegalArgumentException e) {
            if (log.isWarnEnabled()) {
                log.warn("Failed to register component: {}", component.getComponentId(), e);
            }
            return false;
        }
    }

    /**
     * Registers a single component.
     *
     * @param component the component to register
     * @return {@code true} if registration was successful
     */
    public boolean register(final UIComponent component) {
        if (component == null) {
            return false;
        }
        final ComponentRegistry registry = context.getComponentRegistry();
        return registerComponent(registry, component);
    }

    /**
     * Unregisters all components that were added through this builder.
     *
     * @return the number of components successfully unregistered
     */
    public int unregisterAll() {
        final ComponentRegistry registry = context.getComponentRegistry();
        int unregistered = 0;

        for (final UIComponent component : components) {
            final boolean success = unregisterComponent(registry, component);
            if (success) {
                unregistered++;
            }
        }

        return unregistered;
    }

    /**
     * Unregister a single component.
     */
    private boolean unregisterComponent(final ComponentRegistry registry, final UIComponent component) {
        final boolean success = registry.unregisterComponent(component.getComponentId(), pluginId);
        if (success && log.isInfoEnabled()) {
            log.info("Unregistered component: {}", component.getComponentId());
        }
        return success;
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
    public UIComponentBuilder selectTab(final String componentId) {
        publishSelectTabEvent(componentId);
        return this;
    }

    /**
     * Publishes a SelectTabEvent to select a tab.
     */
    private void publishSelectTabEvent(final String componentId) {
        // Essayer d'abord via l'EventBus
        com.protonmail.landrevillejf.swingide.core.bus.EventBus eventBus = null;
        try {
            eventBus = context.getService(com.protonmail.landrevillejf.swingide.core.bus.EventBus.class);
        } catch (RuntimeException e) {
            if (log.isDebugEnabled()) {
                log.debug("EventBus not available via service", e);
            }
        }

        if (eventBus != null) {
            final SelectTabEvent event = new SelectTabEvent(componentId, pluginId);
            eventBus.publish(event);
            if (log.isInfoEnabled()) {
                log.info("Published SelectTabEvent for component: {}", componentId);
            }
        } else if (log.isInfoEnabled()) {
            log.info("EventBus not available, tab '{}' registered but not auto-selected", componentId);
        }
    }

    /**
     * Unregisters a specific component by its ID.
     *
     * @param componentId the ID of the component to unregister
     * @return true if the component was unregistered, false otherwise
     */
    public boolean unregisterComponent(final String componentId) {
        final ComponentRegistry registry = context.getComponentRegistry();
        final boolean unregistered = registry.unregisterComponent(componentId, pluginId);
        if (unregistered) {
            // Also remove from our internal list if present
            components.removeIf(comp -> comp.getComponentId().equals(componentId));
            if (log.isInfoEnabled()) {
                log.info("Unregistered component: {}", componentId);
            }
        } else if (log.isWarnEnabled()) {
            log.warn("Failed to unregister component: {}", componentId);
        }
        return unregistered;
    }
}