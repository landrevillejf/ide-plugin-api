package com.protonmail.landrevillejf.swingide.plugin.events;

public class BuildStartedEvent extends BaseEvent {
    private final String projectPath;
    private final String buildType;
    private final String target;

    public BuildStartedEvent(String source, String projectPath, String buildType, String target) {
        super(source);
        this.projectPath = projectPath;
        this.buildType = buildType;
        this.target = target;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public String getBuildType() {
        return buildType;
    }

    public String getTarget() {
        return target;
    }
}