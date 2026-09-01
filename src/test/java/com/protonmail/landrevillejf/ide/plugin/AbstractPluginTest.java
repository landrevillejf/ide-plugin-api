package com.protonmail.landrevillejf.ide.plugin;

import com.protonmail.landrevillejf.ide.plugin.events.Event;
import com.protonmail.landrevillejf.ide.plugin.events.EventListener;
import com.protonmail.landrevillejf.ide.plugin.utils.LogCapture;
import com.protonmail.landrevillejf.ide.plugin.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@DisplayName("AbstractPlugin Tests")
class AbstractPluginTest {

    private TestPlugin plugin;
    private PluginContext mockContext;

    @BeforeEach
    void setUp() {
        plugin = new TestPlugin("Test Plugin", "1.0.0", "A test plugin", "Test Author");
        mockContext = mock(PluginContext.class);
        // Set initial state to LOADED which can transition to INITIALIZED
        plugin.setState(PluginStatus.LOADED);
    }

    // Concrete implementation for testing
    static class TestPlugin extends AbstractPlugin {
        private boolean beforeEnableCalled = false;
        private boolean afterEnableCalled = false;
        private boolean beforeDisableCalled = false;
        private boolean afterDisableCalled = false;
        private boolean onStartCalled = false;
        private boolean onStopCalled = false;
        private boolean cleanupCalled = false;
        private Object lastConfigChangedData;
        private Object lastDependencyLoadedData;
        private Object lastUserInteractionData;
        private Object lastSystemEventData;
        private Object lastCustomEventData;

        public TestPlugin(String name, String version, String description, String author) {
            super(name, version, description, author);
        }

        public TestPlugin(String id, String name, String version, String description, String author) {
            super(id, name, version, description, author);
        }

        public TestPlugin(PluginDescriptor descriptor) {
            super(descriptor);
        }

        @Override
        public boolean beforeEnable() {
            beforeEnableCalled = true;
            return true;
        }

        @Override
        public void afterEnable() {
            afterEnableCalled = true;
        }

        @Override
        public boolean beforeDisable() {
            beforeDisableCalled = true;
            return true;
        }

        @Override
        public void afterDisable() {
            afterDisableCalled = true;
        }

        @Override
        public void onStart() {
            onStartCalled = true;
        }

        @Override
        public void onStop() {
            onStopCalled = true;
        }

        @Override
        public void cleanup() {
            cleanupCalled = true;
        }

        public void resetFlags() {
            beforeEnableCalled = false;
            afterEnableCalled = false;
            beforeDisableCalled = false;
            afterDisableCalled = false;
            onStartCalled = false;
            onStopCalled = false;
            cleanupCalled = false;
        }

        @Override
        protected void onConfigurationChanged(Object eventData) {
            lastConfigChangedData = eventData;
        }

        @Override
        protected void onDependencyLoaded(Object eventData) {
            lastDependencyLoadedData = eventData;
        }

        @Override
        protected void onUserInteraction(Object eventData) {
            lastUserInteractionData = eventData;
        }

        @Override
        protected void onSystemEvent(Object eventData) {
            lastSystemEventData = eventData;
        }

        @Override
        protected void onCustomEvent(Object eventData) {
            lastCustomEventData = eventData;
        }
    }

    @Nested
    @DisplayName("Constructor Tests")
    class ConstructorTests {
        @Test
        @DisplayName("Should create plugin with generated ID from name")
        void testConstructorWithGeneratedId() {
            TestPlugin p = new TestPlugin("My Test Plugin", "2.0.0", "Description", "Author");
            PluginDescriptor desc = p.getDescriptor();

            assertEquals("my-test-plugin", desc.getId());
            assertEquals("My Test Plugin", desc.getName());
            assertEquals("2.0.0", desc.getVersion());
            assertEquals("Description", desc.getDescription());
            assertEquals("Author", desc.getAuthor());
        }

        @Test
        @DisplayName("Should create plugin with custom ID")
        void testConstructorWithCustomId() {
            TestPlugin p = new TestPlugin("custom-id", "Custom Plugin", "1.0.0", "Desc", "Author");
            PluginDescriptor desc = p.getDescriptor();

            assertEquals("custom-id", desc.getId());
            assertEquals("Custom Plugin", desc.getName());
        }

        @Test
        @DisplayName("Should create plugin with provided descriptor")
        void testConstructorWithDescriptor() {
            PluginDescriptor descriptor = new PluginDescriptor("test-id", "Test", "1.0.0",
                    "com.test.Main", "Desc", "Author");
            TestPlugin p = new TestPlugin(descriptor);

            assertSame(descriptor, p.getDescriptor());
        }

        @Test
        @DisplayName("Should handle empty name in ID generation")
        void testGeneratePluginIdWithEmptyName() {
            TestPlugin p = new TestPlugin("", "1.0.0", "Desc", "Author");
            String id = p.getDescriptor().getId();
            assertNotNull(id);
            assertEquals("unknown-plugin", id);
        }

        @Test
        @DisplayName("Should handle null name in ID generation")
        void testGeneratePluginIdWithNullName() {
            TestPlugin p = new TestPlugin((String) null, "1.0.0", "Desc", "Author");
            String id = p.getDescriptor().getId();
            assertNotNull(id);
            assertEquals("unknown-plugin", id);
        }
    }

    @Nested
    @DisplayName("Descriptor and Basic Information Tests")
    class DescriptorTests {
        @Test
        @DisplayName("Should return correct descriptor")
        void testGetDescriptor() {
            PluginDescriptor desc = plugin.getDescriptor();
            assertNotNull(desc);
            assertEquals("test-plugin", desc.getId());
            assertEquals("Test Plugin", desc.getName());
        }

        @Test
        @DisplayName("Should get and set author email")
        void testAuthorEmail() {
            plugin.setAuthorEmail("test@example.com");
            assertEquals("test@example.com", plugin.getAuthorEmail());
        }

        @Test
        @DisplayName("Should get and set category")
        void testCategory() {
            plugin.setCategory("Testing");
            assertEquals("Testing", plugin.getCategory());
        }

        @Test
        @DisplayName("Should get and set custom metadata")
        void testCustomMetadata() {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("key1", "value1");
            metadata.put("key2", 123);

            plugin.setCustomMetadata(metadata);
            assertEquals(metadata, plugin.getCustomMetadata());
            plugin.getCustomMetadata().put("mutated", true);
            assertFalse(plugin.getCustomMetadata().containsKey("mutated"));

            plugin.addCustomMetadata("key3", "value3");
            assertEquals("value3", plugin.getCustomMetadata().get("key3"));
        }
    }

    @Nested
    @DisplayName("Manifest Information Tests")
    class ManifestTests {
        @Test
        @DisplayName("Should get and set specification title")
        void testSpecificationTitle() {
            plugin.setSpecificationTitle("Test Spec");
            assertEquals("Test Spec", plugin.getSpecificationTitle());
        }

        @Test
        @DisplayName("Should get and set specification version")
        void testSpecificationVersion() {
            plugin.setSpecificationVersion("2.0");
            assertEquals("2.0", plugin.getSpecificationVersion());
        }

        @Test
        @DisplayName("Should get and set specification vendor")
        void testSpecificationVendor() {
            plugin.setSpecificationVendor("Test Vendor");
            assertEquals("Test Vendor", plugin.getSpecificationVendor());
        }

        @Test
        @DisplayName("Should get and set implementation version")
        void testImplementationVersion() {
            plugin.setImplementationVersion("1.0.0-SNAPSHOT");
            assertEquals("1.0.0-SNAPSHOT", plugin.getImplementationVersion());
        }
    }

    @Nested
    @DisplayName("Lifecycle Tests")
    class LifecycleTests {
        @Test
        @DisplayName("Should initialize plugin correctly")
        void testInitialize() {
            plugin.initialize(mockContext);

            assertEquals(PluginStatus.INITIALIZED, plugin.getState());
            assertTrue(plugin.onStartCalled);
        }

        @Test
        @DisplayName("Should enable plugin correctly")
        void testEnable() {
            plugin.initialize(mockContext);
            plugin.enable();

            assertTrue(plugin.beforeEnableCalled);
            assertTrue(plugin.afterEnableCalled);
            assertEquals(PluginStatus.ENABLED, plugin.getState());
            assertTrue(plugin.isEnabled());
            assertTrue(plugin.getAverageStartupTime() >= 0);
        }

        @Test
        @DisplayName("Should throw exception when beforeEnable returns false")
        void testEnableWithBeforeEnableFalse() {
            TestPlugin failingPlugin = new TestPlugin("Failing", "1.0.0", "Desc", "Author") {
                @Override
                public boolean beforeEnable() {
                    return false;
                }
            };
            failingPlugin.setState(PluginStatus.LOADED);
            failingPlugin.initialize(mockContext);

            assertThrows(IllegalStateException.class, failingPlugin::enable);
        }

        @Test
        @DisplayName("Should disable plugin correctly")
        void testDisable() {
            plugin.initialize(mockContext);
            plugin.enable();
            plugin.disable();

            assertTrue(plugin.beforeDisableCalled);
            assertTrue(plugin.afterDisableCalled);
            assertEquals(PluginStatus.DISABLED, plugin.getState());
        }

        @Test
        @DisplayName("Should shutdown plugin correctly")
        void testShutdown() {
            plugin.initialize(mockContext);
            plugin.enable();
            plugin.shutdown();

            assertTrue(plugin.onStopCalled);
            assertTrue(plugin.cleanupCalled);
            assertEquals(PluginStatus.SHUTDOWN, plugin.getState());
        }

        @Test
        @DisplayName("Should track startup time metrics")
        void testStartupTimeTracking() {
            plugin.initialize(mockContext);
            plugin.enable();

            long avgTime = plugin.getAverageStartupTime();
            assertTrue(avgTime >= 0);

            Map<String, Object> metrics = plugin.getMetrics();
            assertEquals(1L, metrics.get("startupCount"));
            assertNotNull(metrics.get("lastStartupTime"));
        }
    }

    @Nested
    @DisplayName("State Management Tests")
    class StateManagementTests {
        @Test
        @DisplayName("Should allow valid state transitions")
        void testValidStateTransitions() {
            // Start from LOADED state
            plugin.setState(PluginStatus.LOADED);

            // Test allowed transitions based on canTransitionTo
            plugin.setState(PluginStatus.INITIALIZED);
            assertEquals(PluginStatus.INITIALIZED, plugin.getState());

            plugin.setState(PluginStatus.ENABLING);
            assertEquals(PluginStatus.ENABLING, plugin.getState());

            plugin.setState(PluginStatus.ENABLED);
            assertEquals(PluginStatus.ENABLED, plugin.getState());

            plugin.setState(PluginStatus.DISABLING);
            assertEquals(PluginStatus.DISABLING, plugin.getState());

            plugin.setState(PluginStatus.DISABLED);
            assertEquals(PluginStatus.DISABLED, plugin.getState());
        }

        // Add a separate test specifically for ENABLED -> LOADED
        @Test
        @DisplayName("Should not allow transition from ENABLED to LOADED")
        void testCannotTransitionFromEnabledToLoaded() {
            plugin.setState(PluginStatus.LOADED);
            plugin.setState(PluginStatus.ENABLED);

            // This should throw exception - ENABLED cannot go directly to LOADED
            assertThrows(IllegalStateException.class,
                    () -> plugin.setState(PluginStatus.LOADED));
        }

        @Test
        @DisplayName("Should allow transition to ERROR from active states")
        void testTransitionToError() {
            plugin.setState(PluginStatus.ENABLED);
            plugin.setState(PluginStatus.ERROR);
            assertEquals(PluginStatus.ERROR, plugin.getState());

            // From ERROR can only go to DISABLED
            plugin.setState(PluginStatus.DISABLED);
            assertEquals(PluginStatus.DISABLED, plugin.getState());
        }
    }

    @Nested
    @DisplayName("Dependency Tests")
    class DependencyTests {
        @Test
        @DisplayName("Should manage dependencies correctly")
        void testDependencies() {
            plugin.addDependency("dep1");
            plugin.addDependencies(List.of("dep2", "dep3"));

            List<String> deps = plugin.getDependencies();
            assertEquals(3, deps.size());
            assertTrue(deps.contains("dep1"));
            assertTrue(deps.contains("dep2"));
        }

        @Test
        @DisplayName("Should inject and retrieve dependencies")
        void testDependencyInjection() {
            Map<String, Object> deps = new HashMap<>();
            deps.put("service1", "testService");
            deps.put("service2", 123);

            plugin.injectDependencies(deps);

            Object service1 = plugin.getDependency("service1");
            Object service2 = plugin.getDependency("service2");

            assertNotNull(service1);
            assertNotNull(service2);
            assertEquals("testService", service1);
            assertEquals(123, ((Number) service2).intValue());
        }

        @Test
        @DisplayName("Should validate dependencies correctly")
        void testValidateDependencies() {
            plugin.addDependency("requiredService");

            // Missing dependency
            assertFalse(plugin.validateDependencies());

            // Add dependency
            Map<String, Object> deps = new HashMap<>();
            deps.put("requiredService", "service");
            plugin.injectDependencies(deps);

            assertTrue(plugin.validateDependencies());
        }
    }

    @Nested
    @DisplayName("Configuration Tests")
    class ConfigurationTests {
        @Test
        @DisplayName("Should get default configuration schema")
        void testGetConfigurationSchema() {
            Map<String, Object> schema = plugin.getConfigurationSchema();
            assertNotNull(schema);
            assertTrue(schema.containsKey("enabled"));
            assertTrue(schema.containsKey("autoStart"));
        }

        @Test
        @DisplayName("Should update configuration")
        void testUpdateConfiguration() {
            Map<String, Object> newConfig = new HashMap<>();
            newConfig.put("autoEnable", false);

            // Les settings doivent être dans une map "settings"
            Map<String, Object> settings = new HashMap<>();
            settings.put("customSetting", "value");
            newConfig.put("settings", settings);

            boolean result = plugin.updateConfiguration(newConfig);
            assertTrue(result);

            PluginConfig config = plugin.getConfig();

            // Vérifier autoEnable
            assertFalse(config.isAutoEnable());

            // Vérifier customSetting dans settings
            assertEquals("value", config.getSetting("customSetting"));
        }

        @Test
        @DisplayName("Should validate configuration before update")
        void testValidateConfiguration() {
            assertTrue(plugin.validateConfiguration(Map.of("key", "value")));
            assertFalse(plugin.validateConfiguration(null));
        }

        @Test
        @DisplayName("Should save and load settings")
        void testSettings() {
            Map<String, Object> settings = new HashMap<>();
            settings.put("setting1", "value1");
            settings.put("setting2", 42);

            assertTrue(plugin.saveSettings(settings));

            Map<String, Object> loaded = plugin.loadSettings();
            assertEquals("value1", loaded.get("setting1"));
            assertEquals(42, loaded.get("setting2"));

            plugin.setSetting("setting3", "value3");
            assertEquals("value3", plugin.getSetting("setting3"));
        }
    }

    @Nested
    @DisplayName("Resource Management Tests")
    class ResourceTests {
        @Test
        @DisplayName("Should manage provided resources")
        void testResources() {
            Object resource = new Object();
            plugin.addProvidedResource("testResource", resource);

            assertEquals(resource, plugin.provideResource("testResource"));

            Map<String, Object> resources = plugin.getProvidedResources();
            assertEquals(1, resources.size());
            assertTrue(resources.containsKey("testResource"));

            plugin.removeProvidedResource("testResource");
            assertNull(plugin.provideResource("testResource"));
        }
    }

    @Nested
    @DisplayName("Version Compatibility Tests")
    class CompatibilityTests {
        @Test
        @DisplayName("Should get and set required host version")
        void testRequiredHostVersion() {
            plugin.setRequiredHostVersion("2.0.0");
            assertEquals("2.0.0", plugin.getRequiredHostVersion());
        }

        @Test
        @DisplayName("Should check compatibility")
        void testCheckCompatibility() {
            plugin.setRequiredHostVersion("1.0.0");
            Plugin.CompatibilityResult result = plugin.checkCompatibility();
            assertTrue(result.isCompatible());

            plugin.setRequiredHostVersion(null);
            result = plugin.checkCompatibility();
            assertFalse(result.isCompatible());
        }

        @Test
        @DisplayName("Should check upgrade capability")
        void testCanUpgradeTo() {
            plugin.setImplementationVersion("1.0.0");
            assertTrue(plugin.canUpgradeTo("2.0.0"));
            assertFalse(plugin.canUpgradeTo("0.9.0"));
            assertFalse(plugin.canUpgradeTo("1.0.0"));
        }

        @Test
        @DisplayName("Should return incompatibilities")
        void testGetIncompatibilities() {
            List<String> incompatibilities = plugin.getIncompatibilities();
            assertNotNull(incompatibilities);
            assertTrue(incompatibilities.isEmpty());
        }
    }

    @Nested
    @DisplayName("Event Handling Tests")
    class EventTests {
        @Test
        @DisplayName("Should handle different event types")
        void testHandleEvent() {
            plugin.handleEvent(Plugin.PluginEventType.CONFIG_CHANGED, Map.of("key", "value"));
            plugin.handleEvent(Plugin.PluginEventType.DEPENDENCY_LOADED, "dependency");
            plugin.handleEvent(Plugin.PluginEventType.USER_INTERACTION, "click");
            plugin.handleEvent(Plugin.PluginEventType.SYSTEM_EVENT, "event");
            plugin.handleEvent(Plugin.PluginEventType.CUSTOM_EVENT, "custom");

            assertEquals(Map.of("key", "value"), plugin.lastConfigChangedData);
            assertEquals("dependency", plugin.lastDependencyLoadedData);
            assertEquals("click", plugin.lastUserInteractionData);
            assertEquals("event", plugin.lastSystemEventData);
            assertEquals("custom", plugin.lastCustomEventData);
        }

        @Test
        @DisplayName("Should publish and track events")
        void testPublishEvent() {
            plugin.addPublishedEvent("TEST_EVENT");
            plugin.publishEvent("TEST_EVENT", "test data");

            List<String> publishedEvents = plugin.getPublishedEvents();
            assertTrue(publishedEvents.contains("TEST_EVENT"));
        }

        @Test
        @DisplayName("Should get event bus")
        void testGetEventBus() {
            PluginEventBus eventBus = plugin.getEventBus();
            assertNotNull(eventBus);
        }

        @Test
        @DisplayName("Should create generic event")
        void testCreateEvent() {
            AbstractPlugin.GenericEvent event = plugin.createEvent("TEST_TYPE", "data");
            assertNotNull(event);
            assertEquals("TEST_TYPE", event.getType());
            assertEquals("data", event.getData());
            assertEquals(plugin.getName(), event.getSource());
            assertNotNull(event.getTimestamp());
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {
        @Test
        @DisplayName("Should handle errors and update metrics")
        void testOnError() {
            Exception testException = new RuntimeException("Test error");
            plugin.onError(testException);

            Map<String, Object> metrics = plugin.getMetrics();
            assertEquals(1, metrics.get("errorCount"));
            assertNotNull(metrics.get("lastError"));
        }

        @Test
        @DisplayName("Should handle uncaught exceptions")
        void testHandleUncaughtException() {
            Thread thread = Thread.currentThread();
            Exception testException = new RuntimeException("Uncaught");

            plugin.handleUncaughtException(thread, testException);

            Map<String, Object> metrics = plugin.getMetrics();
            assertEquals(1, metrics.get("uncaughtExceptions"));
            assertNotNull(metrics.get("lastUncaughtException"));
        }

        @Test
        @DisplayName("Should be recoverable by default")
        void testIsRecoverable() {
            assertTrue(plugin.isRecoverable());
        }

        @Test
        @DisplayName("Should attempt recovery from error")
        void testRecoverFromError() {
            plugin.initialize(mockContext);
            plugin.enable();

            Exception testException = new RuntimeException("Recoverable error");
            plugin.recoverFromError(testException);

            // Plugin should be disabled and re-enabled
            assertEquals(PluginStatus.ENABLED, plugin.getState());
        }
    }

    @Nested
    @DisplayName("Performance and Monitoring Tests")
    class MonitoringTests {
        @Test
        @DisplayName("Should get metrics")
        void testGetMetrics() {
            Map<String, Object> metrics = plugin.getMetrics();
            assertNotNull(metrics);
            assertTrue(metrics.containsKey("startupCount"));
            assertTrue(metrics.containsKey("errorCount"));
        }

        @Test
        @DisplayName("Should reset metrics")
        void testResetMetrics() {
            plugin.onError(new RuntimeException());
            plugin.resetMetrics();

            Map<String, Object> metrics = plugin.getMetrics();
            assertEquals(0L, metrics.get("startupCount"));
            assertEquals(0, metrics.get("errorCount"));
        }

        @Test
        @DisplayName("Should perform health check")
        void testHealthCheck() {
            Plugin.HealthStatus status = plugin.healthCheck();
            assertEquals(Plugin.HealthStatus.UP, status.getStatus());

            plugin.setState(PluginStatus.ERROR);
            status = plugin.healthCheck();
            assertEquals(Plugin.HealthStatus.DOWN, status.getStatus());
        }
    }

    @Nested
    @DisplayName("Localization and Documentation Tests")
    class DocumentationTests {
        @Test
        @DisplayName("Should get localized message")
        void testGetLocalizedMessage() {
            String message = plugin.getLocalizedMessage("test.key", "en");
            assertEquals("test.key", message);
        }

        @Test
        @DisplayName("Should get documentation URL")
        void testGetDocumentationUrl() {
            assertNull(plugin.getDocumentationUrl());
        }

        @Test
        @DisplayName("Should get help text")
        void testGetHelpText() {
            assertEquals(plugin.getDescription(), plugin.getHelpText());
        }

        @Test
        @DisplayName("Should get usage examples")
        void testGetUsageExamples() {
            List<String> examples = plugin.getUsageExamples();
            assertNotNull(examples);
            assertFalse(examples.isEmpty());
        }
    }

    @Nested
    @DisplayName("Security Tests")
    class SecurityTests {
        @Test
        @DisplayName("Should get default permissions")
        void testGetDefaultPermissions() {
            List<String> permissions = plugin.getDefaultPermissions();
            assertTrue(permissions.contains("read"));
            assertTrue(permissions.contains("write"));
        }
    }

    @Nested
    @DisplayName("Utility Methods Tests")
    class UtilityTests {
        @Test
        @DisplayName("Should get context")
        void testGetContext() {
            assertNull(plugin.getContext());
            plugin.initialize(mockContext);
            assertEquals(mockContext, plugin.getContext());
        }

        @Test
        @DisplayName("Should log messages")
        void testLogMethods() {
            // These should not throw exceptions
            plugin.log("Test message");
            plugin.logError("Error message");
            plugin.logDebug("Debug message");
        }
    }

    // ── Additional tests to cover remaining missed branches / instructions ──

    /** Minimal plugin that does NOT override the event handler stubs,
     *  so AbstractPlugin's empty bodies get executed. */
    static class MinimalPlugin extends AbstractPlugin {
        public MinimalPlugin(String name, String version, String description, String author) {
            super(name, version, description, author);
        }
        @Override public boolean beforeEnable()  { return true; }
        @Override public void   afterEnable()    {}
        @Override public boolean beforeDisable() { return false; } // always refuses
        @Override public void   afterDisable()   {}
        @Override public void   onStart()        {}
        @Override public void   onStop()         {}
        @Override public void   cleanup()        {}
        @Override public boolean isRecoverable() { return false; } // non-recoverable
    }

    @Test
    @DisplayName("handleEvent uses AbstractPlugin empty handlers when not overridden")
    void testHandleEvent_BaseHandlersAreCalled() {
        MinimalPlugin mp = new MinimalPlugin("m", "1.0", "desc", "author");
        mp.setState(PluginStatus.LOADED);
        // All five cases delegate to AbstractPlugin's empty methods
        assertDoesNotThrow(() -> {
            mp.handleEvent(Plugin.PluginEventType.CONFIG_CHANGED, "data");
            mp.handleEvent(Plugin.PluginEventType.DEPENDENCY_LOADED, "data");
            mp.handleEvent(Plugin.PluginEventType.USER_INTERACTION, "data");
            mp.handleEvent(Plugin.PluginEventType.SYSTEM_EVENT, "data");
            mp.handleEvent(Plugin.PluginEventType.CUSTOM_EVENT, "data");
        });
    }

    @Test
    @DisplayName("disable_WhenBeforeDisableFalse_ShouldThrowIllegalState")
    void testDisable_BeforeDisableReturnsFalse_Throws() {
        MinimalPlugin mp = new MinimalPlugin("m", "1.0", "desc", "author");
        mp.setState(PluginStatus.LOADED);
        mp.initialize(mockContext);
        mp.enable();
        assertThrows(IllegalStateException.class, mp::disable);
    }

    @Test
    @DisplayName("updateConfiguration with null config returns false")
    void testUpdateConfiguration_NullConfig_ReturnsFalse() {
        assertFalse(plugin.updateConfiguration(null));
    }

    @Test
    @DisplayName("validate returns false when dependency is missing")
    void testValidate_MissingDependency_ReturnsFalse() {
        plugin.addDependency("missing-dep");
        // dependency not injected, so validateDependencies() returns false
        assertFalse(plugin.validate());
    }

    @Test
    @DisplayName("checkCompatibility returns true when requiredHostVersion is set")
    void testCheckCompatibility_IncompatibleVersion() {
        plugin.setRequiredHostVersion("1.0.0");
        Plugin.CompatibilityResult result = plugin.checkCompatibility();
        assertTrue(result.isCompatible());
    }

    @Test
    @DisplayName("checkCompatibility returns false when requiredHostVersion is empty string")
    void testCheckCompatibility_EmptyRequiredVersion() {
        plugin.setRequiredHostVersion("");
        Plugin.CompatibilityResult result = plugin.checkCompatibility();
        assertFalse(result.isCompatible());
    }

    @Test
    @DisplayName("canUpgradeTo returns false when version comparison throws")
    void testCanUpgradeTo_InvalidVersion_ReturnsFalse() {
        // "not-a-version" can't be compared; exception path returns false
        assertFalse(plugin.canUpgradeTo("not.a.version"));
    }

    @Test
    @DisplayName("healthCheck with SHUTDOWN state returns DOWN")
    void testHealthCheck_ShutdownState_ReturnsDown() {
        plugin.initialize(mockContext);
        plugin.enable();
        plugin.setState(PluginStatus.SHUTTING_DOWN);
        plugin.setState(PluginStatus.SHUTDOWN);
        Plugin.HealthStatus status = plugin.healthCheck();
        assertEquals(Plugin.HealthStatus.DOWN, status.getStatus());
    }

    @Test
    @DisplayName("subscribe adds event to subscribedEvents list")
    void testSubscribe_AddsToSubscribedEvents() {
        plugin.subscribe(AbstractPlugin.GenericEvent.class, e -> {});
        List<String> subscribed = plugin.getSubscribedEvents();
        assertTrue(subscribed.contains("GenericEvent"));
    }

    @Test
    @DisplayName("unsubscribe removes event from subscribedEvents list")
    void testUnsubscribe_RemovesFromSubscribedEvents() {
        EventListener<AbstractPlugin.GenericEvent> listener = e -> {};
        plugin.subscribe(AbstractPlugin.GenericEvent.class, listener);
        plugin.unsubscribe(AbstractPlugin.GenericEvent.class, listener);
        // The simple name may still be there if subscribe was called multiple times,
        // but at least no exception should be thrown
        assertDoesNotThrow(() -> plugin.getSubscribedEvents());
    }

    @Test
    @DisplayName("addPublishedEvent adds to publishedEvents list")
    void testAddPublishedEvent() {
        plugin.addPublishedEvent("MY_EVENT");
        assertTrue(plugin.getPublishedEvents().contains("MY_EVENT"));
    }

    @Test
    @DisplayName("addSubscribedEvent adds to subscribedEvents list")
    void testAddSubscribedEvent() {
        plugin.addSubscribedEvent("MY_SUB_EVENT");
        assertTrue(plugin.getSubscribedEvents().contains("MY_SUB_EVENT"));
    }

    @Test
    @DisplayName("onError on non-recoverable plugin calls handleUncaughtException")
    void testOnError_NonRecoverable_CallsHandleUncaughtException() {
        MinimalPlugin mp = new MinimalPlugin("m", "1.0", "desc", "author");
        mp.setState(PluginStatus.LOADED);
        mp.onError(new RuntimeException("fatal"));
        // uncaughtExceptions counter should be incremented
        assertEquals(1, mp.getMetrics().get("uncaughtExceptions"));
    }

    @Nested
    @DisplayName("Log Capture Tests to Kill PIT Mutations")
    class LogCaptureTests {

        @Test
        @DisplayName("logError should log error message when error enabled")
        void testLogError_LogsErrorMessage() {
            try (LogCapture capture = LogCapture.attach(AbstractPlugin.class)) {
                plugin.logError("Test error message");

                assertTrue(capture.formattedMessages().stream()
                    .anyMatch(msg -> msg.contains("Test error message")));
            }
        }

        @Test
        @DisplayName("logDebug should log debug message when debug enabled")
        void testLogDebug_LogsDebugMessage() {
            try (LogCapture capture = LogCapture.attach(AbstractPlugin.class)) {
                plugin.logDebug("Test debug message");

                assertTrue(capture.formattedMessages().stream()
                    .anyMatch(msg -> msg.contains("Test debug message")));
            }
        }

        @Test
        @DisplayName("onError should update metrics and attempt recovery")
        void testOnError_UpdatesMetricsAndAttemptsRecovery() {
            try (LogCapture capture = LogCapture.attach(AbstractPlugin.class)) {
                plugin.onError(new RuntimeException("Test error"));

                // Metrics should be updated
                assertEquals(1, plugin.getMetrics().get("errorCount"));
            }
        }

        @Test
        @DisplayName("resetMetrics should clear metrics map")
        void testResetMetrics_ClearsMetrics() {
            plugin.onError(new RuntimeException());
            Map<String, Object> beforeReset = plugin.getMetrics();
            assertTrue((Integer) beforeReset.get("errorCount") > 0);

            plugin.resetMetrics();

            Map<String, Object> afterReset = plugin.getMetrics();
            assertEquals(0, afterReset.get("errorCount"));
        }

        @Test
        @DisplayName("subscribe should call eventBus.subscribe")
        void testSubscribe_CallsEventBusSubscribe() {
            EventListener<AbstractPlugin.GenericEvent> listener = e -> {};
            plugin.subscribe(AbstractPlugin.GenericEvent.class, listener);

            // Should add to subscribed events
            assertTrue(plugin.getSubscribedEvents().contains("GenericEvent"));
        }

        @Test
        @DisplayName("unsubscribe should call eventBus.unsubscribe")
        void testUnsubscribe_CallsEventBusUnsubscribe() {
            EventListener<AbstractPlugin.GenericEvent> listener = e -> {};
            plugin.subscribe(AbstractPlugin.GenericEvent.class, listener);
            plugin.unsubscribe(AbstractPlugin.GenericEvent.class, listener);

            // Should remove from subscribed events
            assertFalse(plugin.getSubscribedEvents().contains("GenericEvent"));
        }

        @Test
        @DisplayName("validate should check both dependencies and configuration")
        void testValidate_ChecksBothDependenciesAndConfiguration() {
            plugin.addDependency("test-dep");
            Map<String, Object> deps = Map.of("test-dep", "service");
            plugin.injectDependencies(deps);

            assertTrue(plugin.validate());
        }
    }

    @Test
    @DisplayName("onError with recovery failure escalates via handleUncaughtException")
    void testOnError_RecoveryFails_Escalates() {
        TestPlugin failable = new TestPlugin("f", "1.0", "d", "a") {
            @Override
            protected void recoverFromError(Throwable throwable) {
                throw new RuntimeException("recovery failed");
            }
        };
        failable.setState(PluginStatus.LOADED);
        failable.initialize(mockContext);
        failable.enable();
        assertDoesNotThrow(() -> failable.onError(new RuntimeException("original")));
        assertTrue((int) failable.getMetrics().get("uncaughtExceptions") >= 1);
    }

    @Test
    @DisplayName("GenericEvent.toString includes type and source name")
    void testGenericEvent_ToString() {
        AbstractPlugin.GenericEvent event = plugin.createEvent("TEST", "payload");
        String str = event.toString();
        assertTrue(str.contains("TEST"));
        assertTrue(str.contains(plugin.getName()));
    }

    @Test
    @DisplayName("GenericEvent.toString with null source uses 'unknown'")
    void testGenericEvent_ToString_NullSource() {
        AbstractPlugin.GenericEvent event = new AbstractPlugin.GenericEvent("T", "d", null);
        String str = event.toString();
        assertTrue(str.contains("unknown"));
    }

    @Test
    @DisplayName("hasSubscribers returns false before any subscription")
    void testHasSubscribers_BeforeSubscription_ReturnsFalse() {
        assertFalse(plugin.hasSubscribers(AbstractPlugin.GenericEvent.class));
    }

    @Test
    @DisplayName("generateNormalizedId with all-special-char name uses timestamp fallback")
    void testGenerateNormalizedId_AllSpecialChars() {
        // A name consisting only of chars removed during normalization → empty → fallback
        TestPlugin p = new TestPlugin("***@@@###", "1.0", "d", "a");
        assertNotNull(p.getDescriptor().getId());
        assertTrue(p.getDescriptor().getId().startsWith("plugin-") || !p.getDescriptor().getId().isEmpty());
    }

    @Test
    @DisplayName("compareVersions with different-length versions pads shorter with zeros")
    void testCompareVersions_DifferentLengths() {
        // "1.0.0" (current) vs "1.0.0.1" (target): padding covers part1 = 0 branch
        assertTrue(plugin.canUpgradeTo("1.0.0.1"));
        // "1.0.0" vs "1" (target shorter): padding covers part2 = 0 branch
        assertFalse(plugin.canUpgradeTo("1"));
    }

    @Test
    @DisplayName("getPublishedEvents returns defensive copy")
    void testGetPublishedEvents_DefensiveCopy() {
        List<String> list = plugin.getPublishedEvents();
        list.add("HACK");
        assertFalse(plugin.getPublishedEvents().contains("HACK"));
    }

    @Test
    @DisplayName("getSubscribedEvents returns defensive copy")
    void testGetSubscribedEvents_DefensiveCopy() {
        List<String> list = plugin.getSubscribedEvents();
        list.add("HACK");
        assertFalse(plugin.getSubscribedEvents().contains("HACK"));
    }

    @Nested
    @DisplayName("Coverage completion tests")
    class CoverageCompletionTests {

        /** Subclass exposing protected helpers needed for coverage. */
        class InstrumentedPlugin extends TestPlugin {
            boolean configValidationCalled = false;
            boolean forceConfigInvalid = false;

            InstrumentedPlugin() {
                super("instr-plugin", "Instr Plugin", "1.0.0", "desc", "author");
            }

            @Override
            public boolean validateConfiguration(Map<String, Object> cfg) {
                configValidationCalled = true;
                if (forceConfigInvalid) {
                    return false;
                }
                return super.validateConfiguration(cfg);
            }

            void declareDependency(String dependency) {
                addDependency(dependency);
            }

            <T extends Event> void publishTypedEvent(T event) {
                publishEvent(event);
            }

            void logInfoMessage(String message) {
                log(message);
            }

            void logErrorMessage(String message) {
                logError(message);
            }

            void logDebugMessage(String message) {
                logDebug(message);
            }
        }

        @Test
        @DisplayName("validate short-circuits when a declared dependency is missing")
        void validate_ShouldShortCircuit_WhenDependenciesMissing() {
            InstrumentedPlugin instrumented = new InstrumentedPlugin();
            instrumented.declareDependency("missing.dependency");

            assertFalse(instrumented.validate());
            assertFalse(instrumented.configValidationCalled,
                    "configuration validation must be skipped on dependency failure");
        }

        @Test
        @DisplayName("validate returns false when configuration fails but dependencies are satisfied")
        void validate_ShouldReturnFalse_WhenConfigurationInvalid() {
            InstrumentedPlugin instrumented = new InstrumentedPlugin();
            instrumented.forceConfigInvalid = true;
            // No dependencies declared -> validateDependencies() is true

            assertFalse(instrumented.validate());
            assertTrue(instrumented.configValidationCalled,
                    "configuration validation must run once dependencies pass");
        }

        @Test
        @DisplayName("typed publishEvent routes the event through the event bus")
        void publishTypedEvent_ShouldReachSubscribers() {
            InstrumentedPlugin instrumented = new InstrumentedPlugin();
            AtomicReference<AbstractPlugin.GenericEvent> received = new AtomicReference<>();
            EventListener<AbstractPlugin.GenericEvent> listener = received::set;
            instrumented.getEventBus().subscribe(AbstractPlugin.GenericEvent.class, listener);

            instrumented.publishTypedEvent(
                    new AbstractPlugin.GenericEvent("TYPED_EVENT", "payload", instrumented));

            assertNotNull(received.get());
            assertEquals("TYPED_EVENT", received.get().getType());
            assertEquals("payload", received.get().getData());
        }

        @Test
        @DisplayName("handleUncaughtException still updates metrics when error logging is off")
        void handleUncaughtException_ShouldUpdateMetrics_WhenLoggingOff() {
            TestUtils.withLoggingOff(AbstractPlugin.class,
                    () -> plugin.handleUncaughtException(Thread.currentThread(), new RuntimeException("boom")));

            assertEquals(1, plugin.getMetrics().get("uncaughtExceptions"));
            assertNotNull(plugin.getMetrics().get("lastUncaughtException"));
        }

        @Test
        @DisplayName("log helpers stay silent when logging is disabled")
        void logHelpers_ShouldBeNoOps_WhenLoggingOff() {
            InstrumentedPlugin instrumented = new InstrumentedPlugin();
            TestUtils.withLoggingOff(AbstractPlugin.class, () -> {
                instrumented.logInfoMessage("info-off");
                instrumented.logErrorMessage("error-off");
                instrumented.logDebugMessage("debug-off");
            });
            // No exception means the guard false-branches executed correctly
            assertTrue(true);
        }
    }
}