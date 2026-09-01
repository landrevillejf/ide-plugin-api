package com.protonmail.landrevillejf.ide.plugin.ui;

import com.protonmail.landrevillejf.ide.plugin.ui.listener.BuildListener;

import javax.swing.*;
import java.io.File;

/**
 * Accessor interface that allows plugins to interact with IDE UI components.
 * <p>
 * Implemented by the host IDE's main window and exposed to plugins via
 * {@link com.protonmail.landrevillejf.ide.plugin.PluginContext}. Provides access
 * to tab panes, project explorer, structure panel, file operations, and more.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
public interface UIComponentAccessor {
    // ============ BASIC UI COMPONENTS ============

    /**
     * Returns the main JTabbedPane containing editor tabs.
     */
    JTabbedPane getIdeTabPane();

    /**
     * Returns the bottom JTabbedPane (console, debugger, etc.)
     */
    JTabbedPane getBottomTabPane();

    /**
     * Returns the BuildManager panel.
     */
    JPanel getBuildManagerPanel();

    /**
     * Returns the ProjectExplorer component.
     */
    JComponent getProjectExplorer();

    /**
     * Returns the StructurePanel component.
     */
    JComponent getStructurePanel();

    /**
     * Returns the OutputPanel (console).
     */
    JComponent getOutputPanel();

    /**
     * Returns the advanced status bar.
     */
    JComponent getAdvancedStatusBar();

    // ============ TAB MANAGEMENT ============

    /**
     * Selects a tab by its component ID.
     * @param componentId the UI component ID (stored in clientProperty "ui_component_id")
     * @return true if the tab was found and selected
     */
    boolean selectTabById(String componentId);

    /**
     * Selects a tab by its title.
     * @param title the tab title
     * @return true if the tab was found and selected
     */
    boolean selectTabByTitle(String title);

    // ============ FILE OPERATIONS ============

    /**
     * Opens a file in the editor.
     * @param filePath path to the file to open
     */
    void openFileInEditor(String filePath);

    /**
     * Opens a file at a specific line.
     * @param filePath path to the file
     * @param line line number (1-based)
     */
    void openFileAtLine(String filePath, int line);

    /**
     * Opens a file and navigates to a specific member (method/field).
     * @param filePath path to the file
     * @param memberName name of the method or field to navigate to
     */
    void openFileAtMember(String filePath, String memberName);

    // ============ PROJECT EXPLORER INTERACTIONS ============

    /**
     * Refreshes the project explorer view.
     */
    void refreshProjectExplorer();

    /**
     * Expands all nodes in the project explorer.
     */
    void expandProjectExplorerAll();

    /**
     * Collapses all nodes in the project explorer.
     */
    void collapseProjectExplorerAll();

    /**
     * Selects a file in the project explorer by its path.
     * @param filePath the absolute path of the file to select
     * @return true if the file was found and selected
     */
    boolean selectFileInExplorer(String filePath);

    /**
     * Gets the currently selected file in the project explorer.
     * @return the selected file, or null if none selected
     */
    File getSelectedFileInExplorer();

    /**
     * Gets the currently selected directory in the project explorer.
     * @return the selected directory, or null if none selected
     */
    File getSelectedDirectoryInExplorer();

    /**
     * Creates a new file in the project explorer at the selected location.
     * @param fileName the name of the file to create
     * @return true if the file was created successfully
     */
    boolean createNewFile(String fileName);

    /**
     * Creates a new directory in the project explorer at the selected location.
     * @param directoryName the name of the directory to create
     * @return true if the directory was created successfully
     */
    boolean createNewDirectory(String directoryName);

    /**
     * Creates a new Java class in the project explorer.
     * @param className the name of the class
     * @param packageName the package name (can be empty)
     * @return true if the class was created successfully
     */
    boolean createJavaClass(String className, String packageName);

    /**
     * Deletes the selected file or directory.
     * @return true if deletion was successful
     */
    boolean deleteSelectedFile();

    /**
     * Renames the selected file or directory.
     * @param newName the new name
     * @return true if rename was successful
     */
    boolean renameSelectedFile(String newName);

    /**
     * Copies the selected file path to clipboard.
     */
    void copySelectedPathToClipboard();

    // ============ STRUCTURE PANEL INTERACTIONS ============

    /**
     * Shows the structure of a Java file in the structure panel.
     * @param filePath path to the Java file
     * @return true if the structure was loaded successfully
     */
    boolean showStructure(String filePath);

    /**
     * Refreshes the structure panel for the current file.
     */
    void refreshStructure();

    /**
     * Clears the structure panel.
     */
    void clearStructure();

    /**
     * Gets the current file being displayed in the structure panel.
     * @return the current file, or null if none
     */
    File getCurrentStructureFile();

    /**
     * Navigates to a member (method/field) in the editor from the structure panel.
     * @param memberName the name of the member to navigate to
     * @return true if the member was found and navigated to
     */
    boolean navigateToMember(String memberName);

    /**
     * Expands all nodes in the structure tree.
     */
    void expandStructureAll();

    /**
     * Collapses all nodes in the structure tree.
     */
    void collapseStructureAll();

    /**
     * Filters structure members by type (methods, fields, constructors, etc.).
     * @param filterType "All Members", "Methods Only", "Fields Only", "Constructors Only", "Inner Classes Only"
     */
    void filterStructure(String filterType);

    /**
     * Searches for a member in the structure panel.
     * @param searchText the text to search for
     */
    void searchStructure(String searchText);

    // ============ DIALOGS ============

    /**
     * Shows an error dialog.
     */
    void showErrorDialog(String message);

    /**
     * Shows an info dialog.
     */
    void showInfoDialog(String title, String message);

    /**
     * Shows a warning dialog.
     */
    void showWarningDialog(String title, String message);

    /**
     * Shows a confirmation dialog.
     * @return true if the user confirmed
     */
    boolean showConfirmDialog(String title, String message);

    /**
     * Shows a notification in the status bar.
     */
    void showNotification(String message);

    /**
     * Shows a notification with title.
     */
    void showNotification(String title, String message);

    // ============ PROJECT & EVENT BUS ============

    /**
     * Returns the currently open project.
     */
    Object getCurrentProject();

    /**
     * Returns the IDE's EventBus.
     */
    Object getEventBus();

    // ============ BUILD MANAGER EXTENSIONS ============

    /**
     * Executes a build task on the current project.
     * @param taskName the name of the task to execute (e.g., "build", "clean", "run")
     * @return true if the task was started successfully
     */
    boolean executeBuildTask(String taskName);

    /**
     * Executes a build task with custom arguments.
     * @param taskName the name of the task to execute
     * @param arguments additional arguments to pass to the build tool
     * @return true if the task was started successfully
     */
    boolean executeBuildTask(String taskName, String[] arguments);

    /**
     * Returns the list of available build tasks for the current project.
     * @return array of task names, or empty array if no project is loaded
     */
    String[] getAvailableBuildTasks();

    /**
     * Returns the current build tool (e.g., "Gradle", "Maven", "Python").
     * @return the build tool name, or null if no project is loaded
     */
    String getCurrentBuildTool();

    /**
     * Checks if a build is currently running.
     * @return true if a build task is in progress
     */
    boolean isBuildRunning();

    /**
     * Stops the currently running build task.
     * @return true if a task was stopped
     */
    boolean stopCurrentBuild();

    /**
     * Adds a custom build task to the BuildManager panel.
     */
    void addCustomBuildTask(String taskName, String taskDescription, String category,
                            boolean isDefault, boolean showInToolbar, boolean isVisible);

    /**
     * Removes a custom build task.
     * @return true if the task was removed
     */
    boolean removeCustomBuildTask(String taskName);

    /**
     * Adds a build listener to receive build events.
     */
    void addBuildListener(BuildListener listener);

    /**
     * Removes a build listener.
     */
    void removeBuildListener(BuildListener listener);

    /**
     * Gets the build output as text.
     */
    String getBuildOutput();

    /**
     * Clears the build output panel.
     */
    void clearBuildOutput();

    /**
     * Sets JVM arguments for the build.
     */
    void setBuildJvmArgs(String args);

    /**
     * Gets the current JVM arguments.
     */
    String getBuildJvmArgs();

    /**
     * Sets program arguments for the run task.
     */
    void setProgramArguments(String args);

    /**
     * Gets the current program arguments.
     */
    String getProgramArguments();

    // ============ PROJECT BUILD CONFIGURATION ============

    /**
     * Gets the build configuration for the current project.
     */
    java.util.Map<String, String> getProjectBuildConfig();

    /**
     * Sets a build configuration property for the current project.
     */
    void setProjectBuildConfig(String key, String value);

    /**
     * Detects the project type (Java/Maven/Gradle, Python, etc.)
     */
    String detectProjectType();

    /**
     * Checks if the current project is a Spring Boot project.
     */
    boolean isSpringBootProject();

    /**
     * Gets the Spring Boot version if applicable.
     */
    String getSpringBootVersion();
}