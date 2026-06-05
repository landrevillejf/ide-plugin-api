package com.protonmail.landrevillejf.ide.plugin;

import javax.swing.*;
import java.util.List;

public interface ToolBarProvider {
    /**
     * Returns a list of buttons to add to the main toolbar
     */
    List<JButton> getButtonItems();

    /**
     * Returns the title for this toolbar section
     */
    String getTitle();

    /**
     * Returns the icon for this toolbar section
     */
    Icon getIcon();
}