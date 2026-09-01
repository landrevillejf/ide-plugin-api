package com.protonmail.landrevillejf.ide.plugin;

import javax.swing.*;

/**
 * Interface providing access to IDE UI components.
 * <p>
 * This interface allows plugins to access and interact with core IDE
 * UI components such as the tab pane and dialog system.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
public interface UIComponentAccessor {
    /**
     * Returns the IDE's main tab pane.
     *
     * @return the tab pane component
     */
    JTabbedPane getIdeTabPane();

    /**
     * Shows an error dialog to the user.
     *
     * @param message the error message to display
     */
    void showErrorDialog(String message);

    /**
     * Shows an information dialog to the user.
     *
     * @param title the dialog title
     * @param message the information message to display
     */
    void showInfoDialog(String title, String message);

    /**
     * Shows a warning dialog to the user.
     *
     * @param title the dialog title
     * @param message the warning message to display
     */
    void showWarningDialog(String title, String message);

    /**
     * Shows a confirmation dialog to the user.
     *
     * @param title the dialog title
     * @param message the confirmation message to display
     * @return {@code true} if the user confirms, {@code false} otherwise
     */
    boolean showConfirmDialog(String title, String message);
}