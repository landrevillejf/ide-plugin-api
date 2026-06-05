package com.protonmail.landrevillejf.ide.plugin.service;

import java.util.Map;

/**
 * Configuration builder for plugin services.
 * Provides fluent API to configure and create service instances.
 */
public interface PluginServiceConfiguration {

    /**
     * Builder for creating a complete service configuration.
     */
    interface Builder {
        /**
         * Sets the logging service implementation.
         *
         * @param service the logging service
         * @return this builder
         */
        Builder withLoggingService(PluginLoggingService service);

        /**
         * Sets the cache service implementation.
         *
         * @param service the cache service
         * @return this builder
         */
        Builder withCacheService(PluginCacheService service);

        /**
         * Sets the notification service implementation.
         *
         * @param service the notification service
         * @return this builder
         */
        Builder withNotificationService(PluginNotificationService service);

        /**
         * Sets the metrics service implementation.
         *
         * @param service the metrics service
         * @return this builder
         */
        Builder withMetricsService(PluginMetricsService service);

        /**
         * Sets the permission service implementation.
         *
         * @param service the permission service
         * @return this builder
         */
        Builder withPermissionService(PluginPermissionService service);

        /**
         * Sets the async executor implementation.
         *
         * @param executor the async executor
         * @return this builder
         */
        Builder withAsyncExecutor(PluginAsyncTaskExecutor executor);

        /**
         * Sets the configuration validator implementation.
         *
         * @param validator the configuration validator
         * @return this builder
         */
        Builder withConfigurationValidator(PluginConfigurationValidator validator);

        /**
         * Sets the hook service implementation.
         *
         * @param service the hook service
         * @return this builder
         */
        Builder withHookService(PluginHookService service);

        /**
         * Sets the data store implementation.
         *
         * @param store the data store
         * @return this builder
         */
        Builder withDataStore(PluginDataStore store);

        /**
         * Sets the resource manager implementation.
         *
         * @param manager the resource manager
         * @return this builder
         */
        Builder withResourceManager(PluginResourceManager manager);

        /**
         * Sets the dependency resolver implementation.
         *
         * @param resolver the dependency resolver
         * @return this builder
         */
        Builder withDependencyResolver(PluginDependencyResolver resolver);

        /**
         * Sets the update service implementation.
         *
         * @param service the update service
         * @return this builder
         */
        Builder withUpdateService(PluginUpdateService service);

        /**
         * Sets the monitoring service implementation.
         *
         * @param service the monitoring service
         * @return this builder
         */
        Builder withMonitoringService(PluginMonitoringService service);

        /**
         * Registers a custom service.
         *
         * @param serviceInterface the service interface
         * @param implementation the service implementation
         * @return this builder
         */
        <T> Builder withService(Class<T> serviceInterface, T implementation);

        /**
         * Builds the service locator.
         *
         * @return a configured PluginServiceLocator
         */
        PluginServiceLocator build();
    }

    /**
     * Gets a service configuration builder.
     *
     * @return a new builder instance
     */
    static Builder builder() {
        // This will be implemented by the actual service framework
        throw new UnsupportedOperationException("Service locator implementation not available");
    }

    /**
     * Gets a service configuration from properties.
     *
     * @param properties the properties map
     * @return a PluginServiceLocator configured from properties
     */
    static PluginServiceLocator fromProperties(Map<String, String> properties) {
        // This will be implemented by the actual service framework
        throw new UnsupportedOperationException("Service locator implementation not available");
    }

    /**
     * Gets the default/global service locator.
     *
     * @return the global PluginServiceLocator instance
     */
    static PluginServiceLocator getGlobal() {
        // This will be implemented by the actual service framework
        throw new UnsupportedOperationException("Service locator implementation not available");
    }
}

