package com.protonmail.landrevillejf.ide.plugin;

import com.protonmail.landrevillejf.ide.plugin.service.*;
import com.protonmail.landrevillejf.swingide.core.bus.EventBus;
import com.protonmail.landrevillejf.swingide.core.registry.ServiceRegistry;
import com.protonmail.landrevillejf.ide.plugin.service.*;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

/**
 * Initializes and provides plugin services for the application.
 * This class handles the setup of all plugin services.
 */
@Slf4j
public class PluginServiceInitializer {

    /**
     * Creates an extended plugin context with all services configured.
     *
     * @param serviceRegistry the application service registry
     * @param pluginEventBus the plugin event bus
     * @param applicationEventBus the application event bus
     * @param pluginManager the plugin manager
     * @param pluginDataDirectory the plugin data directory
     * @param pluginId the plugin identifier
     * @return an extended plugin context with all services
     */
    public static ExtendedPluginContext createExtendedPluginContext(
            ServiceRegistry serviceRegistry,
            PluginEventBus pluginEventBus,
            EventBus applicationEventBus,
            PluginManager pluginManager,
            File pluginDataDirectory,
            String pluginId) {

        // Create service locator for all plugin services
        PluginServiceLocator serviceLocator = createServiceLocator(serviceRegistry, pluginEventBus, applicationEventBus);

        // Create and return extended context
        DefaultExtendedPluginContext context = new DefaultExtendedPluginContext(
                serviceRegistry,
                pluginEventBus,
                applicationEventBus,
                pluginManager,
                pluginDataDirectory,
                pluginId,
                serviceLocator
        );

        log.info("Plugin service initializer created extended plugin context for: {}", pluginId);
        return context;
    }

    /**
     * Creates a service locator with stub implementations of all plugin services.
     * These can be replaced with real implementations as needed.
     *
     * @param serviceRegistry the application service registry
     * @param pluginEventBus the plugin event bus
     * @param applicationEventBus the application event bus
     * @return a configured plugin service locator
     */
    public static PluginServiceLocator createServiceLocator(
            ServiceRegistry serviceRegistry,
            PluginEventBus pluginEventBus,
            EventBus applicationEventBus) {

        // Check if a custom locator is registered in the service registry
        PluginServiceLocator customLocator = serviceRegistry.getService(PluginServiceLocator.class);
        if (customLocator != null) {
            log.info("Using custom PluginServiceLocator from registry");
            return customLocator;
        }

        // Create default stub implementation
        log.info("Creating stub PluginServiceLocator with default implementations");
        return new StubPluginServiceLocator();
    }

    /**
     * Registers plugin services in the application service registry.
     * This allows plugins to access services through dependency injection.
     *
     * @param serviceRegistry the application service registry
     * @param context the extended plugin context
     */
    public static void registerServicesInRegistry(ServiceRegistry serviceRegistry, ExtendedPluginContext context) {
        try {
            serviceRegistry.register(PluginServiceLocator.class, context.getServiceLocator());
            serviceRegistry.register(PluginLoggingService.class, context.getLoggingService());
            serviceRegistry.register(PluginCacheService.class, context.getCacheService());
            serviceRegistry.register(PluginMetricsService.class, context.getMetricsService());
            serviceRegistry.register(PluginNotificationService.class, context.getNotificationService());
            serviceRegistry.register(PluginPermissionService.class, context.getPermissionService());
            serviceRegistry.register(PluginAsyncTaskExecutor.class, context.getAsyncTaskExecutor());
            serviceRegistry.register(PluginConfigurationValidator.class, context.getConfigurationValidator());
            serviceRegistry.register(PluginHookService.class, context.getHookService());
            serviceRegistry.register(PluginDataStore.class, context.getDataStore());
            serviceRegistry.register(PluginResourceManager.class, context.getResourceManager());
            serviceRegistry.register(PluginDependencyResolver.class, context.getDependencyResolver());
            serviceRegistry.register(PluginUpdateService.class, context.getUpdateService());
            serviceRegistry.register(PluginMonitoringService.class, context.getMonitoringService());

            log.info("Registered {} plugin services in application service registry", 14);
        } catch (Exception e) {
            log.error("Failed to register plugin services", e);
        }
    }

    /**
     * Stub implementation of PluginServiceLocator providing minimal service implementations.
     * This is used when no specific service implementations are provided.
     */
    private static class StubPluginServiceLocator implements PluginServiceLocator {
        @Override
        public PluginLoggingService getLoggingService() {
            return StubServices.LOGGING_SERVICE;
        }

        @Override
        public PluginCacheService getCacheService() {
            return StubServices.CACHE_SERVICE;
        }

        @Override
        public PluginNotificationService getNotificationService() {
            return StubServices.NOTIFICATION_SERVICE;
        }

        @Override
        public PluginMetricsService getMetricsService() {
            return StubServices.METRICS_SERVICE;
        }

        @Override
        public PluginPermissionService getPermissionService() {
            return StubServices.PERMISSION_SERVICE;
        }

        @Override
        public PluginAsyncTaskExecutor getAsyncTaskExecutor() {
            return StubServices.ASYNC_EXECUTOR;
        }

        @Override
        public PluginConfigurationValidator getConfigurationValidator() {
            return StubServices.CONFIG_VALIDATOR;
        }

        @Override
        public PluginHookService getHookService() {
            return StubServices.HOOK_SERVICE;
        }

        @Override
        public PluginDataStore getDataStore() {
            return StubServices.DATA_STORE;
        }

        @Override
        public PluginResourceManager getResourceManager() {
            return StubServices.RESOURCE_MANAGER;
        }

        @Override
        public PluginDependencyResolver getDependencyResolver() {
            return StubServices.DEPENDENCY_RESOLVER;
        }

        @Override
        public PluginUpdateService getUpdateService() {
            return StubServices.UPDATE_SERVICE;
        }

        @Override
        public PluginMonitoringService getMonitoringService() {
            return StubServices.MONITORING_SERVICE;
        }

        @Override
        public <T> T getService(Class<T> serviceInterface) {
            return null;
        }

        @Override
        public <T> void registerService(Class<T> serviceInterface, T implementation) {
            log.debug("Service registration not supported in stub implementation");
        }
    }

    /**
     * Provider for stub service implementations.
     */
    static class StubServices {
        static final PluginLoggingService LOGGING_SERVICE = new StubLoggingService();
        static final PluginCacheService CACHE_SERVICE = new StubCacheService();
        static final PluginNotificationService NOTIFICATION_SERVICE = new StubNotificationService();
        static final PluginMetricsService METRICS_SERVICE = new StubMetricsService();
        static final PluginPermissionService PERMISSION_SERVICE = new StubPermissionService();
        static final PluginAsyncTaskExecutor ASYNC_EXECUTOR = new StubAsyncExecutor();
        static final PluginConfigurationValidator CONFIG_VALIDATOR = new StubConfigValidator();
        static final PluginHookService HOOK_SERVICE = new StubHookService();
        static final PluginDataStore DATA_STORE = new StubDataStore();
        static final PluginResourceManager RESOURCE_MANAGER = new StubResourceManager();
        static final PluginDependencyResolver DEPENDENCY_RESOLVER = new StubDependencyResolver();
        static final PluginUpdateService UPDATE_SERVICE = new StubUpdateService();
        static final PluginMonitoringService MONITORING_SERVICE = new StubMonitoringService();
    }

    // Stub implementations (minimal no-op implementations)
    static class StubLoggingService implements PluginLoggingService {
        @Override public void setLogLevel(String pluginId, LogLevel level) {}
        @Override public LogLevel getLogLevel(String pluginId) { return LogLevel.INFO; }
        @Override public void log(String pluginId, LogLevel level, String message) {}
        @Override public void log(String pluginId, LogLevel level, String message, Throwable cause) {}
        @Override public void logf(String pluginId, LogLevel level, String format, Object... args) {}
        @Override public void clearLogs(String pluginId) {}
        @Override public java.util.List<String> getRecentLogs(String pluginId, int maxLines) { return java.util.Collections.emptyList(); }
        @Override public void setConsoleOutput(String pluginId, boolean enabled) {}
        @Override public void setFileOutput(String pluginId, boolean enabled, String filePath) {}
        @Override public java.util.Map<String, Object> getStatistics(String pluginId) { return new java.util.HashMap<>(); }
    }

    static class StubCacheService implements PluginCacheService {
        private final java.util.Map<String, java.util.Map<String, Object>> cache = new java.util.HashMap<>();
        @Override public void put(String pluginId, String key, Object value) { cache.computeIfAbsent(pluginId, k -> new java.util.HashMap<>()).put(key, value); }
        @Override public void put(String pluginId, String key, Object value, long ttlMillis) { put(pluginId, key, value); }
        @Override public Object get(String pluginId, String key) { return cache.getOrDefault(pluginId, new java.util.HashMap<>()).get(key); }
        @Override public <T> T get(String pluginId, String key, Class<T> valueClass) { Object v = get(pluginId, key); return valueClass.isInstance(v) ? (T) v : null; }
        @Override public boolean containsKey(String pluginId, String key) { return cache.getOrDefault(pluginId, new java.util.HashMap<>()).containsKey(key); }
        @Override public void remove(String pluginId, String key) { cache.getOrDefault(pluginId, new java.util.HashMap<>()).remove(key); }
        @Override public void clear(String pluginId) { cache.remove(pluginId); }
        @Override public void clearAll() { cache.clear(); }
        @Override public int size(String pluginId) { return cache.getOrDefault(pluginId, new java.util.HashMap<>()).size(); }
        @Override public void setEvictionPolicy(String pluginId, EvictionPolicy policy) {}
        @Override public void setMaxSize(String pluginId, int maxSize) {}
        @Override public java.util.Map<String, Object> getStatistics(String pluginId) { return new java.util.HashMap<>(); }
        @Override public void resetStatistics(String pluginId) {}
        @Override public java.util.List<String> getKeys(String pluginId) { return new java.util.ArrayList<>(cache.getOrDefault(pluginId, new java.util.HashMap<>()).keySet()); }
    }

    static class StubNotificationService implements PluginNotificationService {
        @Override public void notify(String pluginId, String title, String message) {}
        @Override public String notify(String pluginId, NotificationType type, Priority priority, String title, String message) { return "notif-" + System.nanoTime(); }
        @Override public String notifyWithMetadata(String pluginId, NotificationType type, Priority priority, String title, String message, java.util.Map<String, Object> metadata) { return "notif-" + System.nanoTime(); }
        @Override public String notifyWithActions(String pluginId, NotificationType type, Priority priority, String title, String message, java.util.List<NotificationAction> actions) { return "notif-" + System.nanoTime(); }
        @Override public void dismiss(String notificationId) {}
        @Override public java.util.List<Notification> getActiveNotifications(String pluginId) { return java.util.Collections.emptyList(); }
        @Override public java.util.List<Notification> getRecentNotifications(String pluginId, int maxCount) { return java.util.Collections.emptyList(); }
        @Override public void registerListener(String pluginId, java.util.function.Consumer<Notification> listener) {}
        @Override public void unregisterListener(String pluginId, java.util.function.Consumer<Notification> listener) {}
        @Override public void clearNotifications(String pluginId) {}
        @Override public java.util.Map<String, Object> getStatistics(String pluginId) { return new java.util.HashMap<>(); }
    }

    static class StubMetricsService implements PluginMetricsService {
        private final java.util.Map<String, java.util.Map<String, Long>> counters = new java.util.HashMap<>();
        @Override public void incrementCounter(String pluginId, String metricName) { incrementCounter(pluginId, metricName, 1); }
        @Override public void incrementCounter(String pluginId, String metricName, long amount) { java.util.Map<String, Long> m = counters.computeIfAbsent(pluginId, k -> new java.util.HashMap<>()); m.put(metricName, m.getOrDefault(metricName, 0L) + amount); }
        @Override public void decrementCounter(String pluginId, String metricName) { incrementCounter(pluginId, metricName, -1); }
        @Override public void recordTimer(String pluginId, String metricName, long durationMillis) {}
        @Override public TimerContext startTimer(String pluginId, String metricName) { return new TimerContext() { final long s = System.currentTimeMillis(); public long getElapsedMillis() { return System.currentTimeMillis() - s; } public long stop() { return getElapsedMillis(); } public void close() {} }; }
        @Override public void recordHistogram(String pluginId, String metricName, long value) {}
        @Override public void setGauge(String pluginId, String metricName, long value) {}
        @Override public long getCounterValue(String pluginId, String metricName) { return counters.getOrDefault(pluginId, new java.util.HashMap<>()).getOrDefault(metricName, 0L); }
        @Override public java.util.Map<String, Object> getAllMetrics(String pluginId) { return new java.util.HashMap<>(counters.getOrDefault(pluginId, new java.util.HashMap<>())); }
        @Override public java.util.Map<String, Object> getMetricsByType(String pluginId, MetricType type) { return new java.util.HashMap<>(); }
        @Override public void resetMetrics(String pluginId) { counters.remove(pluginId); }
        @Override public void resetMetric(String pluginId, String metricName) { counters.getOrDefault(pluginId, new java.util.HashMap<>()).remove(metricName); }
        @Override public java.util.Map<String, Object> getMetricStatistics(String pluginId, String metricName) { return new java.util.HashMap<>(); }
        @Override public java.util.Map<String, Object> exportMetrics(String pluginId) { return getAllMetrics(pluginId); }
    }

    static class StubPermissionService implements PluginPermissionService {
        private final java.util.Map<String, java.util.Set<String>> perms = new java.util.HashMap<>();
        @Override public boolean grantPermission(String pluginId, String permissionId) { perms.computeIfAbsent(pluginId, k -> new java.util.HashSet<>()).add(permissionId); return true; }
        @Override public boolean revokePermission(String pluginId, String permissionId) { java.util.Set<String> s = perms.get(pluginId); return s != null && s.remove(permissionId); }
        @Override public boolean hasPermission(String pluginId, String permissionId) { java.util.Set<String> s = perms.get(pluginId); return s != null && s.contains(permissionId); }
        @Override public boolean hasAllPermissions(String pluginId, String... permissionIds) { java.util.Set<String> s = perms.get(pluginId); if (s == null) return false; for (String p : permissionIds) { if (!s.contains(p)) return false; } return true; }
        @Override public boolean hasAnyPermission(String pluginId, String... permissionIds) { java.util.Set<String> s = perms.get(pluginId); if (s == null) return false; for (String p : permissionIds) { if (s.contains(p)) return true; } return false; }
        @Override public java.util.Set<String> getPluginPermissions(String pluginId) { return new java.util.HashSet<>(perms.getOrDefault(pluginId, new java.util.HashSet<>())); }
        @Override public boolean assignRole(String pluginId, String roleId) { return true; }
        @Override public boolean removeRole(String pluginId, String roleId) { return true; }
        @Override public java.util.List<String> getPluginRoles(String pluginId) { return java.util.Collections.emptyList(); }
        @Override public Permission createPermission(String permissionId, String description, String category) { return null; }
        @Override public void registerSystemPermission(Permission permission) {}
        @Override public Permission getPermission(String permissionId) { return null; }
        @Override public java.util.List<Permission> getAllPermissions() { return java.util.Collections.emptyList(); }
        @Override public java.util.List<Permission> getPermissionsByCategory(String category) { return java.util.Collections.emptyList(); }
        @Override public Role createRole(String roleId, String name, String description) { return null; }
        @Override public Role getRole(String roleId) { return null; }
        @Override public java.util.List<Role> getAllRoles() { return java.util.Collections.emptyList(); }
        @Override public java.util.List<java.util.Map<String, Object>> getAuditLog(String pluginId) { return java.util.Collections.emptyList(); }
        @Override public void clearAuditLog(String pluginId) {}
    }

    static class StubAsyncExecutor implements PluginAsyncTaskExecutor {
        private int counter = 0;
        @Override public PluginTask executeTask(String pluginId, Runnable task) { return null; }
        @Override public String executeNamedTask(String pluginId, String taskName, Runnable task) { return "task-" + (++counter); }
        @Override public String executeTaskWithPriority(String pluginId, Runnable task, TaskPriority priority) { return "task-" + (++counter); }
        @Override public <T> java.util.concurrent.Future<T> executeCallable(String pluginId, java.util.concurrent.Callable<T> task) { return null; }
        @Override public String scheduleTask(String pluginId, Runnable task, long delayMillis) { return "task-" + (++counter); }
        @Override public String schedulePeriodicTask(String pluginId, Runnable task, long initialDelayMillis, long periodMillis) { return "task-" + (++counter); }
        @Override public PluginTask getTask(String taskId) { return null; }
        @Override public java.util.List<PluginTask> getPluginTasks(String pluginId) { return java.util.Collections.emptyList(); }
        @Override public java.util.List<PluginTask> getActiveTasks(String pluginId) { return java.util.Collections.emptyList(); }
        @Override public boolean cancelTask(String taskId) { return true; }
        @Override public int cancelAllTasks(String pluginId) { return 0; }
        @Override public void setThreadPoolSize(String pluginId, int poolSize) {}
        @Override public int getThreadPoolSize(String pluginId) { return 1; }
        @Override public java.util.Map<String, Object> getStatistics(String pluginId) { return new java.util.HashMap<>(); }
        @Override public void shutdown(String pluginId) {}
    }

    static class StubConfigValidator implements PluginConfigurationValidator {
        @Override public ValidationResult validateConfiguration(String pluginId, java.util.Map<String, Object> config) { return new ValidationResult() { public boolean isValid() { return true; } public java.util.List<ValidationError> getErrors() { return java.util.Collections.emptyList(); } public java.util.List<String> getWarnings() { return java.util.Collections.emptyList(); } }; }
        @Override public ValidationResult validateValue(String pluginId, String path, Object value) { return validateConfiguration(pluginId, new java.util.HashMap<>()); }
        @Override public java.util.Map<String, Object> getSchema(String pluginId) { return new java.util.HashMap<>(); }
        @Override public void registerSchema(String pluginId, java.util.Map<String, Object> schema) {}
        @Override public void registerCustomValidator(String pluginId, String path, ConfigValidator validator) {}
        @Override public java.util.Map<String, ConfigValidator> getCustomValidators(String pluginId) { return new java.util.HashMap<>(); }
        @Override public java.util.Map<String, Object> getDefaultConfiguration(String pluginId) { return new java.util.HashMap<>(); }
        @Override public java.util.Map<String, Object> mergeWithDefaults(String pluginId, java.util.Map<String, Object> partialConfig) { return new java.util.HashMap<>(partialConfig); }
        @Override public java.util.Map<String, Object> generateSampleConfiguration(String pluginId) { return new java.util.HashMap<>(); }
        @Override public java.util.Map<String, Object> getValidationRules(String pluginId) { return new java.util.HashMap<>(); }
    }

    static class StubHookService implements PluginHookService {
        private int counter = 0;
        @Override public String registerHook(String pluginId, HookType hookType, HookCallback callback) { return "hook-" + (++counter); }
        @Override public String registerHookWithPriority(String pluginId, HookType hookType, int priority, HookCallback callback) { return "hook-" + (++counter); }
        @Override public boolean unregisterHook(String hookId) { return true; }
        @Override public int unregisterHooksByType(String pluginId, HookType hookType) { return 0; }
        @Override public HookContext executeHooks(String pluginId, HookType hookType, java.util.Map<String, Object> hookData) { return new HookContext() { public String getPluginId() { return pluginId; } public HookType getHookType() { return hookType; } public java.util.Map<String, Object> getHookData() { return hookData; } public void setResult(Object result) {} public Object getResult() { return null; } public void cancel() {} public boolean isCancelled() { return false; } }; }
        @Override public Object executeHook(String hookId, java.util.Map<String, Object> hookData) { return null; }
        @Override public java.util.List<String> getPluginHooks(String pluginId) { return java.util.Collections.emptyList(); }
        @Override public java.util.List<String> getHooksByType(String pluginId, HookType hookType) { return java.util.Collections.emptyList(); }
        @Override public java.util.List<java.util.Map<String, Object>> getHookExecutionHistory(String pluginId, int maxEntries) { return java.util.Collections.emptyList(); }
        @Override public void clearHookExecutionHistory(String pluginId) {}
    }

    static class StubDataStore implements PluginDataStore {
        private final java.util.Map<String, java.util.Map<String, Object>> store = new java.util.HashMap<>();
        @Override public void store(String pluginId, String key, Object data) { store.computeIfAbsent(pluginId, k -> new java.util.HashMap<>()).put(key, data); }
        @Override public void store(String pluginId, String key, Object data, SerializationFormat format) { store(pluginId, key, data); }
        @Override public Object retrieve(String pluginId, String key) { return store.getOrDefault(pluginId, new java.util.HashMap<>()).get(key); }
        @Override public <T> T retrieve(String pluginId, String key, Class<T> dataClass) { Object v = retrieve(pluginId, key); return dataClass.isInstance(v) ? (T) v : null; }
        @Override public boolean exists(String pluginId, String key) { return store.getOrDefault(pluginId, new java.util.HashMap<>()).containsKey(key); }
        @Override public boolean delete(String pluginId, String key) { java.util.Map<String, Object> m = store.get(pluginId); return m != null && m.remove(key) != null; }
        @Override public void clear(String pluginId) { store.remove(pluginId); }
        @Override public java.util.List<String> getKeys(String pluginId) { return new java.util.ArrayList<>(store.getOrDefault(pluginId, new java.util.HashMap<>()).keySet()); }
        @Override public long getSize(String pluginId, String key) { return 0; }
        @Override public long getTotalSize(String pluginId) { return 0; }
        @Override public java.util.Map<String, Object> exportAllData(String pluginId) { return new java.util.HashMap<>(store.getOrDefault(pluginId, new java.util.HashMap<>())); }
        @Override public void importAllData(String pluginId, java.util.Map<String, Object> data) { store.put(pluginId, new java.util.HashMap<>(data)); }
        @Override public String backup(String pluginId) { return "backup-" + System.currentTimeMillis(); }
        @Override public boolean restore(String pluginId, String backupId) { return true; }
        @Override public java.util.List<java.util.Map<String, Object>> getBackups(String pluginId) { return java.util.Collections.emptyList(); }
        @Override public boolean deleteBackup(String pluginId, String backupId) { return true; }
        @Override public java.util.Map<String, Object> getStatistics(String pluginId) { return new java.util.HashMap<>(); }
    }

    static class StubResourceManager implements PluginResourceManager {
        @Override public boolean registerResource(String pluginId, String resourceId, String name, Object resource) { return true; }
        @Override public boolean registerResourceWithMetadata(String pluginId, String resourceId, String name, String description, String resourceType, Object resource, java.util.Map<String, Object> metadata) { return true; }
        @Override public boolean unregisterResource(String pluginId, String resourceId) { return true; }
        @Override public Resource getResource(String resourceId) { return null; }
        @Override public <T> T getResourceValue(String resourceId, Class<T> valueClass) { return null; }
        @Override public java.util.List<Resource> getPluginResources(String pluginId) { return java.util.Collections.emptyList(); }
        @Override public java.util.List<Resource> getAllResources() { return java.util.Collections.emptyList(); }
        @Override public java.util.List<Resource> getResourcesByType(String resourceType) { return java.util.Collections.emptyList(); }
        @Override public boolean grantResourceAccess(String pluginId, String resourceId) { return true; }
        @Override public boolean revokeResourceAccess(String pluginId, String resourceId) { return true; }
        @Override public boolean hasResourceAccess(String pluginId, String resourceId) { return true; }
        @Override public java.util.List<Resource> getAccessibleResources(String pluginId) { return java.util.Collections.emptyList(); }
        @Override public boolean updateResource(String pluginId, String resourceId, Object newValue) { return true; }
        @Override public java.util.List<java.util.Map<String, Object>> getAccessAuditLog(String resourceId) { return java.util.Collections.emptyList(); }
        @Override public java.util.Map<String, Object> getStatistics() { return new java.util.HashMap<>(); }
    }

    static class StubDependencyResolver implements PluginDependencyResolver {
        @Override public PluginDependency addDependency(String pluginId, String dependencyId, String requiredVersion, DependencyLevel level) { return null; }
        @Override public boolean removeDependency(String pluginId, String dependencyId) { return true; }
        @Override public java.util.List<PluginDependency> getDependencies(String pluginId) { return java.util.Collections.emptyList(); }
        @Override public java.util.List<PluginDependency> getRequiredDependencies(String pluginId) { return java.util.Collections.emptyList(); }
        @Override public java.util.List<PluginDependency> getOptionalDependencies(String pluginId) { return java.util.Collections.emptyList(); }
        @Override public java.util.List<PluginDependency> getConflictingDependencies(String pluginId) { return java.util.Collections.emptyList(); }
        @Override public boolean areRequiredDependenciesResolved(String pluginId) { return true; }
        @Override public boolean isDependencyResolved(String pluginId, String dependencyId) { return true; }
        @Override public java.util.List<String> getResolutionPath(String pluginId) { return java.util.Collections.emptyList(); }
        @Override public java.util.List<java.util.List<String>> detectCircularDependencies(String pluginId) { return java.util.Collections.emptyList(); }
        @Override public java.util.List<String> getDependents(String pluginId) { return java.util.Collections.emptyList(); }
        @Override public java.util.Map<String, Object> validateDependencies(String pluginId) { return new java.util.HashMap<>(); }
        @Override public java.util.Map<String, Object> getDependencyGraph(String pluginId) { return new java.util.HashMap<>(); }
    }

    static class StubUpdateService implements PluginUpdateService {
        @Override public PluginVersion checkForUpdates(String pluginId) { return null; }
        @Override public PluginVersion checkForUpdates(String pluginId, UpdateChannel channel) { return null; }
        @Override public UpdateStatus getUpdateStatus(String pluginId) { return UpdateStatus.CHECKING; }
        @Override public boolean installUpdate(String pluginId, String version) { return true; }
        @Override public boolean cancelUpdate(String pluginId) { return true; }
        @Override public int getUpdateProgress(String pluginId) { return 0; }
        @Override public boolean rollbackVersion(String pluginId, String version) { return true; }
        @Override public java.util.List<PluginVersion> getVersionHistory(String pluginId) { return java.util.Collections.emptyList(); }
        @Override public void setUpdateChannel(String pluginId, UpdateChannel channel) {}
        @Override public UpdateChannel getUpdateChannel(String pluginId) { return UpdateChannel.STABLE; }
        @Override public void setAutoUpdate(String pluginId, boolean enabled) {}
        @Override public boolean isAutoUpdateEnabled(String pluginId) { return false; }
        @Override public java.util.Map<String, Object> getUpdateStatistics() { return new java.util.HashMap<>(); }
    }

    static class StubMonitoringService implements PluginMonitoringService {
        @Override public HealthReport getHealthReport(String pluginId) { return new HealthReport() { public String getPluginId() { return pluginId; } public HealthStatus getStatus() { return HealthStatus.HEALTHY; } public double getCpuUsage() { return 0.0; } public long getMemoryUsage() { return 0; } public int getThreadCount() { return 0; } public long getUptime() { return 0; } public int getErrorCount() { return 0; } public int getWarningCount() { return 0; } public java.util.Map<String, Object> getDetails() { return new java.util.HashMap<>(); } }; }
        @Override public HealthStatus getHealthStatus(String pluginId) { return HealthStatus.HEALTHY; }
        @Override public HealthStatus getGlobalHealthStatus() { return HealthStatus.HEALTHY; }
        @Override public double getCpuUsage(String pluginId) { return 0.0; }
        @Override public long getMemoryUsage(String pluginId) { return 0; }
        @Override public int getThreadCount(String pluginId) { return 0; }
        @Override public long getUptime(String pluginId) { return 0; }
        @Override public int getErrorCount(String pluginId) { return 0; }
        @Override public int getWarningCount(String pluginId) { return 0; }
        @Override public Alert createAlert(String pluginId, AlertSeverity severity, String title, String message) { return null; }
        @Override public java.util.List<Alert> getActiveAlerts() { return java.util.Collections.emptyList(); }
        @Override public java.util.List<Alert> getPluginAlerts(String pluginId) { return java.util.Collections.emptyList(); }
        @Override public void resolveAlert(String alertId) {}
        @Override public java.util.List<Alert> getAlertHistory(int maxCount) { return java.util.Collections.emptyList(); }
        @Override public void clearAlertHistory() {}
        @Override public java.util.List<HealthReport> getAllHealthReports() { return java.util.Collections.emptyList(); }
        @Override public java.util.Map<String, Object> getGlobalStatistics() { return new java.util.HashMap<>(); }
        @Override public void registerHealthMonitorListener(HealthMonitorListener listener) {}
        @Override public void unregisterHealthMonitorListener(HealthMonitorListener listener) {}
    }
}

