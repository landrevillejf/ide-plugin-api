package com.protonmail.landrevillejf.ide.plugin.events;

/**
 * Event fired when a build process finishes.
 * <p>
 * This event contains information about the build result including success status,
 * output, errors, and duration.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
public class BuildFinishedEvent extends BaseEvent {
    private final String projectPath;
    private final String buildType;
    private final boolean success;
    private final String output;
    private final String errors;
    private final long duration;

    /**
     * Creates a new build finished event.
     *
     * @param source the source of this event
     * @param projectPath the path to the project that was built
     * @param buildType the type of build
     * @param success whether the build succeeded
     * @param output the build output
     * @param errors the build errors, if any
     * @param duration the build duration in milliseconds
     */
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

    /**
     * Returns the project path.
     *
     * @return the project path
     */
    public String getProjectPath() {
        return projectPath;
    }

    /**
     * Returns the build type.
     *
     * @return the build type
     */
    public String getBuildType() {
        return buildType;
    }

    /**
     * Returns whether the build succeeded.
     *
     * @return {@code true} if successful, {@code false} otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns the build output.
     *
     * @return the build output
     */
    public String getOutput() {
        return output;
    }

    /**
     * Returns the build errors.
     *
     * @return the build errors, or empty string if none
     */
    public String getErrors() {
        return errors;
    }

    /**
     * Returns the build duration in milliseconds.
     *
     * @return the duration
     */
    public long getDuration() {
        return duration;
    }
}
