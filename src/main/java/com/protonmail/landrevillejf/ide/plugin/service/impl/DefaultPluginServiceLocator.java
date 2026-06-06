package com.protonmail.landrevillejf.ide.plugin.service.impl;

import com.protonmail.landrevillejf.ide.plugin.service.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of PluginServiceLocator providing all plugin services.
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

