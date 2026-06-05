package com.protonmail.landrevillejf.swingide.plugin.events;

public class ProjectClosedEvent extends BaseEvent {
    private final String projectPath;
    private final String projectName;

    public ProjectClosedEvent(String source, String projectPath, String projectName) {
        super(source);
        this.projectPath = projectPath;
        this.projectName = projectName;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public String getProjectName() {
        return projectName;
    }
}