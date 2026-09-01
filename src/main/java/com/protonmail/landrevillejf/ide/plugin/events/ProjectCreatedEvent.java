package com.protonmail.landrevillejf.ide.plugin.events;

import lombok.Getter;

/**
 * Event fired when a project is created in the IDE.
 * <p>
 * This event contains information about the new project's path, name, and type.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
@Getter
public class ProjectCreatedEvent extends BaseEvent {
    private final String projectPath;
    private final String projectName;
    private final String projectType;

    /**
     * Creates a new project created event.
     *
     * @param source the source of this event
     * @param projectPath the project path
     * @param projectName the project name
     * @param projectType the project type
     */
    public ProjectCreatedEvent(String source, String projectPath, String projectName, String projectType) {
        super(source);
        this.projectPath = projectPath;
        this.projectName = projectName;
        this.projectType = projectType;
    }

}