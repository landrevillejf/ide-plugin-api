package com.protonmail.landrevillejf.ide.plugin;

import com.protonmail.landrevillejf.ide.plugin.events.Event;
import com.protonmail.landrevillejf.ide.plugin.events.EventListener;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Abstract base class for plugins that provides default implementations
 * for most {@link Plugin} interface methods.
 * <p>
 * Plugin developers should extend this class rather than implementing
 * the {@link Plugin} interface directly, unless they need different behavior.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public abstract class AbstractPlugin implements Plugin {

    // ========================================================================
    // FIELDS
    // ========================================================================

    private final PluginDescriptor descriptor;
    private final Map<String, Object> dependencies = new ConcurrentHashMap<>();
    private final Map<String, Object> settings = new ConcurrentHashMap<>();
    private final Map<String, Object> providedResources = new ConcurrentHashMap<>();
    private final Map<String, Object> metrics = new ConcurrentHashMap<>();
    private final List<String> dependenciesList = new ArrayList<>();

    // Event system
    private final PluginEventBus eventBus = new PluginEventBus();
    private final List<String> publishedEvents = new ArrayList<>();
    private final List<String> subscribedEvents = new ArrayList<>();

    private PluginContext context;
    private PluginStatus state = PluginStatus.DISABLED;
    private PluginConfig config = PluginConfig.DEFAULT;
    private String category = "General";
    private String authorEmail = "";
    private String requiredHostVersion = "1.0.0";
    private String specificationTitle = "";
    private String specificationVersion = "";
    private String specificationVendor = "";
    private String implementationVersion = "";
    private Map<String, Object> customMetadata = new HashMap<>();

    private final AtomicLong startupTimeTotal = new AtomicLong(0);
    private final AtomicLong startupCount = new AtomicLong(0);
    private long lastStartupTime = 0;

    // ========================================================================
    // CONSTRUCTORS
    // ========================================================================

    /**
     * Creates a new AbstractPlugin with a descriptor built from the provided parameters.
     * <p>
     * The plugin ID will be generated from the name by converting to lowercase
     * and replacing spaces with hyphens.
     * </p>
     *
     * @param name        The plugin name.
     * @param version     The plugin version.
     * @param description The plugin description.
     * @param author      The plugin author.
     */
    protected AbstractPlugin(String name, String version, String description, String author) {
        // Generate an ID from the name
        String id = generatePluginId(name);
        // Use the current class as the main class
        String mainClass = this.getClass().getName();

        this.descriptor = new PluginDescriptor(id, name, version, mainClass, description, author);
        initializeMetrics();
    }

    /**
     * Creates a new AbstractPlugin with a descriptor built from the provided parameters.
     *
     * @param id          The unique plugin identifier.
     * @param name        The plugin name.
     * @param version     The plugin version.
     * @param description The plugin description.
     * @param author      The plugin author.
     */
    protected AbstractPlugin(String id, String name, String version, String description, String author) {
        String mainClass = this.getClass().getName();
        this.descriptor = new PluginDescriptor(id, name, version, mainClass, description, author);
        initializeMetrics();
    }

    /**
     * Creates a new AbstractPlugin with a provided descriptor.
     *
     * @param descriptor The plugin descriptor containing metadata.
     */
    protected AbstractPlugin(PluginDescriptor descriptor) {
        this.descriptor = descriptor;
        initializeMetrics();
    }

    /**
     * Generates a plugin ID from a plugin name.
     * <p>
     * Converts the name to lowercase, replaces spaces with hyphens,
     * and removes any non-alphanumeric characters except hyphens.
     * </p>
     *
     * @param name The plugin name.
     * @return A generated plugin ID.
     */
    private String generatePluginId(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "unknown-plugin";
        }

        // Convert to lowercase
        String id = name.toLowerCase();

        // Replace spaces with hyphens
        id = id.replaceAll("\\s+", "-");

        // Remove any characters that are not alphanumeric or hyphens
        id = id.replaceAll("[^a-z0-9-]", "");

        // Ensure it starts and ends with alphanumeric characters
        id = id.replaceAll("^-+|-+$", "");

        // If the result is empty, use a default
        if (id.isEmpty()) {
            id = "plugin-" + System.currentTimeMillis();
        }

        return id;
    }

    // ========================================================================
    // DESCRIPTOR AND BASIC INFORMATION METHODS
    // ========================================================================

    @Override
    public PluginDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public String getAuthorEmail() {
        return authorEmail;
    }

    /**
     * Sets the author's email address.
     *
     * @param authorEmail The author's email address.
     */
    protected void setAuthorEmail(String authorEmail) {
        this.authorEmail = authorEmail;
    }

    @Override
    public String getCategory() {
        return category;
    }

    /**
     * Sets the plugin category.
     *
     * @param category The plugin category.
     */
    protected void setCategory(String category) {
        this.category = category;
    }

    @Override
    public Map<String, Object> getCustomMetadata() {
        return new HashMap<>(customMetadata);
    }

    /**
     * Sets custom metadata for the plugin.
     *
     * @param customMetadata Custom metadata to set.
     */
    protected void setCustomMetadata(Map<String, Object> customMetadata) {
        this.customMetadata = new HashMap<>(customMetadata);
    }

    /**
     * Adds a single custom metadata entry.
     *
     * @param key   The metadata key.
     * @param value The metadata value.
     */
    protected void addCustomMetadata(String key, Object value) {
        this.customMetadata.put(key, value);
    }

    // ========================================================================
    // MANIFEST INFORMATION METHODS
    // ========================================================================

    @Override
    public String getSpecificationTitle() {
        return specificationTitle;
    }

    /**
     * Sets the specification title.
     *
     * @param specificationTitle The specification title.
     */
    protected void setSpecificationTitle(String specificationTitle) {
        this.specificationTitle = specificationTitle;
    }

    @Override
    public String getSpecificationVersion() {
        return specificationVersion;
    }

    /**
     * Sets the specification version.
     *
     * @param specificationVersion The specification version.
     */
    protected void setSpecificationVersion(String specificationVersion) {
        this.specificationVersion = specificationVersion;
    }

    @Override
    public String getSpecificationVendor() {
        return specificationVendor;
    }

    /**
     * Sets the specification vendor.
     *
     * @param specificationVendor The specification vendor.
     */
    protected void setSpecificationVendor(String specificationVendor) {
        this.specificationVendor = specificationVendor;
    }

    @Override
    public String getImplementationVersion() {
        return implementationVersion;
    }

    /**
     * Sets the implementation version.
     *
     * @param implementationVersion The implementation version.
     */
    protected void setImplementationVersion(String implementationVersion) {
        this.implementationVersion = implementationVersion;
    }

    // ========================================================================
    // LIFECYCLE METHODS
    // ========================================================================

    @Override
    public void initialize(PluginContext context) {
        this.context = context;
        setState(PluginStatus.INITIALIZED);
        onStart();
    }

    @Override
    public void enable() {
        long startTime = System.currentTimeMillis();

        try {
            if (!beforeEnable()) {
                throw new IllegalStateException("Plugin cannot be enabled: beforeEnable() returned false");
            }

            setState(PluginStatus.ENABLING);
            // Plugin-specific enabling logic should be implemented in subclasses

            setState(PluginStatus.ENABLED);
            afterEnable();

        } finally {
            lastStartupTime = System.currentTimeMillis() - startTime;
            startupTimeTotal.addAndGet(lastStartupTime);
            startupCount.incrementAndGet();
            updateStartupMetrics();
        }
    }

    @Override
    public void disable() {
        if (!beforeDisable()) {
            throw new IllegalStateException("Plugin cannot be disabled: beforeDisable() returned false");
        }

        setState(PluginStatus.DISABLING);
        // Plugin-specific disabling logic should be implemented in subclasses

        setState(PluginStatus.DISABLED);
        afterDisable();
    }

    @Override
    public void shutdown() {
        setState(PluginStatus.SHUTTING_DOWN);
        onStop();
        cleanup();
        setState(PluginStatus.SHUTDOWN);
    }

    // ========================================================================
    // STATE MANAGEMENT METHODS
    // ========================================================================

    @Override
    public PluginStatus getState() {
        return state;
    }

    @Override
    public void setState(PluginStatus newState) {
        if (!this.state.canTransitionTo(newState)) {
            throw new IllegalStateException(
                    "Invalid state transition from " + this.state + " to " + newState
            );
        }
        this.state = newState;
    }

    @Override
    public boolean isEnabled() {
        return state == PluginStatus.ENABLED;
    }

    // ========================================================================
    // DEPENDENCY AND CONFIGURATION METHODS
    // ========================================================================

    @Override
    public List<String> getDependencies() {
        return new ArrayList<>(dependenciesList);
    }

    /**
     * Adds a dependency to the plugin's dependency list.
     *
     * @param dependency The dependency identifier to add.
     */
    protected void addDependency(String dependency) {
        dependenciesList.add(dependency);
    }

    /**
     * Adds multiple dependencies to the plugin's dependency list.
     *
     * @param dependencies The dependency identifiers to add.
     */
    protected void addDependencies(List<String> dependencies) {
        dependenciesList.addAll(dependencies);
    }

    @Override
    public void injectDependencies(Map<String, Object> dependencies) {
        this.dependencies.putAll(dependencies);
    }

    /**
     * Gets a specific dependency by name.
     *
     * @param name The name of the dependency.
     * @param <T>  The type of the dependency.
     * @return The dependency, or {@code null} if not found.
     */
    @SuppressWarnings("unchecked")
    protected <T> T getDependency(String name) {
        return (T) dependencies.get(name);
    }

    @Override
    public PluginConfig getConfig() {
        return config;
    }

    /**
     * Sets the plugin configuration.
     *
     * @param config The plugin configuration to set.
     */
    protected void setConfig(PluginConfig config) {
        this.config = config;
    }

    @Override
    public Map<String, Object> getConfigurationSchema() {
        return Map.of(
                "enabled", Map.of("type", "boolean", "default", true),
                "autoStart", Map.of("type", "boolean", "default", false)
        );
    }

    @Override
    public boolean updateConfiguration(Map<String, Object> config) {
        if (validateConfiguration(config)) {
            this.config = new PluginConfig(config);
             publishEvent(PluginEventType.CONFIG_CHANGED, config);
            return true;
        }
        return false;
    }

    @Override
    public boolean saveSettings(Map<String, Object> settings) {
        this.settings.putAll(settings);
        return true;
    }

    @Override
    public Map<String, Object> loadSettings() {
        return new HashMap<>(settings);
    }

    /**
     * Gets a specific setting by key.
     *
     * @param key The setting key.
     * @param <T> The type of the setting value.
     * @return The setting value, or {@code null} if not found.
     */
    @SuppressWarnings("unchecked")
    protected <T> T getSetting(String key) {
        return (T) settings.get(key);
    }

    /**
     * Sets a specific setting.
     *
     * @param key   The setting key.
     * @param value The setting value.
     */
    protected void setSetting(String key, Object value) {
        settings.put(key, value);
    }

    // ========================================================================
    // RESOURCE MANAGEMENT METHODS
    // ========================================================================

    @Override
    public Map<String, Object> getProvidedResources() {
        return new HashMap<>(providedResources);
    }

    @Override
    public Object provideResource(String resourceKey) {
        return providedResources.get(resourceKey);
    }

    /**
     * Adds a resource that this plugin provides.
     *
     * @param key      The resource key.
     * @param resource The resource object.
     */
    protected void addProvidedResource(String key, Object resource) {
        providedResources.put(key, resource);
         publishEvent(PluginEventType.CUSTOM_EVENT,
             Map.of("type", "RESOURCE_ADDED", "key", key, "resource", resource));
    }

    /**
     * Removes a provided resource.
     *
     * @param key The resource key to remove.
     */
    protected void removeProvidedResource(String key) {
        providedResources.remove(key);
    }

    // ========================================================================
    // VALIDATION AND COMPATIBILITY METHODS
    // ========================================================================

    @Override
    public boolean validate() {
        return validateDependencies() && validateConfiguration(config.toMap());
    }

    @Override
    public boolean validateDependencies() {
        for (String dependency : dependenciesList) {
            if (!dependencies.containsKey(dependency)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean validateConfiguration(Map<String, Object> config) {
        // Basic validation - subclasses can override with more specific validation
        return config != null;
    }

    @Override
    public String getRequiredHostVersion() {
        return requiredHostVersion;
    }

    /**
     * Sets the required host version.
     *
     * @param requiredHostVersion The required host version.
     */
    protected void setRequiredHostVersion(String requiredHostVersion) {
        this.requiredHostVersion = requiredHostVersion;
    }

    @Override
    public CompatibilityResult checkCompatibility() {
        // Basic compatibility check - subclasses can override
        boolean compatible = true;
        String message = "Compatible";

        if (getRequiredHostVersion() == null || getRequiredHostVersion().isEmpty()) {
            compatible = false;
            message = "Required host version not specified";
        }

        return new CompatibilityResult(compatible, message);
    }

    @Override
    public List<String> getIncompatibilities() {
        return List.of(); // Subclasses can override
    }

    @Override
    public boolean canUpgradeTo(String targetVersion) {
        // Simple version comparison - subclasses can override with more sophisticated logic
        try {
            String currentVersion = getVersion();
            return compareVersions(currentVersion, targetVersion) < 0;
        } catch (Exception e) {
            return false;
        }
    }

    // ========================================================================
    // EVENT HANDLING METHODS (UPDATED)
    // ========================================================================

    @Override
    public void handleEvent(PluginEventType eventType, Object eventData) {
        switch (eventType) {
            case CONFIG_CHANGED:
                onConfigurationChanged(eventData);
                break;
            case DEPENDENCY_LOADED:
                onDependencyLoaded(eventData);
                break;
            case USER_INTERACTION:
                onUserInteraction(eventData);
                break;
            case SYSTEM_EVENT:
                onSystemEvent(eventData);
                break;
            case CUSTOM_EVENT:
                onCustomEvent(eventData);
                break;
        }
    }

    @Override
    public void publishEvent(String eventType, Object eventData) {
        // Create a generic event and publish it through the event bus
        GenericEvent event = new GenericEvent(eventType, eventData, this);
        eventBus.publish(event);
    }

    /**
     * Publishes a typed event to the event bus.
     *
     * @param event The event to publish.
     * @param <T>   The event type.
     */
    protected <T extends Event> void publishEvent(T event) {
        eventBus.publish(event);
    }

    @Override
    public List<String> getPublishedEvents() {
        return new ArrayList<>(publishedEvents);
    }

    @Override
    public List<String> getSubscribedEvents() {
        return new ArrayList<>(subscribedEvents);
    }

    /**
     * Subscribes to a typed event.
     *
     * @param eventType The class of the event to subscribe to.
     * @param listener  The listener to handle the event.
     * @param <T>       The event type.
     */
    protected <T extends Event> void subscribe(
            Class<T> eventType,
            EventListener<T> listener) {
        eventBus.subscribe(eventType, listener);
        subscribedEvents.add(eventType.getSimpleName());
    }

    /**
     * Unsubscribes from a typed event.
     *
     * @param eventType The class of the event to unsubscribe from.
     * @param listener  The listener to remove.
     * @param <T>       The event type.
     */
    protected <T extends Event> void unsubscribe(
            Class<T> eventType,
            EventListener<T> listener) {
        eventBus.unsubscribe(eventType, listener);
        subscribedEvents.remove(eventType.getSimpleName());
    }

    /**
     * Adds a published event type.
     *
     * @param eventType The event type to add.
     */
    protected void addPublishedEvent(String eventType) {
        publishedEvents.add(eventType);
    }

    /**
     * Adds a subscribed event type.
     *
     * @param eventType The event type to add.
     */
    protected void addSubscribedEvent(String eventType) {
        subscribedEvents.add(eventType);
    }

    // ========================================================================
    // EVENT HANDLER METHODS
    // ========================================================================

    /**
     * Called when configuration changes.
     *
     * @param eventData The new configuration data.
     */
    protected void onConfigurationChanged(Object eventData) {
        // Subclasses can override
    }

    /**
     * Called when a dependency is loaded.
     *
     * @param eventData Information about the loaded dependency.
     */
    protected void onDependencyLoaded(Object eventData) {
        // Subclasses can override
    }

    /**
     * Called on user interaction.
     *
     * @param eventData Information about the user interaction.
     */
    protected void onUserInteraction(Object eventData) {
        // Subclasses can override
    }

    /**
     * Called on system events.
     *
     * @param eventData Information about the system event.
     */
    protected void onSystemEvent(Object eventData) {
        // Subclasses can override
    }

    /**
     * Called on custom events.
     *
     * @param eventData The custom event data.
     */
    protected void onCustomEvent(Object eventData) {
        // Subclasses can override
    }

    // ========================================================================
    // UTILITY METHODS FOR EVENT SYSTEM
    // ========================================================================

    /**
     * Gets the event bus for this plugin.
     *
     * @return The plugin's event bus.
     */
    protected PluginEventBus getEventBus() {
        return eventBus;
    }

    /**
     * Creates a generic event with the specified type and data.
     *
     * @param type The event type.
     * @param data The event data.
     * @return A new GenericEvent instance.
     */
    protected GenericEvent createEvent(String type, Object data) {
        return new GenericEvent(type, data, this);
    }

    /**
     * Checks if there are any subscribers for a specific event type.
     *
     * @param eventType The event type class to check.
     * @return {@code true} if there are subscribers, {@code false} otherwise.
     */
    protected boolean hasSubscribers(Class<? extends Event> eventType) {
        return eventBus.hasSubscribers(eventType);
    }

    // ========================================================================
    // INNER CLASS: GenericEvent
    // ========================================================================

    /**
     * A generic event implementation for string-based event types.
     * Implements the Event interface from the events package.
     */
    public static class GenericEvent implements Event {
        private final String type;
        private final Object data;
        private final long timestamp;
        private final Plugin source;

        /**
         * Creates a new generic event.
         *
         * @param type   The event type.
         * @param data   The event data.
         * @param source The plugin that published the event.
         */
        public GenericEvent(String type, Object data, Plugin source) {
            this.type = type;
            this.data = data;
            this.source = source;
            this.timestamp = System.currentTimeMillis();
        }

        /**
         * Gets the event type.
         *
         * @return The event type.
         */
        public String getType() {
            return type;
        }

        /**
         * Gets the event data.
         *
         * @return The event data.
         */
        public Object getData() {
            return data;
        }

        /**
         * Gets the plugin that published this event.
         *
         * @return The source plugin.
         */
        public String getSource() {
            return source.getName();
        }

        @Override
        public String toString() {
            return "GenericEvent{" +
                    "type='" + type + '\'' +
                    ", source='" + (source != null ? source.getName() : "unknown") + '\'' +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }

    // ========================================================================
    // ERROR HANDLING AND RECOVERY METHODS
    // ========================================================================

    @Override
    public void onError(Throwable throwable) {
        // Default error handling - log and update metrics
        metrics.put("lastError", System.currentTimeMillis());
        metrics.put("errorCount", ((int) metrics.getOrDefault("errorCount", 0)) + 1);

        if (isRecoverable()) {
            try {
                recoverFromError(throwable);
            } catch (Exception e) {
                // If recovery fails, escalate
                throwable.addSuppressed(e);
                handleUncaughtException(Thread.currentThread(), throwable);
            }
        } else {
            handleUncaughtException(Thread.currentThread(), throwable);
        }
    }

    @Override
    public void handleUncaughtException(Thread thread, Throwable throwable) {
        log.error("[" + getName() + "] Uncaught exception in thread " + thread.getName() + ":");
        throwable.printStackTrace();

        // Update error metrics
        metrics.put("uncaughtExceptions", ((int) metrics.getOrDefault("uncaughtExceptions", 0)) + 1);
        metrics.put("lastUncaughtException", System.currentTimeMillis());
    }

    @Override
    public boolean isRecoverable() {
        return true;
    }

    /**
     * Attempts to recover from an error.
     *
     * @param throwable The error that occurred.
     */
    protected void recoverFromError(Throwable throwable) {
        // Default recovery strategy - disable and re-enable the plugin
        if (isEnabled()) {
            disable();
            enable();
        }
    }

    // ========================================================================
    // PERFORMANCE AND MONITORING METHODS
    // ========================================================================

    @Override
    public Map<String, Object> getMetrics() {
        return new HashMap<>(metrics);
    }

    @Override
    public void resetMetrics() {
        metrics.clear();
        initializeMetrics();
    }

    @Override
    public long getAverageStartupTime() {
        long count = startupCount.get();
        if (count == 0) {
            return 0;
        }
        return startupTimeTotal.get() / count;
    }

    @Override
    public HealthStatus healthCheck() {
        boolean healthy = state != PluginStatus.ERROR && state != PluginStatus.SHUTDOWN;
        String status = healthy ? HealthStatus.UP : HealthStatus.DOWN;
        String message = healthy ? "Plugin is healthy" : "Plugin is not healthy";

        Map<String, Object> details = new HashMap<>();
        details.put("state", state.name());
        details.put("enabled", isEnabled());
        details.put("averageStartupTime", getAverageStartupTime());
        details.put("errorCount", metrics.getOrDefault("errorCount", 0));

        return new HealthStatus(status, message, details);
    }

    /**
     * Initializes default metrics.
     */
    private void initializeMetrics() {
        metrics.put("startupCount", 0L);
        metrics.put("errorCount", 0);
        metrics.put("uncaughtExceptions", 0);
        metrics.put("lastStartupTime", 0L);
        metrics.put("averageStartupTime", 0L);
        metrics.put("lastError", 0L);
        metrics.put("lastUncaughtException", 0L);
    }

    /**
     * Updates startup-related metrics.
     */
    private void updateStartupMetrics() {
        metrics.put("startupCount", startupCount.get());
        metrics.put("lastStartupTime", lastStartupTime);
        metrics.put("averageStartupTime", getAverageStartupTime());
    }

    // ========================================================================
    // LOCALIZATION AND DOCUMENTATION METHODS
    // ========================================================================

    @Override
    public String getLocalizedMessage(String key, String locale) {
        // Simple implementation - subclasses can override with proper localization
        return key;
    }

    @Override
    public String getDocumentationUrl() {
        // Default implementation - subclasses can override
        return null;
    }

    @Override
    public String getHelpText() {
        return getDescription();
    }

    @Override
    public List<String> getUsageExamples() {
        return List.of(
                "Enable the plugin: plugin.enable()",
                "Disable the plugin: plugin.disable()",
                "Check status: plugin.getState()"
        );
    }

    // ========================================================================
    // SECURITY AND PERMISSION METHODS
    // ========================================================================

    @Override
    public List<String> getDefaultPermissions() {
        return List.of("read", "write");
    }

    // ========================================================================
    // UTILITY METHODS
    // ========================================================================

    /**
     * Gets the plugin context.
     *
     * @return The plugin context, or {@code null} if not initialized.
     */
    protected PluginContext getContext() {
        return context;
    }

    /**
     * Compares two version strings.
     *
     * @param version1 First version string.
     * @param version2 Second version string.
     * @return Negative if version1 < version2, zero if equal, positive if version1 > version2.
     */
    private int compareVersions(String version1, String version2) {
        String[] parts1 = version1.split("\\.");
        String[] parts2 = version2.split("\\.");

        int maxLength = Math.max(parts1.length, parts2.length);

        for (int i = 0; i < maxLength; i++) {
            int part1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int part2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;

            if (part1 != part2) {
                return part1 - part2;
            }
        }

        return 0;
    }

    /**
     * Logs a message with the plugin name prefix.
     *
     * @param message The message to log.
     */
    protected void log(String message) {
        log.info("[{}] {}", getName(), message);
    }

    /**
     * Logs an error message with the plugin name prefix.
     *
     * @param message The error message to log.
     */
    protected void logError(String message) {
        log.error("[{}] ERROR: {}", getName(), message);
    }

    /**
     * Logs a debug message with the plugin name prefix.
     *
     * @param message The debug message to log.
     */
    protected void logDebug(String message) {
        log.info("[{}] DEBUG: {}", getName(), message);
    }
}