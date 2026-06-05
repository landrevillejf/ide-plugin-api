package com.protonmail.landrevillejf.ide.plugin.events;

public class TabSelectedEvent extends BaseEvent {
    private final String tabId;
    private final String tabTitle;

    public TabSelectedEvent(String source, String tabId, String tabTitle) {
        super(source);
        this.tabId = tabId;
        this.tabTitle = tabTitle;
    }

    public String getTabId() {
        return tabId;
    }

    public String getTabTitle() {
        return tabTitle;
    }
}
