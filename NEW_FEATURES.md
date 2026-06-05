# Plugin API Extensions

This document describes all the new features and services added to the Plugin API to provide comprehensive plugin development capabilities.

## Overview

The Plugin API has been significantly extended with new services that provide:

- **Advanced Logging** - Centralized, configurable logging with multiple output targets
- **Caching** - Configurable caching with TTL and eviction policies
- **Notifications** - Advanced notification system with priorities and actions
- **Metrics** - Comprehensive metrics collection and monitoring
- **Permissions** - Role-based access control for plugins
- **Async Execution** - Task scheduling and execution with thread pooling
- **Configuration Validation** - Schema-based configuration validation
- **Hooks** - Plugin lifecycle and event hooks system
- **Data Persistence** - Persistent storage with backup/restore capabilities
- **Resource Management** - Cross-plugin resource sharing
- **Dependency Resolution** - Plugin dependency management and resolution
- **Lifecycle Monitoring** - Plugin lifecycle event listening
- **Updates** - Plugin update management and versioning
- **Global Monitoring** - System-wide plugin health and monitoring

## Services

### 1. PluginLoggingService

Centralized logging service with configurable log levels and multiple output targets.

**Key Features:**
- Configurable log levels (TRACE, DEBUG, INFO, WARN, ERROR, FATAL)
- Multiple output targets (console, file)
- Log history and statistics
- Per-plugin log level configuration

**Usage Example:**
```java
PluginLoggingService logger = context.getLoggingService();
logger.setLogLevel("my-plugin", LogLevel.DEBUG);
logger.log("my-plugin", LogLevel.INFO, "Plugin initialized successfully");
```

### 2. PluginCacheService

In-memory caching service with TTL and eviction policies.

**Key Features:**
- TTL-based cache expiration
- Eviction policies (LRU, FIFO, LFU)
- Configurable max size per plugin
- Cache statistics (hits, misses, evictions)
- Type-safe retrievals

**Usage Example:**
```java
PluginCacheService cache = context.getCacheService();
cache.put("my-plugin", "user-prefs", userPreferences, 3600000);
UserPrefs cached = cache.get("my-plugin", "user-prefs", UserPrefs.class);
```

### 3. PluginNotificationService

Advanced notification system with priorities, actions, and multiple notification types.

**Key Features:**
- Multiple notification types (INFO, SUCCESS, WARNING, ERROR, DEBUG, CUSTOM)
- Priority levels (LOW, NORMAL, HIGH, CRITICAL)
- Notification actions with callbacks
- Custom metadata support
- Listener registration

**Usage Example:**
```java
PluginNotificationService notif = context.getNotificationService();
String notifId = notif.notify("my-plugin", NotificationType.SUCCESS, 
                              Priority.HIGH, "Success", "Operation completed!");
```

### 4. PluginMetricsService

Comprehensive metrics collection with counters, timers, histograms, and gauges.

**Key Features:**
- Counter metrics
- Timer measurements with context
- Histogram value distributions
- Gauge point-in-time measurements
- Per-metric statistics
- Metrics export

**Usage Example:**
```java
PluginMetricsService metrics = context.getMetricsService();
try (TimerContext timer = metrics.startTimer("my-plugin", "operation.duration")) {
    // perform operation
}
metrics.incrementCounter("my-plugin", "operations.total");
```

### 5. PluginPermissionService

Role-based access control for plugins with granular permission management.

**Key Features:**
- Permission and role definitions
- Permission assignment and revocation
- Role-based access control
- Audit logging
- Permission categories

**Usage Example:**
```java
PluginPermissionService perms = context.getPermissionService();
boolean hasFileAccess = perms.hasPermission("my-plugin", "filesystem.read");
perms.grantPermission("my-plugin", "network.access");
```

### 6. PluginAsyncTaskExecutor

Asynchronous task execution with priority, scheduling, and monitoring.

**Key Features:**
- Async task execution with priorities
- Task scheduling with delays
- Periodic task scheduling
- Thread pool management
- Task state monitoring
- Callable support for results

**Usage Example:**
```java
PluginAsyncTaskExecutor executor = context.getAsyncTaskExecutor();
String taskId = executor.executeNamedTask("my-plugin", "background-job", () -> {
    // long-running operation
});
```

### 7. PluginConfigurationValidator

Configuration validation with schema support and custom validators.

**Key Features:**
- Schema-based validation
- Custom validators
- Default configuration merge
- Sample configuration generation
- Validation error reporting

**Usage Example:**
```java
PluginConfigurationValidator validator = context.getConfigurationValidator();
ValidationResult result = validator.validateConfiguration("my-plugin", config);
if (!result.isValid()) {
    for (ValidationError error : result.getErrors()) {
        logger.log("my-plugin", LogLevel.ERROR, error.getMessage());
    }
}
```

### 8. PluginHookService

Plugin lifecycle and event hooks system.

**Key Features:**
- Predefined hooks (PRE/POST_INIT, PRE/POST_ENABLE, etc.)
- Custom hooks
- Priority-based execution
- Hook execution context
- Execution history tracking

**Usage Example:**
```java
PluginHookService hooks = context.getHookService();
hooks.registerHook("my-plugin", HookType.POST_INIT, (context) -> {
    // initialization complete
});
```

### 9. PluginDataStore

Persistent data storage with backup/restore capabilities.

**Key Features:**
- Key-value storage
- Multiple serialization formats (JSON, XML, BINARY, PROPERTIES)
- Backup and restore functionality
- Data export/import
- Storage statistics

**Usage Example:**
```java
PluginDataStore store = context.getDataStore();
store.store("my-plugin", "settings", mySettings, SerializationFormat.JSON);
MySettings loaded = store.retrieve("my-plugin", "settings", MySettings.class);
```

### 10. PluginResourceManager

Cross-plugin resource sharing with access control.

**Key Features:**
- Resource registration and sharing
- Resource access control
- Resource metadata
- Access audit logging
- Resource filtering by type

**Usage Example:**
```java
PluginResourceManager resources = context.getResourceManager();
resources.registerResource("provider-plugin", "resource-id", "My Resource", myObject);
Object resource = resources.getResource("resource-id");
```

### 11. PluginDependencyResolver

Plugin dependency management and resolution.

**Key Features:**
- Dependency registration
- Version requirement management
- Circular dependency detection
- Dependency resolution path
- Compatibility validation

**Usage Example:**
```java
PluginDependencyResolver deps = context.getDependencyResolver();
boolean resolved = deps.areRequiredDependenciesResolved("my-plugin");
List<String> path = deps.getResolutionPath("my-plugin");
```

### 12. PluginLifecycleListener

Plugin lifecycle event monitoring interface.

**Key Features:**
- Load/unload events
- Initialize/shutdown events
- Enable/disable events
- Error handling callbacks
- State change notifications

**Usage Example:**
```java
PluginLifecycleListener listener = new PluginLifecycleListener() {
    @Override
    public void onLoaded(String pluginId) {
        System.out.println("Plugin loaded: " + pluginId);
    }
};
```

### 13. PluginUpdateService

Plugin update management and versioning.

**Key Features:**
- Update checking
- Version management
- Beta/development channels
- Auto-update configuration
- Update progress tracking
- Rollback support

**Usage Example:**
```java
PluginUpdateService updates = context.getUpdateService();
PluginVersion latest = updates.checkForUpdates("my-plugin");
if (latest != null) {
    updates.installUpdate("my-plugin", latest.getVersion());
}
```

### 14. PluginMonitoringService

Global system-wide plugin health and performance monitoring.

**Key Features:**
- Plugin health reports
- CPU and memory usage monitoring
- Thread monitoring
- Alert system
- Alert history tracking
- Health change listeners

**Usage Example:**
```java
PluginMonitoringService monitoring = context.getMonitoringService();
HealthReport health = monitoring.getHealthReport("my-plugin");
double cpuUsage = monitoring.getCpuUsage("my-plugin");
```

## Access Pattern

All services are accessible through the `PluginServiceLocator` interface:

```java
// Get through ExtendedPluginContext
if (context instanceof ExtendedPluginContext) {
    ExtendedPluginContext extContext = (ExtendedPluginContext) context;
    PluginLoggingService logger = extContext.getLoggingService();
    PluginCacheService cache = extContext.getCacheService();
    // etc.
}

// Or directly through the locator
PluginServiceLocator locator = context.getService(PluginServiceLocator.class);
PluginLoggingService logger = locator.getLoggingService();
```

## Best Practices

1. **Use Logging Service** - Instead of System.out or direct logger calls, use PluginLoggingService
2. **Cache Appropriately** - Use cache for frequently accessed data with reasonable TTLs
3. **Handle Permissions** - Check permissions before sensitive operations
4. **Register Hooks** - Use hooks for proper lifecycle management
5. **Monitor Performance** - Collect metrics for important operations
6. **Validate Configuration** - Always validate plugin configuration
7. **Use Async Tasks** - Execute long-running operations asynchronously
8. **Persist Data** - Use DataStore for important plugin data
9. **Share Resources** - Use ResourceManager for inter-plugin communication
10. **Handle Dependencies** - Properly declare and manage plugin dependencies

## Migration Guide

For existing plugins:

1. Update your `PluginContext` imports to use `ExtendedPluginContext` where possible
2. Migrate custom logging to `PluginLoggingService`
3. Migrate custom caching to `PluginCacheService`
4. Add hooks instead of duplicate lifecycle methods
5. Collect metrics for important operations
6. Use the new data persistence instead of custom file handling

