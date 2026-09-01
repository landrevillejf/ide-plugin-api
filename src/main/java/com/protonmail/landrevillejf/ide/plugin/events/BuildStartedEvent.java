package com.protonmail.landrevillejf.ide.plugin.events;

/**
 * Event fired when a build process starts.
 * <p>
 * This event contains information about the project being built,
 * the build type, and the build target.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
public class BuildStartedEvent extends BaseEvent {
    private final String projectPath;
    private final String buildType;
    private final String target;

    /**
     * Creates a new build started event.
     *
     * @param source the source of this event
     * @param projectPath the path to the project being built
     * @param buildType the type of build (e.g., "debug", "release")
     * @param target the build target
     */
    public BuildStartedEvent(String source, String projectPath, String buildType, String target) {
        super(source);
        this.projectPath = projectPath;
        this.buildType = buildType;
        this.target = target;
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
     * Returns the build target.
     *
     * @return the build target
     */
    public String getTarget() {
        return target;
    }
}