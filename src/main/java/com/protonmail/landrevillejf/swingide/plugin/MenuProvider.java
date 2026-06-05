package com.protonmail.landrevillejf.swingide.plugin;

import javax.swing.*;
import java.util.List;

public interface MenuProvider {
    List<JMenuItem> getMenuItems();
    String getMenuLocation(); // "Tools", "View", etc.
    String getTitle();
    Icon getIcon();
}