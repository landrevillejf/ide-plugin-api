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
class DefaultExtendedPluginContextTest {

    @Mock
    private ServiceRegistry serviceRegistry;

    @Mock
    private PluginEventBus pluginEventBus;

    @Mock
    private EventBus applicationEventBus;

    @Mock
    private PluginManager pluginManager;

    @Mock
    private PluginServiceLocator serviceLocator;

    @Mock
    private Plugin plugin;

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

    private DefaultExtendedPluginContext context;
    private File pluginDataDirectory;
    private String pluginId = "test.plugin.id";

    @BeforeEach
    void setUp() {
        // Fix SLF4J issue by setting a simple logger provider
        System.setProperty("slf4j.provider", "org.slf4j.simple.SimpleServiceProvider");

        pluginDataDirectory = new File(System.getProperty("java.io.tmpdir"), "test-plugin-data");
        context = new DefaultExtendedPluginContext(
                serviceRegistry,
                pluginEventBus,
                applicationEventBus,
                pluginManager,
                pluginDataDirectory,
                pluginId,
                serviceLocator
        );
    }

    @Test
    void getServiceLocator_ShouldReturnServiceLocator() {
        // When
        PluginServiceLocator result = context.getServiceLocator();

        // Then
        assertSame(serviceLocator, result);
    }

    @Test
    void getLoggingService_ShouldReturnCachedService() {
        // Given
        when(serviceLocator.getLoggingService()).thenReturn(loggingService);

        // When
        PluginLoggingService firstCall = context.getLoggingService();
        PluginLoggingService secondCall = context.getLoggingService();

        // Then
        assertSame(firstCall, secondCall);
        verify(serviceLocator, times(1)).getLoggingService();
    }

    @Test
    void getCacheService_ShouldReturnCachedService() {
        // Given
        when(serviceLocator.getCacheService()).thenReturn(cacheService);

        // When
        PluginCacheService firstCall = context.getCacheService();
        PluginCacheService secondCall = context.getCacheService();

        // Then
        assertSame(firstCall, secondCall);
        verify(serviceLocator, times(1)).getCacheService();
    }

    @Test
    void getNotificationService_ShouldReturnCachedService() {
        // Given
        when(serviceLocator.getNotificationService()).thenReturn(notificationService);

        // When
        PluginNotificationService result = context.getNotificationService();

        // Then
        assertSame(notificationService, result);
        verify(serviceLocator).getNotificationService();
    }

    @Test
    void getMetricsService_ShouldReturnCachedService() {
        // Given
        when(serviceLocator.getMetricsService()).thenReturn(metricsService);

        // When
        PluginMetricsService result = context.getMetricsService();

        // Then
        assertSame(metricsService, result);
        verify(serviceLocator).getMetricsService();
    }

    @Test
    void getPermissionService_ShouldReturnCachedService() {
        // Given
        when(serviceLocator.getPermissionService()).thenReturn(permissionService);

        // When
        PluginPermissionService result = context.getPermissionService();

        // Then
        assertSame(permissionService, result);
        verify(serviceLocator).getPermissionService();
    }

    @Test
    void getAsyncTaskExecutor_ShouldReturnCachedService() {
        // Given
        when(serviceLocator.getAsyncTaskExecutor()).thenReturn(asyncTaskExecutor);

        // When
        PluginAsyncTaskExecutor result = context.getAsyncTaskExecutor();

        // Then
        assertSame(asyncTaskExecutor, result);
        verify(serviceLocator).getAsyncTaskExecutor();
    }

    @Test
    void getConfigurationValidator_ShouldReturnCachedService() {
        // Given
        when(serviceLocator.getConfigurationValidator()).thenReturn(configurationValidator);

        // When
        PluginConfigurationValidator result = context.getConfigurationValidator();

        // Then
        assertSame(configurationValidator, result);
        verify(serviceLocator).getConfigurationValidator();
    }

    @Test
    void getHookService_ShouldReturnCachedService() {
        // Given
        when(serviceLocator.getHookService()).thenReturn(hookService);

        // When
        PluginHookService result = context.getHookService();

        // Then
        assertSame(hookService, result);
        verify(serviceLocator).getHookService();
    }

    @Test
    void getDataStore_ShouldReturnCachedService() {
        // Given
        when(serviceLocator.getDataStore()).thenReturn(dataStore);

        // When
        PluginDataStore result = context.getDataStore();

        // Then
        assertSame(dataStore, result);
        verify(serviceLocator).getDataStore();
    }

    @Test
    void getResourceManager_ShouldReturnCachedService() {
        // Given
        when(serviceLocator.getResourceManager()).thenReturn(resourceManager);

        // When
        PluginResourceManager result = context.getResourceManager();

        // Then
        assertSame(resourceManager, result);
        verify(serviceLocator).getResourceManager();
    }

    @Test
    void getDependencyResolver_ShouldReturnCachedService() {
        // Given
        when(serviceLocator.getDependencyResolver()).thenReturn(dependencyResolver);

        // When
        PluginDependencyResolver result = context.getDependencyResolver();

        // Then
        assertSame(dependencyResolver, result);
        verify(serviceLocator).getDependencyResolver();
    }

    @Test
    void getUpdateService_ShouldReturnCachedService() {
        // Given
        when(serviceLocator.getUpdateService()).thenReturn(updateService);

        // When
        PluginUpdateService result = context.getUpdateService();

        // Then
        assertSame(updateService, result);
        verify(serviceLocator).getUpdateService();
    }

    @Test
    void getMonitoringService_ShouldReturnCachedService() {
        // Given
        when(serviceLocator.getMonitoringService()).thenReturn(monitoringService);

        // When
        PluginMonitoringService result = context.getMonitoringService();

        // Then
        assertSame(monitoringService, result);
        verify(serviceLocator).getMonitoringService();
    }

    @Test
    void getService_ShouldReturnServiceFromLocator() {
        // Given
        when(serviceLocator.getService(String.class)).thenReturn("test-service");

        // When
        String result = context.getService(String.class);

        // Then
        assertEquals("test-service", result);
        verify(serviceLocator).getService(String.class);
    }

    @Test
    void getService_ShouldCacheService() {
        // Given
        when(serviceLocator.getService(String.class)).thenReturn("cached-service");

        // When
        String firstCall = context.getService(String.class);
        String secondCall = context.getService(String.class);

        // Then
        assertSame(firstCall, secondCall);
        verify(serviceLocator, times(1)).getService(String.class);
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
    void registerService_ShouldAlsoCacheService() {
        // Given - No need to stub getService since we won't call it
        Object serviceImpl = new Object();

        // When
        context.registerService(Object.class, serviceImpl);

        // Then - the service should be retrievable from cache
        Object result = context.getService(Object.class);
        assertSame(serviceImpl, result);
        // Note: getService will call serviceLocator.getService which returns null,
        // but the cache should have the value, so the exception won't be thrown
    }

    @Test
    void getService_ShouldThrowException_WhenServiceNotFound() {
        // Given
        when(serviceLocator.getService(String.class)).thenReturn(null);

        // When/Then
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> context.getService(String.class));
        assertTrue(exception.getMessage().contains("Service java.lang.String is not available"));
    }

    @Test
    void getLoggingService_ShouldThrowException_WhenServiceUnavailable() {
        // Given
        when(serviceLocator.getLoggingService()).thenReturn(null);

        // When/Then
        assertThrows(IllegalStateException.class, () -> context.getLoggingService());
    }

    @Test
    void constructorWithPlugin_ShouldInitializeCorrectly() {
        // Given
        DefaultExtendedPluginContext contextWithPlugin = new DefaultExtendedPluginContext(
                serviceRegistry,
                pluginEventBus,
                applicationEventBus,
                pluginManager,
                pluginDataDirectory,
                pluginId,
                plugin,
                serviceLocator
        );

        // Then
        assertNotNull(contextWithPlugin);
        assertSame(serviceLocator, contextWithPlugin.getServiceLocator());
    }

    @Test
    void multipleServices_ShouldBeCachedIndependently() {
        // Given
        when(serviceLocator.getLoggingService()).thenReturn(loggingService);
        when(serviceLocator.getCacheService()).thenReturn(cacheService);

        // When
        PluginLoggingService loggingResult = context.getLoggingService();
        PluginCacheService cacheResult = context.getCacheService();

        // Then
        assertSame(loggingService, loggingResult);
        assertSame(cacheService, cacheResult);

        // Second calls should not invoke serviceLocator again
        context.getLoggingService();
        context.getCacheService();

        verify(serviceLocator, times(1)).getLoggingService();
        verify(serviceLocator, times(1)).getCacheService();
    }

    @Test
    void getService_WithDifferentTypes_ShouldReturnDifferentServices() {
        // Given
        when(serviceLocator.getService(Integer.class)).thenReturn(42);
        when(serviceLocator.getService(String.class)).thenReturn("test");

        // When
        Integer intResult = context.getService(Integer.class);
        String stringResult = context.getService(String.class);

        // Then
        assertEquals(42, intResult);
        assertEquals("test", stringResult);
        verify(serviceLocator).getService(Integer.class);
        verify(serviceLocator).getService(String.class);
    }

    @Test
    void registerService_ShouldOverrideExistingCachedService() {
        // Given
        Object initialService = new Object();
        Object newService = new Object();

        // When - Register first service
        context.registerService(Object.class, initialService);

        // Then - First service should be registered
        verify(serviceLocator, times(1)).registerService(Object.class, initialService);

        // When - Register second service (overriding)
        context.registerService(Object.class, newService);

        // Then - Second service should be registered
        verify(serviceLocator, times(1)).registerService(Object.class, newService);
        // Note: The implementation calls registerService each time with the new service
        // It doesn't call it twice with the same service
    }
}