package com.protonmail.landrevillejf.ide.plugin;

import com.protonmail.landrevillejf.ide.plugin.service.*;

/**
 * Extended plugin context providing comprehensive access to all plugin services.
 * <p>
 * This interface extends the basic {@link PluginContext} with convenience methods
 * for retrieving each of the 13 standard plugin services directly, without requiring
 * a {@link PluginServiceLocator} lookup.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 * @see PluginContext
 * @see DefaultExtendedPluginContext
 */
public interface ExtendedPluginContext extends PluginContext {

    /**
     * Gets the service locator for accessing all plugin services.
     *
     * @return the service locator
     */
    PluginServiceLocator getServiceLocator();

    /**
     * Gets the logging service.
     *
     * @return the logging service
     */
    default PluginLoggingService getLoggingService() {
        return getServiceLocator().getLoggingService();
    }

    /**
     * Gets the cache service.
     *
     * @return the cache service
     */
    default PluginCacheService getCacheService() {
        return getServiceLocator().getCacheService();
    }

    /**
     * Gets the notification service.
     *
     * @return the notification service
     */
    default PluginNotificationService getNotificationService() {
        return getServiceLocator().getNotificationService();
    }

    /**
     * Gets the metrics service.
     *
     * @return the metrics service
     */
    default PluginMetricsService getMetricsService() {
        return getServiceLocator().getMetricsService();
    }

    /**
     * Gets the permission service.
     *
     * @return the permission service
     */
    default PluginPermissionService getPermissionService() {
        return getServiceLocator().getPermissionService();
    }

    /**
     * Gets the async task executor.
     *
     * @return the async task executor
     */
    default PluginAsyncTaskExecutor getAsyncTaskExecutor() {
        return getServiceLocator().getAsyncTaskExecutor();
    }

    /**
     * Gets the configuration validator.
     *
     * @return the configuration validator
     */
    default PluginConfigurationValidator getConfigurationValidator() {
        return getServiceLocator().getConfigurationValidator();
    }

    /**
     * Gets the hook service.
     *
     * @return the hook service
     */
    default PluginHookService getHookService() {
        return getServiceLocator().getHookService();
    }

    /**
     * Gets the data store.
     *
     * @return the data store
     */
    default PluginDataStore getDataStore() {
        return getServiceLocator().getDataStore();
    }

    /**
     * Gets the resource manager.
     *
     * @return the resource manager
     */
    default PluginResourceManager getResourceManager() {
        return getServiceLocator().getResourceManager();
    }

    /**
     * Gets the dependency resolver.
     *
     * @return the dependency resolver
     */
    default PluginDependencyResolver getDependencyResolver() {
        return getServiceLocator().getDependencyResolver();
    }

    /**
     * Gets the update service.
     *
     * @return the update service
     */
    default PluginUpdateService getUpdateService() {
        return getServiceLocator().getUpdateService();
    }

    /**
     * Gets the monitoring service.
     *
     * @return the monitoring service
     */
    default PluginMonitoringService getMonitoringService() {
        return getServiceLocator().getMonitoringService();
    }

    /**
     * Gets a custom service by interface.
     *
     * @param serviceInterface the service interface
     * @return the service, or null if not found
     */
    default <T> T getService(Class<T> serviceInterface) {
        return getServiceLocator().getService(serviceInterface);
    }

    /**
     * Registers a custom service.
     *
     * @param serviceInterface the service interface
     * @param implementation the service implementation
     */
    default <T> void registerService(Class<T> serviceInterface, T implementation) {
        getServiceLocator().registerService(serviceInterface, implementation);
    }

    /**
     * Sets the plugin manager for this context.
     * This method is called during initialization to provide access to plugin management.
     *
     * @param pluginManager the plugin manager instance
     */
    void setPluginManager(PluginManager pluginManager);
}

