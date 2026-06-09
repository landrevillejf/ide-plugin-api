package com.protonmail.landrevillejf.ide.plugin.events;

import lombok.Getter;

@Getter
public class TabClosedEvent extends BaseEvent {
    private final String tabId;
    private final String tabTitle;

    public TabClosedEvent(String source, String tabId, String tabTitle) {
        super(source);
        this.tabId = tabId;
        this.tabTitle = tabTitle;
    }

}