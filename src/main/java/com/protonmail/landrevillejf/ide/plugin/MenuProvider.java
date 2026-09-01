package com.protonmail.landrevillejf.ide.plugin;

import javax.swing.*;
import java.util.List;

/**
 * Interface for plugins that want to add menu items to the IDE's menu bar.
 * <p>
 * Plugins implementing this interface can contribute menu items to
 * specific locations in the IDE's menu structure.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
public interface MenuProvider {
    /**
     * Returns the menu items to add to the IDE menu.
     *
     * @return a list of menu items
     */
    List<JMenuItem> getMenuItems();

    /**
     * Returns the location in the menu bar where these items should be added.
     * Common values include "Tools", "View", "File", etc.
     *
     * @return the menu location identifier
     */
    String getMenuLocation();

    /**
     * Returns the title for the menu section.
     *
     * @return the menu title
     */
    String getTitle();

    /**
     * Returns the icon for the menu section.
     *
     * @return the menu icon, or {@code null} if none
     */
    Icon getIcon();
}