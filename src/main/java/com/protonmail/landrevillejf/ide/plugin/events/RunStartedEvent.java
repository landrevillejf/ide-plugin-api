package com.protonmail.landrevillejf.ide.plugin.events;

import lombok.Getter;

@Getter
public class RunStartedEvent extends BaseEvent {
    private final String projectPath;
    private final String runConfiguration;

    public RunStartedEvent(String source, String projectPath, String runConfiguration) {
        super(source);
        this.projectPath = projectPath;
        this.runConfiguration = runConfiguration;
    }

}