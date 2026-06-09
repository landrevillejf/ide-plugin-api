package com.protonmail.landrevillejf.ide.plugin.events;

import lombok.Getter;

@Getter
public class TabSelectedEvent extends BaseEvent {
    private final String tabId;
    private final String tabTitle;

    public TabSelectedEvent(String source, String tabId, String tabTitle) {
        super(source);
        this.tabId = tabId;
        this.tabTitle = tabTitle;
    }

}
