package com.protonmail.landrevillejf.ide.plugin.events;

public class RunFinishedEvent extends BaseEvent {
    private final String projectPath;
    private final String runConfiguration;
    private final boolean success;
    private final int exitCode;
    private final String output;

    public RunFinishedEvent(String source, String projectPath, String runConfiguration,
                            boolean success, int exitCode, String output) {
        super(source);
        this.projectPath = projectPath;
        this.runConfiguration = runConfiguration;
        this.success = success;
        this.exitCode = exitCode;
        this.output = output;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public String getRunConfiguration() {
        return runConfiguration;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getExitCode() {
        return exitCode;
    }

    public String getOutput() {
        return output;
    }
}
