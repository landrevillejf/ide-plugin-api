package com.protonmail.landrevillejf.ide.plugin.events;

/**
 * Event fired when a project is closed in the IDE.
 * <p>
 * This event contains information about the closed project's path and name.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
public class ProjectClosedEvent extends BaseEvent {
    private final String projectPath;
    private final String projectName;

    /**
     * Creates a new project closed event.
     *
     * @param source the source of this event
     * @param projectPath the project path
     * @param projectName the project name
     */
    public ProjectClosedEvent(String source, String projectPath, String projectName) {
        super(source);
        this.projectPath = projectPath;
        this.projectName = projectName;
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
}