package com.protonmail.landrevillejf.ide.plugin;

import javax.swing.*;
import java.util.List;

/**
 * Interface for plugins that want to add buttons to the IDE's toolbar.
 * <p>
 * Plugins implementing this interface can contribute toolbar buttons
 * to the main IDE toolbar.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
public interface ToolBarProvider {
    /**
     * Returns a list of buttons to add to the main toolbar.
     *
     * @return a list of toolbar buttons
     */
    List<JButton> getButtonItems();

    /**
     * Returns the title for this toolbar section.
     *
     * @return the toolbar section title
     */
    String getTitle();

    /**
     * Returns the icon for this toolbar section.
     *
     * @return the toolbar section icon, or {@code null} if none
     */
    Icon getIcon();
}