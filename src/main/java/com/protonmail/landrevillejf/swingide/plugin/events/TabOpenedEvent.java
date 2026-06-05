package com.protonmail.landrevillejf.swingide.plugin.events;

public class TabOpenedEvent extends BaseEvent {
    private final String tabId;
    private final String tabTitle;
    private final String filePath;
    private final String tabType;

    public TabOpenedEvent(String source, String tabId, String tabTitle, String filePath, String tabType) {
        super(source);
        this.tabId = tabId;
        this.tabTitle = tabTitle;
        this.filePath = filePath;
        this.tabType = tabType;
    }

    public String getTabId() {
        return tabId;
    }

    public String getTabTitle() {
        return tabTitle;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getTabType() {
        return tabType;
    }
}
