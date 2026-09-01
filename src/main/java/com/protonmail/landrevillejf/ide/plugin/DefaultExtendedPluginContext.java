package com.protonmail.landrevillejf.ide.plugin;

import com.protonmail.landrevillejf.ide.plugin.service.*;
import com.protonmail.landrevillejf.swingide.core.bus.EventBus;
import com.protonmail.landrevillejf.swingide.core.registry.ServiceRegistry;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Extended implementation of {@link DefaultPluginContext} that provides access to all plugin services.
 * <p>
 * This class integrates all plugin services into the core plugin context by delegating
 * to a {@link PluginServiceLocator}. Service instances are cached in a {@link ConcurrentHashMap}
 * to avoid repeated lookups.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 * @see ExtendedPluginContext
 * @see DefaultPluginContext
 */
public class DefaultExtendedPluginContext extends DefaultPluginContext implements ExtendedPluginContext {

    private final PluginServiceLocator serviceLocator;
    private final ConcurrentHashMap<Class<?>, Object> serviceCache;

    /**
     * Creates a new extended plugin context with a service locator.
     *
     * @param serviceRegistry      the application service registry
     * @param pluginEventBus       the plugin-level event bus
     * @param applicationEventBus  the application-level event bus
     * @param pluginManager        the plugin manager
     * @param pluginDataDirectory  the plugin data directory
     * @param pluginId             the unique plugin identifier
     * @param serviceLocator       the service locator for accessing plugin services
     */
    public DefaultExtendedPluginContext(ServiceRegistry serviceRegistry,
                                       PluginEventBus pluginEventBus,
                                       EventBus applicationEventBus,
                                       PluginManager pluginManager,
                                       File pluginDataDirectory,
                                       String pluginId,
                                       PluginServiceLocator serviceLocator) {
        super(serviceRegistry, pluginEventBus, applicationEventBus, pluginManager, pluginDataDirectory, pluginId);
        this.serviceLocator = serviceLocator;
        this.serviceCache = new ConcurrentHashMap<>();
    }

    /**
     * Creates a new extended plugin context with a service locator and plugin instance.
     *
     * @param serviceRegistry      the application service registry
     * @param pluginEventBus       the plugin-level event bus
     * @param applicationEventBus  the application-level event bus
     * @param pluginManager        the plugin manager
     * @param pluginDataDirectory  the plugin data directory
     * @param pluginId             the unique plugin identifier
     * @param plugin               the plugin instance
     * @param serviceLocator       the service locator for accessing plugin services
     */
    public DefaultExtendedPluginContext(ServiceRegistry serviceRegistry,
                                       PluginEventBus pluginEventBus,
                                       EventBus applicationEventBus,
                                       PluginManager pluginManager,
                                       File pluginDataDirectory,
                                       String pluginId,
                                       Plugin plugin,
                                       PluginServiceLocator serviceLocator) {
        super(serviceRegistry, pluginEventBus, applicationEventBus, pluginManager, pluginDataDirectory, pluginId, plugin);
        this.serviceLocator = serviceLocator;
        this.serviceCache = new ConcurrentHashMap<>();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public PluginServiceLocator getServiceLocator() {
        return serviceLocator;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public PluginLoggingService getLoggingService() {
        return cacheService(PluginLoggingService.class, () -> serviceLocator.getLoggingService());
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public PluginCacheService getCacheService() {
        return cacheService(PluginCacheService.class, () -> serviceLocator.getCacheService());
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public PluginNotificationService getNotificationService() {
        return cacheService(PluginNotificationService.class, () -> serviceLocator.getNotificationService());
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public PluginMetricsService getMetricsService() {
        return cacheService(PluginMetricsService.class, () -> serviceLocator.getMetricsService());
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public PluginPermissionService getPermissionService() {
        return cacheService(PluginPermissionService.class, () -> serviceLocator.getPermissionService());
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public PluginAsyncTaskExecutor getAsyncTaskExecutor() {
        return cacheService(PluginAsyncTaskExecutor.class, () -> serviceLocator.getAsyncTaskExecutor());
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public PluginConfigurationValidator getConfigurationValidator() {
        return cacheService(PluginConfigurationValidator.class, () -> serviceLocator.getConfigurationValidator());
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public PluginHookService getHookService() {
        return cacheService(PluginHookService.class, () -> serviceLocator.getHookService());
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public PluginDataStore getDataStore() {
        return cacheService(PluginDataStore.class, () -> serviceLocator.getDataStore());
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public PluginResourceManager getResourceManager() {
        return cacheService(PluginResourceManager.class, () -> serviceLocator.getResourceManager());
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public PluginDependencyResolver getDependencyResolver() {
        return cacheService(PluginDependencyResolver.class, () -> serviceLocator.getDependencyResolver());
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public PluginUpdateService getUpdateService() {
        return cacheService(PluginUpdateService.class, () -> serviceLocator.getUpdateService());
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public PluginMonitoringService getMonitoringService() {
        return cacheService(PluginMonitoringService.class, () -> serviceLocator.getMonitoringService());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> T getService(Class<T> serviceInterface) {
        return cacheService(serviceInterface, () -> serviceLocator.getService(serviceInterface));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> void registerService(Class<T> serviceInterface, T implementation) {
        serviceLocator.registerService(serviceInterface, implementation);
        serviceCache.put(serviceInterface, implementation);
    }

    /**
     * Retrieves a service from the cache, loading it via the supplied factory if absent.
     *
     * @param <T>        the service type
     * @param serviceClass the class of the service
     * @param supplier    factory that produces the service instance on demand
     * @return the cached or newly created service instance
     * @throws IllegalStateException if the supplier returns {@code null}
     */
    @SuppressWarnings("unchecked")
    private <T> T cacheService(Class<T> serviceClass, java.util.function.Supplier<T> supplier) {
        // Use computeIfAbsent for better thread safety
        return (T) serviceCache.computeIfAbsent(serviceClass, key -> {
            T service = supplier.get();
            if (service == null) {
                throw new IllegalStateException("Service " + serviceClass.getName() + " is not available");
            }
            return service;
        });
    }
}

