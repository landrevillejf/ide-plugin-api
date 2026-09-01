package com.protonmail.landrevillejf.ide.plugin.events;

/**
 * Event fired when a run/execution finishes.
 * <p>
 * This event contains information about the run configuration, success status,
 * exit code, and output.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
public class RunFinishedEvent extends BaseEvent {
    private final String projectPath;
    private final String runConfiguration;
    private final boolean success;
    private final int exitCode;
    private final String output;

    /**
     * Creates a new run finished event.
     *
     * @param source the source of this event
     * @param projectPath the project path
     * @param runConfiguration the run configuration name
     * @param success whether the run succeeded
     * @param exitCode the exit code
     * @param output the run output
     */
    public RunFinishedEvent(String source, String projectPath, String runConfiguration,
                            boolean success, int exitCode, String output) {
        super(source);
        this.projectPath = projectPath;
        this.runConfiguration = runConfiguration;
        this.success = success;
        this.exitCode = exitCode;
        this.output = output;
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
     * Returns the run configuration name.
     *
     * @return the run configuration
     */
    public String getRunConfiguration() {
        return runConfiguration;
    }

    /**
     * Returns whether the run succeeded.
     *
     * @return {@code true} if successful, {@code false} otherwise
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Returns the exit code.
     *
     * @return the exit code
     */
    public int getExitCode() {
        return exitCode;
    }

    /**
     * Returns the run output.
     *
     * @return the output
     */
    public String getOutput() {
        return output;
    }
}
