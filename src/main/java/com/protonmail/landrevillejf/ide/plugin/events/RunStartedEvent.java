package com.protonmail.landrevillejf.ide.plugin.events;

public class RunStartedEvent extends BaseEvent {
    private final String projectPath;
    private final String runConfiguration;

    public RunStartedEvent(String source, String projectPath, String runConfiguration) {
        super(source);
        this.projectPath = projectPath;
        this.runConfiguration = runConfiguration;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public String getRunConfiguration() {
        return runConfiguration;
    }
}