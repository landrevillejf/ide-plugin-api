package com.protonmail.landrevillejf.ide.plugin.ui;

import lombok.Getter;

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
@Getter
public class UIComponent implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * -- GETTER --
     *  Gets the unique identifier for this component.
     *
     * @return the component ID
     */
    private final String componentId;
    /**
     * -- GETTER --
     *  Gets the type of this component.
     *
     * @return the component type
     */
    private final ComponentType type;
    /**
     * -- GETTER --
     *  Gets the display title for this component.
     *
     * @return the title
     */
    private final String title;
    /**
     * -- GETTER --
     *  Gets the actual Swing component.
     *
     * @return the component
     */
    private final JComponent component;
    /**
     * -- GETTER --
     *  Gets the path to the icon resource.
     *
     * @return the icon path, or null if no icon path
     */
    private final String iconPath;
    /**
     * -- GETTER --
     *  Gets the icon for this component.
     *
     * @return the icon, or null if no icon
     */
    private transient Icon icon;  // transient pour ne pas sérialiser l'icône
    /**
     * -- GETTER --
     *  Gets the display order for this component.
     *
     * @return the order (lower = earlier in display)
     */
    private final int order;
    /**
     * -- GETTER --
     *  Checks if this component can be removed by the user.
     *
     * @return {@code true} if removable, {@code false} otherwise
     */
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
            final String componentId,
            final ComponentType type,
            final String title,
            final JComponent component,
            final String iconPath
    ) {
        this(componentId, type, title, component, iconPath, null, Integer.MAX_VALUE, true);
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
            final String componentId,
            final ComponentType type,
            final String title,
            final JComponent component,
            final Icon icon
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
            final String componentId,
            final ComponentType type,
            final String title,
            final JComponent component,
            final String iconPath,
            final int order,
            final boolean removable
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
            final String componentId,
            final ComponentType type,
            final String title,
            final JComponent component,
            final Icon icon,
            final int order,
            final boolean removable
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
            final String componentId,
            final ComponentType type,
            final String title,
            final JComponent component,
            final String iconPath,
            final Icon icon,
            final int order,
            final boolean removable
    ) {
        validateParameters(componentId, type, component);

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
     * Validates constructor parameters.
     *
     * @param componentId the component ID
     * @param type the component type
     * @param component the Swing component
     */
    private void validateParameters(final String componentId, final ComponentType type, final JComponent component) {
        if (componentId == null || componentId.isBlank()) {
            throw new IllegalArgumentException("componentId cannot be null or empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }
        if (component == null) {
            throw new IllegalArgumentException("component cannot be null");
        }
    }

    /**
     * Sets the icon for this component.
     *
     * @param icon the icon to set
     */
    public void setIcon(final Icon icon) {
        this.icon = icon;
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
    @Getter
    public enum ComponentType {
        /** A tab in the main IDE editor area (where code files are edited) */
        IDE_TAB("IDE Tab", "A tab in the main editor area"),

        /** A panel in the bottom panel area (e.g., console, build output) */
        BOTTOM_PANEL("Bottom Panel", "A panel in the bottom area"),

        /** A left sidebar panel (e.g., project structure, outline) */
        LEFT_SIDEBAR("Left Sidebar", "A panel in the left sidebar"),

        /** A right sidebar panel */
        RIGHT_SIDEBAR("Right Sidebar", "A panel in the right sidebar"),

        /** A button in the main toolbar */
        TOOLBAR_BUTTON("Toolbar Button", "A button in the main toolbar"),

        /** A menu item (will be added to a plugins menu or appropriate location) */
        MENU_ITEM("Menu Item", "A menu item"),

        /** A custom dockable panel (e.g., like IntelliJ's tool windows) */
        DOCKABLE_PANEL("Dockable Panel", "A dockable tool window"),

        /** A status bar component */
        STATUS_BAR("Status Bar", "A component in the status bar");

        private final String displayName;
        private final String description;

        ComponentType(final String displayName, final String description) {
            this.displayName = displayName;
            this.description = description;
        }

    }
}