package com.protonmail.landrevillejf.ide.plugin;

/**
 * Enumeration representing the various states a plugin can be in during its lifecycle.
 * <p>
 * This enum defines all possible states for a plugin, from unloaded to shutdown,
 * including intermediate states during transitions. Each state has rules for
 * valid transitions to other states.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
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

    /**
     * Creates a new plugin status.
     *
     * @param displayName the human-readable display name
     * @param active whether this status represents an active state
     * @param inactive whether this status represents an inactive state
     */
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

            case ENABLING -> newState == ENABLED;

            case DISABLING -> newState == DISABLED;

            case SHUTTING_DOWN -> newState == SHUTDOWN;

            case RELOADING -> newState == LOADED;

            // SHUTDOWN is a terminal state: no further transitions allowed
            default -> false;
        };
    }

    /**
     * Checks if a transition to ERROR state is valid from the current state.
     *
     * @param newState the target state
     * @return {@code true} if transition to ERROR is valid, {@code false} otherwise
     */
    private boolean canTransitionToError(PluginStatus newState) {
        return newState == ERROR && switch (this) {
            case ENABLED, DISABLED, INITIALIZED, LOADED, ENABLING, DISABLING, SHUTTING_DOWN -> true;
            default -> false;
        };
    }
}
