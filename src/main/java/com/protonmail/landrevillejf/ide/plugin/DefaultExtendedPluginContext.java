package com.protonmail.landrevillejf.ide.plugin;

import com.protonmail.landrevillejf.ide.plugin.service.*;
import com.protonmail.landrevillejf.swingide.core.bus.EventBus;
import com.protonmail.landrevillejf.swingide.core.registry.ServiceRegistry;
import com.protonmail.landrevillejf.ide.plugin.service.*;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Extended implementation of DefaultPluginContext that provides access to all plugin services.
 * This class integrates all the new plugin services into the core plugin context.
 */
public class DefaultExtendedPluginContext extends DefaultPluginContext implements ExtendedPluginContext {

    private final PluginServiceLocator serviceLocator;
    private final ConcurrentHashMap<Class<?>, Object> serviceCache;

    /**
     * Creates a new extended plugin context with service locator.
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
     * Creates a new extended plugin context with service locator and plugin.
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

    @Override
    public PluginServiceLocator getServiceLocator() {
        return serviceLocator;
    }

    @Override
    public PluginLoggingService getLoggingService() {
        return cacheService(PluginLoggingService.class, () -> serviceLocator.getLoggingService());
    }

    @Override
    public PluginCacheService getCacheService() {
        return cacheService(PluginCacheService.class, () -> serviceLocator.getCacheService());
    }

    @Override
    public PluginNotificationService getNotificationService() {
        return cacheService(PluginNotificationService.class, () -> serviceLocator.getNotificationService());
    }

    @Override
    public PluginMetricsService getMetricsService() {
        return cacheService(PluginMetricsService.class, () -> serviceLocator.getMetricsService());
    }

    @Override
    public PluginPermissionService getPermissionService() {
        return cacheService(PluginPermissionService.class, () -> serviceLocator.getPermissionService());
    }

    @Override
    public PluginAsyncTaskExecutor getAsyncTaskExecutor() {
        return cacheService(PluginAsyncTaskExecutor.class, () -> serviceLocator.getAsyncTaskExecutor());
    }

    @Override
    public PluginConfigurationValidator getConfigurationValidator() {
        return cacheService(PluginConfigurationValidator.class, () -> serviceLocator.getConfigurationValidator());
    }

    @Override
    public PluginHookService getHookService() {
        return cacheService(PluginHookService.class, () -> serviceLocator.getHookService());
    }

    @Override
    public PluginDataStore getDataStore() {
        return cacheService(PluginDataStore.class, () -> serviceLocator.getDataStore());
    }

    @Override
    public PluginResourceManager getResourceManager() {
        return cacheService(PluginResourceManager.class, () -> serviceLocator.getResourceManager());
    }

    @Override
    public PluginDependencyResolver getDependencyResolver() {
        return cacheService(PluginDependencyResolver.class, () -> serviceLocator.getDependencyResolver());
    }

    @Override
    public PluginUpdateService getUpdateService() {
        return cacheService(PluginUpdateService.class, () -> serviceLocator.getUpdateService());
    }

    @Override
    public PluginMonitoringService getMonitoringService() {
        return cacheService(PluginMonitoringService.class, () -> serviceLocator.getMonitoringService());
    }

    @Override
    public <T> T getService(Class<T> serviceInterface) {
        return cacheService(serviceInterface, () -> serviceLocator.getService(serviceInterface));
    }

    @Override
    public <T> void registerService(Class<T> serviceInterface, T implementation) {
        serviceLocator.registerService(serviceInterface, implementation);
        serviceCache.put(serviceInterface, implementation);
    }

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

