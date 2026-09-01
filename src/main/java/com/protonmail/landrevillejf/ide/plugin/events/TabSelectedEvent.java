package com.protonmail.landrevillejf.ide.plugin.events;

import lombok.Getter;

/**
 * Event fired when a tab is selected (activated) in the IDE.
 * <p>
 * This event contains information about the selected tab's identifier and title.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
@Getter
public class TabSelectedEvent extends BaseEvent {
    private final String tabId;
    private final String tabTitle;

    /**
     * Creates a new tab selected event.
     *
     * @param source   the source of this event
     * @param tabId    the tab identifier
     * @param tabTitle the tab title
     */
    public TabSelectedEvent(String source, String tabId, String tabTitle) {
        super(source);
        this.tabId = tabId;
        this.tabTitle = tabTitle;
    }

}
