package com.protonmail.landrevillejf.ide.plugin;

import com.protonmail.landrevillejf.ide.plugin.events.CodeCompiledEvent;
import com.protonmail.landrevillejf.ide.plugin.events.EditorEvents;
import com.protonmail.landrevillejf.ide.plugin.events.FileCreatedEvent;
import com.protonmail.landrevillejf.ide.plugin.service.PluginLifecycleListener;
import com.protonmail.landrevillejf.ide.plugin.service.PluginServiceConfiguration;
import com.protonmail.landrevillejf.ide.plugin.service.PluginServiceLocator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Coverage-completion tests for the core public API surface:
 * state machine, events, exceptions, default interface methods,
 * service stubs and the plugin manager provider.
 */
@DisplayName("Core API coverage tests")
class CoreApiCoverageTest {

    // =====================================================================
    // PluginStatus
    // =====================================================================

    @Nested
    @DisplayName("PluginStatus state machine")
    class PluginStatusTests {

        private final Map<PluginStatus, Set<PluginStatus>> allowed = new HashMap<>();

        {
            allowed.put(PluginStatus.UNLOADED, Set.of(PluginStatus.LOADED));
            allowed.put(PluginStatus.LOADED, Set.of(
                    PluginStatus.INITIALIZED, PluginStatus.ENABLED,
                    PluginStatus.DISABLED, PluginStatus.ENABLING, PluginStatus.ERROR));
            allowed.put(PluginStatus.INITIALIZED, Set.of(
                    PluginStatus.ENABLED, PluginStatus.ENABLING,
                    PluginStatus.DISABLED, PluginStatus.ERROR));
            allowed.put(PluginStatus.ENABLED, Set.of(
                    PluginStatus.DISABLED, PluginStatus.DISABLING,
                    PluginStatus.SHUTTING_DOWN, PluginStatus.ERROR));
            allowed.put(PluginStatus.DISABLED, Set.of(
                    PluginStatus.LOADED, PluginStatus.ENABLED,
                    PluginStatus.ENABLING, PluginStatus.SHUTTING_DOWN, PluginStatus.ERROR));
            allowed.put(PluginStatus.ERROR, Set.of(PluginStatus.DISABLED));
            allowed.put(PluginStatus.RELOADING, Set.of(PluginStatus.LOADED));
            allowed.put(PluginStatus.ENABLING, Set.of(PluginStatus.ENABLED, PluginStatus.ERROR));
            allowed.put(PluginStatus.DISABLING, Set.of(PluginStatus.DISABLED, PluginStatus.ERROR));
            allowed.put(PluginStatus.SHUTTING_DOWN, Set.of(PluginStatus.SHUTDOWN, PluginStatus.ERROR));
            allowed.put(PluginStatus.SHUTDOWN, Set.of());
        }

        @Test
        @DisplayName("full transition matrix matches the documented state machine")
        void fullTransitionMatrix() {
            for (PluginStatus from : PluginStatus.values()) {
                for (PluginStatus to : PluginStatus.values()) {
                    boolean expected = from == to || allowed.get(from).contains(to);
                    assertThat(from.canTransitionTo(to))
                            .as("transition %s -> %s", from, to)
                            .isEqualTo(expected);
                }
            }
        }

        @Test
        @DisplayName("null target state is never allowed")
        void nullTargetIsRejected() {
            for (PluginStatus from : PluginStatus.values()) {
                assertThat(from.canTransitionTo(null)).isFalse();
            }
        }

        @Test
        @DisplayName("active and inactive flags")
        void activeInactiveFlags() {
            assertThat(PluginStatus.ENABLED.isActive()).isTrue();
            assertThat(PluginStatus.DISABLED.isInactive()).isTrue();
            for (PluginStatus status : PluginStatus.values()) {
                boolean expectedActive = status == PluginStatus.ENABLED;
                boolean expectedInactive = status == PluginStatus.DISABLED;
                assertThat(status.isActive()).isEqualTo(expectedActive);
                assertThat(status.isInactive()).isEqualTo(expectedInactive);
            }
        }

        @Test
        @DisplayName("toString returns the display name")
        void toStringReturnsDisplayName() {
            assertThat(PluginStatus.UNLOADED.toString()).isEqualTo("Unloaded");
            assertThat(PluginStatus.SHUTTING_DOWN.toString()).isEqualTo("Shutting Down");
            assertThat(PluginStatus.ENABLED.toString()).isEqualTo("Enabled");
        }
    }

    // =====================================================================
    // Plugin interface defaults
    // =====================================================================

    @Nested
    @DisplayName("Plugin interface default methods")
    class PluginDefaultsTests {

        @Test
        @DisplayName("default getState returns DISABLED")
        void defaultGetState() {
            Plugin plugin = Mockito.mock(Plugin.class, Mockito.CALLS_REAL_METHODS);
            assertThat(plugin.getState()).isEqualTo(PluginStatus.DISABLED);
        }

        @Test
        @DisplayName("default setState accepts valid transitions")
        void defaultSetStateValid() {
            Plugin plugin = Mockito.mock(Plugin.class, Mockito.CALLS_REAL_METHODS);
            plugin.setState(PluginStatus.ENABLED);
        }

        @Test
        @DisplayName("default setState rejects invalid transitions")
        void defaultSetStateInvalid() {
            Plugin plugin = Mockito.mock(Plugin.class, Mockito.CALLS_REAL_METHODS);
            assertThatThrownBy(() -> plugin.setState(PluginStatus.UNLOADED))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Invalid state transition");
        }
    }

    // =====================================================================
    // Exceptions
    // =====================================================================

    @Test
    @DisplayName("PluginValidationException carries message and cause")
    void pluginValidationException() {
        PluginValidationException simple = new PluginValidationException("bad config");
        assertThat(simple.getMessage()).isEqualTo("bad config");
        assertThat(simple.getCause()).isNull();

        IllegalStateException cause = new IllegalStateException("root");
        PluginValidationException withCause = new PluginValidationException("bad config", cause);
        assertThat(withCause.getMessage()).isEqualTo("bad config");
        assertThat(withCause.getCause()).isSameAs(cause);
    }

    // =====================================================================
    // Events
    // =====================================================================

    @Nested
    @DisplayName("Editor events")
    class EditorEventsTests {

        @Test
        @DisplayName("ActiveEditorChangedEvent exposes component and path")
        void activeEditorChangedEvent() {
            Object component = new Object();
            EditorEvents.ActiveEditorChangedEvent event =
                    new EditorEvents.ActiveEditorChangedEvent(component, "/tmp/a.txt");
            assertThat(event.getEditorComponent()).isSameAs(component);
            assertThat(event.getFilePath()).isEqualTo("/tmp/a.txt");
            assertThat(event.getTimestamp()).isNotNull();
            assertThat(event.getSource()).isEmpty();
        }

        @Test
        @DisplayName("DocumentChangedEvent exposes content, path and caret")
        void documentChangedEvent() {
            EditorEvents.DocumentChangedEvent event =
                    new EditorEvents.DocumentChangedEvent("text", "/tmp/b.txt", 42);
            assertThat(event.getContent()).isEqualTo("text");
            assertThat(event.getFilePath()).isEqualTo("/tmp/b.txt");
            assertThat(event.getCaretPosition()).isEqualTo(42);
            assertThat(event.getTimestamp()).isNotNull();
            assertThat(event.getSource()).isEmpty();
        }

        @Test
        @DisplayName("BeforeFileSaveEvent exposes content and path")
        void beforeFileSaveEvent() {
            EditorEvents.BeforeFileSaveEvent event =
                    new EditorEvents.BeforeFileSaveEvent("content", "/tmp/c.txt");
            assertThat(event.getContent()).isEqualTo("content");
            assertThat(event.getFilePath()).isEqualTo("/tmp/c.txt");
            assertThat(event.getTimestamp()).isNotNull();
            assertThat(event.getSource()).isEmpty();
        }

        @Test
        @DisplayName("FileOpenedEvent exposes path and content")
        void fileOpenedEvent() {
            EditorEvents.FileOpenedEvent event =
                    new EditorEvents.FileOpenedEvent("/tmp/d.txt", "data");
            assertThat(event.getFilePath()).isEqualTo("/tmp/d.txt");
            assertThat(event.getContent()).isEqualTo("data");
            assertThat(event.getTimestamp()).isNotNull();
            assertThat(event.getSource()).isEmpty();
        }
    }

    @Test
    @DisplayName("FileCreatedEvent derives file type from extension")
    void fileCreatedEvent() {
        FileCreatedEvent withExtension = new FileCreatedEvent("test", "/tmp/doc.txt");
        assertThat(withExtension.getFilePath()).isEqualTo("/tmp/doc.txt");
        assertThat(withExtension.getFileType()).isEqualTo("txt");
        assertThat(withExtension.getFile()).isNotNull();
        assertThat(withExtension.getSource()).isEqualTo("test");
        assertThat(withExtension.getTimestamp()).isNotNull();

        FileCreatedEvent withoutExtension = new FileCreatedEvent("test", "/tmp/README");
        assertThat(withoutExtension.getFileType()).isEmpty();
    }

    @Nested
    @DisplayName("CodeCompiledEvent")
    class CodeCompiledEventTests {

        @Test
        @DisplayName("basic constructor initialises identity fields")
        void basicConstructor() {
            CodeCompiledEvent event = new CodeCompiledEvent("compiler", "Main", "/src/Main.java");
            assertThat(event.getSource()).isEqualTo("compiler");
            assertThat(event.getClassName()).isEqualTo("Main");
            assertThat(event.getFilePath()).isEqualTo("/src/Main.java");
            assertThat(event.getTimestamp()).isNotNull();
            assertThat(event.getStatus()).isEqualTo("FAILED");
        }

        @Test
        @DisplayName("success constructor records timing")
        void successConstructor() {
            CodeCompiledEvent event =
                    new CodeCompiledEvent("compiler", "Main", "/src/Main.java", true, 250L);
            assertThat(event.isSuccess()).isTrue();
            assertThat(event.getCompilationTime()).isEqualTo(250L);
            assertThat(event.getStatus()).isEqualTo("SUCCESS");
        }

        @Test
        @DisplayName("no-args constructor produces empty event")
        void noArgsConstructor() {
            CodeCompiledEvent event = new CodeCompiledEvent();
            assertThat(event.isSuccess()).isFalse();
            assertThat(event.getStatus()).isEqualTo("FAILED");
            assertThat(event.getTimestamp()).isNotNull();
        }

        @Test
        @DisplayName("error and warning counters")
        void errorWarningCounters() {
            CodeCompiledEvent event = new CodeCompiledEvent();
            assertThat(event.hasErrors()).isFalse();
            assertThat(event.hasWarnings()).isFalse();
            event.setErrorCount(2);
            event.setWarningCount(3);
            assertThat(event.hasErrors()).isTrue();
            assertThat(event.hasWarnings()).isTrue();
        }

        @Test
        @DisplayName("compilation speed handles zero values")
        void compilationSpeed() {
            CodeCompiledEvent event = new CodeCompiledEvent();
            assertThat(event.getCompilationSpeed()).isZero();

            event.setCompilationTime(0L);
            event.setLineCount(100);
            assertThat(event.getCompilationSpeed()).isZero();

            event.setCompilationTime(1000L);
            event.setLineCount(0);
            assertThat(event.getCompilationSpeed()).isZero();

            event.setCompilationTime(1000L);
            event.setLineCount(500);
            assertThat(event.getCompilationSpeed()).isEqualTo(500.0);
        }

        @Test
        @DisplayName("file name extraction handles all separators")
        void fileNameExtraction() {
            CodeCompiledEvent event = new CodeCompiledEvent();

            event.setFilePath("/a/b/c/File.java");
            assertThat(event.getFileName()).isEqualTo("File.java");

            event.setFilePath("C:\\dir\\File.java");
            assertThat(event.getFileName()).isEqualTo("File.java");

            event.setFilePath("Plain.java");
            assertThat(event.getFileName()).isEqualTo("Plain.java");

            event.setFilePath(null);
            assertThat(event.getFileName()).isNull();
        }

        @Test
        @DisplayName("file extension extraction handles edge cases")
        void fileExtensionExtraction() {
            CodeCompiledEvent event = new CodeCompiledEvent();

            event.setFilePath("/a/b/File.Java");
            assertThat(event.getFileExtension()).isEqualTo("java");

            event.setFilePath("noextension");
            assertThat(event.getFileExtension()).isEmpty();

            event.setFilePath(".hidden");
            assertThat(event.getFileExtension()).isEmpty();

            event.setFilePath(null);
            assertThat(event.getFileExtension()).isEmpty();
        }
    }

    // =====================================================================
    // Service configuration and lifecycle listener
    // =====================================================================

    @Test
    @DisplayName("PluginServiceConfiguration static factories are unsupported stubs")
    void pluginServiceConfigurationStubs() {
        assertThatThrownBy(PluginServiceConfiguration::builder)
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("not available");
        assertThatThrownBy(() -> PluginServiceConfiguration.fromProperties(Map.of()))
                .isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(PluginServiceConfiguration::getGlobal)
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    @DisplayName("PluginLifecycleListener default callbacks are no-ops")
    void pluginLifecycleListenerDefaults() {
        PluginLifecycleListener listener = new PluginLifecycleListener() {
        };
        listener.onBeforeLoad("p1");
        listener.onLoaded("p1");
        listener.onLoadFailed("p1", new IllegalStateException("load"));
        listener.onBeforeInitialize("p1");
        listener.onInitialized("p1");
        listener.onInitializationFailed("p1", new IllegalStateException("init"));
        listener.onBeforeEnable("p1");
        listener.onEnabled("p1");
        listener.onEnableFailed("p1", new IllegalStateException("enable"));
        listener.onBeforeDisable("p1");
        listener.onDisabled("p1");
        listener.onDisableFailed("p1", new IllegalStateException("disable"));
        listener.onBeforeUnload("p1");
        listener.onUnloaded("p1");
        listener.onBeforeUpgrade("p1", "1.0", "2.0");
        listener.onUpgraded("p1", "1.0", "2.0");
        listener.onPluginError("p1", new IllegalStateException("boom"));
        listener.onStateChanged("p1", "ENABLED", "DISABLED");
    }

    // =====================================================================
    // PluginManagerProvider (double-checked singleton + stub manager)
    // =====================================================================

    @Nested
    @DisplayName("PluginManagerProvider")
    class PluginManagerProviderTests {

        @Test
        @DisplayName("provider can be instantiated")
        void providerConstructor() {
            assertThat(new PluginManagerProvider()).isNotNull();
        }

        @Test
        @DisplayName("getInstance returns a stable singleton")
        void singletonIsStable() {
            PluginManager first = PluginManagerProvider.getInstance();
            PluginManager second = PluginManagerProvider.getInstance();
            assertThat(first).isNotNull();
            assertThat(second).isSameAs(first);
        }

        @Test
        @DisplayName("stub manager supports enable/disable lifecycle")
        void stubManagerLifecycle() throws Exception {
            PluginManager manager = PluginManagerProvider.getInstance();

            assertThat(manager.getPluginStatus("ghost")).isEqualTo(PluginStatus.UNLOADED);
            assertThat(manager.isPluginEnabled("p1")).isFalse();

            manager.enablePlugin("p1");
            assertThat(manager.isPluginEnabled("p1")).isTrue();
            assertThat(manager.getAllPluginStates()).containsEntry("p1", true);

            manager.disablePlugin("p1");
            assertThat(manager.isPluginEnabled("p1")).isFalse();
            assertThat(manager.getAllPluginStates()).containsEntry("p1", false);

            manager.enablePluginByName("p1");
            assertThat(manager.isPluginEnabled("p1")).isTrue();

            manager.loadPlugin(new java.io.File("nonexistent.jar"));
            manager.loadAllPlugins();

            assertThat(manager.getPlugin("unknown")).isNull();
            assertThat(manager.getLoadedPlugins()).isEmpty();

            manager.unloadPlugin("p1");
            assertThat(manager.getAllPluginStates()).doesNotContainKey("p1");

            manager.shutdownAll();
        }

        @Test
        @DisplayName("stub manager context and plugin inspection via reflection")
        void stubManagerContextAndReflection() throws Exception {
            PluginManager manager = PluginManagerProvider.getInstance();
            assertThat(manager.getPluginContext()).isNotNull();
            assertThat(manager.getPluginContext().getPluginId()).isEqualTo("stub-plugin-manager");

            // The stub's private 'plugins' map is otherwise unreachable; use
            // reflection so getEnabledPlugins/getPluginStatus true-branches run.
            Field pluginsField = manager.getClass().getDeclaredField("plugins");
            pluginsField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Plugin> plugins = (Map<String, Plugin>) pluginsField.get(manager);

            Plugin plugin = Mockito.mock(Plugin.class);
            plugins.put("reflected", plugin);
            try {
                manager.enablePlugin("reflected");
                assertThat(manager.getPlugin("reflected")).isSameAs(plugin);
                assertThat(manager.getLoadedPlugins()).containsExactly(plugin);
                assertThat(manager.getEnabledPlugins()).containsExactly(plugin);
                assertThat(manager.getPluginStatus("reflected")).isEqualTo(PluginStatus.ENABLED);

                manager.disablePlugin("reflected");
                assertThat(manager.getEnabledPlugins()).isEmpty();
                assertThat(manager.getPluginStatus("reflected")).isEqualTo(PluginStatus.DISABLED);

                manager.disableAllPlugins();
                assertThat(manager.getAllPluginStates()).containsEntry("reflected", false);
            } finally {
                plugins.remove("reflected");
                manager.unloadPlugin("reflected");
            }
        }
    }

    // =====================================================================
    // PluginServiceInitializer + stub services
    // =====================================================================

    @Nested
    @DisplayName("PluginServiceInitializer stubs")
    class ServiceInitializerStubTests {

        private final PluginServiceLocator locator =
                PluginServiceInitializer.createServiceLocator(
                        new com.protonmail.landrevillejf.swingide.core.registry.SimpleServiceRegistry(),
                        new PluginEventBus(),
                        new com.protonmail.landrevillejf.swingide.core.bus.EventBus());

        @Test
        @DisplayName("initializer can be instantiated")
        void initializerConstructor() {
            assertThat(new PluginServiceInitializer()).isNotNull();
        }

        @Test
        @DisplayName("locator exposes every stub service")
        void locatorExposesAllServices() {
            assertThat(locator.getLoggingService()).isNotNull();
            assertThat(locator.getCacheService()).isNotNull();
            assertThat(locator.getNotificationService()).isNotNull();
            assertThat(locator.getMetricsService()).isNotNull();
            assertThat(locator.getPermissionService()).isNotNull();
            assertThat(locator.getAsyncTaskExecutor()).isNotNull();
            assertThat(locator.getConfigurationValidator()).isNotNull();
            assertThat(locator.getHookService()).isNotNull();
            assertThat(locator.getDataStore()).isNotNull();
            assertThat(locator.getResourceManager()).isNotNull();
            assertThat(locator.getDependencyResolver()).isNotNull();
            assertThat(locator.getUpdateService()).isNotNull();
            assertThat(locator.getMonitoringService()).isNotNull();
        }

        @Test
        @DisplayName("locator generic accessors")
        void locatorGenericAccessors() {
            assertThat(locator.getService(
                    com.protonmail.landrevillejf.ide.plugin.service.PluginLoggingService.class))
                    .isNotNull();
            locator.registerService(String.class, "custom");
            assertThat(locator.getService(String.class)).isEqualTo("custom");
        }

        @Test
        @DisplayName("stub logging service behaviour")
        void stubLoggingService() {
            var logging = locator.getLoggingService();
            logging.setLogLevel("p", com.protonmail.landrevillejf.ide.plugin.service
                    .PluginLoggingService.LogLevel.DEBUG);
            assertThat(logging.getLogLevel("p"))
                    .isEqualTo(com.protonmail.landrevillejf.ide.plugin.service
                            .PluginLoggingService.LogLevel.INFO);
            logging.log("p", com.protonmail.landrevillejf.ide.plugin.service
                    .PluginLoggingService.LogLevel.INFO, "msg");
            logging.log("p", com.protonmail.landrevillejf.ide.plugin.service
                    .PluginLoggingService.LogLevel.INFO, "msg", new RuntimeException());
            logging.logf("p", com.protonmail.landrevillejf.ide.plugin.service
                    .PluginLoggingService.LogLevel.INFO, "%s", "arg");
            logging.clearLogs("p");
            logging.setConsoleOutput("p", true);
            logging.setFileOutput("p", true, "/tmp/log.txt");
            assertThat(logging.getRecentLogs("p", 10)).isEmpty();
            assertThat(logging.getStatistics("p")).isEmpty();
        }

        @Test
        @DisplayName("stub permission service behaviour")
        void stubPermissionService() {
            var permissions = locator.getPermissionService();
            assertThat(permissions.grantPermission("p", "read")).isTrue();
            assertThat(permissions.hasPermission("p", "read")).isTrue();
            assertThat(permissions.hasPermission("p", "write")).isFalse();
            assertThat(permissions.hasPermission("unknown-plugin", "read")).isFalse();
            assertThat(permissions.hasAllPermissions("p", "read")).isTrue();
            assertThat(permissions.hasAllPermissions("p", "read", "write")).isFalse();
            assertThat(permissions.hasAllPermissions("unknown-plugin", "read")).isFalse();
            assertThat(permissions.hasAnyPermission("p", "write", "read")).isTrue();
            assertThat(permissions.hasAnyPermission("p", "write", "admin")).isFalse();
            assertThat(permissions.hasAnyPermission("unknown-plugin", "read")).isFalse();
            assertThat(permissions.getPluginPermissions("p")).containsExactly("read");
            assertThat(permissions.revokePermission("p", "read")).isTrue();
            assertThat(permissions.revokePermission("p", "read")).isFalse();
            assertThat(permissions.revokePermission("unknown-plugin", "read")).isFalse();
            assertThat(permissions.assignRole("p", "role")).isTrue();
            assertThat(permissions.removeRole("p", "role")).isTrue();
            assertThat(permissions.getPluginRoles("p")).isEmpty();
            assertThat(permissions.createPermission("id", "desc", "cat")).isNull();
            permissions.registerSystemPermission(null);
            assertThat(permissions.getPermission("id")).isNull();
            assertThat(permissions.getAllPermissions()).isEmpty();
            assertThat(permissions.getPermissionsByCategory("cat")).isEmpty();
            assertThat(permissions.createRole("r", "name", "desc")).isNull();
            assertThat(permissions.getRole("r")).isNull();
            assertThat(permissions.getAllRoles()).isEmpty();
            assertThat(permissions.getAuditLog("p")).isEmpty();
            permissions.clearAuditLog("p");
        }

        @Test
        @DisplayName("stub metrics service behaviour")
        void stubMetricsService() {
            var metrics = locator.getMetricsService();
            metrics.incrementCounter("p", "hits");
            assertThat(metrics.getCounterValue("p", "hits")).isEqualTo(1L);
            metrics.incrementCounter("p", "hits", 5L);
            assertThat(metrics.getCounterValue("p", "hits")).isEqualTo(6L);
            metrics.decrementCounter("p", "hits");
            assertThat(metrics.getCounterValue("p", "hits")).isEqualTo(5L);
            metrics.recordTimer("p", "op", 10L);
            metrics.recordHistogram("p", "size", 42L);
            metrics.setGauge("p", "temp", 7L);
            metrics.resetMetric("p", "hits");
            assertThat(metrics.getCounterValue("p", "hits")).isZero();
            metrics.incrementCounter("p", "other", 2L);
            metrics.resetMetrics("p");
            assertThat(metrics.getCounterValue("p", "other")).isZero();
            assertThat(metrics.getAllMetrics("p")).isEmpty();
        }

        @Test
        @DisplayName("stub async executor behaviour")
        void stubAsyncExecutor() {
            var executor = locator.getAsyncTaskExecutor();
            assertThat(executor.executeTask("p", () -> {
            })).isNull();
            assertThat(executor.executeNamedTask("p", "task", () -> {
            })).startsWith("task-");
            assertThat(executor.executeCallable("p", () -> "v")).isNull();
            assertThat(executor.getTask("task-1")).isNull();
            assertThat(executor.getPluginTasks("p")).isEmpty();
            assertThat(executor.getActiveTasks("p")).isEmpty();
            assertThat(executor.cancelAllTasks("p")).isZero();
            executor.setThreadPoolSize("p", 4);
            executor.shutdown("p");
        }

        @Test
        @DisplayName("stub config validator behaviour")
        void stubConfigValidator() {
            var validator = locator.getConfigurationValidator();
            validator.registerSchema("p", Map.of());
            validator.registerCustomValidator("p", "path", value -> null);
            var result = validator.validateConfiguration("p", Map.of());
            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrors()).isEmpty();
            assertThat(result.getWarnings()).isEmpty();
        }

        @Test
        @DisplayName("stub notification service behaviour")
        void stubNotificationService() {
            var notifications = locator.getNotificationService();
            var id = notifications.notify("p",
                    com.protonmail.landrevillejf.ide.plugin.service.PluginNotificationService
                            .NotificationType.INFO,
                    com.protonmail.landrevillejf.ide.plugin.service.PluginNotificationService
                            .Priority.NORMAL,
                    "title", "message");
            assertThat(id).startsWith("notif-");
            assertThat(notifications.getRecentNotifications("p", 5)).isEmpty();
            notifications.registerListener("p", notification -> {
            });
            notifications.unregisterListener("p", notification -> {
            });
            notifications.clearNotifications("p");
        }

        @Test
        @DisplayName("stub monitoring service behaviour")
        void stubMonitoringService() {
            var monitoring = locator.getMonitoringService();
            var report = monitoring.getHealthReport("p");
            assertThat(report.getPluginId()).isEqualTo("p");
            assertThat(report.getCpuUsage()).isZero();
            assertThat(report.getMemoryUsage()).isZero();
            assertThat(report.getThreadCount()).isZero();
            assertThat(report.getUptime()).isZero();
            assertThat(report.getErrorCount()).isZero();
            assertThat(report.getWarningCount()).isZero();
        }

        @Test
        @DisplayName("stub update service behaviour")
        void stubUpdateService() {
            var updates = locator.getUpdateService();
            assertThat(updates.checkForUpdates("p",
                    com.protonmail.landrevillejf.ide.plugin.service.PluginUpdateService
                            .UpdateChannel.STABLE)).isNull();
            assertThat(updates.getVersionHistory("p")).isEmpty();
            updates.setUpdateChannel("p",
                    com.protonmail.landrevillejf.ide.plugin.service.PluginUpdateService
                            .UpdateChannel.STABLE);
        }

        @Test
        @DisplayName("stub resource manager behaviour")
        void stubResourceManager() {
            var resources = locator.getResourceManager();
            assertThat(resources.getResource("r")).isNull();
            assertThat(resources.getResourceValue("r", String.class)).isNull();
            assertThat(resources.getPluginResources("p")).isEmpty();
            assertThat(resources.getAllResources()).isEmpty();
            assertThat(resources.getResourcesByType("type")).isEmpty();
            assertThat(resources.getAccessibleResources("p")).isEmpty();
            assertThat(resources.getAccessAuditLog("r")).isEmpty();
        }

        @Test
        @DisplayName("createExtendedPluginContext wires services")
        void createExtendedPluginContext() {
            var registry = new com.protonmail.landrevillejf.swingide.core.registry.SimpleServiceRegistry();
            ExtendedPluginContext context = PluginServiceInitializer.createExtendedPluginContext(
                    registry,
                    new PluginEventBus(),
                    new com.protonmail.landrevillejf.swingide.core.bus.EventBus(),
                    null,
                    new java.io.File("build/tmp-plugin-data"),
                    "context-test");
            assertThat(context).isNotNull();
            assertThat(context.getPluginId()).isEqualTo("context-test");
            assertThat(context.getLoggingService()).isNotNull();
        }
    }
}
