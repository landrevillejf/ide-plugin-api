package com.protonmail.landrevillejf.swingide.plugin.ui;

import javax.swing.*;
import java.io.Serializable;

/**
 * Represents a UI component that a plugin wants to add to the IDE.
 *
 * <p>
 * This class encapsulates all the information needed to add a component to the IDE,
 * including its type, location, and visual properties.
 * </p>
 *
 * @author landrevillejf
 * @version 1.1.0
 */
public class UIComponent implements Serializable {
    private static final long serialVersionUID = 2L;

    private final String componentId;
    private final ComponentType type;
    private final String title;
    private final JComponent component;
    private final String iconPath;
    private transient Icon icon;  // transient pour ne pas sérialiser l'icône
    private final int order;
    private final boolean removable;

    /**
     * Creates a new UI component.
     *
     * @param componentId Unique identifier for this component
     * @param type The type of component (IDE_TAB, BOTTOM_PANEL, TOOLBAR_BUTTON, etc.)
     * @param title Display title or label for the component
     * @param component The actual Swing component
     * @param iconPath Optional path to an icon resource
     */
    public UIComponent(
            String componentId,
            ComponentType type,
            String title,
            JComponent component,
            String iconPath
    ) {
        this(componentId, type, title, component, iconPath, (Icon)null, Integer.MAX_VALUE, true);
    }

    /**
     * Creates a new UI component with an Icon.
     *
     * @param componentId Unique identifier for this component
     * @param type The type of component
     * @param title Display title or label for the component
     * @param component The actual Swing component
     * @param icon The icon for the component
     */
    public UIComponent(
            String componentId,
            ComponentType type,
            String title,
            JComponent component,
            Icon icon
    ) {
        this(componentId, type, title, component, null, icon, Integer.MAX_VALUE, true);
    }

    /**
     * Creates a new UI component with ordering using icon path.
     *
     * @param componentId Unique identifier for this component
     * @param type The type of component
     * @param title Display title or label for the component
     * @param component The actual Swing component
     * @param iconPath Optional path to an icon resource
     * @param order Display order (lower numbers appear first)
     * @param removable Whether the user can remove this component
     */
    public UIComponent(
            String componentId,
            ComponentType type,
            String title,
            JComponent component,
            String iconPath,
            int order,
            boolean removable
    ) {
        this(componentId, type, title, component, iconPath, null, order, removable);
    }

    /**
     * Creates a new UI component with ordering using Icon.
     *
     * @param componentId Unique identifier for this component
     * @param type The type of component
     * @param title Display title or label for the component
     * @param component The actual Swing component
     * @param icon The icon for the component
     * @param order Display order (lower numbers appear first)
     * @param removable Whether the user can remove this component
     */
    public UIComponent(
            String componentId,
            ComponentType type,
            String title,
            JComponent component,
            Icon icon,
            int order,
            boolean removable
    ) {
        this(componentId, type, title, component, null, icon, order, removable);
    }

    /**
     * Creates a new UI component with both icon path and Icon (icon takes precedence).
     *
     * @param componentId Unique identifier for this component
     * @param type The type of component
     * @param title Display title or label for the component
     * @param component The actual Swing component
     * @param iconPath Optional path to an icon resource
     * @param icon The icon for the component (takes precedence over iconPath)
     * @param order Display order (lower numbers appear first)
     * @param removable Whether the user can remove this component
     */
    private UIComponent(
            String componentId,
            ComponentType type,
            String title,
            JComponent component,
            String iconPath,
            Icon icon,
            int order,
            boolean removable
    ) {
        if (componentId == null || componentId.trim().isEmpty()) {
            throw new IllegalArgumentException("componentId cannot be null or empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }
        if (component == null) {
            throw new IllegalArgumentException("component cannot be null");
        }

        this.componentId = componentId;
        this.type = type;
        this.title = title != null ? title : "";
        this.component = component;
        this.iconPath = iconPath;
        this.icon = icon;
        this.order = order;
        this.removable = removable;
    }

    /**
     * Gets the unique identifier for this component.
     *
     * @return the component ID
     */
    public String getComponentId() {
        return componentId;
    }

    /**
     * Gets the type of this component.
     *
     * @return the component type
     */
    public ComponentType getType() {
        return type;
    }

    /**
     * Gets the display title for this component.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the actual Swing component.
     *
     * @return the component
     */
    public JComponent getComponent() {
        return component;
    }

    /**
     * Gets the path to the icon resource.
     *
     * @return the icon path, or null if no icon path
     */
    public String getIconPath() {
        return iconPath;
    }

    /**
     * Gets the icon for this component.
     *
     * @return the icon, or null if no icon
     */
    public Icon getIcon() {
        return icon;
    }

    /**
     * Sets the icon for this component.
     *
     * @param icon the icon to set
     */
    public void setIcon(Icon icon) {
        this.icon = icon;
    }

    /**
     * Gets the display order for this component.
     *
     * @return the order (lower = earlier in display)
     */
    public int getOrder() {
        return order;
    }

    /**
     * Checks if this component can be removed by the user.
     *
     * @return {@code true} if removable, {@code false} otherwise
     */
    public boolean isRemovable() {
        return removable;
    }

    /**
     * Checks if this component has an icon.
     *
     * @return {@code true} if icon is not null
     */
    public boolean hasIcon() {
        return icon != null;
    }

    @Override
    public String toString() {
        return "UIComponent{" +
                "componentId='" + componentId + '\'' +
                ", type=" + type +
                ", title='" + title + '\'' +
                ", hasIcon=" + (icon != null) +
                ", order=" + order +
                '}';
    }

    /**
     * Enumeration of component types that plugins can add to the IDE.
     */
    public enum ComponentType {
        /**
         * A tab in the main IDE editor area (where code files are edited)
         */
        IDE_TAB("IDE Tab", "A tab in the main editor area"),

        /**
         * A panel in the bottom panel area (e.g., console, build output)
         */
        BOTTOM_PANEL("Bottom Panel", "A panel in the bottom area"),

        /**
         * A left sidebar panel (e.g., project structure, outline)
         */
        LEFT_SIDEBAR("Left Sidebar", "A panel in the left sidebar"),

        /**
         * A right sidebar panel
         */
        RIGHT_SIDEBAR("Right Sidebar", "A panel in the right sidebar"),

        /**
         * A button in the main toolbar
         */
        TOOLBAR_BUTTON("Toolbar Button", "A button in the main toolbar"),

        /**
         * A menu item (will be added to a plugins menu or appropriate location)
         */
        MENU_ITEM("Menu Item", "A menu item"),

        /**
         * A custom dockable panel (e.g., like IntelliJ's tool windows)
         */
        DOCKABLE_PANEL("Dockable Panel", "A dockable tool window"),

        /**
         * A status bar component
         */
        STATUS_BAR_COMPONENT("Status Bar", "A component in the status bar");

        private final String displayName;
        private final String description;

        ComponentType(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }
}