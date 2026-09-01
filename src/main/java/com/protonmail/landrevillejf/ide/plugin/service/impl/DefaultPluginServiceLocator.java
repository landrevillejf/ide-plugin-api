package com.protonmail.landrevillejf.ide.plugin.service.impl;

import com.protonmail.landrevillejf.ide.plugin.service.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of {@link com.protonmail.landrevillejf.ide.plugin.service.PluginServiceLocator}
 * providing all 13 built-in plugin services.
 * <p>
 * Instantiates default implementations for all services and provides
 * a generic service lookup mechanism with support for custom service registration.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 * @see com.protonmail.landrevillejf.ide.plugin.service.PluginServiceLocator
 */
public class DefaultPluginServiceLocator implements PluginServiceLocator {

    private final PluginLoggingService loggingService;
    private final PluginCacheService cacheService;
    private final PluginNotificationService notificationService;
    private final PluginMetricsService metricsService;
    private final PluginPermissionService permissionService;
    private final PluginAsyncTaskExecutor asyncExecutor;
    private final PluginConfigurationValidator configValidator;
    private final PluginHookService hookService;
    private final PluginDataStore dataStore;
    private final PluginResourceManager resourceManager;
    private final PluginDependencyResolver dependencyResolver;
    private final PluginUpdateService updateService;
    private final PluginMonitoringService monitoringService;
    private final Map<Class<?>, Object> customServices;

    public DefaultPluginServiceLocator() {
        this.loggingService = new DefaultPluginLoggingService();
        this.cacheService = new DefaultPluginCacheService();
        this.notificationService = new DefaultPluginNotificationService();
        this.metricsService = new DefaultPluginMetricsService();
        this.permissionService = new DefaultPluginPermissionService();
        this.asyncExecutor = new DefaultPluginAsyncTaskExecutor();
        this.configValidator = new DefaultPluginConfigurationValidator();
        this.hookService = new DefaultPluginHookService();
        this.dataStore = new DefaultPluginDataStore();
        this.resourceManager = new DefaultPluginResourceManager();
        this.dependencyResolver = new DefaultPluginDependencyResolver();
        this.updateService = new DefaultPluginUpdateService();
        this.monitoringService = new DefaultPluginMonitoringService();
        this.customServices = new HashMap<>();
    }

    @Override
    public PluginLoggingService getLoggingService() {
        return loggingService;
    }

    @Override
    public PluginCacheService getCacheService() {
        return cacheService;
    }

    @Override
    public PluginNotificationService getNotificationService() {
        return notificationService;
    }

    @Override
    public PluginMetricsService getMetricsService() {
        return metricsService;
    }

    @Override
    public PluginPermissionService getPermissionService() {
        return permissionService;
    }

    @Override
    public PluginAsyncTaskExecutor getAsyncTaskExecutor() {
        return asyncExecutor;
    }

    @Override
    public PluginConfigurationValidator getConfigurationValidator() {
        return configValidator;
    }

    @Override
    public PluginHookService getHookService() {
        return hookService;
    }

    @Override
    public PluginDataStore getDataStore() {
        return dataStore;
    }

    @Override
    public PluginResourceManager getResourceManager() {
        return resourceManager;
    }

    @Override
    public PluginDependencyResolver getDependencyResolver() {
        return dependencyResolver;
    }

    @Override
    public PluginUpdateService getUpdateService() {
        return updateService;
    }

    @Override
    public PluginMonitoringService getMonitoringService() {
        return monitoringService;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getService(Class<T> serviceInterface) {
        // Check built-in services first
        if (serviceInterface == PluginLoggingService.class) return (T) loggingService;
        if (serviceInterface == PluginCacheService.class) return (T) cacheService;
        if (serviceInterface == PluginNotificationService.class) return (T) notificationService;
        if (serviceInterface == PluginMetricsService.class) return (T) metricsService;
        if (serviceInterface == PluginPermissionService.class) return (T) permissionService;
        if (serviceInterface == PluginAsyncTaskExecutor.class) return (T) asyncExecutor;
        if (serviceInterface == PluginConfigurationValidator.class) return (T) configValidator;
        if (serviceInterface == PluginHookService.class) return (T) hookService;
        if (serviceInterface == PluginDataStore.class) return (T) dataStore;
        if (serviceInterface == PluginResourceManager.class) return (T) resourceManager;
        if (serviceInterface == PluginDependencyResolver.class) return (T) dependencyResolver;
        if (serviceInterface == PluginUpdateService.class) return (T) updateService;
        if (serviceInterface == PluginMonitoringService.class) return (T) monitoringService;

        // Fall back to custom registered services
        Object service = customServices.get(serviceInterface);
        if (service == null) {
            return null;
        }
        return (T) service;
    }

    @Override
    public <T> void registerService(Class<T> serviceInterface, T implementation) {
        customServices.put(serviceInterface, implementation);
    }
}

