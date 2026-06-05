package com.protonmail.landrevillejf.swingide.plugin.events;

public class ProjectCreatedEvent extends BaseEvent {
    private final String projectPath;
    private final String projectName;
    private final String projectType;

    public ProjectCreatedEvent(String source, String projectPath, String projectName, String projectType) {
        super(source);
        this.projectPath = projectPath;
        this.projectName = projectName;
        this.projectType = projectType;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public String getProjectName() {
        return projectName;
    }

    public String getProjectType() {
        return projectType;
    }
}