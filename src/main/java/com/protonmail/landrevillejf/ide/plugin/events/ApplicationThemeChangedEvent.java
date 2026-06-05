package com.protonmail.landrevillejf.ide.plugin.events;

public class ApplicationThemeChangedEvent extends BaseEvent {
    private final String themeName;
    private final boolean darkMode;

    public ApplicationThemeChangedEvent(String source, String themeName, boolean darkMode) {
        super(source);
        this.themeName = themeName;
        this.darkMode = darkMode;
    }

    public String getThemeName() {
        return themeName;
    }

    public boolean isDarkMode() {
        return darkMode;
    }
}
