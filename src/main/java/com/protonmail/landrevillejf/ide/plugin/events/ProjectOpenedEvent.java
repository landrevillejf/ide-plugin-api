package com.protonmail.landrevillejf.ide.plugin.events;

import java.io.File;

/**
 * Event fired when a project is opened in the IDE.
 * <p>
 * This event contains information about the project path, name, and directory.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
public class ProjectOpenedEvent extends BaseEvent {
    private final String projectPath;
    private final String projectName;
    private final File projectDirectory;

    /**
     * Creates a new project opened event.
     *
     * @param source the source of this event
     * @param projectPath the project path
     * @param projectName the project name
     */
    public ProjectOpenedEvent(String source, String projectPath, String projectName) {
        super(source);
        this.projectPath = projectPath;
        this.projectName = projectName;
        this.projectDirectory = new File(projectPath);
    }

    /**
     * Returns the project path.
     *
     * @return the project path
     */
    public String getProjectPath() {
        return projectPath;
    }

    /**
     * Returns the project name.
     *
     * @return the project name
     */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Returns the project directory.
     *
     * @return the project directory
     */
    public File getProjectDirectory() {
        return projectDirectory;
    }
}



