package com.protonmail.landrevillejf.swingide.plugin.events;

public class BuildFinishedEvent extends BaseEvent {
    private final String projectPath;
    private final String buildType;
    private final boolean success;
    private final String output;
    private final String errors;
    private final long duration;

    public BuildFinishedEvent(String source, String projectPath, String buildType,
                              boolean success, String output, String errors, long duration) {
        super(source);
        this.projectPath = projectPath;
        this.buildType = buildType;
        this.success = success;
        this.output = output;
        this.errors = errors;
        this.duration = duration;
    }

    public String getProjectPath() {
        return projectPath;
    }

    public String getBuildType() {
        return buildType;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getOutput() {
        return output;
    }

    public String getErrors() {
        return errors;
    }

    public long getDuration() {
        return duration;
    }
}
