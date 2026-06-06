package com.protonmail.landrevillejf.ide.plugin;

import com.protonmail.landrevillejf.ide.plugin.service.*;
import com.protonmail.landrevillejf.swingide.core.bus.EventBus;
import com.protonmail.landrevillejf.swingide.core.registry.ServiceRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PluginServiceInitializerTest {

    @Mock
    private ServiceRegistry serviceRegistry;

    @Mock
    private PluginEventBus pluginEventBus;

    @Mock
    private EventBus applicationEventBus;

    @Mock
    private PluginManager pluginManager;

    @Mock
    private PluginServiceLocator customServiceLocator;

    private File pluginDataDirectory;
    private String pluginId = "test.plugin.id";

    @BeforeEach
    void setUp() {
        pluginDataDirectory = new File(System.getProperty("java.io.tmpdir"), "test-plugin-data");
    }

    @Test
    void createExtendedPluginContext_ShouldCreateContextWithServices() {
        // When
        ExtendedPluginContext context = PluginServiceInitializer.createExtendedPluginContext(
                serviceRegistry,
                pluginEventBus,
                applicationEventBus,
                pluginManager,
                pluginDataDirectory,
                pluginId
        );

        // Then
        assertNotNull(context);
        assertTrue(context instanceof DefaultExtendedPluginContext);

        // Verify service locator was created
        PluginServiceLocator serviceLocator = context.getServiceLocator();
        assertNotNull(serviceLocator);

        // Verify stub services are available
        assertNotNull(context.getLoggingService());
        assertNotNull(context.getCacheService());
        assertNotNull(context.getNotificationService());
        assertNotNull(context.getMetricsService());
        assertNotNull(context.getPermissionService());
        assertNotNull(context.getAsyncTaskExecutor());
        assertNotNull(context.getConfigurationValidator());
        assertNotNull(context.getHookService());
        assertNotNull(context.getDataStore());
        assertNotNull(context.getResourceManager());
        assertNotNull(context.getDependencyResolver());
        assertNotNull(context.getUpdateService());
        assertNotNull(context.getMonitoringService());
    }

    @Test
    void createServiceLocator_ShouldReturnStubWhenNoCustomLocator() {
        // Given
        when(serviceRegistry.getService(PluginServiceLocator.class)).thenReturn(null);

        // When
        PluginServiceLocator result = PluginServiceInitializer.createServiceLocator(
                serviceRegistry,
                pluginEventBus,
                applicationEventBus
        );

        // Then
        assertNotNull(result);
        assertTrue(result instanceof PluginServiceInitializer.StubPluginServiceLocator);
        verify(serviceRegistry).getService(PluginServiceLocator.class);
    }

    @Test
    void createServiceLocator_ShouldReturnCustomLocatorWhenAvailable() {
        // Given
        when(serviceRegistry.getService(PluginServiceLocator.class)).thenReturn(customServiceLocator);

        // When
        PluginServiceLocator result = PluginServiceInitializer.createServiceLocator(
                serviceRegistry,
                pluginEventBus,
                applicationEventBus
        );

        // Then
        assertSame(customServiceLocator, result);
        verify(serviceRegistry).getService(PluginServiceLocator.class);
    }

    @Test
    void registerServicesInRegistry_ShouldRegisterAllPluginServices() {
        // Given
        ExtendedPluginContext context = PluginServiceInitializer.createExtendedPluginContext(
                serviceRegistry,
                pluginEventBus,
                applicationEventBus,
                pluginManager,
                pluginDataDirectory,
                pluginId
        );

        // When
        PluginServiceInitializer.registerServicesInRegistry(serviceRegistry, context);

        // Then
        verify(serviceRegistry).register(PluginServiceLocator.class, context.getServiceLocator());
        verify(serviceRegistry).register(PluginLoggingService.class, context.getLoggingService());
        verify(serviceRegistry).register(PluginCacheService.class, context.getCacheService());
        verify(serviceRegistry).register(PluginMetricsService.class, context.getMetricsService());
        verify(serviceRegistry).register(PluginNotificationService.class, context.getNotificationService());
        verify(serviceRegistry).register(PluginPermissionService.class, context.getPermissionService());
        verify(serviceRegistry).register(PluginAsyncTaskExecutor.class, context.getAsyncTaskExecutor());
        verify(serviceRegistry).register(PluginConfigurationValidator.class, context.getConfigurationValidator());
        verify(serviceRegistry).register(PluginHookService.class, context.getHookService());
        verify(serviceRegistry).register(PluginDataStore.class, context.getDataStore());
        verify(serviceRegistry).register(PluginResourceManager.class, context.getResourceManager());
        verify(serviceRegistry).register(PluginDependencyResolver.class, context.getDependencyResolver());
        verify(serviceRegistry).register(PluginUpdateService.class, context.getUpdateService());
        verify(serviceRegistry).register(PluginMonitoringService.class, context.getMonitoringService());
    }

    @Test
    void registerServicesInRegistry_ShouldHandleRegistrationErrors() {
        // Given
        ExtendedPluginContext context = PluginServiceInitializer.createExtendedPluginContext(
                serviceRegistry,
                pluginEventBus,
                applicationEventBus,
                pluginManager,
                pluginDataDirectory,
                pluginId
        );

        // Simulate an exception during registration
        doThrow(new RuntimeException("Registration failed"))
                .when(serviceRegistry)
                .register(eq(PluginServiceLocator.class), any());

        // When/Then - should not throw exception (error is logged internally)
        assertDoesNotThrow(() -> PluginServiceInitializer.registerServicesInRegistry(serviceRegistry, context));
    }

    @Test
    void stubServiceLocator_ShouldReturnStubImplementations() {
        // Given
        PluginServiceLocator locator = PluginServiceInitializer.createServiceLocator(
                serviceRegistry,
                pluginEventBus,
                applicationEventBus
        );

        // Then - verify all stub services are non-null
        assertNotNull(locator.getLoggingService());
        assertNotNull(locator.getCacheService());
        assertNotNull(locator.getNotificationService());
        assertNotNull(locator.getMetricsService());
        assertNotNull(locator.getPermissionService());
        assertNotNull(locator.getAsyncTaskExecutor());
        assertNotNull(locator.getConfigurationValidator());
        assertNotNull(locator.getHookService());
        assertNotNull(locator.getDataStore());
        assertNotNull(locator.getResourceManager());
        assertNotNull(locator.getDependencyResolver());
        assertNotNull(locator.getUpdateService());
        assertNotNull(locator.getMonitoringService());
    }

    @Test
    void stubServiceLocator_GetService_ShouldReturnNull() {
        // Given
        PluginServiceLocator locator = PluginServiceInitializer.createServiceLocator(
                serviceRegistry,
                pluginEventBus,
                applicationEventBus
        );

        // When
        Object result = locator.getService(String.class);

        // Then
        assertNull(result);
    }

    @Test
    void stubServiceLocator_RegisterService_ShouldDoNothing() {
        // Given
        PluginServiceLocator locator = PluginServiceInitializer.createServiceLocator(
                serviceRegistry,
                pluginEventBus,
                applicationEventBus
        );

        // When/Then - should not throw exception
        assertDoesNotThrow(() -> locator.registerService(String.class, "test"));
    }

    @Test
    void stubLoggingService_ShouldHaveDefaultBehavior() {
        // Given
        PluginLoggingService loggingService = PluginServiceInitializer.StubServices.LOGGING_SERVICE;

        // Then
        assertEquals(PluginLoggingService.LogLevel.INFO, loggingService.getLogLevel("any"));
        assertDoesNotThrow(() -> loggingService.log("plugin", PluginLoggingService.LogLevel.INFO, "test"));
        assertDoesNotThrow(() -> loggingService.clearLogs("plugin"));
        assertNotNull(loggingService.getRecentLogs("plugin", 10));
        assertTrue(loggingService.getRecentLogs("plugin", 10).isEmpty());
        assertNotNull(loggingService.getStatistics("plugin"));
    }

    @Test
    void stubCacheService_ShouldStoreAndRetrieveValues() {
        // Given
        PluginCacheService cacheService = PluginServiceInitializer.StubServices.CACHE_SERVICE;
        String pluginId = "test";
        String key = "testKey";
        String value = "testValue";

        // When
        cacheService.put(pluginId, key, value);

        // Then
        assertEquals(value, cacheService.get(pluginId, key));
        assertTrue(cacheService.containsKey(pluginId, key));
        assertEquals(1, cacheService.size(pluginId));

        // When - remove
        cacheService.remove(pluginId, key);

        // Then
        assertFalse(cacheService.containsKey(pluginId, key));
        assertEquals(0, cacheService.size(pluginId));
    }

    @Test
    void stubCacheService_ShouldHandleTTL() {
        // Given
        PluginCacheService cacheService = PluginServiceInitializer.StubServices.CACHE_SERVICE;
        String pluginId = "test";
        String key = "ttlKey";
        String value = "ttlValue";

        // When - TTL is currently ignored in stub (uses normal put)
        assertDoesNotThrow(() -> cacheService.put(pluginId, key, value, 1000));

        // Then
        assertEquals(value, cacheService.get(pluginId, key));
    }

    @Test
    void stubNotificationService_ShouldNotThrowExceptions() {
        // Given
        PluginNotificationService notificationService = PluginServiceInitializer.StubServices.NOTIFICATION_SERVICE;

        // Then
        assertDoesNotThrow(() -> notificationService.notify("plugin", "title", "message"));
        assertDoesNotThrow(() -> notificationService.dismiss("notif-id"));
        assertNotNull(notificationService.getActiveNotifications("plugin"));
        assertTrue(notificationService.getActiveNotifications("plugin").isEmpty());
        assertNotNull(notificationService.getStatistics("plugin"));
    }

    @Test
    void stubMetricsService_ShouldIncrementCounters() {
        // Given
        PluginMetricsService metricsService = PluginServiceInitializer.StubServices.METRICS_SERVICE;
        String pluginId = "test";
        String metricName = "test.metric";

        // When
        metricsService.incrementCounter(pluginId, metricName);
        metricsService.incrementCounter(pluginId, metricName, 5);

        // Then
        assertEquals(6, metricsService.getCounterValue(pluginId, metricName));

        // When - reset
        metricsService.resetMetric(pluginId, metricName);

        // Then
        assertEquals(0, metricsService.getCounterValue(pluginId, metricName));
    }

    @Test
    void stubMetricsService_ShouldHandleTimer() {
        // Given
        PluginMetricsService metricsService = PluginServiceInitializer.StubServices.METRICS_SERVICE;

        // When
        PluginMetricsService.TimerContext timer = metricsService.startTimer("test", "timer.metric");

        // Then
        assertNotNull(timer);
        assertDoesNotThrow(() -> timer.stop());
        assertDoesNotThrow(() -> timer.close());
    }

    @Test
    void stubPermissionService_ShouldManagePermissions() {
        // Given
        PluginPermissionService permissionService = PluginServiceInitializer.StubServices.PERMISSION_SERVICE;
        String pluginId = "test";
        String permissionId = "test.permission";

        // When - grant permission
        assertTrue(permissionService.grantPermission(pluginId, permissionId));

        // Then
        assertTrue(permissionService.hasPermission(pluginId, permissionId));
        assertTrue(permissionService.hasAllPermissions(pluginId, permissionId));
        assertTrue(permissionService.hasAnyPermission(pluginId, permissionId, "other"));

        // When - revoke permission
        assertTrue(permissionService.revokePermission(pluginId, permissionId));

        // Then
        assertFalse(permissionService.hasPermission(pluginId, permissionId));
    }

    @Test
    void stubDataStore_ShouldStoreAndRetrieveData() {
        // Given
        PluginDataStore dataStore = PluginServiceInitializer.StubServices.DATA_STORE;
        String pluginId = "test";
        String key = "dataKey";
        String value = "dataValue";

        // When
        dataStore.store(pluginId, key, value);

        // Then
        assertEquals(value, dataStore.retrieve(pluginId, key));
        assertTrue(dataStore.exists(pluginId, key));
        assertTrue(dataStore.getKeys(pluginId).contains(key));

        // When - delete
        assertTrue(dataStore.delete(pluginId, key));

        // Then
        assertFalse(dataStore.exists(pluginId, key));
    }

    @Test
    void stubDataStore_ShouldHandleExportImport() {
        // Given
        PluginDataStore dataStore = PluginServiceInitializer.StubServices.DATA_STORE;
        String pluginId = "test";

        dataStore.store(pluginId, "key1", "value1");
        dataStore.store(pluginId, "key2", "value2");

        // When - export
        var exported = dataStore.exportAllData(pluginId);

        // Then
        assertEquals(2, exported.size());

        // When - clear and import
        dataStore.clear(pluginId);
        dataStore.importAllData(pluginId, exported);

        // Then
        assertEquals("value1", dataStore.retrieve(pluginId, "key1"));
        assertEquals("value2", dataStore.retrieve(pluginId, "key2"));
    }

    @Test
    void stubAsyncExecutor_ShouldReturnTaskIds() {
        // Given
        PluginAsyncTaskExecutor executor = PluginServiceInitializer.StubServices.ASYNC_EXECUTOR;

        // When
        String taskId = executor.executeNamedTask("test", "taskName", () -> {});

        // Then
        assertNotNull(taskId);
        assertTrue(taskId.startsWith("task-"));

        // When - cancel
        assertTrue(executor.cancelTask(taskId));
    }

    @Test
    void stubUpdateService_ShouldReturnDefaultValues() {
        // Given
        PluginUpdateService updateService = PluginServiceInitializer.StubServices.UPDATE_SERVICE;

        // Then
        assertNull(updateService.checkForUpdates("plugin"));
        assertEquals(PluginUpdateService.UpdateStatus.CHECKING, updateService.getUpdateStatus("plugin"));
        assertTrue(updateService.installUpdate("plugin", "1.0.0"));
        assertTrue(updateService.cancelUpdate("plugin"));
        assertEquals(0, updateService.getUpdateProgress("plugin"));
        assertEquals(PluginUpdateService.UpdateChannel.STABLE, updateService.getUpdateChannel("plugin"));
        assertFalse(updateService.isAutoUpdateEnabled("plugin"));
        assertNotNull(updateService.getUpdateStatistics());
    }

    @Test
    void stubMonitoringService_ShouldReturnHealthyStatus() {
        // Given
        PluginMonitoringService monitoringService = PluginServiceInitializer.StubServices.MONITORING_SERVICE;

        // Then
        assertEquals(PluginMonitoringService.HealthStatus.HEALTHY, monitoringService.getHealthStatus("plugin"));
        assertEquals(PluginMonitoringService.HealthStatus.HEALTHY, monitoringService.getGlobalHealthStatus());
        assertEquals(0.0, monitoringService.getCpuUsage("plugin"));
        assertEquals(0, monitoringService.getErrorCount("plugin"));
        assertNotNull(monitoringService.getAllHealthReports());
        assertTrue(monitoringService.getAllHealthReports().isEmpty());
    }

    @Test
    void stubDependencyResolver_ShouldReturnResolvedTrue() {
        // Given
        PluginDependencyResolver resolver = PluginServiceInitializer.StubServices.DEPENDENCY_RESOLVER;

        // Then
        assertTrue(resolver.areRequiredDependenciesResolved("plugin"));
        assertTrue(resolver.isDependencyResolved("plugin", "dep"));
        assertNotNull(resolver.getDependencies("plugin"));
        assertTrue(resolver.getDependencies("plugin").isEmpty());
    }

    @Test
    void stubResourceManager_ShouldReturnSuccessForOperations() {
        // Given
        PluginResourceManager resourceManager = PluginServiceInitializer.StubServices.RESOURCE_MANAGER;

        // Then
        assertTrue(resourceManager.registerResource("plugin", "resId", "name", new Object()));
        assertTrue(resourceManager.unregisterResource("plugin", "resId"));
        assertTrue(resourceManager.grantResourceAccess("plugin", "resId"));
        assertTrue(resourceManager.hasResourceAccess("plugin", "resId"));
        assertNotNull(resourceManager.getStatistics());
    }

    @Test
    void stubHookService_ShouldGenerateHookIds() {
        // Given
        PluginHookService hookService = PluginServiceInitializer.StubServices.HOOK_SERVICE;

        // When - for void return type, use a code block without return
        String hookId = hookService.registerHook("plugin", PluginHookService.HookType.PRE_ENABLE, data -> {
            // Do nothing - void return type
            System.out.println("Hook executed with data: " + data);
        });

        // Then
        assertNotNull(hookId);
        assertTrue(hookId.startsWith("hook-"));
        assertTrue(hookService.unregisterHook(hookId));
    }

    @Test
    void stubConfigValidator_ShouldReturnValidResults() {
        // Given
        PluginConfigurationValidator validator = PluginServiceInitializer.StubServices.CONFIG_VALIDATOR;

        // When
        PluginConfigurationValidator.ValidationResult result = validator.validateConfiguration("plugin", new java.util.HashMap<>());

        // Then
        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
        assertNotNull(validator.getSchema("plugin"));
        assertNotNull(validator.getDefaultConfiguration("plugin"));
    }
}