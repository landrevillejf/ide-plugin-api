package com.protonmail.landrevillejf.ide.plugin.service.impl;

import com.protonmail.landrevillejf.ide.plugin.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DefaultPluginServiceLocatorTest {

    private DefaultPluginServiceLocator serviceLocator;

    @BeforeEach
    void setUp() {
        serviceLocator = new DefaultPluginServiceLocator();
    }

    @Test
    void getLoggingService() {
        PluginLoggingService service = serviceLocator.getLoggingService();

        assertNotNull(service);
        assertTrue(service instanceof DefaultPluginLoggingService);
    }

    @Test
    void getCacheService() {
        PluginCacheService service = serviceLocator.getCacheService();

        assertNotNull(service);
        assertTrue(service instanceof DefaultPluginCacheService);
    }

    @Test
    void getNotificationService() {
        PluginNotificationService service = serviceLocator.getNotificationService();

        assertNotNull(service);
        assertTrue(service instanceof DefaultPluginNotificationService);
    }

    @Test
    void getMetricsService() {
        PluginMetricsService service = serviceLocator.getMetricsService();

        assertNotNull(service);
        assertTrue(service instanceof DefaultPluginMetricsService);
    }

    @Test
    void getPermissionService() {
        PluginPermissionService service = serviceLocator.getPermissionService();

        assertNotNull(service);
        assertTrue(service instanceof DefaultPluginPermissionService);
    }

    @Test
    void getAsyncTaskExecutor() {
        PluginAsyncTaskExecutor service = serviceLocator.getAsyncTaskExecutor();

        assertNotNull(service);
        assertTrue(service instanceof DefaultPluginAsyncTaskExecutor);
    }

    @Test
    void getConfigurationValidator() {
        PluginConfigurationValidator service = serviceLocator.getConfigurationValidator();

        assertNotNull(service);
        assertTrue(service instanceof DefaultPluginConfigurationValidator);
    }

    @Test
    void getHookService() {
        PluginHookService service = serviceLocator.getHookService();

        assertNotNull(service);
        assertTrue(service instanceof DefaultPluginHookService);
    }

    @Test
    void getDataStore() {
        PluginDataStore service = serviceLocator.getDataStore();

        assertNotNull(service);
        assertTrue(service instanceof DefaultPluginDataStore);
    }

    @Test
    void getResourceManager() {
        PluginResourceManager service = serviceLocator.getResourceManager();

        assertNotNull(service);
        assertTrue(service instanceof DefaultPluginResourceManager);
    }

    @Test
    void getDependencyResolver() {
        PluginDependencyResolver service = serviceLocator.getDependencyResolver();

        assertNotNull(service);
        assertTrue(service instanceof DefaultPluginDependencyResolver);
    }

    @Test
    void getUpdateService() {
        PluginUpdateService service = serviceLocator.getUpdateService();

        assertNotNull(service);
        assertTrue(service instanceof DefaultPluginUpdateService);
    }

    @Test
    void getMonitoringService() {
        PluginMonitoringService service = serviceLocator.getMonitoringService();

        assertNotNull(service);
        assertTrue(service instanceof DefaultPluginMonitoringService);
    }

    @Test
    void getService() {
        // Register a custom service first
        TestService testService = new TestServiceImpl();
        serviceLocator.registerService(TestService.class, testService);

        TestService retrieved = serviceLocator.getService(TestService.class);

        assertNotNull(retrieved);
        assertSame(testService, retrieved);
    }

    @Test
    void getServiceBuiltInLogging() {
        PluginLoggingService service = serviceLocator.getService(PluginLoggingService.class);
        assertNotNull(service);
        assertSame(serviceLocator.getLoggingService(), service);
    }

    @Test
    void getServiceBuiltInCache() {
        PluginCacheService service = serviceLocator.getService(PluginCacheService.class);
        assertNotNull(service);
        assertSame(serviceLocator.getCacheService(), service);
    }

    @Test
    void getServiceBuiltInNotification() {
        PluginNotificationService service = serviceLocator.getService(PluginNotificationService.class);
        assertNotNull(service);
        assertSame(serviceLocator.getNotificationService(), service);
    }

    @Test
    void getServiceBuiltInMetrics() {
        PluginMetricsService service = serviceLocator.getService(PluginMetricsService.class);
        assertNotNull(service);
        assertSame(serviceLocator.getMetricsService(), service);
    }

    @Test
    void getServiceBuiltInPermission() {
        PluginPermissionService service = serviceLocator.getService(PluginPermissionService.class);
        assertNotNull(service);
        assertSame(serviceLocator.getPermissionService(), service);
    }

    @Test
    void getServiceBuiltInAsyncExecutor() {
        PluginAsyncTaskExecutor service = serviceLocator.getService(PluginAsyncTaskExecutor.class);
        assertNotNull(service);
        assertSame(serviceLocator.getAsyncTaskExecutor(), service);
    }

    @Test
    void getServiceBuiltInConfigValidator() {
        PluginConfigurationValidator service = serviceLocator.getService(PluginConfigurationValidator.class);
        assertNotNull(service);
        assertSame(serviceLocator.getConfigurationValidator(), service);
    }

    @Test
    void getServiceBuiltInHook() {
        PluginHookService service = serviceLocator.getService(PluginHookService.class);
        assertNotNull(service);
        assertSame(serviceLocator.getHookService(), service);
    }

    @Test
    void getServiceBuiltInDataStore() {
        PluginDataStore service = serviceLocator.getService(PluginDataStore.class);
        assertNotNull(service);
        assertSame(serviceLocator.getDataStore(), service);
    }

    @Test
    void getServiceBuiltInResourceManager() {
        PluginResourceManager service = serviceLocator.getService(PluginResourceManager.class);
        assertNotNull(service);
        assertSame(serviceLocator.getResourceManager(), service);
    }

    @Test
    void getServiceBuiltInDependencyResolver() {
        PluginDependencyResolver service = serviceLocator.getService(PluginDependencyResolver.class);
        assertNotNull(service);
        assertSame(serviceLocator.getDependencyResolver(), service);
    }

    @Test
    void getServiceBuiltInUpdateService() {
        PluginUpdateService service = serviceLocator.getService(PluginUpdateService.class);
        assertNotNull(service);
        assertSame(serviceLocator.getUpdateService(), service);
    }

    @Test
    void getServiceBuiltInMonitoringService() {
        PluginMonitoringService service = serviceLocator.getService(PluginMonitoringService.class);
        assertNotNull(service);
        assertSame(serviceLocator.getMonitoringService(), service);
    }

    @Test
    void getNonExistentService() {
        NonExistentService service = serviceLocator.getService(NonExistentService.class);

        assertNull(service);
    }

    @Test
    void registerService() {
        CustomService customService = new CustomServiceImpl();
        serviceLocator.registerService(CustomService.class, customService);

        CustomService retrieved = serviceLocator.getService(CustomService.class);

        assertNotNull(retrieved);
        assertSame(customService, retrieved);
    }

    @Test
    void registerAndOverrideService() {
        CustomService firstService = new CustomServiceImpl();
        CustomService secondService = new AnotherCustomServiceImpl();

        serviceLocator.registerService(CustomService.class, firstService);
        CustomService retrieved1 = serviceLocator.getService(CustomService.class);
        assertSame(firstService, retrieved1);

        // Override
        serviceLocator.registerService(CustomService.class, secondService);
        CustomService retrieved2 = serviceLocator.getService(CustomService.class);
        assertSame(secondService, retrieved2);
    }

    @Test
    void registerMultipleServices() {
        ServiceA serviceA = new ServiceAImpl();
        ServiceB serviceB = new ServiceBImpl();

        serviceLocator.registerService(ServiceA.class, serviceA);
        serviceLocator.registerService(ServiceB.class, serviceB);

        assertSame(serviceA, serviceLocator.getService(ServiceA.class));
        assertSame(serviceB, serviceLocator.getService(ServiceB.class));
    }

    @Test
    void allServicesAreDifferentInstances() {
        PluginLoggingService logging = serviceLocator.getLoggingService();
        PluginCacheService cache = serviceLocator.getCacheService();
        PluginNotificationService notification = serviceLocator.getNotificationService();

        assertNotSame(logging, cache);
        assertNotSame(logging, notification);
        assertNotSame(cache, notification);
    }

    @Test
    void servicesAreReusable() {
        PluginLoggingService logging1 = serviceLocator.getLoggingService();
        PluginLoggingService logging2 = serviceLocator.getLoggingService();

        assertSame(logging1, logging2);
    }

    @Test
    void registerNullService() {
        serviceLocator.registerService(CustomService.class, null);

        CustomService retrieved = serviceLocator.getService(CustomService.class);

        assertNull(retrieved);
    }

    // Test interfaces and implementations
    private interface TestService {
        String doSomething();
    }

    private static class TestServiceImpl implements TestService {
        @Override
        public String doSomething() {
            return "test";
        }
    }

    private interface NonExistentService {
    }

    private interface CustomService {
    }

    private static class CustomServiceImpl implements CustomService {
    }

    private static class AnotherCustomServiceImpl implements CustomService {
    }

    private interface ServiceA {
    }

    private static class ServiceAImpl implements ServiceA {
    }

    private interface ServiceB {
    }

    private static class ServiceBImpl implements ServiceB {
    }
}