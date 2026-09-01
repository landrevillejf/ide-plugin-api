package com.protonmail.landrevillejf.ide.plugin;

import com.protonmail.landrevillejf.ide.plugin.events.Event;
import com.protonmail.landrevillejf.ide.plugin.events.EventListener;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
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

        // Convert to lowercase with explicit Locale
        String id = name.toLowerCase(Locale.ROOT);

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

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public PluginDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     * <p>
     * Stores the context, transitions the state to {@code INITIALIZED},
     * and invokes {@link #onStart()}.
     * </p>
     */
    @Override
    public void initialize(PluginContext context) {
        this.context = context;
        setState(PluginStatus.INITIALIZED);
        onStart();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Calls {@link #beforeEnable()}, transitions through {@code ENABLING}
     * to {@code ENABLED}, and invokes {@link #afterEnable()}. Startup
     * time metrics are recorded on completion.
     * </p>
     */
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

    /**
     * {@inheritDoc}
     * <p>
     * Calls {@link #beforeDisable()}, transitions through {@code DISABLING}
     * to {@code DISABLED}, and invokes {@link #afterDisable()}.
     * </p>
     */
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

    /**
     * {@inheritDoc}
     * <p>
     * Transitions through {@code SHUTTING_DOWN}, invokes {@link #onStop()}
     * and {@link #cleanup()}, then transitions to {@code SHUTDOWN}.
     * </p>
     */
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

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public PluginStatus getState() {
        return state;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Validates the transition using {@link PluginStatus#canTransitionTo(PluginStatus)}
     * and stores the new state if valid.
     * </p>
     *
     * @throws IllegalStateException if the transition is not allowed
     */
    @Override
    public void setState(PluginStatus newState) {
        if (!this.state.canTransitionTo(newState)) {
            throw new IllegalStateException(
                    "Invalid state transition from " + this.state + " to " + newState
            );
        }
        this.state = newState;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        return state == PluginStatus.ENABLED;
    }

    // ========================================================================
    // DEPENDENCY AND CONFIGURATION METHODS
    // ========================================================================

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     * <p>
     * All entries are stored in an internal {@link ConcurrentHashMap}.
     * </p>
     */
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

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public Map<String, Object> getConfigurationSchema() {
        return Map.of(
                "enabled", Map.of("type", "boolean", "default", true),
                "autoStart", Map.of("type", "boolean", "default", false)
        );
    }

    /**
     * {@inheritDoc}
     * <p>
     * Validates the configuration before applying it. If valid, publishes
     * a {@code CONFIG_CHANGED} event.
     * </p>
     *
     * @return {@code true} if the configuration was valid and applied
     */
    @Override
    public boolean updateConfiguration(Map<String, Object> config) {
        if (validateConfiguration(config)) {
            this.config = new PluginConfig(config);
            publishEvent(PluginEventType.CONFIG_CHANGED, config);
            return true;
        }
        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public boolean saveSettings(Map<String, Object> settings) {
        this.settings.putAll(settings);
        return true;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public Map<String, Object> getProvidedResources() {
        return new HashMap<>(providedResources);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     * <p>
     * Delegates to {@link #validateDependencies()} and
     * {@link #validateConfiguration(Map)}.
     * </p>
     *
     * @return {@code true} if both dependencies and configuration are valid
     */
    @Override
    public boolean validate() {
        return validateDependencies() && validateConfiguration(config.toMap());
    }

    /**
     * {@inheritDoc}
     * <p>
     * Checks that every declared dependency has been injected.
     * </p>
     *
     * @return {@code true} if all dependencies are present
     */
    @Override
    public boolean validateDependencies() {
        for (String dependency : dependenciesList) {
            if (!dependencies.containsKey(dependency)) {
                return false;
            }
        }
        return true;
    }

    /**
     * {@inheritDoc}
     * <p>
     * The base implementation only checks for {@code null}.
     * Subclasses should override with stricter validation.
     * </p>
     *
     * @return {@code true} if the configuration map is non-null
     */
    @Override
    public boolean validateConfiguration(Map<String, Object> config) {
        // Basic validation - subclasses can override with more specific validation
        return config != null;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     * <p>
     * The base implementation always returns a compatible result.
     * Subclasses can override with more specific checks.
     * </p>
     *
     * @return a {@link CompatibilityResult} indicating compatibility status
     */
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

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public List<String> getIncompatibilities() {
        return List.of(); // Subclasses can override
    }

    /**
     * {@inheritDoc}
     * <p>
     * Uses simple numeric version comparison. Subclasses can override
     * with more sophisticated logic.
     * </p>
     *
     * @return {@code true} if the current version is lower than the target
     */
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

    /**
     * {@inheritDoc}
     * <p>
     * Dispatches to the appropriate {@code on*} handler method based
     * on the event type.
     * </p>
     */
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
            default:
                // CUSTOM_EVENT and any unknown event type are routed to the custom handler
                onCustomEvent(eventData);
                break;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Creates a {@link GenericEvent} and publishes it through the
     * internal event bus.
     * </p>
     */
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

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public List<String> getPublishedEvents() {
        return new ArrayList<>(publishedEvents);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     * <p>
     * Updates error metrics and attempts automatic recovery if
     * {@link #isRecoverable()} returns {@code true}. If recovery fails,
     * the error is escalated to {@link #handleUncaughtException(Thread, Throwable)}.
     * </p>
     */
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

    /**
     * {@inheritDoc}
     * <p>
     * Logs the exception at ERROR level and updates error metrics.
     * </p>
     */
    @Override
    public void handleUncaughtException(Thread thread, Throwable throwable) {
        if (log.isErrorEnabled()) {
            log.error("[" + getName() + "] Uncaught exception in thread " + thread.getName() + ":", throwable);
        }

        // Update error metrics
        metrics.put("uncaughtExceptions", ((int) metrics.getOrDefault("uncaughtExceptions", 0)) + 1);
        metrics.put("lastUncaughtException", System.currentTimeMillis());
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code true} always, indicating the plugin can recover automatically
     */
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

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public Map<String, Object> getMetrics() {
        return new HashMap<>(metrics);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Clears all metrics and re-initializes default counters.
     * </p>
     */
    @Override
    public void resetMetrics() {
        metrics.clear();
        initializeMetrics();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public long getAverageStartupTime() {
        long count = startupCount.get();
        if (count == 0) {
            return 0;
        }
        return startupTimeTotal.get() / count;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Reports {@code DOWN} if the plugin is in {@code ERROR} or {@code SHUTDOWN}
     * state; otherwise reports {@code UP}.
     * </p>
     *
     * @return {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     * <p>
     * The base implementation simply returns the key unchanged.
     * Subclasses can override with proper resource-bundle lookup.
     * </p>
     *
     * @return {@inheritDoc}
     */
    @Override
    public String getLocalizedMessage(String key, String locale) {
        // Simple implementation - subclasses can override with proper localization
        return key;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@code null} always; subclasses should override
     */
    @Override
    public String getDocumentationUrl() {
        // Default implementation - subclasses can override
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public String getHelpText() {
        return getDescription();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
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

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
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
        if (log.isInfoEnabled()) {
            log.info("[{}] {}", getName(), message);
        }
    }

    /**
     * Logs an error message with the plugin name prefix.
     *
     * @param message The error message to log.
     */
    protected void logError(String message) {
        if (log.isErrorEnabled()) {
            log.error("[{}] ERROR: {}", getName(), message);
        }
    }

    /**
     * Logs a debug message with the plugin name prefix.
     *
     * @param message The debug message to log.
     */
    protected void logDebug(String message) {
        if (log.isInfoEnabled()) {
            log.info("[{}] DEBUG: {}", getName(), message);
        }
    }
}