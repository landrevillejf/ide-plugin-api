package com.protonmail.landrevillejf.ide.plugin.events;

import lombok.Getter;

/**
 * Event fired when a tab is closed in the IDE.
 * <p>
 * This event contains information about the closed tab's identifier and title.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
@Getter
public class TabClosedEvent extends BaseEvent {
    private final String tabId;
    private final String tabTitle;

    /**
     * Creates a new tab closed event.
     *
     * @param source   the source of this event
     * @param tabId    the tab identifier
     * @param tabTitle the tab title
     */
    public TabClosedEvent(String source, String tabId, String tabTitle) {
        super(source);
        this.tabId = tabId;
        this.tabTitle = tabTitle;
    }

}