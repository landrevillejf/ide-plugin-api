package com.protonmail.landrevillejf.ide.plugin;

import com.protonmail.landrevillejf.ide.plugin.utils.TestUtils;
import com.protonmail.landrevillejf.swingide.core.bus.EventBus;
import com.protonmail.landrevillejf.swingide.core.registry.ServiceRegistry;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("DefaultPluginManager Tests")
class DefaultPluginManagerTest {

    private ServiceRegistry mockServiceRegistry;
    private EventBus mockEventBus;
    private PluginContext mockContext;
    private DefaultPluginManager pluginManager;

    @TempDir
    Path tempDir;

    private File pluginsDir;

    @BeforeEach
    void setUp() {
        mockServiceRegistry = mock(ServiceRegistry.class);
        mockEventBus = mock(EventBus.class);
        mockContext = mock(PluginContext.class);

        pluginsDir = tempDir.resolve("plugins").toFile();

        when(mockContext.getPluginDataPath()).thenReturn(tempDir.resolve("plugin-data").toString());

        pluginManager = new DefaultPluginManager(
                mockServiceRegistry,
                mockEventBus,
                mockContext,
                pluginsDir.getAbsolutePath()
        );
    }

    private void addMockPlugin(String pluginName, Plugin mockPlugin) throws Exception {
        TestUtils.addMockPlugin(pluginManager, pluginName, mockPlugin);
    }

    @Nested
    @DisplayName("Directory Management Tests")
    class DirectoryManagementTests {

        @Test
        @DisplayName("Should create plugins directory if it doesn't exist")
        void testSetPluginsDirectoryCreatesDir() {
            File newPluginsDir = tempDir.resolve("new-plugins-dir").toFile();
            assertFalse(newPluginsDir.exists());

            pluginManager.setPluginsDirectory(newPluginsDir.getAbsolutePath());
            assertTrue(newPluginsDir.exists());
        }

        @Test
        @DisplayName("Should set plugins directory correctly")
        void testSetPluginsDirectory() {
            String newPath = tempDir.resolve("another-dir").toString();
            pluginManager.setPluginsDirectory(newPath);

            File expectedDir = new File(newPath);
            assertTrue(expectedDir.exists());
        }
    }

    @Nested
    @DisplayName("Plugin Unloading Tests")
    class PluginUnloadingTests {

        @Test
        @DisplayName("Should unload plugin")
        void testUnloadPlugin() throws Exception {
            Plugin mockPlugin = mock(Plugin.class);
            addMockPlugin("TestPlugin", mockPlugin);

            pluginManager.unloadPlugin("TestPlugin");
            verify(mockPlugin, times(1)).shutdown();
        }

        @Test
        @DisplayName("Should handle unloading non-existent plugin")
        void testUnloadNonExistentPlugin() {
            assertDoesNotThrow(() -> pluginManager.unloadPlugin("NonExistentPlugin"));
        }

        @Test
        @DisplayName("Should unload all plugins")
        void testUnloadAllPlugins() throws Exception {
            Plugin mockPlugin1 = mock(Plugin.class);
            Plugin mockPlugin2 = mock(Plugin.class);
            addMockPlugin("Plugin1", mockPlugin1);
            addMockPlugin("Plugin2", mockPlugin2);

            pluginManager.unloadAllPlugins();
            verify(mockPlugin1, times(1)).shutdown();
            verify(mockPlugin2, times(1)).shutdown();
        }
    }

    @Nested
    @DisplayName("Plugin Management Tests")
    class PluginManagementTests {

        @Test
        @DisplayName("Should get all plugins")
        void testGetPlugins() throws Exception {
            Plugin mockPlugin1 = mock(Plugin.class);
            Plugin mockPlugin2 = mock(Plugin.class);
            addMockPlugin("Plugin1", mockPlugin1);
            addMockPlugin("Plugin2", mockPlugin2);

            Map<String, Plugin> plugins = pluginManager.getPlugins();
            assertEquals(2, plugins.size());
            assertTrue(plugins.containsKey("Plugin1"));
            assertTrue(plugins.containsKey("Plugin2"));
        }

        @Test
        @DisplayName("Should get plugin by name")
        void testGetPlugin() throws Exception {
            Plugin mockPlugin = mock(Plugin.class);
            addMockPlugin("TestPlugin", mockPlugin);

            Plugin retrieved = pluginManager.getPlugin("TestPlugin");
            assertSame(mockPlugin, retrieved);
        }

        @Test
        @DisplayName("Should return null for non-existent plugin")
        void testGetPluginNotFound() {
            Plugin retrieved = pluginManager.getPlugin("NonExistent");
            assertNull(retrieved);
        }

        @Test
        @DisplayName("Should get loaded plugins list")
        void testGetLoadedPlugins() throws Exception {
            Plugin mockPlugin1 = mock(Plugin.class);
            Plugin mockPlugin2 = mock(Plugin.class);
            addMockPlugin("Plugin1", mockPlugin1);
            addMockPlugin("Plugin2", mockPlugin2);

            List<Plugin> loadedPlugins = pluginManager.getLoadedPlugins();
            assertEquals(2, loadedPlugins.size());
            assertTrue(loadedPlugins.contains(mockPlugin1));
            assertTrue(loadedPlugins.contains(mockPlugin2));
        }

        @Test
        @DisplayName("Should get plugin status")
        void testGetPluginStatus() throws Exception {
            Plugin mockPlugin = mock(Plugin.class);
            addMockPlugin("TestPlugin", mockPlugin);

            // Simuler l'activation du plugin
            doAnswer(invocation -> {
                when(mockPlugin.getState()).thenReturn(PluginStatus.ENABLED);
                return null;
            }).when(mockPlugin).enable();

            when(mockPlugin.getState()).thenReturn(PluginStatus.DISABLED);

            pluginManager.enablePlugin("TestPlugin");
            PluginStatus status = pluginManager.getPluginStatus("TestPlugin");
            assertEquals(PluginStatus.ENABLED, status);
        }

        @Test
        @DisplayName("Should return UNLOADED for non-existent plugin status")
        void testGetPluginStatusNotFound() {
            PluginStatus status = pluginManager.getPluginStatus("NonExistent");
            assertEquals(PluginStatus.UNLOADED, status);
        }

        @Test
        @DisplayName("Should get all plugin states")
        void testGetAllPluginStates() throws Exception {
            Plugin mockPlugin1 = mock(Plugin.class);
            Plugin mockPlugin2 = mock(Plugin.class);
            addMockPlugin("Plugin1", mockPlugin1);
            addMockPlugin("Plugin2", mockPlugin2);

            // ⭐ MODIFIER DIRECTEMENT LA MAP DES ÉTATS (sans appeler enablePlugin)
            Field statesField = DefaultPluginManager.class.getDeclaredField("pluginEnabledStates");
            statesField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Boolean> states = (Map<String, Boolean>) statesField.get(pluginManager);
            states.put("Plugin1", true);  // Plugin1 activé
            states.put("Plugin2", false); // Plugin2 désactivé

            // Simuler le comportement du plugin
            when(mockPlugin1.isEnabled()).thenReturn(true);
            when(mockPlugin2.isEnabled()).thenReturn(false);
            when(mockPlugin1.getState()).thenReturn(PluginStatus.ENABLED);
            when(mockPlugin2.getState()).thenReturn(PluginStatus.DISABLED);

            Map<String, Boolean> result = pluginManager.getAllPluginStates();

            assertTrue(result.containsKey("Plugin1"));
            assertTrue(result.containsKey("Plugin2"));
            assertTrue(result.get("Plugin1"));  // Plugin1 devrait être true
            assertFalse(result.get("Plugin2")); // Plugin2 devrait être false
        }

        @Nested
        @DisplayName("Plugin Enable/Disable Tests")
        class EnableDisableTests {

            @Test
            @DisplayName("Should enable plugin")
            void testEnablePlugin() throws Exception {
                Plugin mockPlugin = mock(Plugin.class);
                addMockPlugin("TestPlugin", mockPlugin);
                when(mockPlugin.getState()).thenReturn(PluginStatus.DISABLED);
                doNothing().when(mockPlugin).enable();

                assertDoesNotThrow(() -> pluginManager.enablePlugin("TestPlugin"));
                verify(mockPlugin, times(1)).enable();
            }

            @Test
            @DisplayName("Should not enable already enabled plugin")
            void testEnableAlreadyEnabledPlugin() throws Exception {
                Plugin mockPlugin = mock(Plugin.class);
                addMockPlugin("TestPlugin", mockPlugin);

                // ⭐ Simuler que le plugin est déjà activé
                when(mockPlugin.getState()).thenReturn(PluginStatus.ENABLED);
                when(mockPlugin.isEnabled()).thenReturn(true);  // ← AJOUTER CETTE LIGNE

                doNothing().when(mockPlugin).enable();

                pluginManager.enablePlugin("TestPlugin");
                verify(mockPlugin, never()).enable();
            }

            @Test
            @DisplayName("Should throw exception when enabling non-existent plugin")
            void testEnableNonExistentPlugin() {
                assertThrows(IllegalArgumentException.class, () -> {
                    pluginManager.enablePlugin("NonExistent");
                });
            }

            @Test
            @DisplayName("Should disable plugin")
            void testDisablePlugin() throws Exception {
                Plugin mockPlugin = mock(Plugin.class);
                addMockPlugin("TestPlugin", mockPlugin);
                when(mockPlugin.getState()).thenReturn(PluginStatus.ENABLED);
                doNothing().when(mockPlugin).disable();

                pluginManager.enablePlugin("TestPlugin");
                assertDoesNotThrow(() -> pluginManager.disablePlugin("TestPlugin"));
                verify(mockPlugin, times(1)).disable();
            }

            @Test
            @DisplayName("Should handle disabling already disabled plugin")
            void testDisableAlreadyDisabledPlugin() throws Exception {
                Plugin mockPlugin = mock(Plugin.class);
                addMockPlugin("TestPlugin", mockPlugin);
                when(mockPlugin.getState()).thenReturn(PluginStatus.DISABLED);

                pluginManager.disablePlugin("TestPlugin");
                verify(mockPlugin, never()).disable();
            }

            @Test
            @DisplayName("Should disable all plugins")
            void testDisableAllPlugins() throws Exception {
                Plugin mockPlugin1 = mock(Plugin.class);
                Plugin mockPlugin2 = mock(Plugin.class);
                addMockPlugin("Plugin1", mockPlugin1);
                addMockPlugin("Plugin2", mockPlugin2);
                when(mockPlugin1.getState()).thenReturn(PluginStatus.DISABLED);
                when(mockPlugin2.getState()).thenReturn(PluginStatus.DISABLED);

                pluginManager.disableAllPlugins();
            }

            @Test
            @DisplayName("Should enable plugin by name")
            void testEnablePluginByName() throws Exception {
                Plugin mockPlugin = mock(Plugin.class);
                addMockPlugin("TestPlugin", mockPlugin);
                when(mockPlugin.getState()).thenReturn(PluginStatus.DISABLED);
                when(mockPlugin.getName()).thenReturn("TestPlugin");
                doNothing().when(mockPlugin).enable();

                pluginManager.enablePluginByName("TestPlugin");
                verify(mockPlugin, times(1)).enable();
            }

            @Test
            @DisplayName("Should check if plugin is enabled")
            void testIsPluginEnabled() throws Exception {
                Plugin mockPlugin = mock(Plugin.class);
                addMockPlugin("TestPlugin", mockPlugin);
                when(mockPlugin.getState()).thenReturn(PluginStatus.ENABLED);
                when(mockPlugin.isEnabled()).thenReturn(true);

                pluginManager.enablePlugin("TestPlugin");
                assertTrue(pluginManager.isPluginEnabled("TestPlugin"));
            }

            @Test
            @DisplayName("Should return false for non-existent plugin enabled check")
            void testIsPluginEnabledForNonExistent() {
                assertFalse(pluginManager.isPluginEnabled("NonExistent"));
            }

            @Test
            @DisplayName("Should get enabled plugins")
            void testGetEnabledPlugins() throws Exception {
                Plugin mockPlugin1 = mock(Plugin.class);
                Plugin mockPlugin2 = mock(Plugin.class);
                addMockPlugin("Plugin1", mockPlugin1);
                addMockPlugin("Plugin2", mockPlugin2);
                when(mockPlugin1.isEnabled()).thenReturn(true);
                when(mockPlugin2.isEnabled()).thenReturn(false);

                doAnswer(invocation -> {
                    when(mockPlugin1.getState()).thenReturn(PluginStatus.ENABLED);
                    return null;
                }).when(mockPlugin1).enable();

                pluginManager.enablePlugin("Plugin1");

                List<Plugin> enabledPlugins = pluginManager.getEnabledPlugins();
                assertEquals(1, enabledPlugins.size());
                assertTrue(enabledPlugins.contains(mockPlugin1));
            }
        }

        @Nested
        @DisplayName("State Management Tests")
        class StateManagementTests {

            @Test
            @DisplayName("Should reset all plugin states")
            void testResetAllPluginStates() throws Exception {
                Plugin mockPlugin = mock(Plugin.class);
                addMockPlugin("TestPlugin", mockPlugin);

                pluginManager.resetAllPluginStates();
                assertFalse(pluginManager.isPluginEnabled("TestPlugin"));
            }
        }

        @Nested
        @DisplayName("Shutdown Tests")
        class ShutdownTests {

            @Test
            @DisplayName("Should shutdown all plugins and cleanup")
            void testShutdownAll() throws Exception {
                Plugin mockPlugin = mock(Plugin.class);
                addMockPlugin("TestPlugin", mockPlugin);

                assertDoesNotThrow(() -> pluginManager.shutdownAll());
                verify(mockEventBus, times(1)).shutdown();
                verify(mockPlugin, times(1)).shutdown();
            }
        }

        @Nested
        @DisplayName("Load and Enable Plugin Tests")
        class LoadAndEnableTests {

            @Test
            @DisplayName("Should handle non-existent plugin in loadAndEnable")
            void testLoadAndEnableNonExistentPlugin() {
                assertDoesNotThrow(() -> {
                    pluginManager.loadAndEnablePluginByName(pluginsDir.getAbsolutePath(), "NonExistent");
                });
            }
        }

        @Nested
        @DisplayName("Edge Cases and Error Handling Tests")
        class EdgeCaseTests {

            @Test
            @DisplayName("Should handle null plugins directory")
            void testNullPluginsDirectory() {
                DefaultPluginManager manager = new DefaultPluginManager(
                        mockServiceRegistry,
                        mockEventBus,
                        mockContext,
                        null
                );

                assertDoesNotThrow(() -> manager.loadAllPlugins());
                assertDoesNotThrow(() -> manager.loadPlugins(null));
            }

            @Test
            @DisplayName("Should handle corrupted JAR files")
            void testCorruptedJarFile() throws IOException {
                File corruptedJar = new File(pluginsDir, "corrupted.jar");
                try (FileOutputStream fos = new FileOutputStream(corruptedJar)) {
                    fos.write("This is not a valid JAR file".getBytes());
                }

                assertDoesNotThrow(() -> pluginManager.loadAllPlugins());
            }

            @Test
            @DisplayName("Should handle plugin with null name")
            void testPluginWithNullName() {
                assertDoesNotThrow(() -> pluginManager.isPluginEnabled(null));
            }

            @Test
            @DisplayName("Should handle concurrent plugin operations")
            void testConcurrentOperations() throws Exception {
                Plugin mockPlugin = mock(Plugin.class);
                addMockPlugin("TestPlugin", mockPlugin);
                when(mockPlugin.getState()).thenReturn(PluginStatus.DISABLED);
                doNothing().when(mockPlugin).enable();
                doNothing().when(mockPlugin).disable();

                Thread enableThread = new Thread(() -> {
                    pluginManager.enablePlugin("TestPlugin");
                });

                Thread disableThread = new Thread(() -> {
                    pluginManager.disablePlugin("TestPlugin");
                });

                assertDoesNotThrow(() -> {
                    enableThread.start();
                    disableThread.start();
                    enableThread.join();
                    disableThread.join();
                });
            }
        }
    }
}