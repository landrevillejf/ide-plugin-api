package com.protonmail.landrevillejf.ide.plugin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PluginInterfaceTest {

    @Mock
    private PluginDescriptor descriptor;

    private TestPlugin plugin;

    // Implémentation concrète pour tester les méthodes default
    private static class TestPlugin implements Plugin {
        private PluginDescriptor descriptor;
        private PluginStatus state = PluginStatus.DISABLED;
        private boolean enabled = false;
        private String authorEmail = "test@example.com";
        private String category = "Test";
        private String specificationTitle = "Test Spec";
        private String specificationVersion = "1.0.0";
        private String specificationVendor = "Test Vendor";
        private String implementationVersion = "1.0.0";
        private List<String> dependencies = List.of();
        private String requiredHostVersion = "1.0.0";

        public void setDescriptor(PluginDescriptor descriptor) {
            this.descriptor = descriptor;
        }

        @Override
        public PluginDescriptor getDescriptor() {
            return descriptor;
        }

        @Override
        public String getAuthorEmail() {
            return authorEmail;
        }

        @Override
        public String getCategory() {
            return category;
        }

        @Override
        public String getSpecificationTitle() {
            return specificationTitle;
        }

        @Override
        public String getSpecificationVersion() {
            return specificationVersion;
        }

        @Override
        public String getSpecificationVendor() {
            return specificationVendor;
        }

        @Override
        public String getImplementationVersion() {
            return implementationVersion;
        }

        @Override
        public void initialize(PluginContext context) {
        }

        @Override
        public void enable() {
            enabled = true;
            state = PluginStatus.ENABLED;
        }

        @Override
        public void disable() {
            enabled = false;
            state = PluginStatus.DISABLED;
        }

        @Override
        public void shutdown() {
        }

        @Override
        public boolean isEnabled() {
            return enabled;
        }

        @Override
        public List<String> getDependencies() {
            return dependencies;
        }

        @Override
        public void injectDependencies(Map<String, Object> dependencies) {
        }

        @Override
        public boolean validate() {
            return true;
        }

        @Override
        public String getRequiredHostVersion() {
            return requiredHostVersion;
        }

        @Override
        public PluginStatus getState() {
            return state;
        }

        @Override
        public void setState(PluginStatus newState) {
            if (!state.canTransitionTo(newState)) {
                throw new IllegalStateException("Invalid transition");
            }
            state = newState;
        }
    }

    @BeforeEach
    void setUp() {
        plugin = new TestPlugin();
        // Ne pas stuber le descriptor ici - on le fait uniquement dans les tests qui en ont besoin
    }

    @Test
    void getName_ShouldReturnDescriptorName() {
        // Given
        when(descriptor.getName()).thenReturn("Test Plugin");
        plugin.setDescriptor(descriptor);

        // When
        String name = plugin.getName();

        // Then
        assertEquals("Test Plugin", name);
        verify(descriptor).getName();
    }

    @Test
    void getVersion_ShouldReturnDescriptorVersion() {
        // Given
        when(descriptor.getVersion()).thenReturn("1.0.0");
        plugin.setDescriptor(descriptor);

        // When
        String version = plugin.getVersion();

        // Then
        assertEquals("1.0.0", version);
        verify(descriptor).getVersion();
    }

    @Test
    void getDescription_ShouldReturnDescriptorDescription() {
        // Given
        when(descriptor.getDescription()).thenReturn("Test Description");
        plugin.setDescriptor(descriptor);

        // When
        String description = plugin.getDescription();

        // Then
        assertEquals("Test Description", description);
        verify(descriptor).getDescription();
    }

    @Test
    void getAuthor_ShouldReturnDescriptorAuthor() {
        // Given
        when(descriptor.getAuthor()).thenReturn("Test Author");
        plugin.setDescriptor(descriptor);

        // When
        String author = plugin.getAuthor();

        // Then
        assertEquals("Test Author", author);
        verify(descriptor).getAuthor();
    }

    @Test
    void getCustomMetadata_ShouldReturnEmptyMapByDefault() {
        // When
        Map<String, Object> metadata = plugin.getCustomMetadata();

        // Then
        assertNotNull(metadata);
        assertTrue(metadata.isEmpty());
    }

    @Test
    void preInitialize_ShouldNotThrowException() {
        // When/Then
        assertDoesNotThrow(() -> plugin.preInitialize());
    }

    @Test
    void beforeEnable_ShouldReturnTrueByDefault() {
        // When
        boolean result = plugin.beforeEnable();

        // Then
        assertTrue(result);
    }

    @Test
    void afterEnable_ShouldNotThrowException() {
        // When/Then
        assertDoesNotThrow(() -> plugin.afterEnable());
    }

    @Test
    void beforeDisable_ShouldReturnTrueByDefault() {
        // When
        boolean result = plugin.beforeDisable();

        // Then
        assertTrue(result);
    }

    @Test
    void afterDisable_ShouldNotThrowException() {
        // When/Then
        assertDoesNotThrow(() -> plugin.afterDisable());
    }

    @Test
    void onStart_ShouldNotThrowException() {
        // When/Then
        assertDoesNotThrow(() -> plugin.onStart());
    }

    @Test
    void onStop_ShouldNotThrowException() {
        // When/Then
        assertDoesNotThrow(() -> plugin.onStop());
    }

    @Test
    void cleanup_ShouldNotThrowException() {
        // When/Then
        assertDoesNotThrow(() -> plugin.cleanup());
    }

    @Test
    void getState_ShouldReturnDisabledByDefault() {
        // When
        PluginStatus state = plugin.getState();

        // Then
        assertEquals(PluginStatus.DISABLED, state);
    }

    @Test
    void setState_WithValidTransition_ShouldChangeState() {
        // When
        plugin.setState(PluginStatus.ENABLED);

        // Then
        assertEquals(PluginStatus.ENABLED, plugin.getState());
    }

    @Test
    void setState_WithInvalidTransition_ShouldThrowException() {
        // Given
        plugin.setState(PluginStatus.ENABLED);

        // When/Then
        assertThrows(IllegalStateException.class, () -> {
            plugin.setState(PluginStatus.LOADED);
        });
    }

    @Test
    void getConfig_ShouldReturnDefaultConfig() {
        // When
        PluginConfig config = plugin.getConfig();

        // Then
        assertSame(PluginConfig.DEFAULT, config);
    }

    @Test
    void getConfigurationSchema_ShouldReturnEmptyMap() {
        // When
        Map<String, Object> schema = plugin.getConfigurationSchema();

        // Then
        assertNotNull(schema);
        assertTrue(schema.isEmpty());
    }

    @Test
    void updateConfiguration_ShouldReturnTrueByDefault() {
        // When
        boolean result = plugin.updateConfiguration(Map.of("key", "value"));

        // Then
        assertTrue(result);
    }

    @Test
    void saveSettings_ShouldReturnTrueByDefault() {
        // When
        boolean result = plugin.saveSettings(Map.of("setting", "value"));

        // Then
        assertTrue(result);
    }

    @Test
    void loadSettings_ShouldReturnEmptyMap() {
        // When
        Map<String, Object> settings = plugin.loadSettings();

        // Then
        assertNotNull(settings);
        assertTrue(settings.isEmpty());
    }

    @Test
    void getProvidedResources_ShouldReturnEmptyMap() {
        // When
        Map<String, Object> resources = plugin.getProvidedResources();

        // Then
        assertNotNull(resources);
        assertTrue(resources.isEmpty());
    }

    @Test
    void provideResource_ShouldReturnNull() {
        // When
        Object resource = plugin.provideResource("any-key");

        // Then
        assertNull(resource);
    }

    @Test
    void getPluginClassLoader_ShouldReturnNonNull() {
        // When
        ClassLoader classLoader = plugin.getPluginClassLoader();

        // Then
        assertNotNull(classLoader);
    }

    @Test
    void requiresIsolation_ShouldReturnFalseByDefault() {
        // When
        boolean result = plugin.requiresIsolation();

        // Then
        assertFalse(result);
    }

    @Test
    void validateDependencies_ShouldReturnTrueByDefault() {
        // When
        boolean result = plugin.validateDependencies();

        // Then
        assertTrue(result);
    }

    @Test
    void validateConfiguration_ShouldReturnTrueByDefault() {
        // When
        boolean result = plugin.validateConfiguration(Map.of());

        // Then
        assertTrue(result);
    }

    @Test
    void getMinimumHostVersion_ShouldReturnRequiredHostVersion() {
        // When
        String minVersion = plugin.getMinimumHostVersion();

        // Then
        assertEquals(plugin.getRequiredHostVersion(), minVersion);
    }

    @Test
    void getMaximumHostVersion_ShouldReturnNullByDefault() {
        // When
        String maxVersion = plugin.getMaximumHostVersion();

        // Then
        assertNull(maxVersion);
    }

    @Test
    void checkCompatibility_ShouldReturnCompatibleResult() {
        // When
        Plugin.CompatibilityResult result = plugin.checkCompatibility();

        // Then
        assertNotNull(result);
        assertTrue(result.isCompatible());
        assertEquals("Compatible", result.getMessage());
    }

    @Test
    void getIncompatibilities_ShouldReturnEmptyList() {
        // When
        List<String> incompatibilities = plugin.getIncompatibilities();

        // Then
        assertNotNull(incompatibilities);
        assertTrue(incompatibilities.isEmpty());
    }

    @Test
    void canUpgradeTo_ShouldReturnTrueByDefault() {
        // When
        boolean result = plugin.canUpgradeTo("2.0.0");

        // Then
        assertTrue(result);
    }

    @Test
    void handleEvent_WithEnumType_ShouldNotThrowException() {
        // When/Then
        assertDoesNotThrow(() -> plugin.handleEvent(Plugin.PluginEventType.CONFIG_CHANGED, "data"));
    }

    @Test
    void handleEvent_WithStringType_ShouldConvertToEnum() {
        // When/Then
        assertDoesNotThrow(() -> plugin.handleEvent("CONFIG_CHANGED", "data"));
    }

    @Test
    void handleEvent_WithInvalidStringType_ShouldUseCustomEvent() {
        // When/Then
        assertDoesNotThrow(() -> plugin.handleEvent("UNKNOWN_EVENT", "data"));
    }

    @Test
    void publishEvent_WithStringType_ShouldNotThrowException() {
        // When/Then
        assertDoesNotThrow(() -> plugin.publishEvent("test-event", "data"));
    }

    @Test
    void publishEvent_WithEnumType_ShouldDelegateToStringVersion() {
        // When/Then
        assertDoesNotThrow(() -> plugin.publishEvent(Plugin.PluginEventType.CONFIG_CHANGED, "data"));
    }

    @Test
    void getPublishedEvents_ShouldReturnEmptyList() {
        // When
        List<String> events = plugin.getPublishedEvents();

        // Then
        assertNotNull(events);
        assertTrue(events.isEmpty());
    }

    @Test
    void getSubscribedEvents_ShouldReturnEmptyList() {
        // When
        List<String> events = plugin.getSubscribedEvents();

        // Then
        assertNotNull(events);
        assertTrue(events.isEmpty());
    }

    @Test
    void onError_ShouldNotThrowException() {
        // When/Then
        assertDoesNotThrow(() -> plugin.onError(new RuntimeException()));
    }

    @Test
    void handleUncaughtException_ShouldPrintToSystemErr() {
        // Given - initialiser le descriptor pour que getName() fonctionne
        when(descriptor.getName()).thenReturn("Test Plugin");
        plugin.setDescriptor(descriptor);

        ByteArrayOutputStream errStream = new ByteArrayOutputStream();
        PrintStream originalErr = System.err;
        System.setErr(new PrintStream(errStream));

        try {
            RuntimeException exception = new RuntimeException("Test exception");

            // When
            plugin.handleUncaughtException(Thread.currentThread(), exception);

            // Then
            String output = errStream.toString();
            assertTrue(output.contains("Uncaught exception in plugin"));
            assertTrue(output.contains("Test Plugin"));
            assertTrue(output.contains("Test exception"));
        } finally {
            System.setErr(originalErr);
        }
    }

    @Test
    void isRecoverable_ShouldReturnTrueByDefault() {
        // When
        boolean result = plugin.isRecoverable();

        // Then
        assertTrue(result);
    }

    @Test
    void getMetrics_ShouldReturnEmptyMap() {
        // When
        Map<String, Object> metrics = plugin.getMetrics();

        // Then
        assertNotNull(metrics);
        assertTrue(metrics.isEmpty());
    }

    @Test
    void resetMetrics_ShouldNotThrowException() {
        // When/Then
        assertDoesNotThrow(() -> plugin.resetMetrics());
    }

    @Test
    void getAverageStartupTime_ShouldReturnZero() {
        // When
        long time = plugin.getAverageStartupTime();

        // Then
        assertEquals(0L, time);
    }

    @Test
    void healthCheck_ShouldReturnHealthyStatus() {
        // When
        Plugin.HealthStatus status = plugin.healthCheck();

        // Then
        assertNotNull(status);
        assertEquals(Plugin.HealthStatus.UP, status.getStatus());
        assertEquals("Healthy", status.getMessage());
        assertNotNull(status.getDetails());
    }

    @Test
    void getLocalizedMessage_ShouldReturnKeyWhenNoLocalization() {
        // When
        String message = plugin.getLocalizedMessage("test.key", "en");

        // Then
        assertEquals("test.key", message);
    }

    @Test
    void getDocumentationUrl_ShouldReturnNull() {
        // When
        String url = plugin.getDocumentationUrl();

        // Then
        assertNull(url);
    }

    @Test
    void getHelpText_ShouldReturnDescription() {
        // Given
        when(descriptor.getDescription()).thenReturn("Test Description");
        plugin.setDescriptor(descriptor);

        // When
        String helpText = plugin.getHelpText();

        // Then
        assertEquals("Test Description", helpText);
    }

    @Test
    void getUsageExamples_ShouldReturnEmptyList() {
        // When
        List<String> examples = plugin.getUsageExamples();

        // Then
        assertNotNull(examples);
        assertTrue(examples.isEmpty());
    }

    @Test
    void getDefaultPermissions_ShouldReturnEmptyList() {
        // When
        List<String> permissions = plugin.getDefaultPermissions();

        // Then
        assertNotNull(permissions);
        assertTrue(permissions.isEmpty());
    }

    @Test
    void onUpgrade_ShouldNotThrowException() {
        // When/Then
        assertDoesNotThrow(() -> plugin.onUpgrade("1.0.0", "2.0.0"));
    }

    @Test
    void compatibilityResult_ConstructorAndGetters() {
        // When
        Plugin.CompatibilityResult result = new Plugin.CompatibilityResult(true, "Compatible message");

        // Then
        assertTrue(result.isCompatible());
        assertEquals("Compatible message", result.getMessage());

        // When - incompatible
        Plugin.CompatibilityResult incompatible = new Plugin.CompatibilityResult(false, "Incompatible message");

        // Then
        assertFalse(incompatible.isCompatible());
    }

    @Test
    void healthStatus_ConstructorsAndGetters() {
        // When - simple constructor
        Plugin.HealthStatus status = new Plugin.HealthStatus(Plugin.HealthStatus.UP, "Healthy");

        // Then
        assertEquals(Plugin.HealthStatus.UP, status.getStatus());
        assertEquals("Healthy", status.getMessage());
        assertNotNull(status.getDetails());
        assertTrue(status.getDetails().isEmpty());

        // When - with details
        Map<String, Object> details = Map.of("cpu", 0.5, "memory", 1024);
        Plugin.HealthStatus statusWithDetails = new Plugin.HealthStatus(Plugin.HealthStatus.DEGRADED, "Degraded", details);

        // Then
        assertEquals(Plugin.HealthStatus.DEGRADED, statusWithDetails.getStatus());
        assertEquals("Degraded", statusWithDetails.getMessage());
        assertEquals(details, statusWithDetails.getDetails());
    }

    @Test
    void pluginEventType_ValuesShouldExist() {
        // Then
        assertNotNull(Plugin.PluginEventType.valueOf("CONFIG_CHANGED"));
        assertNotNull(Plugin.PluginEventType.valueOf("DEPENDENCY_LOADED"));
        assertNotNull(Plugin.PluginEventType.valueOf("USER_INTERACTION"));
        assertNotNull(Plugin.PluginEventType.valueOf("SYSTEM_EVENT"));
        assertNotNull(Plugin.PluginEventType.valueOf("CUSTOM_EVENT"));
    }
}