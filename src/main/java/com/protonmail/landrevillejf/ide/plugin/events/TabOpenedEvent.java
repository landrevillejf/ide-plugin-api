package com.protonmail.landrevillejf.ide.plugin.events;

import lombok.Getter;

@Getter
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

}
