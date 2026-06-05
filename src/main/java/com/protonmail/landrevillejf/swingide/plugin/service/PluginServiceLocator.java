package com.protonmail.landrevillejf.swingide.plugin.service;

/**
 * Service locator for accessing all plugin services.
 * This class provides centralized access to all plugin-related services.
 */
public interface PluginServiceLocator {

    /**
     * Gets the logging service.
     *
     * @return the logging service
     */
    PluginLoggingService getLoggingService();

    /**
     * Gets the cache service.
     *
     * @return the cache service
     */
    PluginCacheService getCacheService();

    /**
     * Gets the notification service.
     *
     * @return the notification service
     */
    PluginNotificationService getNotificationService();

    /**
     * Gets the metrics service.
     *
     * @return the metrics service
     */
    PluginMetricsService getMetricsService();

    /**
     * Gets the permission service.
     *
     * @return the permission service
     */
    PluginPermissionService getPermissionService();

    /**
     * Gets the async task executor.
     *
     * @return the async task executor
     */
    PluginAsyncTaskExecutor getAsyncTaskExecutor();

    /**
     * Gets the configuration validator.
     *
     * @return the configuration validator
     */
    PluginConfigurationValidator getConfigurationValidator();

    /**
     * Gets the hook service.
     *
     * @return the hook service
     */
    PluginHookService getHookService();

    /**
     * Gets the data store.
     *
     * @return the data store
     */
    PluginDataStore getDataStore();

    /**
     * Gets the resource manager.
     *
     * @return the resource manager
     */
    PluginResourceManager getResourceManager();

    /**
     * Gets the dependency resolver.
     *
     * @return the dependency resolver
     */
    PluginDependencyResolver getDependencyResolver();

    /**
     * Gets the update service.
     *
     * @return the update service
     */
    PluginUpdateService getUpdateService();

    /**
     * Gets the monitoring service.
     *
     * @return the monitoring service
     */
    PluginMonitoringService getMonitoringService();

    /**
     * Gets a custom service by interface.
     *
     * @param serviceInterface the service interface
     * @return the service, or null if not found
     */
    <T> T getService(Class<T> serviceInterface);

    /**
     * Registers a custom service.
     *
     * @param serviceInterface the service interface
     * @param implementation the service implementation
     */
    <T> void registerService(Class<T> serviceInterface, T implementation);
}

