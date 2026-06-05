package com.protonmail.landrevillejf.swingide.plugin.events;

public class MenuItemClickedEvent extends BaseEvent {
    private final String menuId;
    private final String itemId;
    private final String itemText;

    public MenuItemClickedEvent(String source, String menuId, String itemId, String itemText) {
        super(source);
        this.menuId = menuId;
        this.itemId = itemId;
        this.itemText = itemText;
    }

    public String getMenuId() {
        return menuId;
    }

    public String getItemId() {
        return itemId;
    }

    public String getItemText() {
        return itemText;
    }
}
