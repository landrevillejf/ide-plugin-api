package com.protonmail.landrevillejf.ide.plugin.events;

import lombok.Getter;

/**
 * Event fired when a run/execution starts.
 * <p>
 * This event contains information about the project being run and the
 * run configuration being used.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
@Getter
public class RunStartedEvent extends BaseEvent {
    private final String projectPath;
    private final String runConfiguration;

    /**
     * Creates a new run started event.
     *
     * @param source the source of this event
     * @param projectPath the project path
     * @param runConfiguration the run configuration name
     */
    public RunStartedEvent(String source, String projectPath, String runConfiguration) {
        super(source);
        this.projectPath = projectPath;
        this.runConfiguration = runConfiguration;
    }

}