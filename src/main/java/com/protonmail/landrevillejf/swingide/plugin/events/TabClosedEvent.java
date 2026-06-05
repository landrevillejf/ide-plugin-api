package com.protonmail.landrevillejf.swingide.plugin.events;

public class TabClosedEvent extends BaseEvent {
    private final String tabId;
    private final String tabTitle;

    public TabClosedEvent(String source, String tabId, String tabTitle) {
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