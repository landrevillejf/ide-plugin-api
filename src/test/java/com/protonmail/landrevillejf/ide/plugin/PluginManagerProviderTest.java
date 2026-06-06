package com.protonmail.landrevillejf.ide.plugin;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.parallel.Isolated;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Isolated
class PluginManagerProviderTest {

    @BeforeEach
    void setUp() throws Exception {
        // Reset the singleton instance before each test using reflection
        resetSingleton();
    }

    @AfterEach
    void tearDown() throws Exception {
        // Clean up after each test
        resetSingleton();
    }

    private void resetSingleton() throws Exception {
        Field instanceField = PluginManagerProvider.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    @Test
    void getInstance_ShouldReturnSameInstance_WhenCalledMultipleTimes() {
        // When
        PluginManager firstInstance = PluginManagerProvider.getInstance();
        PluginManager secondInstance = PluginManagerProvider.getInstance();

        // Then
        assertNotNull(firstInstance);
        assertSame(firstInstance, secondInstance);
    }

    @Test
    void getInstance_ShouldReturnStubPluginManager_WhenNoServiceLoaderImplementation() {
        // When - no real implementation found via ServiceLoader
        PluginManager pluginManager = PluginManagerProvider.getInstance();

        // Then - should return stub implementation
        assertNotNull(pluginManager);

        // Verify stub behavior
        assertNotNull(pluginManager.getPluginContext());
        assertFalse(pluginManager.isPluginEnabled("test.plugin"));

        pluginManager.enablePlugin("test.plugin");
        assertTrue(pluginManager.isPluginEnabled("test.plugin"));

        pluginManager.disablePlugin("test.plugin");
        assertFalse(pluginManager.isPluginEnabled("test.plugin"));
    }

    @Test
    void stubPluginManager_GetPluginContext_ShouldReturnNonNullContext() {
        // Given
        PluginManager pluginManager = PluginManagerProvider.getInstance();

        // When
        PluginContext context = pluginManager.getPluginContext();

        // Then
        assertNotNull(context);
        assertTrue(context instanceof DefaultPluginContext);
    }

    @Test
    void stubPluginManager_EnablePlugin_ShouldChangePluginState() {
        // Given
        PluginManager pluginManager = PluginManagerProvider.getInstance();
        String pluginId = "test.plugin.1";

        assertFalse(pluginManager.isPluginEnabled(pluginId));

        // When
        pluginManager.enablePlugin(pluginId);

        // Then
        assertTrue(pluginManager.isPluginEnabled(pluginId));
    }

    @Test
    void stubPluginManager_DisablePlugin_ShouldChangePluginState() {
        // Given
        PluginManager pluginManager = PluginManagerProvider.getInstance();
        String pluginId = "test.plugin.2";

        pluginManager.enablePlugin(pluginId);
        assertTrue(pluginManager.isPluginEnabled(pluginId));

        // When
        pluginManager.disablePlugin(pluginId);

        // Then
        assertFalse(pluginManager.isPluginEnabled(pluginId));
    }

    @Test
    void stubPluginManager_GetAllPluginStates_ShouldReturnAllStates() {
        // Given
        PluginManager pluginManager = PluginManagerProvider.getInstance();

        pluginManager.enablePlugin("plugin.1");
        pluginManager.enablePlugin("plugin.2");
        pluginManager.disablePlugin("plugin.3");

        // When
        Map<String, Boolean> states = pluginManager.getAllPluginStates();

        // Then
        assertNotNull(states);
        assertTrue(states.containsKey("plugin.1"));
        assertTrue(states.containsKey("plugin.2"));
        assertTrue(states.containsKey("plugin.3"));
        assertTrue(states.get("plugin.1"));
        assertTrue(states.get("plugin.2"));
        assertFalse(states.get("plugin.3"));
    }

    @Test
    void stubPluginManager_LoadPlugin_ShouldNotThrowException() {
        // Given
        PluginManager pluginManager = PluginManagerProvider.getInstance();
        File pluginFile = new File("test-plugin.jar");

        // When/Then - should not throw exception (stub just prints)
        assertDoesNotThrow(() -> pluginManager.loadPlugin(pluginFile));
    }

    @Test
    void stubPluginManager_UnloadPlugin_ShouldRemovePluginState() {
        // Given
        PluginManager pluginManager = PluginManagerProvider.getInstance();
        String pluginId = "plugin.to.unload";

        pluginManager.enablePlugin(pluginId);
        assertTrue(pluginManager.isPluginEnabled(pluginId));

        // When
        pluginManager.unloadPlugin(pluginId);

        // Then
        assertFalse(pluginManager.isPluginEnabled(pluginId));
        assertNull(pluginManager.getPlugin(pluginId));
    }

    @Test
    void stubPluginManager_GetPlugin_ShouldReturnNullForUnknownPlugin() {
        // Given
        PluginManager pluginManager = PluginManagerProvider.getInstance();

        // When
        Plugin plugin = pluginManager.getPlugin("unknown.plugin");

        // Then
        assertNull(plugin);
    }

    @Test
    void stubPluginManager_GetLoadedPlugins_ShouldReturnEmptyList_WhenNoPluginsLoaded() {
        // Given
        PluginManager pluginManager = PluginManagerProvider.getInstance();

        // When
        List<Plugin> loadedPlugins = pluginManager.getLoadedPlugins();

        // Then
        assertNotNull(loadedPlugins);
        assertTrue(loadedPlugins.isEmpty());
    }

    @Test
    void stubPluginManager_GetEnabledPlugins_ShouldReturnEmptyList_WhenNoPluginsEnabled() {
        // Given
        PluginManager pluginManager = PluginManagerProvider.getInstance();

        // When
        List<Plugin> enabledPlugins = pluginManager.getEnabledPlugins();

        // Then
        assertNotNull(enabledPlugins);
        assertTrue(enabledPlugins.isEmpty());
    }

    @Test
    void stubPluginManager_ShutdownAll_ShouldClearAllStates() {
        // Given
        PluginManager pluginManager = PluginManagerProvider.getInstance();

        pluginManager.enablePlugin("plugin.1");
        pluginManager.enablePlugin("plugin.2");

        assertTrue(pluginManager.isPluginEnabled("plugin.1"));
        assertTrue(pluginManager.isPluginEnabled("plugin.2"));

        // When
        pluginManager.shutdownAll();

        // Then
        assertFalse(pluginManager.isPluginEnabled("plugin.1"));
        assertFalse(pluginManager.isPluginEnabled("plugin.2"));
        assertTrue(pluginManager.getAllPluginStates().isEmpty());
    }

    @Test
    void stubPluginManager_LoadAllPlugins_ShouldNotThrowException() {
        // Given
        PluginManager pluginManager = PluginManagerProvider.getInstance();

        // When/Then
        assertDoesNotThrow(() -> pluginManager.loadAllPlugins());
    }

    @Test
    void stubPluginManager_DisableAllPlugins_ShouldDisableAllEnabledPlugins() {
        // Given
        PluginManager pluginManager = PluginManagerProvider.getInstance();

        pluginManager.enablePlugin("plugin.1");
        pluginManager.enablePlugin("plugin.2");

        assertTrue(pluginManager.isPluginEnabled("plugin.1"));
        assertTrue(pluginManager.isPluginEnabled("plugin.2"));

        // When
        pluginManager.disableAllPlugins();

        // Then
        assertFalse(pluginManager.isPluginEnabled("plugin.1"));
        assertFalse(pluginManager.isPluginEnabled("plugin.2"));
    }

    @Test
    void stubPluginManager_EnablePluginByName_ShouldEnablePlugin() {
        // Given
        PluginManager pluginManager = PluginManagerProvider.getInstance();
        String pluginName = "test.plugin.by.name";

        assertFalse(pluginManager.isPluginEnabled(pluginName));

        // When
        pluginManager.enablePluginByName(pluginName);

        // Then
        assertTrue(pluginManager.isPluginEnabled(pluginName));
    }

    @Test
    void stubPluginManager_GetPluginStatus_ShouldReturnCorrectStatus() {
        // Given
        PluginManager pluginManager = PluginManagerProvider.getInstance();
        String loadedPluginId = "loaded.plugin";
        String unloadedPluginId = "unloaded.plugin";
        String disabledPluginId = "disabled.plugin";

        // Simulate loaded plugin (add to plugins map via reflection or mock)
        // For stub implementation, status is based on whether plugin exists in plugins map

        // When - plugin not in map
        PluginStatus status = pluginManager.getPluginStatus(unloadedPluginId);

        // Then
        assertEquals(PluginStatus.UNLOADED, status);
    }

    @Test
    void stubPluginManager_LoadPlugin_WithNullFile_ShouldNotThrowException() {
        // Given
        PluginManager pluginManager = PluginManagerProvider.getInstance();

        // When/Then
        assertDoesNotThrow(() -> pluginManager.loadPlugin(null));
    }

    @Test
    void stubPluginManager_UnloadPlugin_WithNullId_ShouldNotThrowException() {
        // Given
        PluginManager pluginManager = PluginManagerProvider.getInstance();

        // When/Then
        assertDoesNotThrow(() -> pluginManager.unloadPlugin(null));
    }

    @Test
    void stubPluginManager_EnablePlugin_WithNullId_ShouldNotThrowException() {
        // Given
        PluginManager pluginManager = PluginManagerProvider.getInstance();

        // When/Then
        assertDoesNotThrow(() -> pluginManager.enablePlugin(null));
    }

    @Test
    void stubPluginManager_MultipleOperations_ShouldMaintainConsistentState() {
        // Given
        PluginManager pluginManager = PluginManagerProvider.getInstance();
        String pluginId = "consistency.test";

        // When - sequence of operations
        pluginManager.enablePlugin(pluginId);
        assertTrue(pluginManager.isPluginEnabled(pluginId));

        pluginManager.disablePlugin(pluginId);
        assertFalse(pluginManager.isPluginEnabled(pluginId));

        pluginManager.enablePlugin(pluginId);
        assertTrue(pluginManager.isPluginEnabled(pluginId));

        pluginManager.unloadPlugin(pluginId);
        assertFalse(pluginManager.isPluginEnabled(pluginId));

        // Re-enable after unload
        pluginManager.enablePlugin(pluginId);
        assertTrue(pluginManager.isPluginEnabled(pluginId));
    }

    @Test
    void stubPluginManager_GetPluginContext_ShouldReturnSameContext_ForSameInstance() {
        // Given
        PluginManager pluginManager = PluginManagerProvider.getInstance();

        // When
        PluginContext context1 = pluginManager.getPluginContext();
        PluginContext context2 = pluginManager.getPluginContext();

        // Then
        assertSame(context1, context2);
    }

    @Test
    void stubPluginManager_ShouldHandleMultiplePluginsIndependently() {
        // Given
        PluginManager pluginManager = PluginManagerProvider.getInstance();
        String plugin1 = "plugin.independent.1";
        String plugin2 = "plugin.independent.2";

        // When
        pluginManager.enablePlugin(plugin1);
        pluginManager.disablePlugin(plugin2);

        // Then
        assertTrue(pluginManager.isPluginEnabled(plugin1));
        assertFalse(pluginManager.isPluginEnabled(plugin2));

        // When - disable plugin1, enable plugin2
        pluginManager.disablePlugin(plugin1);
        pluginManager.enablePlugin(plugin2);

        // Then
        assertFalse(pluginManager.isPluginEnabled(plugin1));
        assertTrue(pluginManager.isPluginEnabled(plugin2));
    }
}