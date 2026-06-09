package com.protonmail.landrevillejf.ide.plugin.events;

import lombok.Getter;

@Getter
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

}