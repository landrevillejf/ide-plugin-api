package com.protonmail.landrevillejf.swingide.plugin.events;

import java.io.File;

public class ProjectOpenedEvent extends BaseEvent {
    private final String projectPath;
    private final String projectName;
    private final File projectDirectory;

    public ProjectOpenedEvent(String source, String projectPath, String projectName) {
        super(source);
        this.projectPath = projectPath;
        this.projectName = projectName;
        this.projectDirectory = new File(projectPath);
    }

    public String getProjectPath() {
        return projectPath;
    }

    public String getProjectName() {
        return projectName;
    }

    public File getProjectDirectory() {
        return projectDirectory;
    }
}



