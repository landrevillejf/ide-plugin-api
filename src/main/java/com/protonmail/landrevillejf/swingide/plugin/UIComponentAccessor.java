package com.protonmail.landrevillejf.swingide.plugin;

import javax.swing.*;

public interface UIComponentAccessor {
    JTabbedPane getIdeTabPane();
    void showErrorDialog(String message);
    void showInfoDialog(String title, String message);
    void showWarningDialog(String title, String message);
    boolean showConfirmDialog(String title, String message);
}