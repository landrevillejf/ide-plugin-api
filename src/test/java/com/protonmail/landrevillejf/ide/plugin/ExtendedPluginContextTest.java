package com.protonmail.landrevillejf.ide.plugin;

import com.protonmail.landrevillejf.ide.plugin.service.*;
import com.protonmail.landrevillejf.ide.plugin.ui.ComponentRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExtendedPluginContextTest {

    @Mock
    private PluginServiceLocator serviceLocator;

    @Mock
    private PluginLoggingService loggingService;

    @Mock
    private PluginCacheService cacheService;

    @Mock
    private PluginNotificationService notificationService;

    @Mock
    private PluginMetricsService metricsService;

    @Mock
    private PluginPermissionService permissionService;

    @Mock
    private PluginAsyncTaskExecutor asyncTaskExecutor;

    @Mock
    private PluginConfigurationValidator configurationValidator;

    @Mock
    private PluginHookService hookService;

    @Mock
    private PluginDataStore dataStore;

    @Mock
    private PluginResourceManager resourceManager;

    @Mock
    private PluginDependencyResolver dependencyResolver;

    @Mock
    private PluginUpdateService updateService;

    @Mock
    private PluginMonitoringService monitoringService;

    @Mock
    private PluginManager pluginManager;

    @Mock
    private PluginEventBus eventBus;

    @Mock
    private ComponentRegistry componentRegistry;

    private ExtendedPluginContext context;

    // Implementation correcte de TestExtendedPluginContext
    private static class TestExtendedPluginContext implements ExtendedPluginContext {
        private final PluginServiceLocator serviceLocator;
        private PluginManager pluginManager;
        private final PluginEventBus eventBus;
        private final ComponentRegistry componentRegistry;
        private final String pluginId = "test-plugin";
        private final File pluginDataPath = new File(System.getProperty("java.io.tmpdir"), "test-plugin");

        public TestExtendedPluginContext(PluginServiceLocator serviceLocator) {
            this.serviceLocator = serviceLocator;
            this.eventBus = new PluginEventBus();
            this.componentRegistry = new ComponentRegistry();
        }

        @Override
        public PluginServiceLocator getServiceLocator() {
            return serviceLocator;
        }

        @Override
        public void setPluginManager(PluginManager pluginManager) {
            this.pluginManager = pluginManager;
        }

        @Override
        public <T> void unregisterService(Class<T> serviceClass) {
            // Implementation vide mais non-nulle
        }

        @Override
        public void registerService(Object service) {
            // Implementation vide mais non-nulle
        }

        @Override
        public PluginManager getPluginManager() {
            return pluginManager;
        }

        @Override
        public PluginEventBus getEventBus() {
            return eventBus;
        }

        @Override
        public void showNotification(String title, String message) {
            // Implementation vide mais non-nulle
        }

        @Override
        public void logDebug(String s) {
            // Implementation vide mais non-nulle
        }

        @Override
        public ComponentRegistry getComponentRegistry() {
            return componentRegistry;
        }

        @Override
        public String getPluginId() {
            return "";
        }

        @Override
        public Plugin getPlugin() {
            return null;
        }

        @Override
        public File getPluginDataDirectory() {
            return null;
        }

        @Override
        public void logInfo(String message) {
            // Implementation vide mais non-nulle
        }

        @Override
        public void logWarning(String message) {
            // Implementation vide mais non-nulle
        }

        @Override
        public void logError(String message, Throwable throwable) {
            // Implementation vide mais non-nulle
        }

        @Override
        public String getPluginDataPath() {
            return pluginDataPath.getAbsolutePath();
        }
    }

    @BeforeEach
    void setUp() {
        context = new TestExtendedPluginContext(serviceLocator);
    }

    @Test
    void getServiceLocator_ShouldReturnServiceLocator() {
        // When
        PluginServiceLocator result = context.getServiceLocator();

        // Then
        assertSame(serviceLocator, result);
    }

    @Test
    void getLoggingService_ShouldDelegateToServiceLocator() {
        // Given
        when(serviceLocator.getLoggingService()).thenReturn(loggingService);

        // When
        PluginLoggingService result = context.getLoggingService();

        // Then
        assertSame(loggingService, result);
        verify(serviceLocator).getLoggingService();
    }

    @Test
    void getLoggingService_ShouldReturnNull_WhenServiceNotFound() {
        // Given
        when(serviceLocator.getLoggingService()).thenReturn(null);

        // When
        PluginLoggingService result = context.getLoggingService();

        // Then
        assertNull(result);
        verify(serviceLocator).getLoggingService();
    }

    @Test
    void getCacheService_ShouldDelegateToServiceLocator() {
        // Given
        when(serviceLocator.getCacheService()).thenReturn(cacheService);

        // When
        PluginCacheService result = context.getCacheService();

        // Then
        assertSame(cacheService, result);
        verify(serviceLocator).getCacheService();
    }

    @Test
    void getNotificationService_ShouldDelegateToServiceLocator() {
        // Given
        when(serviceLocator.getNotificationService()).thenReturn(notificationService);

        // When
        PluginNotificationService result = context.getNotificationService();

        // Then
        assertSame(notificationService, result);
        verify(serviceLocator).getNotificationService();
    }

    @Test
    void getMetricsService_ShouldDelegateToServiceLocator() {
        // Given
        when(serviceLocator.getMetricsService()).thenReturn(metricsService);

        // When
        PluginMetricsService result = context.getMetricsService();

        // Then
        assertSame(metricsService, result);
        verify(serviceLocator).getMetricsService();
    }

    @Test
    void getPermissionService_ShouldDelegateToServiceLocator() {
        // Given
        when(serviceLocator.getPermissionService()).thenReturn(permissionService);

        // When
        PluginPermissionService result = context.getPermissionService();

        // Then
        assertSame(permissionService, result);
        verify(serviceLocator).getPermissionService();
    }

    @Test
    void getAsyncTaskExecutor_ShouldDelegateToServiceLocator() {
        // Given
        when(serviceLocator.getAsyncTaskExecutor()).thenReturn(asyncTaskExecutor);

        // When
        PluginAsyncTaskExecutor result = context.getAsyncTaskExecutor();

        // Then
        assertSame(asyncTaskExecutor, result);
        verify(serviceLocator).getAsyncTaskExecutor();
    }

    @Test
    void getConfigurationValidator_ShouldDelegateToServiceLocator() {
        // Given
        when(serviceLocator.getConfigurationValidator()).thenReturn(configurationValidator);

        // When
        PluginConfigurationValidator result = context.getConfigurationValidator();

        // Then
        assertSame(configurationValidator, result);
        verify(serviceLocator).getConfigurationValidator();
    }

    @Test
    void getHookService_ShouldDelegateToServiceLocator() {
        // Given
        when(serviceLocator.getHookService()).thenReturn(hookService);

        // When
        PluginHookService result = context.getHookService();

        // Then
        assertSame(hookService, result);
        verify(serviceLocator).getHookService();
    }

    @Test
    void getDataStore_ShouldDelegateToServiceLocator() {
        // Given
        when(serviceLocator.getDataStore()).thenReturn(dataStore);

        // When
        PluginDataStore result = context.getDataStore();

        // Then
        assertSame(dataStore, result);
        verify(serviceLocator).getDataStore();
    }

    @Test
    void getResourceManager_ShouldDelegateToServiceLocator() {
        // Given
        when(serviceLocator.getResourceManager()).thenReturn(resourceManager);

        // When
        PluginResourceManager result = context.getResourceManager();

        // Then
        assertSame(resourceManager, result);
        verify(serviceLocator).getResourceManager();
    }

    @Test
    void getDependencyResolver_ShouldDelegateToServiceLocator() {
        // Given
        when(serviceLocator.getDependencyResolver()).thenReturn(dependencyResolver);

        // When
        PluginDependencyResolver result = context.getDependencyResolver();

        // Then
        assertSame(dependencyResolver, result);
        verify(serviceLocator).getDependencyResolver();
    }

    @Test
    void getUpdateService_ShouldDelegateToServiceLocator() {
        // Given
        when(serviceLocator.getUpdateService()).thenReturn(updateService);

        // When
        PluginUpdateService result = context.getUpdateService();

        // Then
        assertSame(updateService, result);
        verify(serviceLocator).getUpdateService();
    }

    @Test
    void getMonitoringService_ShouldDelegateToServiceLocator() {
        // Given
        when(serviceLocator.getMonitoringService()).thenReturn(monitoringService);

        // When
        PluginMonitoringService result = context.getMonitoringService();

        // Then
        assertSame(monitoringService, result);
        verify(serviceLocator).getMonitoringService();
    }

    @Test
    void getService_ShouldDelegateToServiceLocator() {
        // Given
        Object customService = new Object();
        when(serviceLocator.getService(Object.class)).thenReturn(customService);

        // When
        Object result = context.getService(Object.class);

        // Then
        assertSame(customService, result);
        verify(serviceLocator).getService(Object.class);
    }

    @Test
    void getService_ShouldReturnNull_WhenServiceNotFound() {
        // Given
        when(serviceLocator.getService(String.class)).thenReturn(null);

        // When
        String result = context.getService(String.class);

        // Then
        assertNull(result);
        verify(serviceLocator).getService(String.class);
    }

    @Test
    void registerService_ShouldDelegateToServiceLocator() {
        // Given
        Object serviceImpl = new Object();

        // When
        context.registerService(Object.class, serviceImpl);

        // Then
        verify(serviceLocator).registerService(Object.class, serviceImpl);
    }

    @Test
    void registerService_ShouldAllowOverriding() {
        // Given
        Object serviceImpl1 = new Object();
        Object serviceImpl2 = new Object();

        // When
        context.registerService(Object.class, serviceImpl1);
        context.registerService(Object.class, serviceImpl2);

        // Then
        verify(serviceLocator).registerService(Object.class, serviceImpl1);
        verify(serviceLocator).registerService(Object.class, serviceImpl2);
    }

    @Test
    void setPluginManager_ShouldStorePluginManager() {
        // Given
        TestExtendedPluginContext testContext = (TestExtendedPluginContext) context;

        // When
        testContext.setPluginManager(pluginManager);

        // Then
        assertSame(pluginManager, testContext.getPluginManager());
    }

    @Test
    void setPluginManager_ShouldAcceptNull() {
        // Given
        TestExtendedPluginContext testContext = (TestExtendedPluginContext) context;

        // When
        testContext.setPluginManager(null);

        // Then
        assertNull(testContext.getPluginManager());
    }

    @Test
    void getPluginManager_ShouldReturnSetValue() {
        // Given
        TestExtendedPluginContext testContext = (TestExtendedPluginContext) context;
        testContext.setPluginManager(pluginManager);

        // When
        PluginManager result = testContext.getPluginManager();

        // Then
        assertSame(pluginManager, result);
    }

    @Test
    void getEventBus_ShouldReturnNonNullEventBus() {
        // When
        PluginEventBus result = context.getEventBus();

        // Then
        assertNotNull(result);
    }

    @Test
    void getComponentRegistry_ShouldReturnNonNullComponentRegistry() {
        // When
        ComponentRegistry result = context.getComponentRegistry();

        // Then
        assertNotNull(result);
    }

    @Test
    void getPluginDataPath_ShouldReturnStringPath() {
        // When
        String result = context.getPluginDataPath();

        // Then
        assertNotNull(result);
        assertTrue(result.contains("test-plugin"));
    }

    @Test
    void showNotification_ShouldNotThrowException() {
        // When/Then
        assertDoesNotThrow(() -> context.showNotification("Title", "Message"));
    }

    @Test
    void logDebug_ShouldNotThrowException() {
        // When/Then
        assertDoesNotThrow(() -> context.logDebug("Debug message"));
    }

    @Test
    void logInfo_ShouldNotThrowException() {
        // When/Then
        assertDoesNotThrow(() -> context.logInfo("Info message"));
    }

    @Test
    void logWarning_ShouldNotThrowException() {
        // When/Then
        assertDoesNotThrow(() -> context.logWarning("Warning message"));
    }

    @Test
    void logError_ShouldNotThrowException() {
        // When/Then
        assertDoesNotThrow(() -> context.logError("Error message", new RuntimeException()));
    }

    @Test
    void logError_WithNullThrowable_ShouldNotThrowException() {
        // When/Then
        assertDoesNotThrow(() -> context.logError("Error message", null));
    }

    @Test
    void unregisterService_ShouldNotThrowException() {
        // When/Then
        assertDoesNotThrow(() -> context.unregisterService(String.class));
    }

    @Test
    void registerServiceByObject_ShouldNotThrowException() {
        // When/Then
        assertDoesNotThrow(() -> context.registerService(new Object()));
    }

    @Test
    void allDefaultMethods_ShouldAllDelegateToServiceLocator() {
        // Verify that all default methods properly delegate
        when(serviceLocator.getLoggingService()).thenReturn(loggingService);
        when(serviceLocator.getCacheService()).thenReturn(cacheService);
        when(serviceLocator.getNotificationService()).thenReturn(notificationService);
        when(serviceLocator.getMetricsService()).thenReturn(metricsService);
        when(serviceLocator.getPermissionService()).thenReturn(permissionService);
        when(serviceLocator.getAsyncTaskExecutor()).thenReturn(asyncTaskExecutor);
        when(serviceLocator.getConfigurationValidator()).thenReturn(configurationValidator);
        when(serviceLocator.getHookService()).thenReturn(hookService);
        when(serviceLocator.getDataStore()).thenReturn(dataStore);
        when(serviceLocator.getResourceManager()).thenReturn(resourceManager);
        when(serviceLocator.getDependencyResolver()).thenReturn(dependencyResolver);
        when(serviceLocator.getUpdateService()).thenReturn(updateService);
        when(serviceLocator.getMonitoringService()).thenReturn(monitoringService);

        assertSame(loggingService, context.getLoggingService());
        assertSame(cacheService, context.getCacheService());
        assertSame(notificationService, context.getNotificationService());
        assertSame(metricsService, context.getMetricsService());
        assertSame(permissionService, context.getPermissionService());
        assertSame(asyncTaskExecutor, context.getAsyncTaskExecutor());
        assertSame(configurationValidator, context.getConfigurationValidator());
        assertSame(hookService, context.getHookService());
        assertSame(dataStore, context.getDataStore());
        assertSame(resourceManager, context.getResourceManager());
        assertSame(dependencyResolver, context.getDependencyResolver());
        assertSame(updateService, context.getUpdateService());
        assertSame(monitoringService, context.getMonitoringService());

        verify(serviceLocator, times(1)).getLoggingService();
        verify(serviceLocator, times(1)).getCacheService();
        verify(serviceLocator, times(1)).getNotificationService();
        verify(serviceLocator, times(1)).getMetricsService();
        verify(serviceLocator, times(1)).getPermissionService();
        verify(serviceLocator, times(1)).getAsyncTaskExecutor();
        verify(serviceLocator, times(1)).getConfigurationValidator();
        verify(serviceLocator, times(1)).getHookService();
        verify(serviceLocator, times(1)).getDataStore();
        verify(serviceLocator, times(1)).getResourceManager();
        verify(serviceLocator, times(1)).getDependencyResolver();
        verify(serviceLocator, times(1)).getUpdateService();
        verify(serviceLocator, times(1)).getMonitoringService();
    }
}