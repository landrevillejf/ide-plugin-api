package com.protonmail.landrevillejf.ide.plugin;

public enum PluginStatus {
    UNLOADED("Unloaded", false, false),       // Pas encore chargé
    LOADED("Loaded", false, false),           // Chargé mais non initialisé
    INITIALIZED("Initialized", false, false), // Initialisé mais désactivé
    ENABLED("Enabled", true, false),          // Activé et fonctionnel
    DISABLED("Disabled", false, true),        // Désactivé
    ERROR("Error", false, false),             // En état d'erreur
    RELOADING("Reloading", false, false),
    ENABLING("Enabling", false, false),
    DISABLING("Disabling", false, false),
    SHUTTING_DOWN("Shutting Down", false, false),
    SHUTDOWN("Shutdown", false, false);       // En cours de rechargement

    private final String displayName;
    private final boolean active;
    private final boolean inactive;

    PluginStatus(String displayName, boolean active, boolean inactive) {
        this.displayName = displayName;
        this.active = active;
        this.inactive = inactive;
    }

    /**
     * Checks if the plugin is in an active state.
     *
     * @return true if the status is either ENABLED or STARTED.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Checks if the plugin is in an inactive state.
     *
     * @return true if the status is either DISABLED, STOPPED, or DESTROYED.
     */
    public boolean isInactive() {
        return inactive;
    }

    /**
     * Returns a user-friendly description of the status.
     *
     * @return a string representing the status in a readable format.
     */
    @Override
    public String toString() {
        return displayName;
    }

    /**
     * Validates if the plugin is allowed to transition to the given state.
     *
     * @param newState The state the plugin is transitioning to.
     * @return true if the transition is valid, false otherwise.
     */
    public boolean canTransitionTo(PluginStatus newState) {
        if (newState == null) {
            return false;
        }

        // Allow same state transitions (idempotent operations)
        if (this == newState) {
            return true;
        }

        // Can always transition to ERROR from most states
        if (canTransitionToError(newState)) {
            return true;
        }

        // From ERROR can only go to DISABLED
        if (this == ERROR) {
            return newState == DISABLED;
        }

        return switch (this) {
            case UNLOADED -> newState == LOADED;

            case LOADED -> newState == INITIALIZED ||  // AJOUTÉ: LOADED -> INITIALIZED
                    newState == ENABLED ||
                    newState == DISABLED ||
                    newState == ENABLING;

            case INITIALIZED -> newState == ENABLED ||
                    newState == ENABLING ||
                    newState == DISABLED;

            case DISABLED -> newState == LOADED ||
                    newState == ENABLED ||
                    newState == ENABLING ||
                    newState == SHUTTING_DOWN;

            case ENABLED -> newState == DISABLED ||
                    newState == DISABLING ||
                    newState == SHUTTING_DOWN;

            case ENABLING -> newState == ENABLED ||
                    newState == ERROR;

            case DISABLING -> newState == DISABLED ||
                    newState == ERROR;

            case SHUTTING_DOWN -> newState == SHUTDOWN ||
                    newState == ERROR;

            case RELOADING -> newState == LOADED ||
                    newState == ERROR;

            case SHUTDOWN -> false;

            default -> false;
        };
    }

    private boolean canTransitionToError(PluginStatus newState) {
        return newState == ERROR && switch (this) {
            case ENABLED, DISABLED, INITIALIZED, LOADED, ENABLING, DISABLING, SHUTTING_DOWN -> true;
            default -> false;
        };
    }
}
