package com.protonmail.landrevillejf.ide.plugin.events;

/**
 * Event fired when a menu item is clicked.
 * <p>
 * This event contains information about the menu, item ID, and item text.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
public class MenuItemClickedEvent extends BaseEvent {
    private final String menuId;
    private final String itemId;
    private final String itemText;

    /**
     * Creates a new menu item clicked event.
     *
     * @param source the source of this event
     * @param menuId the menu identifier
     * @param itemId the item identifier
     * @param itemText the item text
     */
    public MenuItemClickedEvent(String source, String menuId, String itemId, String itemText) {
        super(source);
        this.menuId = menuId;
        this.itemId = itemId;
        this.itemText = itemText;
    }

    /**
     * Returns the menu identifier.
     *
     * @return the menu ID
     */
    public String getMenuId() {
        return menuId;
    }

    /**
     * Returns the item identifier.
     *
     * @return the item ID
     */
    public String getItemId() {
        return itemId;
    }

    /**
     * Returns the item text.
     *
     * @return the item text
     */
    public String getItemText() {
        return itemText;
    }
}
