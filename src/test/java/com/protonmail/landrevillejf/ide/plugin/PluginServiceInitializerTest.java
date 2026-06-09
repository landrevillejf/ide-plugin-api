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

    // ==================== TESTS SUPPLEMENTAIRES POUR TUER LES MUTANTS ====================

    @Test
    void stubLoggingService_GetStatistics_ShouldReturnEmptyMap() {
        // Given
        PluginLoggingService loggingService = PluginServiceInitializer.StubServices.LOGGING_SERVICE;

        // When
        var stats = loggingService.getStatistics("any-plugin");

        // Then
        assertNotNull(stats);
        assertTrue(stats.isEmpty());
    }

    @Test
    void stubCacheService_Put_WithTTL_ShouldNotThrowException() {
        // Given
        PluginCacheService cacheService = PluginServiceInitializer.StubServices.CACHE_SERVICE;

        // When/Then - le mutant sur put avec TTL (ligne 227)
        assertDoesNotThrow(() -> cacheService.put("plugin", "key", "value", 5000));
        assertEquals("value", cacheService.get("plugin", "key"));
    }

    @Test
    void stubCacheService_Get_WithClass_ShouldReturnNullWhenTypeMismatch() {
        // Given
        PluginCacheService cacheService = PluginServiceInitializer.StubServices.CACHE_SERVICE;
        cacheService.put("plugin", "key", "stringValue");

        // When
        Integer result = cacheService.get("plugin", "key", Integer.class);

        // Then - ligne 229, cast conditionnel
        assertNull(result);
    }

    @Test
    void stubCacheService_GetKeys_ShouldReturnListOfKeys() {
        // Given
        PluginCacheService cacheService = PluginServiceInitializer.StubServices.CACHE_SERVICE;
        cacheService.put("plugin", "key1", "value1");
        cacheService.put("plugin", "key2", "value2");

        // When
        var keys = cacheService.getKeys("plugin");

        // Then - ligne 239
        assertNotNull(keys);
        assertEquals(2, keys.size());
        assertTrue(keys.contains("key1"));
        assertTrue(keys.contains("key2"));
    }

    @Test
    void stubCacheService_ClearAll_ShouldClearAllPluginData() {
        // Given
        PluginCacheService cacheService = PluginServiceInitializer.StubServices.CACHE_SERVICE;
        cacheService.put("plugin1", "key", "value1");
        cacheService.put("plugin2", "key", "value2");

        // When - ligne 233
        cacheService.clearAll();

        // Then
        assertNull(cacheService.get("plugin1", "key"));
        assertNull(cacheService.get("plugin2", "key"));
    }

    @Test
    void stubNotificationService_GetStatistics_ShouldReturnEmptyMap() {
        // Given
        PluginNotificationService notificationService = PluginServiceInitializer.StubServices.NOTIFICATION_SERVICE;

        // When
        var stats = notificationService.getStatistics("plugin");

        // Then - ligne 253
        assertNotNull(stats);
        assertTrue(stats.isEmpty());
    }

    @Test
    void stubMetricsService_GetAllMetrics_ShouldReturnNonEmptyMapAfterIncrement() {
        // Given
        PluginMetricsService metricsService = PluginServiceInitializer.StubServices.METRICS_SERVICE;
        metricsService.incrementCounter("plugin", "test.metric", 42);

        // When - ligne 266
        var allMetrics = metricsService.getAllMetrics("plugin");

        // Then
        assertNotNull(allMetrics);
        assertEquals(42L, allMetrics.get("test.metric"));
    }

    @Test
    void stubMetricsService_Timer_GetElapsedMillis_ShouldReturnNonNegative() {
        // Given
        PluginMetricsService metricsService = PluginServiceInitializer.StubServices.METRICS_SERVICE;
        var timer = metricsService.startTimer("plugin", "timer");

        // When
        long elapsed = timer.getElapsedMillis();
        long stopped = timer.stop();

        // Then - ligne 262 (getElapsedMillis et stop)
        assertTrue(elapsed >= 0);
        assertTrue(stopped >= 0);
    }

    @Test
    void stubPermissionService_RevokePermission_ShouldReturnFalseWhenNotExists() {
        // Given
        PluginPermissionService permissionService = PluginServiceInitializer.StubServices.PERMISSION_SERVICE;

        // When - ligne 277
        boolean result = permissionService.revokePermission("plugin", "nonexistent");

        // Then
        assertFalse(result);
    }

    @Test
    void stubPermissionService_HasAllPermissions_WithNullSet_ShouldReturnFalse() {
        // Given
        PluginPermissionService permissionService = PluginServiceInitializer.StubServices.PERMISSION_SERVICE;

        // When - plugin n'a aucun permission (set est null ou vide)
        boolean result = permissionService.hasAllPermissions("unknown-plugin", "perm1", "perm2");

        // Then - ligne 279
        assertFalse(result);
    }

    @Test
    void stubPermissionService_HasAnyPermission_WithNullSet_ShouldReturnFalse() {
        // Given
        PluginPermissionService permissionService = PluginServiceInitializer.StubServices.PERMISSION_SERVICE;

        // When - plugin n'a aucun permission
        boolean result = permissionService.hasAnyPermission("unknown-plugin", "perm1", "perm2");

        // Then - ligne 280
        assertFalse(result);
    }

    @Test
    void stubAsyncExecutor_IncrementCounter_ShouldWorkMultipleTimes() {
        // Given
        PluginAsyncTaskExecutor executor = PluginServiceInitializer.StubServices.ASYNC_EXECUTOR;

        // When - ligne 300 (Replaced integer addition with subtraction)
        String taskId1 = executor.executeNamedTask("plugin", "task1", () -> {});
        String taskId2 = executor.executeNamedTask("plugin", "task2", () -> {});

        // Then
        assertNotEquals(taskId1, taskId2);
    }

    @Test
    void stubConfigValidator_GetSchema_ShouldReturnEmptyMap() {
        // Given
        PluginConfigurationValidator validator = PluginServiceInitializer.StubServices.CONFIG_VALIDATOR;

        // When - ligne 319
        var schema = validator.getSchema("plugin");

        // Then
        assertNotNull(schema);
        assertTrue(schema.isEmpty());
    }

    @Test
    void stubConfigValidator_GetDefaultConfiguration_ShouldReturnEmptyMap() {
        // Given
        PluginConfigurationValidator validator = PluginServiceInitializer.StubServices.CONFIG_VALIDATOR;

        // When - ligne 323
        var defaultConfig = validator.getDefaultConfiguration("plugin");

        // Then
        assertNotNull(defaultConfig);
        assertTrue(defaultConfig.isEmpty());
    }

    @Test
    void stubDataStore_Delete_ShouldReturnFalseWhenKeyNotExists() {
        // Given
        PluginDataStore dataStore = PluginServiceInitializer.StubServices.DATA_STORE;

        // When - ligne 350
        boolean result = dataStore.delete("plugin", "nonexistent");

        // Then
        assertFalse(result);
    }

    @Test
    void stubDataStore_Store_WithFormat_ShouldWork() {
        // Given
        PluginDataStore dataStore = PluginServiceInitializer.StubServices.DATA_STORE;

        // When - ligne 346
        assertDoesNotThrow(() -> dataStore.store("plugin", "key", "value", PluginDataStore.SerializationFormat.JSON));

        // Then
        assertEquals("value", dataStore.retrieve("plugin", "key"));
    }

    @Test
    void stubResourceManager_GetStatistics_ShouldReturnEmptyMap() {
        // Given
        PluginResourceManager resourceManager = PluginServiceInitializer.StubServices.RESOURCE_MANAGER;

        // When - ligne 379
        var stats = resourceManager.getStatistics();

        // Then
        assertNotNull(stats);
        assertTrue(stats.isEmpty());
    }

    @Test
    void stubUpdateService_GetUpdateStatistics_ShouldReturnEmptyMap() {
        // Given
        PluginUpdateService updateService = PluginServiceInitializer.StubServices.UPDATE_SERVICE;

        // When - ligne 411
        var stats = updateService.getUpdateStatistics();

        // Then
        assertNotNull(stats);
        assertTrue(stats.isEmpty());
    }

    @Test
    void stubMonitoringService_GetHealthReport_ShouldReturnNonNull() {
        // Given
        PluginMonitoringService monitoringService = PluginServiceInitializer.StubServices.MONITORING_SERVICE;

        // When - ligne 415
        var report = monitoringService.getHealthReport("plugin");

        // Then
        assertNotNull(report);
        assertEquals("plugin", report.getPluginId());
        assertEquals(PluginMonitoringService.HealthStatus.HEALTHY, report.getStatus());
        assertNotNull(report.getDetails());
    }

    @Test
    void stubHookService_ExecuteHooks_ShouldReturnNonNullContext() {
        // Given
        PluginHookService hookService = PluginServiceInitializer.StubServices.HOOK_SERVICE;

        // When - ligne 335
        var context = hookService.executeHooks("plugin", PluginHookService.HookType.PRE_ENABLE, new java.util.HashMap<>());

        // Then
        assertNotNull(context);
        assertEquals("plugin", context.getPluginId());
        assertEquals(PluginHookService.HookType.PRE_ENABLE, context.getHookType());
        assertNotNull(context.getHookData());
        assertFalse(context.isCancelled());
    }

    @Test
    void stubPermissionService_GrantPermission_WithExistingSet_ShouldAddPermission() {
        // Given
        PluginPermissionService permissionService = PluginServiceInitializer.StubServices.PERMISSION_SERVICE;

        // First grant
        permissionService.grantPermission("plugin", "perm1");

        // When - second grant for same plugin (lambda line 276)
        boolean result = permissionService.grantPermission("plugin", "perm2");

        // Then
        assertTrue(result);
        assertTrue(permissionService.hasPermission("plugin", "perm1"));
        assertTrue(permissionService.hasPermission("plugin", "perm2"));
    }

    // ==================== TESTS POUR TUER LES MUTANTS RESTANTS ====================

    @Test
    void stubLoggingService_GetStatistics_ShouldReturnEmptyMap_AndKillMutant() {
        // Given - Pour tuer le mutant ligne 221 (EMPTY_RETURNS)
        PluginLoggingService loggingService = PluginServiceInitializer.StubServices.LOGGING_SERVICE;

        // When
        var stats = loggingService.getStatistics("any-plugin");

        // Then - Vérifier que la map est bien vide (pas null)
        assertNotNull(stats);
        assertTrue(stats.isEmpty());
        // Le mutant remplacerait par Collections.emptyMap() - même comportement,
        // mais on vérifie l'instance
        assertTrue(stats instanceof java.util.HashMap);
    }

    @Test
    void stubCacheService_GetStatistics_ShouldReturnEmptyMap_AndKillMutant() {
        // Given - Pour tuer le mutant ligne 237 (EMPTY_RETURNS)
        PluginCacheService cacheService = PluginServiceInitializer.StubServices.CACHE_SERVICE;

        // When
        var stats = cacheService.getStatistics("plugin");

        // Then
        assertNotNull(stats);
        assertTrue(stats.isEmpty());
        assertTrue(stats instanceof java.util.HashMap);
    }

    @Test
    void stubMetricsService_GetMetricsByType_ShouldReturnEmptyMap_AndKillMutant() {
        // Given - Pour tuer le mutant ligne 267 (EMPTY_RETURNS)
        PluginMetricsService metricsService = PluginServiceInitializer.StubServices.METRICS_SERVICE;

        // When
        var metrics = metricsService.getMetricsByType("plugin", PluginMetricsService.MetricType.COUNTER);

        // Then
        assertNotNull(metrics);
        assertTrue(metrics.isEmpty());
    }

    @Test
    void stubMetricsService_GetMetricStatistics_ShouldReturnEmptyMap_AndKillMutant() {
        // Given - Pour tuer le mutant ligne 270 (EMPTY_RETURNS)
        PluginMetricsService metricsService = PluginServiceInitializer.StubServices.METRICS_SERVICE;

        // When
        var stats = metricsService.getMetricStatistics("plugin", "metric");

        // Then
        assertNotNull(stats);
        assertTrue(stats.isEmpty());
    }

    @Test
    void stubMetricsService_ExportMetrics_ShouldReturnEmptyMap_AndKillMutant() {
        // Given - Pour tuer le mutant ligne 271 (EMPTY_RETURNS)
        PluginMetricsService metricsService = PluginServiceInitializer.StubServices.METRICS_SERVICE;

        // When
        var exported = metricsService.exportMetrics("plugin");

        // Then
        assertNotNull(exported);
        assertTrue(exported.isEmpty());
    }

    @Test
    void stubPermissionService_GetPluginPermissions_ShouldReturnEmptySet_AndKillMutant() {
        // Given - Pour tuer le mutant ligne 281 (EMPTY_RETURNS)
        PluginPermissionService permissionService = PluginServiceInitializer.StubServices.PERMISSION_SERVICE;

        // When
        var permissions = permissionService.getPluginPermissions("unknown-plugin");

        // Then
        assertNotNull(permissions);
        assertTrue(permissions.isEmpty());
    }

    @Test
    void stubPermissionService_AssignRole_ShouldReturnTrue_AndKillMutant() {
        // Given - Pour tuer le mutant ligne 282 (FALSE_RETURNS)
        PluginPermissionService permissionService = PluginServiceInitializer.StubServices.PERMISSION_SERVICE;

        // When
        boolean result = permissionService.assignRole("plugin", "admin");

        // Then
        assertTrue(result); // Le mutant remplacerait par false
    }

    @Test
    void stubPermissionService_RemoveRole_ShouldReturnTrue_AndKillMutant() {
        // Given - Pour tuer le mutant ligne 283 (FALSE_RETURNS)
        PluginPermissionService permissionService = PluginServiceInitializer.StubServices.PERMISSION_SERVICE;

        // When
        boolean result = permissionService.removeRole("plugin", "admin");

        // Then
        assertTrue(result); // Le mutant remplacerait par false
    }

    @Test
    void stubAsyncExecutor_ExecuteTaskWithPriority_ShouldReturnTaskId_AndKillMutant() {
        // Given - Pour tuer le mutant ligne 301 (NULL_RETURNS et INCREMENTS)
        PluginAsyncTaskExecutor executor = PluginServiceInitializer.StubServices.ASYNC_EXECUTOR;

        // When
        String taskId = executor.executeTaskWithPriority("plugin", () -> {},
                PluginAsyncTaskExecutor.TaskPriority.NORMAL);

        // Then
        assertNotNull(taskId);
        assertTrue(taskId.startsWith("task-"));

        // Vérifier que le compteur s'incrémente (tuer le mutant sur l'addition)
        String taskId2 = executor.executeTaskWithPriority("plugin", () -> {},
                PluginAsyncTaskExecutor.TaskPriority.HIGH);
        assertNotEquals(taskId, taskId2);
    }

    @Test
    void stubAsyncExecutor_ScheduleTask_ShouldReturnTaskId_AndKillMutant() {
        // Given - Pour tuer le mutant ligne 303 (NULL_RETURNS et INCREMENTS)
        PluginAsyncTaskExecutor executor = PluginServiceInitializer.StubServices.ASYNC_EXECUTOR;

        // When
        String taskId = executor.scheduleTask("plugin", () -> {}, 1000);

        // Then
        assertNotNull(taskId);
        assertTrue(taskId.startsWith("task-"));

        // Vérifier l'incrémentation
        String taskId2 = executor.scheduleTask("plugin", () -> {}, 2000);
        assertNotEquals(taskId, taskId2);
    }

    @Test
    void stubAsyncExecutor_SchedulePeriodicTask_ShouldReturnTaskId_AndKillMutant() {
        // Given - Pour tuer le mutant ligne 304 (NULL_RETURNS et INCREMENTS)
        PluginAsyncTaskExecutor executor = PluginServiceInitializer.StubServices.ASYNC_EXECUTOR;

        // When
        String taskId = executor.schedulePeriodicTask("plugin", () -> {}, 1000, 5000);

        // Then
        assertNotNull(taskId);
        assertTrue(taskId.startsWith("task-"));

        // Vérifier l'incrémentation
        String taskId2 = executor.schedulePeriodicTask("plugin", () -> {}, 1000, 5000);
        assertNotEquals(taskId, taskId2);
    }

    @Test
    void stubAsyncExecutor_GetThreadPoolSize_ShouldReturnOne_AndKillMutant() {
        // Given - Pour tuer le mutant ligne 311 (PRIMITIVE_RETURNS)
        PluginAsyncTaskExecutor executor = PluginServiceInitializer.StubServices.ASYNC_EXECUTOR;

        // When
        int poolSize = executor.getThreadPoolSize("plugin");

        // Then
        assertEquals(1, poolSize); // Le mutant remplacerait par 0
    }

    @Test
    void stubAsyncExecutor_GetStatistics_ShouldReturnEmptyMap_AndKillMutant() {
        // Given - Pour tuer le mutant ligne 312 (EMPTY_RETURNS)
        PluginAsyncTaskExecutor executor = PluginServiceInitializer.StubServices.ASYNC_EXECUTOR;

        // When
        var stats = executor.getStatistics("plugin");

        // Then
        assertNotNull(stats);
        assertTrue(stats.isEmpty());
    }

    @Test
    void stubConfigValidator_ValidateValue_ShouldReturnValidResult_AndKillMutant() {
        // Given - Pour tuer le mutant ligne 318 (NULL_RETURNS)
        PluginConfigurationValidator validator = PluginServiceInitializer.StubServices.CONFIG_VALIDATOR;

        // When
        var result = validator.validateValue("plugin", "path", "value");

        // Then
        assertNotNull(result);
        assertTrue(result.isValid());
    }

    @Test
    void stubConfigValidator_GetCustomValidators_ShouldReturnEmptyMap_AndKillMutant() {
        // Given - Pour tuer le mutant ligne 322 (EMPTY_RETURNS)
        PluginConfigurationValidator validator = PluginServiceInitializer.StubServices.CONFIG_VALIDATOR;

        // When
        var validators = validator.getCustomValidators("plugin");

        // Then
        assertNotNull(validators);
        assertTrue(validators.isEmpty());
    }

    @Test
    void stubConfigValidator_MergeWithDefaults_ShouldReturnPartialConfig_AndKillMutant() {
        // Given - Pour tuer le mutant ligne 324 (EMPTY_RETURNS)
        PluginConfigurationValidator validator = PluginServiceInitializer.StubServices.CONFIG_VALIDATOR;
        java.util.Map<String, Object> partial = new java.util.HashMap<>();
        partial.put("key", "value");

        // When
        var merged = validator.mergeWithDefaults("plugin", partial);

        // Then
        assertNotNull(merged);
        assertEquals("value", merged.get("key"));
        assertEquals(1, merged.size());
    }

    @Test
    void stubConfigValidator_GenerateSampleConfiguration_ShouldReturnEmptyMap_AndKillMutant() {
        // Given - Pour tuer le mutant ligne 325 (EMPTY_RETURNS)
        PluginConfigurationValidator validator = PluginServiceInitializer.StubServices.CONFIG_VALIDATOR;

        // When
        var sample = validator.generateSampleConfiguration("plugin");

        // Then
        assertNotNull(sample);
        assertTrue(sample.isEmpty());
    }

    @Test
    void stubConfigValidator_GetValidationRules_ShouldReturnEmptyMap_AndKillMutant() {
        // Given - Pour tuer le mutant ligne 326 (EMPTY_RETURNS)
        PluginConfigurationValidator validator = PluginServiceInitializer.StubServices.CONFIG_VALIDATOR;

        // When
        var rules = validator.getValidationRules("plugin");

        // Then
        assertNotNull(rules);
        assertTrue(rules.isEmpty());
    }

    @Test
    void stubHookService_RegisterHookWithPriority_ShouldReturnHookId_AndKillMutant() {
        // Given - Pour tuer le mutant ligne 332 (NULL_RETURNS et INCREMENTS)
        PluginHookService hookService = PluginServiceInitializer.StubServices.HOOK_SERVICE;

        // When - Utiliser une lambda void (sans return)
        String hookId = hookService.registerHookWithPriority("plugin",
                PluginHookService.HookType.PRE_ENABLE, 10, data -> {});

        // Then
        assertNotNull(hookId);
        assertTrue(hookId.startsWith("hook-"));

        // Vérifier l'incrémentation
        String hookId2 = hookService.registerHookWithPriority("plugin",
                PluginHookService.HookType.PRE_ENABLE, 20, data -> {});
        assertNotEquals(hookId, hookId2);
    }

    @Test
    void stubDataStore_Retrieve_WithClass_ShouldReturnNullWhenTypeMismatch_AndKillMutant() {
        // Given - Pour tuer le mutant ligne 348 (REMOVE_CONDITIONALS)
        PluginDataStore dataStore = PluginServiceInitializer.StubServices.DATA_STORE;
        dataStore.store("plugin", "key", "stringValue");

        // When
        Integer result = dataStore.retrieve("plugin", "key", Integer.class);

        // Then
        assertNull(result); // Le cast échoue, retourne null
    }

    @Test
    void stubDataStore_Backup_ShouldReturnBackupId_AndKillMutant() {
        // Given - Pour tuer le mutant ligne 357 (EMPTY_RETURNS)
        PluginDataStore dataStore = PluginServiceInitializer.StubServices.DATA_STORE;

        // When
        String backupId = dataStore.backup("plugin");

        // Then
        assertNotNull(backupId);
        assertTrue(backupId.startsWith("backup-"));
        assertFalse(backupId.isEmpty());
    }

    @Test
    void stubDataStore_Restore_ShouldReturnTrue_AndKillMutant() {
        // Given - Pour tuer le mutant ligne 358 (FALSE_RETURNS)
        PluginDataStore dataStore = PluginServiceInitializer.StubServices.DATA_STORE;

        // When
        boolean result = dataStore.restore("plugin", "backup-123");

        // Then
        assertTrue(result); // Le mutant remplacerait par false
    }

    @Test
    void stubDataStore_DeleteBackup_ShouldReturnTrue_AndKillMutant() {
        // Given - Pour tuer le mutant ligne 360 (FALSE_RETURNS)
        PluginDataStore dataStore = PluginServiceInitializer.StubServices.DATA_STORE;

        // When
        boolean result = dataStore.deleteBackup("plugin", "backup-123");

        // Then
        assertTrue(result); // Le mutant remplacerait par false
    }

    @Test
    void stubDataStore_GetStatistics_ShouldReturnEmptyMap_AndKillMutant() {
        // Given - Pour tuer le mutant ligne 361 (EMPTY_RETURNS)
        PluginDataStore dataStore = PluginServiceInitializer.StubServices.DATA_STORE;

        // When
        var stats = dataStore.getStatistics("plugin");

        // Then
        assertNotNull(stats);
        assertTrue(stats.isEmpty());
    }

    @Test
    void stubResourceManager_RegisterResourceWithMetadata_ShouldReturnTrue_AndKillMutant() {
        // Given - Pour tuer le mutant ligne 366 (FALSE_RETURNS)
        PluginResourceManager resourceManager = PluginServiceInitializer.StubServices.RESOURCE_MANAGER;

        // When
        boolean result = resourceManager.registerResourceWithMetadata("plugin", "resId", "name",
                "description", "type", new Object(), new java.util.HashMap<>());

        // Then
        assertTrue(result); // Le mutant remplacerait par false
    }

    @Test
    void stubResourceManager_RevokeResourceAccess_ShouldReturnTrue_AndKillMutant() {
        // Given - Pour tuer le mutant ligne 374 (FALSE_RETURNS)
        PluginResourceManager resourceManager = PluginServiceInitializer.StubServices.RESOURCE_MANAGER;

        // When
        boolean result = resourceManager.revokeResourceAccess("plugin", "resId");

        // Then
        assertTrue(result); // Le mutant remplacerait par false
    }

    @Test
    void stubResourceManager_UpdateResource_ShouldReturnTrue_AndKillMutant() {
        // Given - Pour tuer le mutant ligne 377 (FALSE_RETURNS)
        PluginResourceManager resourceManager = PluginServiceInitializer.StubServices.RESOURCE_MANAGER;

        // When
        boolean result = resourceManager.updateResource("plugin", "resId", "newValue");

        // Then
        assertTrue(result); // Le mutant remplacerait par false
    }

    @Test
    void stubDependencyResolver_RemoveDependency_ShouldReturnTrue_AndKillMutant() {
        // Given - Pour tuer le mutant ligne 384 (FALSE_RETURNS)
        PluginDependencyResolver resolver = PluginServiceInitializer.StubServices.DEPENDENCY_RESOLVER;

        // When
        boolean result = resolver.removeDependency("plugin", "depId");

        // Then
        assertTrue(result); // Le mutant remplacerait par false
    }

    @Test
    void stubDependencyResolver_ValidateDependencies_ShouldReturnEmptyMap_AndKillMutant() {
        // Given - Pour tuer le mutant ligne 394 (EMPTY_RETURNS)
        PluginDependencyResolver resolver = PluginServiceInitializer.StubServices.DEPENDENCY_RESOLVER;

        // When
        var result = resolver.validateDependencies("plugin");

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void stubDependencyResolver_GetDependencyGraph_ShouldReturnEmptyMap_AndKillMutant() {
        // Given - Pour tuer le mutant ligne 395 (EMPTY_RETURNS)
        PluginDependencyResolver resolver = PluginServiceInitializer.StubServices.DEPENDENCY_RESOLVER;

        // When
        var graph = resolver.getDependencyGraph("plugin");

        // Then
        assertNotNull(graph);
        assertTrue(graph.isEmpty());
    }

    @Test
    void stubUpdateService_RollbackVersion_ShouldReturnTrue_AndKillMutant() {
        // Given - Pour tuer le mutant ligne 405 (FALSE_RETURNS)
        PluginUpdateService updateService = PluginServiceInitializer.StubServices.UPDATE_SERVICE;

        // When
        boolean result = updateService.rollbackVersion("plugin", "1.0.0");

        // Then
        assertTrue(result); // Le mutant remplacerait par false
    }

    @Test
    void stubMonitoringService_GetGlobalStatistics_ShouldReturnEmptyMap_AndKillMutant() {
        // Given - Pour tuer le mutant ligne 431 (EMPTY_RETURNS)
        PluginMonitoringService monitoringService = PluginServiceInitializer.StubServices.MONITORING_SERVICE;

        // When
        var stats = monitoringService.getGlobalStatistics();

        // Then
        assertNotNull(stats);
        assertTrue(stats.isEmpty());
    }

    @Test
    void stubCacheService_Get_WithClass_WhenValueIsInstance_ShouldReturnValue() {
        // Given - Pour tester le chemin où l'instance correspond (ligne 229)
        PluginCacheService cacheService = PluginServiceInitializer.StubServices.CACHE_SERVICE;
        cacheService.put("plugin", "key", "stringValue");

        // When
        String result = cacheService.get("plugin", "key", String.class);

        // Then
        assertEquals("stringValue", result);
    }

    @Test
    void stubHookService_GetHookData_ShouldReturnOriginalMap() {
        // Given - Pour tuer le mutant ligne 335 (EMPTY_RETURNS sur getHookData)
        PluginHookService hookService = PluginServiceInitializer.StubServices.HOOK_SERVICE;
        java.util.Map<String, Object> hookData = new java.util.HashMap<>();
        hookData.put("test", "value");

        // When
        var context = hookService.executeHooks("plugin", PluginHookService.HookType.PRE_ENABLE, hookData);

        // Then
        assertNotNull(context.getHookData());
        assertEquals("value", context.getHookData().get("test"));
    }
}