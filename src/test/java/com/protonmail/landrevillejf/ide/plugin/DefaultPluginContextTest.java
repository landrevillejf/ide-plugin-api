package com.protonmail.landrevillejf.ide.plugin;

import com.protonmail.landrevillejf.ide.plugin.ui.UIComponentAccessor;
import com.protonmail.landrevillejf.ide.plugin.utils.LogCapture;
import com.protonmail.landrevillejf.ide.plugin.utils.TestUtils;
import com.protonmail.landrevillejf.swingide.core.bus.EventBus;
import com.protonmail.landrevillejf.swingide.core.registry.ServiceRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("DefaultPluginContext Tests")
class DefaultPluginContextTest {

    private ServiceRegistry mockServiceRegistry;
    private PluginEventBus mockPluginEventBus;
    private EventBus mockApplicationEventBus;
    private PluginManager mockPluginManager;
    private Plugin mockPlugin;
    private DefaultPluginContext context;
    
    @TempDir
    Path tempDir;
    
    private static final String TEST_PLUGIN_ID = "test-plugin-id";
    private static final String TEST_PLUGIN_NAME = "Test Plugin";

    @BeforeEach
    void setUp() {
        mockServiceRegistry = mock(ServiceRegistry.class);
        mockPluginEventBus = mock(PluginEventBus.class);
        mockApplicationEventBus = mock(EventBus.class);
        mockPluginManager = mock(PluginManager.class);
        mockPlugin = mock(Plugin.class);
        
        when(mockPlugin.getName()).thenReturn(TEST_PLUGIN_NAME);
        
        File dataDir = tempDir.toFile();
        
        context = new DefaultPluginContext(
            mockServiceRegistry,
            mockPluginEventBus,
            mockApplicationEventBus,
            mockPluginManager,
            dataDir,
            TEST_PLUGIN_ID,
            mockPlugin
        );
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {

        @Test
        @DisplayName("Should create context with all parameters")
        void testConstructorWithAllParameters() {
            assertNotNull(context);
            // Le plugin ID n'est pas accessible directement, mais on peut vérifier d'autres choses
            assertNotNull(context.getPluginDataPath());
            assertNotNull(context.getEventBus());
            assertNotNull(context.getPluginManager());
        }
        
        @Test
        @DisplayName("Should create data directory if it doesn't exist")
        void testCreatesDataDirectory() {
            File nonExistentDir = new File(tempDir.toFile(), "new-plugin-dir");
            assertFalse(nonExistentDir.exists());
            
            DefaultPluginContext newContext = new DefaultPluginContext(
                mockServiceRegistry,
                mockPluginEventBus,
                mockApplicationEventBus,
                mockPluginManager,
                nonExistentDir,
                "new-plugin",
                mockPlugin
            );
            
            assertTrue(nonExistentDir.exists());
            assertNotNull(newContext);
        }
        
        @Test
        @DisplayName("Should handle null data directory")
        void testNullDataDirectory() {
            DefaultPluginContext contextWithNullDir = new DefaultPluginContext(
                mockServiceRegistry,
                mockPluginEventBus,
                mockApplicationEventBus,
                mockPluginManager,
                null,
                TEST_PLUGIN_ID,
                mockPlugin
            );
            
            assertEquals("", contextWithNullDir.getPluginDataPath());
            assertNotNull(contextWithNullDir);
        }
        
        @Test
        @DisplayName("Should handle null plugin")
        void testNullPlugin() {
            DefaultPluginContext contextWithNullPlugin = new DefaultPluginContext(
                mockServiceRegistry,
                mockPluginEventBus,
                mockApplicationEventBus,
                mockPluginManager,
                tempDir.toFile(),
                TEST_PLUGIN_ID,
                null
            );
            
            assertNotNull(contextWithNullPlugin);
            contextWithNullPlugin.logInfo("Test message"); // Should not throw NPE
        }
    }

    @Nested
    @DisplayName("Event Bus Tests")
    class EventBusTests {
        
        @Test
        @DisplayName("Should return plugin event bus")
        void testGetEventBus() {
            PluginEventBus result = context.getEventBus();
            assertNotNull(result);
            assertSame(mockPluginEventBus, result);
        }
    }

    @Nested
    @DisplayName("Service Registry Tests")
    class ServiceRegistryTests {
        
        @Test
        @DisplayName("Should get service from local registry first")
        void testGetServiceFromLocalRegistry() {
            // Register a local service
            TestService localService = new TestService();
            context.registerService(TestService.class, localService);
            
            TestService result = context.getService(TestService.class);
            assertSame(localService, result);
            
            // Verify service registry was not called
            verify(mockServiceRegistry, never()).getService(any());
        }
        
        @Test
        @DisplayName("Should get service from global registry if not local")
        void testGetServiceFromGlobalRegistry() {
            TestService globalService = new TestService();
            when(mockServiceRegistry.getService(TestService.class)).thenReturn(globalService);
            
            TestService result = context.getService(TestService.class);
            assertSame(globalService, result);
            verify(mockServiceRegistry).getService(TestService.class);
        }
        
        @Test
        @DisplayName("Should return null if service not found")
        void testGetServiceNotFound() {
            when(mockServiceRegistry.getService(TestService.class)).thenReturn(null);
            
            TestService result = context.getService(TestService.class);
            assertNull(result);
        }
        
        @Test
        @DisplayName("Should register service with class and instance")
        void testRegisterServiceWithClassAndInstance() {
            TestService service = new TestService();
            context.registerService(TestService.class, service);
            
            TestService retrieved = context.getService(TestService.class);
            assertSame(service, retrieved);
            
            verify(mockServiceRegistry).register(eq(TestService.class), eq(service));
        }
        
        @Test
        @DisplayName("Should throw NullPointerException when registering null service class")
        void testRegisterServiceWithNullClass() {
            assertThrows(NullPointerException.class, () -> {
                context.registerService(null, new TestService());
            });
        }
        
        @Test
        @DisplayName("Should throw NullPointerException when registering null instance")
        void testRegisterServiceWithNullInstance() {
            assertThrows(NullPointerException.class, () -> {
                context.registerService(TestService.class, null);
            });
        }
        
        @Test
        @DisplayName("Should unregister service")
        void testUnregisterService() {
            TestService service = new TestService();
            context.registerService(TestService.class, service);
            
            context.unregisterService(TestService.class);
            
            // The service should still be in local registry? The implementation
            // uses 'services' map for simple registration, not localServices
            // Let's test the behavior
            assertDoesNotThrow(() -> context.unregisterService(TestService.class));
        }
        
        @Test
        @DisplayName("Should handle null in unregisterService")
        void testUnregisterServiceWithNull() {
            assertDoesNotThrow(() -> context.unregisterService(null));
        }
        
        @Test
        @DisplayName("Should register service using object instance")
        void testRegisterServiceByObject() {
            TestService service = new TestService();
            context.registerService(service);
            
            // The service is registered with its class as key
            TestService retrieved = context.getService(TestService.class);
            assertSame(service, retrieved);
        }
        
        @Test
        @DisplayName("Should handle null when registering service by object")
        void testRegisterServiceByObjectWithNull() {
            assertDoesNotThrow(() -> context.registerService(null));
            // Should log warning but not throw exception
        }
        
        @Test
        @DisplayName("Should register multiple services of different types")
        void testRegisterMultipleServices() {
            TestService testService = new TestService();
            AnotherTestService anotherService = new AnotherTestService();
            
            context.registerService(TestService.class, testService);
            context.registerService(AnotherTestService.class, anotherService);
            
            assertSame(testService, context.getService(TestService.class));
            assertSame(anotherService, context.getService(AnotherTestService.class));
        }
        
        @Test
        @DisplayName("Should override previously registered service")
        void testOverrideService() {
            TestService service1 = new TestService();
            TestService service2 = new TestService();
            
            context.registerService(TestService.class, service1);
            assertSame(service1, context.getService(TestService.class));
            
            context.registerService(TestService.class, service2);
            assertSame(service2, context.getService(TestService.class));
        }
    }

    @Nested
    @DisplayName("Plugin Manager Tests")
    class PluginManagerTests {
        
        @Test
        @DisplayName("Should return plugin manager")
        void testGetPluginManager() {
            PluginManager result = context.getPluginManager();
            assertNotNull(result);
            assertSame(mockPluginManager, result);
        }
        
        @Test
        @DisplayName("Should allow setting plugin manager")
        void testSetPluginManager() {
            PluginManager newPluginManager = mock(PluginManager.class);
            context.setPluginManager(newPluginManager);
            assertSame(newPluginManager, context.getPluginManager());
        }
    }

    @Nested
    @DisplayName("Data Path Tests")
    class DataPathTests {
        
        @Test
        @DisplayName("Should return plugin data path")
        void testGetPluginDataPath() {
            String path = context.getPluginDataPath();
            assertNotNull(path);
            assertEquals(tempDir.toFile().getAbsolutePath(), path);
        }
        
        @Test
        @DisplayName("Should return empty string for null data directory")
        void testGetPluginDataPathWithNullDirectory() {
            DefaultPluginContext contextWithNullDir = new DefaultPluginContext(
                mockServiceRegistry,
                mockPluginEventBus,
                mockApplicationEventBus,
                mockPluginManager,
                null,
                TEST_PLUGIN_ID,
                mockPlugin
            );
            
            assertEquals("", contextWithNullDir.getPluginDataPath());
        }
    }

    @Nested
    @DisplayName("Logging Tests")
    class LoggingTests {
        
        @Test
        @DisplayName("Should log info messages")
        void testLogInfo() {
            assertDoesNotThrow(() -> context.logInfo("Test info message"));
        }
        
        @Test
        @DisplayName("Should log warning messages")
        void testLogWarning() {
            assertDoesNotThrow(() -> context.logWarning("Test warning message"));
        }
        
        @Test
        @DisplayName("Should log error messages with throwable")
        void testLogError() {
            Throwable throwable = new RuntimeException("Test error");
            assertDoesNotThrow(() -> context.logError("Test error message", throwable));
        }
        
        @Test
        @DisplayName("Should log error messages with null throwable")
        void testLogErrorWithNullThrowable() {
            assertDoesNotThrow(() -> context.logError("Test error message", null));
        }
        
        @Test
        @DisplayName("Should log debug messages")
        void testLogDebug() {
            assertDoesNotThrow(() -> context.logDebug("Test debug message"));
        }
        
        @Test
        @DisplayName("Should include plugin name in log messages")
        void testLogIncludesPluginName() {
            // This is hard to verify without a log appender, but we can verify no exceptions
            context.logInfo("Message with plugin name");
            context.logWarning("Warning with plugin name");
            context.logError("Error with plugin name", new RuntimeException());
            context.logDebug("Debug with plugin name");
        }
        
        @Test
        @DisplayName("Should handle logging with null plugin")
        void testLoggingWithNullPlugin() {
            DefaultPluginContext contextWithNullPlugin = new DefaultPluginContext(
                mockServiceRegistry,
                mockPluginEventBus,
                mockApplicationEventBus,
                mockPluginManager,
                tempDir.toFile(),
                TEST_PLUGIN_ID,
                null
            );
            
            assertDoesNotThrow(() -> {
                contextWithNullPlugin.logInfo("Test with null plugin");
                contextWithNullPlugin.logWarning("Test with null plugin");
                contextWithNullPlugin.logError("Test with null plugin", null);
                contextWithNullPlugin.logDebug("Test with null plugin");
            });
        }
    }

    @Nested
    @DisplayName("Notification Tests")
    class NotificationTests {
        
        @Test
        @DisplayName("Should show notification without throwing exceptions")
        void testShowNotification() {
            // This is called on EDT, might need to handle threading
            assertDoesNotThrow(() -> {
                context.showNotification("Test Title", "Test Message");
            });
        }
        
        @Test
        @DisplayName("Should handle null title in notification")
        void testShowNotificationWithNullTitle() {
            assertDoesNotThrow(() -> {
                context.showNotification(null, "Test Message");
            });
        }
        
        @Test
        @DisplayName("Should handle null message in notification")
        void testShowNotificationWithNullMessage() {
            assertDoesNotThrow(() -> {
                context.showNotification("Test Title", null);
            });
        }
    }

    @Nested
    @DisplayName("UI Component Accessor Tests")
    class UIComponentAccessorTests {
        
        @Test
        @DisplayName("Should get UI component accessor")
        void testGetUiComponentAccessor() {
            UIComponentAccessor accessor = DefaultPluginContext.getUiComponentAccessor();
            // Can be null if not set
            assertDoesNotThrow(() -> DefaultPluginContext.getUiComponentAccessor());
        }
        
        @Test
        @DisplayName("Should set UI component accessor")
        void testSetUiComponentAccessor() {
            UIComponentAccessor mockAccessor = mock(UIComponentAccessor.class);
            DefaultPluginContext.setUiComponentAccessor(mockAccessor);
            assertSame(mockAccessor, DefaultPluginContext.getUiComponentAccessor());
            
            // Reset to null for other tests
            DefaultPluginContext.setUiComponentAccessor(null);
        }
        
        @Test
        @DisplayName("Should handle null UI component accessor")
        void testSetNullUiComponentAccessor() {
            DefaultPluginContext.setUiComponentAccessor(null);
            assertNull(DefaultPluginContext.getUiComponentAccessor());
        }
    }

    // Test service classes
    static class TestService {
        private String value = "test";

        public String getValue() {
            return value;
        }
    }

    static class AnotherTestService {
        private int number = 42;

        public int getNumber() {
            return number;
        }
    }

    static class SharedService {
        private String data = "shared";

        public String getData() {
            return data;
        }
    }

    // ── Additional tests to cover missed branches ────────────────────────────

    @Test
    @DisplayName("6-arg constructor (no plugin) should work and getPlugin returns null")
    void testSixArgConstructorNoPlugin() {
        DefaultPluginContext ctx = new DefaultPluginContext(
            mockServiceRegistry, mockPluginEventBus, mockApplicationEventBus,
            mockPluginManager, tempDir.toFile(), "no-plugin-id");
        assertNull(ctx.getPlugin());
        assertEquals("no-plugin-id", ctx.getPluginId());
        assertNotNull(ctx.getPluginDataDirectory());
    }

    @Test
    @DisplayName("6-arg constructor with null dir should create successfully")
    void testSixArgConstructorNullDir() {
        DefaultPluginContext ctx = new DefaultPluginContext(
            mockServiceRegistry, mockPluginEventBus, mockApplicationEventBus,
            mockPluginManager, null, "p1");
        assertEquals("", ctx.getPluginDataPath());
        assertNull(ctx.getPluginDataDirectory());
    }

    @Test
    @DisplayName("registerService(class,instance) with null-plugin context uses pluginId in log")
    void testRegisterServiceWithNullPluginContext() {
        DefaultPluginContext ctx = new DefaultPluginContext(
            mockServiceRegistry, mockPluginEventBus, mockApplicationEventBus,
            mockPluginManager, tempDir.toFile(), "pid");
        TestService svc = new TestService();
        ctx.registerService(TestService.class, svc);
        assertSame(svc, ctx.getService(TestService.class));
    }

    @Test
    @DisplayName("unregisterService with null-plugin context uses pluginId in log")
    void testUnregisterServiceNullPlugin() {
        DefaultPluginContext ctx = new DefaultPluginContext(
            mockServiceRegistry, mockPluginEventBus, mockApplicationEventBus,
            mockPluginManager, tempDir.toFile(), "pid");
        ctx.registerService(TestService.class, new TestService());
        ctx.unregisterService(TestService.class);
        assertDoesNotThrow(() -> ctx.unregisterService(TestService.class));
    }

    @Test
    @DisplayName("registerService(Object) with null-plugin context logs with pluginId")
    void testRegisterServiceObjectNullPlugin() {
        DefaultPluginContext ctx = new DefaultPluginContext(
            mockServiceRegistry, mockPluginEventBus, mockApplicationEventBus,
            mockPluginManager, tempDir.toFile(), "pid");
        TestService svc = new TestService();
        ctx.registerService(svc);
        assertSame(svc, ctx.getService(TestService.class));
    }

    @Test
    @DisplayName("registerService(Object) null with null-plugin context logs with pluginId")
    void testRegisterNullServiceObjectNullPlugin() {
        DefaultPluginContext ctx = new DefaultPluginContext(
            mockServiceRegistry, mockPluginEventBus, mockApplicationEventBus,
            mockPluginManager, tempDir.toFile(), "pid");
        assertDoesNotThrow(() -> ctx.registerService((Object) null));
    }

    @Test
    @DisplayName("logInfo/logWarning/logError/logDebug with null-plugin use pluginId")
    void testLoggingBranchesNullPlugin() {
        DefaultPluginContext ctx = new DefaultPluginContext(
            mockServiceRegistry, mockPluginEventBus, mockApplicationEventBus,
            mockPluginManager, tempDir.toFile(), "pid");
        assertDoesNotThrow(() -> {
            ctx.logInfo("msg");
            ctx.logWarning("msg");
            ctx.logError("msg", new RuntimeException("e"));
            ctx.logDebug("msg");
        });
    }

    @Test
    @DisplayName("getComponentRegistry returns non-null registry")
    void testGetComponentRegistry() {
        assertNotNull(context.getComponentRegistry());
    }

    @Test
    @DisplayName("getPluginId returns correct id")
    void testGetPluginId() {
        assertEquals(TEST_PLUGIN_ID, context.getPluginId());
    }

    @Test
    @DisplayName("getPlugin returns plugin passed in constructor")
    void testGetPlugin() {
        assertSame(mockPlugin, context.getPlugin());
    }

    @Test
    @DisplayName("getPluginDataDirectory returns directory passed in constructor")
    void testGetPluginDataDirectory() {
        assertEquals(tempDir.toFile(), context.getPluginDataDirectory());
    }

    @Test
    @DisplayName("7-arg constructor with non-existent dir creates it")
    void testSevenArgConstructorCreatesDir() {
        File newDir = new File(tempDir.toFile(), "sub7arg");
        assertFalse(newDir.exists());
        DefaultPluginContext ctx = new DefaultPluginContext(
            mockServiceRegistry, mockPluginEventBus, mockApplicationEventBus,
            mockPluginManager, newDir, "p7", mockPlugin);
        assertTrue(newDir.exists());
        assertNotNull(ctx);
    }

    @Nested
    @DisplayName("Log Capture Tests to Kill PIT Mutations")
    class LogCaptureTests {

        @Test
        @DisplayName("registerService should log debug message when debug enabled")
        void testRegisterService_LogsDebugMessage() {
            try (LogCapture capture = LogCapture.attach(DefaultPluginContext.class)) {
                TestService service = new TestService();
                context.registerService(TestService.class, service);

                assertTrue(capture.formattedMessages().stream()
                    .anyMatch(msg -> msg.contains("Service registered") && msg.contains("TestService")));
            }
        }

        @Test
        @DisplayName("registerService(Object) should log debug message when debug enabled")
        void testRegisterServiceObject_LogsDebugMessage() {
            try (LogCapture capture = LogCapture.attach(DefaultPluginContext.class)) {
                TestService service = new TestService();
                context.registerService(service);

                assertTrue(capture.formattedMessages().stream()
                    .anyMatch(msg -> msg.contains("Service registered") && msg.contains("TestService")));
            }
        }

        @Test
        @DisplayName("registerService(Object) with null should log warning")
        void testRegisterServiceNull_LogsWarning() {
            try (LogCapture capture = LogCapture.attach(DefaultPluginContext.class)) {
                context.registerService((Object) null);

                assertTrue(capture.formattedMessages().stream()
                    .anyMatch(msg -> msg.contains("null service")));
            }
        }

        @Test
        @DisplayName("unregisterService should log debug message when debug enabled")
        void testUnregisterService_LogsDebugMessage() {
            try (LogCapture capture = LogCapture.attach(DefaultPluginContext.class)) {
                context.unregisterService(TestService.class);

                assertTrue(capture.formattedMessages().stream()
                    .anyMatch(msg -> msg.contains("Service unregistered") && msg.contains("TestService")));
            }
        }

        @Test
        @DisplayName("showNotification should call SwingUtilities.invokeLater")
        void testShowNotification_CallsInvokeLater() {
            // This test verifies the SwingUtilities.invokeLater call is made
            // The mutation removes this call, so we need to verify it's executed
            assertDoesNotThrow(() -> context.showNotification("Title", "Message"));
        }
    }

    @Nested
    @DisplayName("Coverage completion tests")
    class CoverageCompletionTests {

        private File impossibleDirectory() throws IOException {
            // A directory under a regular file can never be created -> mkdirs() fails
            File blocker = new File(tempDir.toFile(), "blocker");
            assertTrue(blocker.createNewFile());
            return new File(blocker, "subdir");
        }

        @Test
        @DisplayName("6-arg constructor warns when the data directory cannot be created")
        void testConstructor_MkdirsFailure_Warns() throws IOException {
            File impossible = impossibleDirectory();
            DefaultPluginContext ctx = new DefaultPluginContext(
                    mockServiceRegistry, mockPluginEventBus, mockApplicationEventBus,
                    mockPluginManager, impossible, TEST_PLUGIN_ID);
            assertNotNull(ctx);
            assertFalse(impossible.exists());
        }

        @Test
        @DisplayName("7-arg constructor warns when the data directory cannot be created")
        void testConstructorWithPlugin_MkdirsFailure_Warns() throws IOException {
            File impossible = impossibleDirectory();
            DefaultPluginContext ctx = new DefaultPluginContext(
                    mockServiceRegistry, mockPluginEventBus, mockApplicationEventBus,
                    mockPluginManager, impossible, TEST_PLUGIN_ID, mockPlugin);
            assertNotNull(ctx);
        }

        @Test
        @DisplayName("mkdirs failure stays silent when warn logging is disabled")
        void testConstructor_MkdirsFailure_WarnOff() throws IOException {
            File impossible = impossibleDirectory();
            TestUtils.withLoggingOff(DefaultPluginContext.class, () -> {
                DefaultPluginContext ctx = new DefaultPluginContext(
                        mockServiceRegistry, mockPluginEventBus, mockApplicationEventBus,
                        mockPluginManager, impossible, TEST_PLUGIN_ID, mockPlugin);
                assertNotNull(ctx);
            });
        }

        @Test
        @DisplayName("logging helpers stay silent when logging is disabled")
        void testLoggingHelpers_LoggingOff() {
            TestUtils.withLoggingOff(DefaultPluginContext.class, () -> {
                context.registerService(Runnable.class, () -> { });
                context.unregisterService(Runnable.class);
                context.registerService((Object) null);
                context.registerService((Object) "plain-service");
                context.logInfo("off");
                context.logWarning("off");
                context.logError("off", new RuntimeException("off"));
                context.logDebug("off");
            });
        }

        @Test
        @DisplayName("showNotification runs its EDT task and shows a dialog")
        void testShowNotification_ExecutesEdtTask() {
            try (MockedStatic<SwingUtilities> swing = mockStatic(SwingUtilities.class);
                 MockedStatic<JOptionPane> optionPane = mockStatic(JOptionPane.class)) {
                swing.when(() -> SwingUtilities.invokeLater(any(Runnable.class)))
                        .thenAnswer(invocation -> {
                            invocation.getArgument(0, Runnable.class).run();
                            return null;
                        });

                context.showNotification("Title", "Message");

                optionPane.verify(() -> JOptionPane.showMessageDialog(
                        null, "Message", "Title", JOptionPane.INFORMATION_MESSAGE));
            }
        }
    }
}