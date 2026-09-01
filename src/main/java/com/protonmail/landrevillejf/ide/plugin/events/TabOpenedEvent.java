package com.protonmail.landrevillejf.ide.plugin.events;

import lombok.Getter;

/**
 * Event fired when a tab is opened in the IDE.
 * <p>
 * This event contains information about the opened tab's identifier, title,
 * file path, and type.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
@Getter
public class TabOpenedEvent extends BaseEvent {
    private final String tabId;
    private final String tabTitle;
    private final String filePath;
    private final String tabType;

    /**
     * Creates a new tab opened event.
     *
     * @param source   the source of this event
     * @param tabId    the tab identifier
     * @param tabTitle the tab title
     * @param filePath the file path associated with the tab
     * @param tabType  the type of tab (e.g., "editor", "tool window")
     */
    public TabOpenedEvent(String source, String tabId, String tabTitle, String filePath, String tabType) {
        super(source);
        this.tabId = tabId;
        this.tabTitle = tabTitle;
        this.filePath = filePath;
        this.tabType = tabType;
    }

}
