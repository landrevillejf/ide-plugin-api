package com.protonmail.landrevillejf.ide.plugin.events;

/**
 * Event fired when the application theme changes.
 * <p>
 * This event contains information about the new theme name and whether
 * dark mode is enabled.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
public class ApplicationThemeChangedEvent extends BaseEvent {
    private final String themeName;
    private final boolean darkMode;

    /**
     * Creates a new application theme changed event.
     *
     * @param source the source of this event
     * @param themeName the new theme name
     * @param darkMode whether dark mode is enabled
     */
    public ApplicationThemeChangedEvent(String source, String themeName, boolean darkMode) {
        super(source);
        this.themeName = themeName;
        this.darkMode = darkMode;
    }

    /**
     * Returns the theme name.
     *
     * @return the theme name
     */
    public String getThemeName() {
        return themeName;
    }

    /**
     * Returns whether dark mode is enabled.
     *
     * @return {@code true} if dark mode is enabled, {@code false} otherwise
     */
    public boolean isDarkMode() {
        return darkMode;
    }
}
