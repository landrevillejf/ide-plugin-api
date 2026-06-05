package com.protonmail.landrevillejf.ide.plugin;

import java.util.List;
import java.util.Map;

/**
 * The main interface for all plugins in the application.
 * <p>
 * Plugins are modular components that can be loaded, initialized, enabled,
 * and disabled at runtime. They provide extensibility to the host application.
 * </p>
 * <p>
 * Implementations should extend {@link AbstractPlugin} or implement this interface
 * directly, providing concrete implementations for all abstract methods.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
public interface Plugin {

    // ========================================================================
    // DESCRIPTOR AND BASIC INFORMATION METHODS
    // ========================================================================

    /**
     * Returns the descriptor containing metadata about this plugin.
     *
     * @return The {@link PluginDescriptor} for this plugin.
     */
    PluginDescriptor getDescriptor();

    /**
     * Returns the name of the plugin.
     *
     * @return The plugin name.
     */
    default String getName() {
        return getDescriptor().getName();
    }

    /**
     * Returns the version of the plugin.
     *
     * @return The plugin version.
     */
    default String getVersion() {
        return getDescriptor().getVersion();
    }

    /**
     * Returns a description of the plugin's functionality.
     *
     * @return The plugin description.
     */
    default String getDescription() {
        return getDescriptor().getDescription();
    }

    /**
     * Returns the author of the plugin.
     *
     * @return The plugin author.
     */
    default String getAuthor() {
        return getDescriptor().getAuthor();
    }

    /**
     * Returns the email address of the plugin author.
     *
     * @return The author's email address.
     */
    String getAuthorEmail();

    /**
     * Returns the category of the plugin for UI or grouping purposes.
     *
     * @return The plugin category (e.g., "UI", "Tools", "Language Support").
     */
    String getCategory();

    /**
     * Returns custom metadata for the plugin.
     *
     * @return A map containing custom metadata key-value pairs.
     */
    default Map<String, Object> getCustomMetadata() {
        return Map.of();
    }

    // ========================================================================
    // MANIFEST INFORMATION METHODS
    // ========================================================================

    /**
     * Returns the specification title of the plugin from the MANIFEST.MF.
     *
     * @return The specification title.
     */
    String getSpecificationTitle();

    /**
     * Returns the specification version of the plugin from the MANIFEST.MF.
     *
     * @return The specification version.
     */
    String getSpecificationVersion();

    /**
     * Returns the specification vendor of the plugin from the MANIFEST.MF.
     *
     * @return The specification vendor.
     */
    String getSpecificationVendor();

    /**
     * Returns the implementation version of the plugin from the MANIFEST.MF.
     *
     * @return The implementation version.
     */
    String getImplementationVersion();

    // ========================================================================
    // LIFECYCLE METHODS
    // ========================================================================

    /**
     * This method is called before the plugin is initialized.
     * <p>
     * Override this method to prepare any resources or configurations required
     * before initialization.
     * </p>
     */
    default void preInitialize() {
    }

    /**
     * Initializes the plugin with the provided context.
     * <p>
     * This method is called once when the plugin is loaded. It should perform
     * setup operations that don't require the plugin to be enabled.
     * </p>
     *
     * @param context The plugin context providing access to host services.
     */
    void initialize(PluginContext context);

    /**
     * Called before the plugin is enabled.
     * <p>
     * Return {@code false} to prevent the plugin from being enabled.
     * </p>
     *
     * @return {@code true} if the plugin can be enabled, {@code false} otherwise.
     */
    default boolean beforeEnable() {
        return true;
    }

    /**
     * Enables the plugin.
     * <p>
     * This method is called when the plugin should start its main functionality.
     * The plugin should be fully operational after this method completes.
     * </p>
     */
    void enable();

    /**
     * Called after the plugin is enabled.
     * <p>
     * This method is invoked after {@link #enable()} has completed successfully.
     * </p>
     */
    default void afterEnable() {
    }

    /**
     * Called before the plugin is disabled.
     * <p>
     * Return {@code false} to prevent the plugin from being disabled.
     * </p>
     *
     * @return {@code true} if the plugin can be disabled, {@code false} otherwise.
     */
    default boolean beforeDisable() {
        return true;
    }

    /**
     * Disables the plugin.
     * <p>
     * This method is called when the plugin should stop its main functionality.
     * The plugin should release resources but remain in memory.
     * </p>
     */
    void disable();

    /**
     * Called after the plugin is disabled.
     * <p>
     * This method is invoked after {@link #disable()} has completed.
     * </p>
     */
    default void afterDisable() {
    }

    /**
     * Shuts down the plugin.
     * <p>
     * This method is called when the plugin is being unloaded from memory.
     * The plugin should release all resources and prepare for garbage collection.
     * </p>
     */
    void shutdown();

    /**
     * This method is called when the plugin is starting up.
     * <p>
     * Override this method to add any startup logic.
     * </p>
     */
    default void onStart() {
    }

    /**
     * This method is called when the plugin is stopping.
     * <p>
     * Override this method to add any shutdown logic.
     * </p>
     */
    default void onStop() {
    }

    /**
     * This method is called during plugin cleanup.
     * <p>
     * Override this method to release any resources or save state.
     * </p>
     */
    default void cleanup() {
    }

    // ========================================================================
    // STATE MANAGEMENT METHODS
    // ========================================================================

    /**
     * Returns the current state of the plugin.
     *
     * @return The current {@link PluginStatus}.
     */
    default PluginStatus getState() {
        return PluginStatus.DISABLED;
    }

    /**
     * Sets the current state of the plugin.
     * <p>
     * Valid transitions are defined in the {@link PluginStatus} enum.
     * </p>
     *
     * @param newState The new state of the plugin.
     * @throws IllegalStateException if the state transition is invalid.
     */
    default void setState(PluginStatus newState) {
        if (!getState().canTransitionTo(newState)) {
            throw new IllegalStateException(
                    "Invalid state transition from " + getState() + " to " + newState
            );
        }
    }

    /**
     * Checks if the plugin is currently enabled.
     *
     * @return {@code true} if the plugin is enabled, {@code false} otherwise.
     */
    boolean isEnabled();

    // ========================================================================
    // DEPENDENCY AND CONFIGURATION METHODS
    // ========================================================================

    /**
     * Returns a list of dependencies that this plugin requires.
     * <p>
     * These could be other plugins, libraries, or system components.
     * </p>
     *
     * @return A list of dependency identifiers.
     */
    List<String> getDependencies();

    /**
     * Injects shared dependencies or resources required by the plugin.
     *
     * @param dependencies A map of dependency names to objects.
     */
    void injectDependencies(Map<String, Object> dependencies);

    /**
     * Returns the configuration for this plugin.
     *
     * @return The {@link PluginConfig} for this plugin.
     */
    default PluginConfig getConfig() {
        return PluginConfig.DEFAULT;
    }

    /**
     * Returns the configuration schema for this plugin.
     * <p>
     * The schema defines the structure and validation rules for the plugin's
     * configuration.
     * </p>
     *
     * @return A map representing the configuration schema.
     */
    default Map<String, Object> getConfigurationSchema() {
        return Map.of();
    }

    /**
     * Updates the plugin's configuration dynamically.
     *
     * @param config A map representing the updated configuration.
     * @return {@code true} if the update is successful, {@code false} otherwise.
     */
    default boolean updateConfiguration(Map<String, Object> config) {
        return true;
    }

    /**
     * Saves plugin-specific settings.
     *
     * @param settings A map of settings to save.
     * @return {@code true} if the settings are saved successfully, {@code false} otherwise.
     */
    default boolean saveSettings(Map<String, Object> settings) {
        return true;
    }

    /**
     * Loads plugin-specific settings.
     *
     * @return A map of the plugin's saved settings.
     */
    default Map<String, Object> loadSettings() {
        return Map.of();
    }

    // ========================================================================
    // RESOURCE MANAGEMENT METHODS
    // ========================================================================

    /**
     * Returns a list of resources this plugin provides that can be used by other plugins.
     *
     * @return A map of resource names to objects.
     */
    default Map<String, Object> getProvidedResources() {
        return Map.of();
    }

    /**
     * Called to request resources from this plugin.
     *
     * @param resourceKey The key identifying the requested resource.
     * @return The requested resource, or {@code null} if not available.
     */
    default Object provideResource(String resourceKey) {
        return null;
    }

    /**
     * Returns the classloader used by this plugin.
     * <p>
     * Useful for isolated plugin loading.
     * </p>
     *
     * @return The plugin's classloader.
     */
    default ClassLoader getPluginClassLoader() {
        return this.getClass().getClassLoader();
    }

    /**
     * Returns whether the plugin requires classloader isolation.
     *
     * @return {@code true} if isolation is required, {@code false} otherwise.
     */
    default boolean requiresIsolation() {
        return false;
    }

    // ========================================================================
    // VALIDATION AND COMPATIBILITY METHODS
    // ========================================================================

    /**
     * Validates the plugin's configuration and dependencies.
     *
     * @return {@code true} if the plugin is valid, {@code false} otherwise.
     */
    boolean validate();

    /**
     * Validates the presence and compatibility of the plugin's dependencies.
     *
     * @return {@code true} if all dependencies are valid, {@code false} otherwise.
     */
    default boolean validateDependencies() {
        return true;
    }

    /**
     * Validates the plugin's configuration against a schema.
     *
     * @param config The configuration to validate.
     * @return {@code true} if the configuration is valid, {@code false} otherwise.
     */
    default boolean validateConfiguration(Map<String, Object> config) {
        return true;
    }

    /**
     * Returns the required version of the host application.
     *
     * @return The minimum required version of the host.
     */
    String getRequiredHostVersion();

    /**
     * Returns the minimum compatible version of the host application.
     *
     * @return The minimum compatible version of the host.
     */
    default String getMinimumHostVersion() {
        return getRequiredHostVersion();
    }

    /**
     * Returns the maximum compatible version of the host application.
     *
     * @return The maximum compatible version, or {@code null} for no upper limit.
     */
    default String getMaximumHostVersion() {
        return null;
    }

    /**
     * Checks compatibility with the current environment.
     *
     * @return A {@link CompatibilityResult} indicating compatibility status.
     */
    default CompatibilityResult checkCompatibility() {
        return new CompatibilityResult(true, "Compatible");
    }

    /**
     * Returns a list of incompatible plugins or libraries.
     *
     * @return A list of incompatible component identifiers.
     */
    default List<String> getIncompatibilities() {
        return List.of();
    }

    /**
     * Checks if this plugin version can be upgraded to the specified version.
     *
     * @param targetVersion The version to upgrade to.
     * @return {@code true} if the upgrade is possible, {@code false} otherwise.
     */
    default boolean canUpgradeTo(String targetVersion) {
        return true;
    }

    // ========================================================================
    // EVENT HANDLING METHODS
    // ========================================================================

    /**
     * Handles events sent to the plugin.
     *
     * @param eventType The type of event.
     * @param eventData The data associated with the event.
     */
    default void handleEvent(PluginEventType eventType, Object eventData) {
    }

    /**
     * Handles string-based events sent to the plugin.
     *
     * @param eventType The type of event as string.
     * @param eventData The data associated with the event.
     */
    default void handleEvent(String eventType, Object eventData) {
        // Convert string to enum if possible, otherwise use CUSTOM_EVENT
        try {
            PluginEventType type = PluginEventType.valueOf(eventType.toUpperCase());
            handleEvent(type, eventData);
        } catch (IllegalArgumentException e) {
            handleEvent(PluginEventType.CUSTOM_EVENT,
                    Map.of("customType", eventType, "data", eventData));
        }
    }

    /**
     * Publishes an event to the plugin system.
     *
     * @param eventType The type of event to publish.
     * @param eventData The data associated with the event.
     */
    default void publishEvent(String eventType, Object eventData) {
    }

    /**
     * Publishes a typed event to the plugin system.
     *
     * @param eventType The type of event to publish.
     * @param eventData The data associated with the event.
     */
    default void publishEvent(PluginEventType eventType, Object eventData) {
        publishEvent(eventType.name(), eventData);
    }

    /**
     * Returns the list of event types this plugin publishes.
     *
     * @return A list of event type identifiers.
     */
    default List<String> getPublishedEvents() {
        return List.of();
    }

    /**
     * Returns the list of event types this plugin subscribes to.
     *
     * @return A list of event type identifiers.
     */
    default List<String> getSubscribedEvents() {
        return List.of();
    }

    // ========================================================================
    // ERROR HANDLING AND RECOVERY METHODS
    // ========================================================================

    /**
     * Called when the plugin encounters an error.
     *
     * @param throwable The error encountered by the plugin.
     */
    default void onError(Throwable throwable) {
    }

    /**
     * Handles uncaught exceptions from the plugin.
     *
     * @param thread The thread where the exception occurred.
     * @param throwable The uncaught throwable.
     */
    default void handleUncaughtException(Thread thread, Throwable throwable) {
        System.err.println("Uncaught exception in plugin " + getName() + ":");
        throwable.printStackTrace();
    }

    /**
     * Returns whether the plugin can recover from errors automatically.
     *
     * @return {@code true} if the plugin is recoverable, {@code false} otherwise.
     */
    default boolean isRecoverable() {
        return true;
    }

    // ========================================================================
    // PERFORMANCE AND MONITORING METHODS
    // ========================================================================

    /**
     * Returns performance metrics for this plugin.
     *
     * @return A map of metric names to values.
     */
    default Map<String, Object> getMetrics() {
        return Map.of();
    }

    /**
     * Resets performance metrics.
     */
    default void resetMetrics() {
    }

    /**
     * Returns the average startup time in milliseconds.
     *
     * @return The average startup time.
     */
    default long getAverageStartupTime() {
        return 0;
    }

    /**
     * Performs a health check on the plugin.
     *
     * @return A {@link HealthStatus} indicating the plugin's health.
     */
    default HealthStatus healthCheck() {
        return new HealthStatus(HealthStatus.UP, "Healthy");
    }

    // ========================================================================
    // LOCALIZATION AND DOCUMENTATION METHODS
    // ========================================================================

    /**
     * Returns localized messages based on the given key and locale.
     *
     * @param key The key for the localized message.
     * @param locale The locale for which the message is required.
     * @return The localized message.
     */
    default String getLocalizedMessage(String key, String locale) {
        return key;
    }

    /**
     * Returns documentation URL for the plugin.
     *
     * @return The documentation URL, or {@code null} if not available.
     */
    default String getDocumentationUrl() {
        return null;
    }

    /**
     * Returns help text for the plugin.
     *
     * @return The help text.
     */
    default String getHelpText() {
        return getDescription();
    }

    /**
     * Returns examples of how to use the plugin.
     *
     * @return A list of usage examples.
     */
    default List<String> getUsageExamples() {
        return List.of();
    }

    // ========================================================================
    // SECURITY AND PERMISSION METHODS
    // ========================================================================

    /**
     * Returns a list of default permissions required by the plugin.
     *
     * @return A list of permission strings.
     */
    default List<String> getDefaultPermissions() {
        return List.of();
    }

    // ========================================================================
    // VERSIONING AND UPGRADE METHODS
    // ========================================================================

    /**
     * Called when the plugin is upgraded.
     *
     * @param oldVersion The previous version of the plugin.
     * @param newVersion The new version of the plugin.
     */
    default void onUpgrade(String oldVersion, String newVersion) {
    }

    // ========================================================================
    // INNER CLASSES AND ENUMS
    // ========================================================================

    /**
     * Represents the type of events that can be handled by plugins.
     */
    enum PluginEventType {
        /** Configuration has changed */
        CONFIG_CHANGED,
        /** A dependency has been loaded */
        DEPENDENCY_LOADED,
        /** User interaction occurred */
        USER_INTERACTION,
        /** System-level event */
        SYSTEM_EVENT,
        /** Custom event defined by the plugin */
        CUSTOM_EVENT
    }

    /**
     * Represents the result of a compatibility check.
     */
    class CompatibilityResult {
        private final boolean compatible;
        private final String message;

        /**
         * Creates a new compatibility result.
         *
         * @param compatible Whether the plugin is compatible.
         * @param message A message describing the compatibility status.
         */
        public CompatibilityResult(boolean compatible, String message) {
            this.compatible = compatible;
            this.message = message;
        }

        /**
         * Returns whether the plugin is compatible.
         *
         * @return {@code true} if compatible, {@code false} otherwise.
         */
        public boolean isCompatible() {
            return compatible;
        }

        /**
         * Returns the compatibility message.
         *
         * @return The message describing compatibility status.
         */
        public String getMessage() {
            return message;
        }
    }

    /**
     * Represents the health status of a plugin.
     */
    class HealthStatus {
        /** Plugin is healthy and functioning normally */
        public static final String UP = "UP";
        /** Plugin is not functioning */
        public static final String DOWN = "DOWN";
        /** Plugin is functioning with limitations */
        public static final String DEGRADED = "DEGRADED";

        private final String status;
        private final String message;
        private final Map<String, Object> details;

        /**
         * Creates a new health status with no details.
         *
         * @param status The health status (UP, DOWN, or DEGRADED).
         * @param message A message describing the health status.
         */
        public HealthStatus(String status, String message) {
            this(status, message, Map.of());
        }

        /**
         * Creates a new health status with details.
         *
         * @param status The health status (UP, DOWN, or DEGRADED).
         * @param message A message describing the health status.
         * @param details Additional details about the health status.
         */
        public HealthStatus(String status, String message, Map<String, Object> details) {
            this.status = status;
            this.message = message;
            this.details = details;
        }

        /**
         * Returns the health status.
         *
         * @return The status (UP, DOWN, or DEGRADED).
         */
        public String getStatus() {
            return status;
        }

        /**
         * Returns the health message.
         *
         * @return The message describing the health status.
         */
        public String getMessage() {
            return message;
        }

        /**
         * Returns additional health details.
         *
         * @return A map of health details.
         */
        public Map<String, Object> getDetails() {
            return details;
        }
    }
}