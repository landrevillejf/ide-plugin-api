package com.protonmail.landrevillejf.ide.plugin;

import com.protonmail.landrevillejf.ide.plugin.events.*;
import com.protonmail.landrevillejf.ide.plugin.service.PluginServiceLocator;
import com.protonmail.landrevillejf.ide.plugin.utils.LogCapture;
import com.protonmail.landrevillejf.ide.plugin.utils.TestUtils;
import com.protonmail.landrevillejf.swingide.core.bus.EventBus;
import com.protonmail.landrevillejf.swingide.core.registry.ServiceRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

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
        ConfigurableJarPlugin.reset();
    }

    private void addMockPlugin(String pluginName, Plugin mockPlugin) throws Exception {
        addMockPlugin(pluginManager, pluginName, mockPlugin);
    }

    private void addMockPlugin(DefaultPluginManager manager, String pluginName, Plugin mockPlugin)
            throws Exception {
        TestUtils.addMockPlugin(manager, pluginName, mockPlugin);
    }

    @SuppressWarnings("unchecked")
    private <T> T getPrivateField(DefaultPluginManager manager, String fieldName) throws Exception {
        Field field = DefaultPluginManager.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(manager);
    }

    private void setPrivateField(DefaultPluginManager manager, String fieldName, Object value) throws Exception {
        Field field = DefaultPluginManager.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(manager, value);
    }

    private Object invokePrivateMethod(
            DefaultPluginManager manager,
            String methodName,
            Class<?>[] parameterTypes,
            Object... args
    ) throws Exception {
        Method method = DefaultPluginManager.class.getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        try {
            return method.invoke(manager, args);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception exception) {
                throw exception;
            }
            if (cause instanceof Error error) {
                throw error;
            }
            throw e;
        }
    }

    private Object invokePrivateMethod(String methodName, Class<?>[] parameterTypes, Object... args)
            throws Exception {
        return invokePrivateMethod(pluginManager, methodName, parameterTypes, args);
    }

    private void addTemporaryPluginFile(File file) throws Exception {
        List<File> temporaryFiles = getPrivateField(pluginManager, "temporaryPluginFiles");
        temporaryFiles.add(file);
    }

    private void setPluginEnabledState(DefaultPluginManager manager, String pluginName, boolean enabled)
            throws Exception {
        Map<String, Boolean> states = getPrivateField(manager, "pluginEnabledStates");
        states.put(pluginName, enabled);
    }

    private Manifest createManifest(String pluginName, String pluginClassName) {
        Manifest manifest = new Manifest();
        Attributes attributes = manifest.getMainAttributes();
        attributes.putValue("Manifest-Version", "1.0");
        attributes.putValue("Plugin-Id", pluginName.toLowerCase());
        attributes.putValue("Plugin-Name", pluginName);
        attributes.putValue("Plugin-Version", "1.0.0");
        attributes.putValue("Plugin-Class", pluginClassName);
        attributes.putValue("Plugin-Description", "Test plugin");
        attributes.putValue("Plugin-Author", "Test Author");
        return manifest;
    }

    private File createJar(
            String fileName,
            Manifest manifest,
            String pluginPropertiesContent
    ) throws IOException {
        File jarFile = tempDir.resolve(fileName).toFile();
        File parent = jarFile.getParentFile();
        if (parent != null && !parent.exists()) {
            assertTrue(parent.mkdirs() || parent.exists());
        }
        try (JarOutputStream jarOutputStream = manifest == null
                ? new JarOutputStream(new FileOutputStream(jarFile))
                : new JarOutputStream(new FileOutputStream(jarFile), manifest)) {
            JarEntry placeholder = new JarEntry("placeholder.txt");
            jarOutputStream.putNextEntry(placeholder);
            jarOutputStream.write("placeholder".getBytes(StandardCharsets.UTF_8));
            jarOutputStream.closeEntry();

            if (pluginPropertiesContent != null) {
                JarEntry entry = new JarEntry("plugin.properties");
                jarOutputStream.putNextEntry(entry);
                jarOutputStream.write(pluginPropertiesContent.getBytes(StandardCharsets.UTF_8));
                jarOutputStream.closeEntry();
            }
        }
        return jarFile;
    }

    private File createPluginJar(String fileName, String pluginName, Class<?> pluginClass)
            throws IOException {
        return createJar(fileName, createManifest(pluginName, pluginClass.getName()), null);
    }

    private DefaultPluginManager createExtendedManager(File externalPluginsDir) {
        PluginServiceLocator serviceLocator = mock(PluginServiceLocator.class);
        DefaultExtendedPluginContext extendedContext = new DefaultExtendedPluginContext(
                mockServiceRegistry,
                new PluginEventBus(),
                mockEventBus,
                mock(PluginManager.class),
                tempDir.resolve("extended-plugin-data").toFile(),
                "extended-parent",
                serviceLocator
        );
        return new DefaultPluginManager(
                mockServiceRegistry,
                mockEventBus,
                extendedContext,
                externalPluginsDir.getAbsolutePath()
        );
    }

    private static void setPluginState(Plugin plugin, PluginStatus state) throws Exception {
        Field stateField = AbstractPlugin.class.getDeclaredField("state");
        stateField.setAccessible(true);
        stateField.set(plugin, state);
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

    @Nested
    @DisplayName("Advanced Coverage Tests")
    class AdvancedCoverageTests {

        @Test
        @DisplayName("Should clear plugins directory when path is null")
        void testSetPluginsDirectoryNullClearsDirectory() throws Exception {
            pluginManager.setPluginsDirectory(null);
            assertNull(getPrivateField(pluginManager, "pluginsDirectory"));
        }

        @Test
        @DisplayName("Should warn when plugins directory cannot be created")
        void testSetPluginsDirectoryWarnsWhenDirectoryCannotBeCreated() throws Exception {
            File blockingFile = tempDir.resolve("blocking-parent.txt").toFile();
            assertTrue(blockingFile.createNewFile());
            File childDirectory = new File(blockingFile, "child");

            try (LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                pluginManager.setPluginsDirectory(childDirectory.getAbsolutePath());
            }

            assertEquals(childDirectory.getAbsolutePath(),
                    ((File) getPrivateField(pluginManager, "pluginsDirectory")).getAbsolutePath());
            assertFalse(childDirectory.exists());
        }

        @Test
        @DisplayName("Should return defensive copies of internal collections")
        void testDefensiveCopiesForCollectionGetters() throws Exception {
            Plugin plugin = mock(Plugin.class);
            addMockPlugin("CopyPlugin", plugin);
            setPluginEnabledState(pluginManager, "CopyPlugin", true);

            Map<String, Plugin> plugins = pluginManager.getPlugins();
            plugins.clear();
            assertEquals(1, pluginManager.getPlugins().size());

            List<Plugin> loadedPlugins = pluginManager.getLoadedPlugins();
            loadedPlugins.clear();
            assertEquals(1, pluginManager.getLoadedPlugins().size());

            Map<String, Boolean> states = pluginManager.getAllPluginStates();
            states.clear();
            assertEquals(1, pluginManager.getAllPluginStates().size());
        }

        @Test
        @DisplayName("Should expose configured plugin context")
        void testGetPluginContextAndGetterReturnConfiguredContext() {
            assertSame(mockContext, pluginManager.getPluginContext());
            assertSame(mockContext, pluginManager.getContext());
        }

        @Test
        @DisplayName("Should load plugin with standard context and disabled state")
        void testLoadPluginWithStandardContext() throws Exception {
            ConfigurableJarPlugin.configure("StandardJarPlugin", false, false, false);
            File jarFile = createPluginJar(
                    "standard-plugin.jar",
                    "StandardJarPlugin",
                    ConfigurableJarPlugin.class
            );

            try (LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                pluginManager.loadPlugin(jarFile);
            }

            Plugin loadedPlugin = pluginManager.getPlugin("StandardJarPlugin");
            assertNotNull(loadedPlugin);
            assertSame(loadedPlugin, pluginManager.getLoadedPlugins().get(0));
            assertEquals(1, ConfigurableJarPlugin.initializeCalls);
            assertEquals(0, ConfigurableJarPlugin.enableCalls);
            assertTrue(ConfigurableJarPlugin.lastContext instanceof DefaultPluginContext);
            assertFalse(pluginManager.getAllPluginStates().containsKey("StandardJarPlugin"));
        }

        @Test
        @DisplayName("Should load plugin with extended context and auto-enable it")
        void testLoadPluginWithExtendedContextAndAutoEnable() throws Exception {
            File externalDir = tempDir.resolve("extended-plugins").toFile();
            DefaultPluginManager extendedManager = createExtendedManager(externalDir);
            ConfigurableJarPlugin.configure("ExtendedJarPlugin", true, false, false);
            File jarFile = createPluginJar(
                    "extended-plugin.jar",
                    "ExtendedJarPlugin",
                    ConfigurableJarPlugin.class
            );

            try (LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                extendedManager.loadPlugin(jarFile);
            }

            assertTrue(extendedManager.isPluginEnabled("ExtendedJarPlugin"));
            assertEquals(1, ConfigurableJarPlugin.enableCalls);
            assertTrue(ConfigurableJarPlugin.lastContext instanceof DefaultExtendedPluginContext);
        }

        @Test
        @DisplayName("Should keep auto-enable failures disabled")
        void testLoadPluginStoresDisabledStateWhenAutoEnableFails() throws Exception {
            ConfigurableJarPlugin.configure("BrokenAutoEnablePlugin", true, true, false);
            File jarFile = createPluginJar(
                    "broken-auto-enable.jar",
                    "BrokenAutoEnablePlugin",
                    ConfigurableJarPlugin.class
            );

            try (LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                pluginManager.loadPlugin(jarFile);
            }

            assertFalse(pluginManager.isPluginEnabled("BrokenAutoEnablePlugin"));
            assertEquals(1, ConfigurableJarPlugin.enableCalls);
            assertNotNull(pluginManager.getPlugin("BrokenAutoEnablePlugin"));
        }

        @Test
        @DisplayName("Should load plugin when configuration is null")
        void testLoadPluginSupportsNullConfig() throws Exception {
            ConfigurableJarPlugin.configure("NullConfigPlugin", false, false, true);
            File jarFile = createPluginJar(
                    "null-config.jar",
                    "NullConfigPlugin",
                    ConfigurableJarPlugin.class
            );

            try (LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                pluginManager.loadPlugin(jarFile);
            }

            assertNotNull(pluginManager.getPlugin("NullConfigPlugin"));
            assertEquals(1, ConfigurableJarPlugin.initializeCalls);
            assertEquals(0, ConfigurableJarPlugin.enableCalls);
        }

        @Test
        @DisplayName("Should reject JAR files without a manifest")
        void testLoadPluginRejectsJarWithoutManifest() throws IOException {
            File jarFile = createJar("manifestless.jar", null, null);

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> pluginManager.loadPlugin(jarFile)
            );
            assertTrue(exception.getMessage().contains("manifestless.jar"));
        }

        @Test
        @DisplayName("Should close the classloader when plugin loading fails after creation")
        void testLoadPluginClosesClassLoaderWhenClassCannotBeLoaded() throws IOException {
            File jarFile = createJar(
                    "missing-class.jar",
                    createManifest("MissingClassPlugin", "missing.PluginClass"),
                    null
            );

            assertThrows(ClassNotFoundException.class, () -> pluginManager.loadPlugin(jarFile));
            assertNull(pluginManager.getPlugin("MissingClassPlugin"));
        }

        @Test
        @DisplayName("Should log and return when loadPlugins receives a missing directory")
        void testLoadPluginsMissingDirectory() {
            try (LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                pluginManager.loadPlugins(tempDir.resolve("missing-dir").toString());
            }

            assertTrue(pluginManager.getPlugins().isEmpty());
        }

        @Test
        @DisplayName("Should ignore empty directories when loading plugins by path")
        void testLoadPluginsIgnoresEmptyDirectory() throws IOException {
            File emptyDir = tempDir.resolve("empty-load-dir").toFile();
            assertTrue(emptyDir.mkdirs());

            pluginManager.loadPlugins(emptyDir.getAbsolutePath());

            assertTrue(pluginManager.getPlugins().isEmpty());
        }

        @Test
        @DisplayName("Should load valid jars and skip invalid jars when loading plugins by path")
        void testLoadPluginsLoadsValidAndSkipsInvalidJars() throws Exception {
            File loadDir = tempDir.resolve("manual-load-dir").toFile();
            assertTrue(loadDir.mkdirs());
            ConfigurableJarPlugin.configure("ManualLoadPlugin", false, false, false);
            createJar(
                    "manual-load-dir/valid.jar",
                    createManifest("ManualLoadPlugin", ConfigurableJarPlugin.class.getName()),
                    null
            );
            File invalidJar = new File(loadDir, "invalid.jar");
            try (FileOutputStream fos = new FileOutputStream(invalidJar)) {
                fos.write("not-a-jar".getBytes(StandardCharsets.UTF_8));
            }

            try (LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                pluginManager.loadPlugins(loadDir.getAbsolutePath());
            }

            assertNotNull(pluginManager.getPlugin("ManualLoadPlugin"));
        }

        @Test
        @DisplayName("Should create a missing configured plugins directory when loading all plugins")
        void testLoadAllPluginsCreatesMissingConfiguredDirectory() throws Exception {
            File missingDirectory = tempDir.resolve("created-on-load").toFile();
            setPrivateField(pluginManager, "pluginsDirectory", missingDirectory);

            try (LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                pluginManager.loadAllPlugins();
            }

            assertTrue(missingDirectory.exists());
            assertTrue(pluginManager.getPlugins().isEmpty());
        }

        @Test
        @DisplayName("Should handle file based plugins directory when loading all plugins")
        void testLoadAllPluginsHandlesFileDirectoryAndEmptyDirectory() throws Exception {
            File pluginsFile = tempDir.resolve("plugins-file.txt").toFile();
            assertTrue(pluginsFile.createNewFile());
            setPrivateField(pluginManager, "pluginsDirectory", pluginsFile);

            try (LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                pluginManager.loadAllPlugins();
            }

            File emptyDirectory = tempDir.resolve("empty-external-plugins").toFile();
            assertTrue(emptyDirectory.mkdirs());
            setPrivateField(pluginManager, "pluginsDirectory", emptyDirectory);

            try (LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                pluginManager.loadAllPlugins();
            }

            assertTrue(pluginManager.getPlugins().isEmpty());
        }

        @Test
        @DisplayName("Should load valid jars and skip invalid jars from the configured directory")
        void testLoadAllPluginsLoadsValidAndSkipsInvalidJars() throws Exception {
            File externalDir = tempDir.resolve("external-plugins").toFile();
            assertTrue(externalDir.mkdirs());
            setPrivateField(pluginManager, "pluginsDirectory", externalDir);
            ConfigurableJarPlugin.configure("ExternalLoadPlugin", false, false, false);
            createJar(
                    "external-plugins/valid.jar",
                    createManifest("ExternalLoadPlugin", ConfigurableJarPlugin.class.getName()),
                    null
            );
            File invalidJar = new File(externalDir, "invalid.jar");
            try (FileOutputStream fos = new FileOutputStream(invalidJar)) {
                fos.write("broken".getBytes(StandardCharsets.UTF_8));
            }

            try (LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                pluginManager.loadAllPlugins();
            }

            assertNotNull(pluginManager.getPlugin("ExternalLoadPlugin"));
        }

        @Test
        @DisplayName("Should close classloaders and publish an unload event")
        void testUnloadPluginClosesClassLoaderAndPublishesEvent() throws Exception {
            Plugin plugin = mock(Plugin.class);
            addMockPlugin("UnloadablePlugin", plugin);
            URLClassLoader classLoader = new URLClassLoader(new URL[0]);
            Map<String, ClassLoader> loaders = getPrivateField(pluginManager, "pluginClassLoaders");
            loaders.put("UnloadablePlugin", classLoader);
            when(plugin.getState()).thenReturn(PluginStatus.DISABLED);

            try (LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                pluginManager.unloadPlugin("UnloadablePlugin");
            }

            verify(plugin).shutdown();
            verify(mockEventBus).publish(isA(PluginUnloadedEvent.class));
            assertNull(pluginManager.getPlugin("UnloadablePlugin"));
        }

        @Test
        @DisplayName("Should tolerate failures while closing plugin classloaders")
        void testUnloadPluginHandlesClassLoaderCloseFailure() throws Exception {
            Plugin plugin = mock(Plugin.class);
            addMockPlugin("CloseFailurePlugin", plugin);
            Map<String, ClassLoader> loaders = getPrivateField(pluginManager, "pluginClassLoaders");
            loaders.put("CloseFailurePlugin", new ThrowingUrlClassLoader());
            when(plugin.getState()).thenReturn(PluginStatus.DISABLED);

            try (LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                pluginManager.unloadPlugin("CloseFailurePlugin");
            }

            verify(plugin).shutdown();
            verify(mockEventBus).publish(isA(PluginUnloadedEvent.class));
        }

        @Test
        @DisplayName("Should suppress shutdown failures while unloading a plugin")
        void testUnloadPluginSuppressesShutdownFailure() throws Exception {
            Plugin plugin = mock(Plugin.class);
            addMockPlugin("BrokenShutdownPlugin", plugin);
            when(plugin.getState()).thenReturn(PluginStatus.DISABLED);
            doThrow(new IllegalStateException("shutdown failed")).when(plugin).shutdown();

            try (LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                pluginManager.unloadPlugin("BrokenShutdownPlugin");
            }

            assertNotNull(pluginManager.getPlugin("BrokenShutdownPlugin"));
            verify(mockEventBus, never()).publish(isA(PluginUnloadedEvent.class));
        }

        @Test
        @DisplayName("Should cleanup temporary plugin files in every branch")
        void testCleanupTemporaryFilesCoversEveryBranch() throws Exception {
            File deletableFile = tempDir.resolve("delete-me.jar").toFile();
            assertTrue(deletableFile.createNewFile());
            File missingFile = tempDir.resolve("already-missing.jar").toFile();
            File nonEmptyDirectory = tempDir.resolve("non-empty-plugin-dir").toFile();
            assertTrue(nonEmptyDirectory.mkdirs());
            File child = new File(nonEmptyDirectory, "child.txt");
            assertTrue(child.createNewFile());
            ThrowingFile throwingFile = new ThrowingFile(tempDir.resolve("throwing-file.jar").toString());

            addTemporaryPluginFile(deletableFile);
            addTemporaryPluginFile(missingFile);
            addTemporaryPluginFile(nonEmptyDirectory);
            addTemporaryPluginFile(throwingFile);

            try (LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                invokePrivateMethod("cleanupTemporaryFiles", new Class<?>[0]);
            }

            assertFalse(deletableFile.exists());
            assertTrue(nonEmptyDirectory.exists());
            List<File> temporaryFiles = getPrivateField(pluginManager, "temporaryPluginFiles");
            assertTrue(temporaryFiles.isEmpty());
        }

        @Test
        @DisplayName("Should normalize plugin states for loading and shutdown")
        void testLifecycleHelperMethodsNormalizePluginStates() throws Exception {
            Plugin unloadable = mock(Plugin.class);
            when(unloadable.getState()).thenReturn(PluginStatus.UNLOADED);
            invokePrivateMethod("markPluginLoaded", new Class<?>[]{Plugin.class}, unloadable);
            verify(unloadable).setState(PluginStatus.LOADED);

            Plugin initialized = mock(Plugin.class);
            when(initialized.getState()).thenReturn(PluginStatus.INITIALIZED);
            invokePrivateMethod("markPluginLoaded", new Class<?>[]{Plugin.class}, initialized);
            verify(initialized, never()).setState(any());

            Plugin unloadedBeforeShutdown = mock(Plugin.class);
            when(unloadedBeforeShutdown.getState()).thenReturn(PluginStatus.UNLOADED);
            invokePrivateMethod("prepareForShutdown", new Class<?>[]{Plugin.class}, unloadedBeforeShutdown);
            verify(unloadedBeforeShutdown).setState(PluginStatus.LOADED);
            verify(unloadedBeforeShutdown).setState(PluginStatus.DISABLED);

            Plugin loadedBeforeShutdown = mock(Plugin.class);
            when(loadedBeforeShutdown.getState()).thenReturn(PluginStatus.LOADED);
            invokePrivateMethod("prepareForShutdown", new Class<?>[]{Plugin.class}, loadedBeforeShutdown);
            verify(loadedBeforeShutdown).setState(PluginStatus.DISABLED);

            Plugin initializedBeforeShutdown = mock(Plugin.class);
            when(initializedBeforeShutdown.getState()).thenReturn(PluginStatus.INITIALIZED);
            invokePrivateMethod("prepareForShutdown", new Class<?>[]{Plugin.class}, initializedBeforeShutdown);
            verify(initializedBeforeShutdown).setState(PluginStatus.DISABLED);

            Plugin errorBeforeShutdown = mock(Plugin.class);
            when(errorBeforeShutdown.getState()).thenReturn(PluginStatus.ERROR);
            invokePrivateMethod("prepareForShutdown", new Class<?>[]{Plugin.class}, errorBeforeShutdown);
            verify(errorBeforeShutdown).setState(PluginStatus.DISABLED);

            Plugin enabledBeforeShutdown = mock(Plugin.class);
            when(enabledBeforeShutdown.getState()).thenReturn(PluginStatus.ENABLED);
            invokePrivateMethod("prepareForShutdown", new Class<?>[]{Plugin.class}, enabledBeforeShutdown);
            verify(enabledBeforeShutdown, never()).setState(any());
        }

        @Test
        @DisplayName("Should find plugin class from properties first")
        void testFindPluginClassNamePrefersProperties() throws Exception {
            File jarFile = createJar(
                    "properties-first.jar",
                    createManifest("PropertiesPlugin", "manifest.PluginClass"),
                    "plugin.class=" + ConfigurableJarPlugin.class.getName() + "\n"
            );

            try (JarFile openedJar = new JarFile(jarFile);
                 LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                String className = (String) invokePrivateMethod(
                        "findPluginClassName",
                        new Class<?>[]{JarFile.class},
                        openedJar
                );

                assertEquals(ConfigurableJarPlugin.class.getName(), className);
            }
        }

        @Test
        @DisplayName("Should fall back to manifest when properties are blank or unreadable")
        void testFindPluginClassNameFallsBackToManifest() throws Exception {
            File blankPropertiesJar = createJar(
                    "blank-properties.jar",
                    createManifest("BlankPropertiesPlugin", ConfigurableJarPlugin.class.getName()),
                    "plugin.class=   \n"
            );
            try (JarFile openedJar = new JarFile(blankPropertiesJar);
                 LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                String className = (String) invokePrivateMethod(
                        "findPluginClassName",
                        new Class<?>[]{JarFile.class},
                        openedJar
                );
                assertEquals(ConfigurableJarPlugin.class.getName(), className);
            }

            File readableJar = createJar(
                    "io-fallback.jar",
                    createManifest("IoFallbackPlugin", ConfigurableJarPlugin.class.getName()),
                    "plugin.class=" + ConfigurableJarPlugin.class.getName() + "\n"
            );
            try (JarFile openedJar = new UnreadablePropertiesJarFile(readableJar);
                 LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                String className = (String) invokePrivateMethod(
                        "findPluginClassName",
                        new Class<?>[]{JarFile.class},
                        openedJar
                );
                assertEquals(ConfigurableJarPlugin.class.getName(), className);
            }
        }

        @Test
        @DisplayName("Should return null when neither properties nor manifest declare a plugin class")
        void testFindPluginClassNameReturnsNull() throws Exception {
            Manifest manifest = createManifest("NoClassPlugin", "placeholder.PluginClass");
            manifest.getMainAttributes().putValue("Plugin-Class", "   ");
            File jarFile = createJar("no-plugin-class.jar", manifest, "plugin.class=\n");

            try (JarFile openedJar = new JarFile(jarFile);
                 LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                String className = (String) invokePrivateMethod(
                        "findPluginClassName",
                        new Class<?>[]{JarFile.class},
                        openedJar
                );

                assertNull(className);
            }
        }

        @Test
        @DisplayName("Should log directory loading warnings when configuration is invalid")
        void testLoadAllPluginsLogsWarningsForInvalidDirectories() throws Exception {
            DefaultPluginManager nullDirectoryManager = new DefaultPluginManager(
                    mockServiceRegistry,
                    mockEventBus,
                    mockContext,
                    null
            );
            try (LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                nullDirectoryManager.loadAllPlugins();
            }

            File blockingFile = tempDir.resolve("blocked-parent-for-load-all.txt").toFile();
            assertTrue(blockingFile.createNewFile());
            File blockedDirectory = new File(blockingFile, "plugins");
            setPrivateField(pluginManager, "pluginsDirectory", blockedDirectory);
            try (LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                pluginManager.loadAllPlugins();
            }
        }

        @Test
        @DisplayName("Should log a warning when loadPlugins receives a null directory")
        void testLoadPluginsNullDirectoryLogsWarning() {
            try (LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                pluginManager.loadPlugins(null);
            }
        }

        @Test
        @DisplayName("Should log when unregistering a menu provider fails during disableAllPlugins")
        void testDisableAllPluginsLogsMenuProviderUnregisterFailure() throws Exception {
            Plugin plugin = mock(Plugin.class, withSettings().extraInterfaces(MenuProvider.class));
            addMockPlugin("UnregisterFailurePlugin", plugin);
            setPluginEnabledState(pluginManager, "UnregisterFailurePlugin", true);
            doThrow(new IllegalStateException("unregister failed"))
                    .when(mockServiceRegistry)
                    .unregister(MenuProvider.class, (MenuProvider) plugin);

            try (LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                pluginManager.disableAllPlugins();
            }

            verify(plugin).disable();
        }

        @Test
        @DisplayName("Should log when forced plugin disable also fails")
        void testDisableAllPluginsLogsForcedDisableFailure() throws Exception {
            Plugin plugin = mock(Plugin.class);
            addMockPlugin("ForcedDisableFailurePlugin", plugin);
            setPluginEnabledState(pluginManager, "ForcedDisableFailurePlugin", true);
            doThrow(new IllegalStateException("disable failed")).when(plugin).disable();

            try (LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                pluginManager.disableAllPlugins();
            }

            verify(plugin, times(2)).disable();
            assertTrue(pluginManager.getAllPluginStates().get("ForcedDisableFailurePlugin"));
        }

        @Test
        @DisplayName("Should log reset operations for stored plugin states")
        void testResetAllPluginStatesLogsResetOperations() throws Exception {
            Plugin plugin = mock(Plugin.class);
            addMockPlugin("ResettablePlugin", plugin);
            setPluginEnabledState(pluginManager, "ResettablePlugin", true);

            try (LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                pluginManager.resetAllPluginStates();
            }

            assertFalse(pluginManager.getAllPluginStates().get("ResettablePlugin"));
        }

        @Test
        @DisplayName("Should log when disabling an already disabled plugin")
        void testDisablePluginAlreadyDisabledLogsDebugMessage() throws Exception {
            Plugin plugin = mock(Plugin.class);
            addMockPlugin("AlreadyDisabledDebugPlugin", plugin);
            when(plugin.getState()).thenReturn(PluginStatus.DISABLED);

            try (LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                pluginManager.disablePlugin("AlreadyDisabledDebugPlugin");
            }

            verify(plugin, never()).disable();
        }

        @Test
        @DisplayName("Should log plugin disable failures before wrapping them")
        void testDisablePluginLogsFailuresBeforeWrapping() throws Exception {
            Plugin plugin = mock(Plugin.class);
            addMockPlugin("LoggedDisableFailurePlugin", plugin);
            when(plugin.getState()).thenReturn(PluginStatus.ENABLED);
            doThrow(new IllegalStateException("disable failed")).when(plugin).disable();

            try (LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                assertThrows(RuntimeException.class, () -> pluginManager.disablePlugin("LoggedDisableFailurePlugin"));
            }
        }

        @Test
        @DisplayName("Should log when enabling an already enabled plugin")
        void testEnablePluginAlreadyEnabledLogsWarningMessage() throws Exception {
            Plugin plugin = mock(Plugin.class);
            addMockPlugin("AlreadyEnabledWarningPlugin", plugin);
            when(plugin.getState()).thenReturn(PluginStatus.ENABLED);
            when(plugin.isEnabled()).thenReturn(true);

            try (LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                pluginManager.enablePlugin("AlreadyEnabledWarningPlugin");
            }

            verify(plugin, never()).enable();
        }

        @Test
        @DisplayName("Should log plugin enable failures before wrapping them")
        void testEnablePluginLogsFailuresBeforeWrapping() throws Exception {
            Plugin plugin = mock(Plugin.class);
            addMockPlugin("LoggedEnableFailurePlugin", plugin);
            when(plugin.isEnabled()).thenReturn(false);
            when(plugin.getState()).thenReturn(PluginStatus.DISABLED);
            doThrow(new IllegalStateException("enable failed")).when(plugin).enable();

            try (LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                assertThrows(RuntimeException.class, () -> pluginManager.enablePlugin("LoggedEnableFailurePlugin"));
            }
        }

        @Test
        @DisplayName("Should log a successful enablePluginByName operation")
        void testEnablePluginByNameLogsSuccessfulEnable() throws Exception {
            Plugin plugin = mock(Plugin.class);
            addMockPlugin("EnableByNamePlugin", plugin);
            when(plugin.getState()).thenReturn(PluginStatus.DISABLED);
            when(plugin.isEnabled()).thenReturn(false);

            try (LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                pluginManager.enablePluginByName("EnableByNamePlugin");
            }

            verify(plugin).enable();
        }

        @Test
        @DisplayName("Should use stored state when checking whether a plugin is enabled")
        void testIsPluginEnabledUsesStoredState() throws Exception {
            Plugin plugin = mock(Plugin.class);
            addMockPlugin("StatePlugin", plugin);
            when(plugin.isEnabled()).thenReturn(false);
            setPluginEnabledState(pluginManager, "StatePlugin", true);

            assertTrue(pluginManager.isPluginEnabled("StatePlugin"));
            verify(plugin, never()).isEnabled();
        }

        @Test
        @DisplayName("Should skip plugins that throw while filtering enabled plugins")
        void testGetEnabledPluginsSkipsPluginsThatThrow() throws Exception {
            Plugin brokenPlugin = mock(Plugin.class);
            addMockPlugin("BrokenFilterPlugin", brokenPlugin);
            when(brokenPlugin.getName()).thenReturn(null);

            try (LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                List<Plugin> enabledPlugins = pluginManager.getEnabledPlugins();
                assertTrue(enabledPlugins.isEmpty());
            }
        }

        @Test
        @DisplayName("Should disable menu provider plugins and publish events")
        void testDisablePluginHandlesMenuProviders() throws Exception {
            Plugin plugin = mock(Plugin.class, withSettings().extraInterfaces(MenuProvider.class));
            addMockPlugin("MenuPlugin", plugin);
            when(plugin.getState()).thenReturn(PluginStatus.ENABLED);

            try (LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                pluginManager.disablePlugin("MenuPlugin");
            }

            verify(plugin).disable();
            verify(mockServiceRegistry).unregister(MenuProvider.class, (MenuProvider) plugin);
            verify(mockEventBus).publish(isA(PluginMenuRemovedEvent.class));
            verify(mockEventBus).publish(isA(PluginDisabledEvent.class));
            assertFalse(pluginManager.getAllPluginStates().get("MenuPlugin"));
        }

        @Test
        @DisplayName("Should wrap plugin disable failures")
        void testDisablePluginWrapsFailures() throws Exception {
            Plugin plugin = mock(Plugin.class);
            addMockPlugin("BrokenDisablePlugin", plugin);
            when(plugin.getState()).thenReturn(PluginStatus.ENABLED);
            doThrow(new IllegalStateException("disable failed")).when(plugin).disable();

            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> pluginManager.disablePlugin("BrokenDisablePlugin")
            );
            assertEquals("Failed to disable plugin: BrokenDisablePlugin", exception.getMessage());
        }

        @Test
        @DisplayName("Should throw when disabling a missing plugin")
        void testDisablePluginThrowsWhenPluginIsMissing() {
            try (LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                assertThrows(IllegalArgumentException.class, () -> pluginManager.disablePlugin("MissingPlugin"));
            }
        }

        @Test
        @DisplayName("Should register menu provider plugins and publish events")
        void testEnablePluginRegistersMenuProviders() throws Exception {
            Plugin plugin = mock(Plugin.class, withSettings().extraInterfaces(MenuProvider.class));
            addMockPlugin("MenuEnablePlugin", plugin);
            when(plugin.getState()).thenReturn(PluginStatus.DISABLED);
            when(plugin.isEnabled()).thenReturn(false);

            try (LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                pluginManager.enablePlugin("MenuEnablePlugin");
            }

            verify(plugin).enable();
            verify(mockServiceRegistry).register(MenuProvider.class, (MenuProvider) plugin);
            verify(mockEventBus).publish(isA(PluginMenuAddedEvent.class));
            verify(mockEventBus).publish(isA(PluginEnabledEvent.class));
            assertTrue(pluginManager.getAllPluginStates().get("MenuEnablePlugin"));
        }

        @Test
        @DisplayName("Should wrap plugin enable failures")
        void testEnablePluginWrapsFailures() throws Exception {
            Plugin plugin = mock(Plugin.class);
            addMockPlugin("BrokenEnablePlugin", plugin);
            when(plugin.isEnabled()).thenReturn(false);
            when(plugin.getState()).thenReturn(PluginStatus.DISABLED);
            doThrow(new IllegalStateException("enable failed")).when(plugin).enable();

            RuntimeException exception = assertThrows(
                    RuntimeException.class,
                    () -> pluginManager.enablePlugin("BrokenEnablePlugin")
            );
            assertEquals("Failed to enable plugin: BrokenEnablePlugin", exception.getMessage());
            assertFalse(pluginManager.getAllPluginStates().get("BrokenEnablePlugin"));
        }

        @Test
        @DisplayName("Should skip enablePluginByName when plugin is already enabled")
        void testEnablePluginByNameSkipsAlreadyEnabledPlugins() throws Exception {
            Plugin plugin = mock(Plugin.class);
            addMockPlugin("AlreadyEnabledPlugin", plugin);
            when(plugin.getState()).thenReturn(PluginStatus.ENABLED);

            try (LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                pluginManager.enablePluginByName("AlreadyEnabledPlugin");
            }

            verify(plugin, never()).enable();
        }

        @Test
        @DisplayName("Should warn when enablePluginByName cannot find the plugin")
        void testEnablePluginByNameLogsWhenPluginMissing() {
            try (LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                pluginManager.enablePluginByName("UnknownPlugin");
            }
        }

        @Test
        @DisplayName("Should disable enabled plugins and unregister menu providers")
        void testDisableAllPluginsDisablesEnabledPlugins() throws Exception {
            Plugin enabledMenuPlugin = mock(Plugin.class, withSettings().extraInterfaces(MenuProvider.class));
            addMockPlugin("EnabledMenuPlugin", enabledMenuPlugin);
            setPluginEnabledState(pluginManager, "EnabledMenuPlugin", true);

            Plugin alreadyDisabledPlugin = mock(Plugin.class);
            addMockPlugin("AlreadyDisabledPlugin", alreadyDisabledPlugin);
            setPluginEnabledState(pluginManager, "AlreadyDisabledPlugin", false);

            try (LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                pluginManager.disableAllPlugins();
            }

            verify(enabledMenuPlugin).disable();
            verify(mockServiceRegistry).unregister(MenuProvider.class, (MenuProvider) enabledMenuPlugin);
            verify(alreadyDisabledPlugin, never()).disable();
            assertFalse(pluginManager.getAllPluginStates().get("EnabledMenuPlugin"));
            assertFalse(pluginManager.getAllPluginStates().get("AlreadyDisabledPlugin"));
        }

        @Test
        @DisplayName("Should force a second disable attempt after an initial failure")
        void testDisableAllPluginsForcesSecondDisableAttemptAfterFailure() throws Exception {
            Plugin plugin = mock(Plugin.class);
            addMockPlugin("RetryDisablePlugin", plugin);
            setPluginEnabledState(pluginManager, "RetryDisablePlugin", true);
            doThrow(new IllegalStateException("first failure"))
                    .doNothing()
                    .when(plugin).disable();

            try (LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                pluginManager.disableAllPlugins();
            }

            verify(plugin, times(2)).disable();
            assertFalse(pluginManager.getAllPluginStates().get("RetryDisablePlugin"));
        }

        @Test
        @DisplayName("Should handle critical verification failures while disabling all plugins")
        void testDisableAllPluginsHandlesCriticalVerificationFailure() throws Exception {
            Plugin plugin = mock(Plugin.class);
            addMockPlugin("CriticalFailurePlugin", plugin);
            when(plugin.getName()).thenReturn(null);

            try (LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                assertDoesNotThrow(() -> pluginManager.disableAllPlugins());
            }
        }

        @Test
        @DisplayName("Should shutdown all plugins, cleanup files and stop the event bus")
        void testShutdownAllCleansFilesAndStopsEventBus() throws Exception {
            Plugin plugin = mock(Plugin.class);
            addMockPlugin("ShutdownPlugin", plugin);
            when(plugin.getState()).thenReturn(PluginStatus.DISABLED);
            File tempPluginFile = tempDir.resolve("shutdown-cleanup.jar").toFile();
            assertTrue(tempPluginFile.createNewFile());
            addTemporaryPluginFile(tempPluginFile);

            try (LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                pluginManager.shutdownAll();
            }

            verify(plugin).shutdown();
            verify(mockEventBus).shutdown();
            assertFalse(tempPluginFile.exists());
            assertTrue(pluginManager.getPlugins().isEmpty());
        }

        @Test
        @DisplayName("Should handle loadAndEnable when the directory is not a directory")
        void testLoadAndEnablePluginByNameHandlesNullJarListing() throws IOException {
            File file = tempDir.resolve("not-a-directory.txt").toFile();
            assertTrue(file.createNewFile());

            try (LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                pluginManager.loadAndEnablePluginByName(file.getAbsolutePath(), "Anything");
            }

            assertTrue(pluginManager.getPlugins().isEmpty());
        }

        @Test
        @DisplayName("Should skip jars that do not declare a plugin class")
        void testLoadAndEnablePluginByNameSkipsMissingPluginClass() throws Exception {
            Manifest manifest = createManifest("NoDeclaredClassPlugin", "placeholder.PluginClass");
            manifest.getMainAttributes().putValue("Plugin-Class", "");
            File jarFile = createJar(
                    "load-and-enable/no-class.jar",
                    manifest,
                    "plugin.class=\n"
            );
            File jarDirectory = jarFile.getParentFile();

            try (LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                pluginManager.loadAndEnablePluginByName(jarDirectory.getAbsolutePath(), "MissingPlugin");
            }

            assertTrue(pluginManager.getPlugins().isEmpty());
        }

        @Test
        @DisplayName("Should skip classes that do not implement Plugin")
        void testLoadAndEnablePluginByNameSkipsNonPluginClasses() throws Exception {
            File jarFile = createJar(
                    "load-and-enable/non-plugin.jar",
                    createManifest("NonPluginClass", NotAPlugin.class.getName()),
                    null
            );

            try (LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                pluginManager.loadAndEnablePluginByName(jarFile.getParentFile().getAbsolutePath(), "NonPluginClass");
            }

            assertTrue(pluginManager.getPlugins().isEmpty());
        }

        @Test
        @DisplayName("Should close the loader when the loaded plugin name does not match")
        void testLoadAndEnablePluginByNameClosesLoaderForNameMismatch() throws Exception {
            ConfigurableJarPlugin.configure("ActualPluginName", false, false, false);
            File jarFile = createJar(
                    "load-and-enable/name-mismatch.jar",
                    createManifest("ManifestPluginName", ConfigurableJarPlugin.class.getName()),
                    null
            );

            try (LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                pluginManager.loadAndEnablePluginByName(jarFile.getParentFile().getAbsolutePath(), "ExpectedPluginName");
            }

            assertNull(pluginManager.getPlugin("ActualPluginName"));
            assertEquals(1, ConfigurableJarPlugin.initializeCalls);
        }

        @Test
        @DisplayName("Should expose the current broken loadAndEnable behavior without a preloaded plugin")
        void testLoadAndEnablePluginByNameBrokenBehaviorWithoutPreloadedPlugin() throws Exception {
            ConfigurableJarPlugin.configure("BrokenTargetPlugin", false, false, false);
            File jarFile = createJar(
                    "broken-load-and-enable/broken-target.jar",
                    createManifest("BrokenTargetPlugin", ConfigurableJarPlugin.class.getName()),
                    null
            );

            try (LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                pluginManager.loadAndEnablePluginByName(jarFile.getParentFile().getAbsolutePath(),
                        "BrokenTargetPlugin");
            }

            assertEquals(1, ConfigurableJarPlugin.initializeCalls);
            assertEquals(0, ConfigurableJarPlugin.enableCalls);
            assertNull(pluginManager.getPlugin("BrokenTargetPlugin"));
        }

        @Test
        @DisplayName("Should only succeed in loadAndEnable when a plugin with the same name was preloaded")
        void testLoadAndEnablePluginByNameSucceedsWithPreloadedPlugin() throws Exception {
            Plugin preloadedPlugin = mock(Plugin.class);
            addMockPlugin("PreloadedTargetPlugin", preloadedPlugin);
            when(preloadedPlugin.getState()).thenReturn(PluginStatus.DISABLED);
            when(preloadedPlugin.isEnabled()).thenReturn(false);
            ConfigurableJarPlugin.configure("PreloadedTargetPlugin", false, false, false);
            File jarFile = createJar(
                    "preloaded-load-and-enable/target.jar",
                    createManifest("PreloadedTargetPlugin", ConfigurableJarPlugin.class.getName()),
                    null
            );

            try (LogCapture ignored = LogCapture.attach(DefaultPluginManager.class)) {
                pluginManager.loadAndEnablePluginByName(jarFile.getParentFile().getAbsolutePath(),
                        "PreloadedTargetPlugin");
            }

            verify(preloadedPlugin).enable();
            Plugin storedPlugin = pluginManager.getPlugin("PreloadedTargetPlugin");
            assertNotNull(storedPlugin);
            assertNotSame(preloadedPlugin, storedPlugin);
            assertTrue(pluginManager.isPluginEnabled("PreloadedTargetPlugin"));
        }
        @Test
        @DisplayName("Should cover logger-disabled directory and cleanup branches")
        void testLoggerDisabledDirectoryAndCleanupBranches() throws Exception {
            TestUtils.withLoggingOffThrowing(DefaultPluginManager.class, () -> {
                File existingDirectory = tempDir.resolve("pre-existing-plugins").toFile();
                assertTrue(existingDirectory.mkdirs());
                pluginManager.setPluginsDirectory(existingDirectory.getAbsolutePath());

                File blockingFile = tempDir.resolve("blocked-parent-no-log.txt").toFile();
                assertTrue(blockingFile.createNewFile());
                File blockedDirectory = new File(blockingFile, "plugins");
                pluginManager.setPluginsDirectory(blockedDirectory.getAbsolutePath());

                File cleanupFile = tempDir.resolve("cleanup-no-log.jar").toFile();
                assertTrue(cleanupFile.createNewFile());
                addTemporaryPluginFile(cleanupFile);
                addTemporaryPluginFile(new ThrowingFile(tempDir.resolve("cleanup-throw-no-log.jar").toString()));

                File nonEmptyDirectory = tempDir.resolve("cleanup-non-empty-no-log").toFile();
                assertTrue(nonEmptyDirectory.mkdirs());
                assertTrue(new File(nonEmptyDirectory, "child.txt").createNewFile());
                addTemporaryPluginFile(nonEmptyDirectory);

                invokePrivateMethod("cleanupTemporaryFiles", new Class<?>[0]);
                pluginManager.shutdownAll();
            });
        }

        @Test
        @DisplayName("Should cover logger-disabled loadAllPlugins and loadPlugins branches")
        void testLoggerDisabledLoadBranches() throws Exception {
            TestUtils.withLoggingOffThrowing(DefaultPluginManager.class, () -> {
                setPrivateField(pluginManager, "pluginsDirectory", null);
                pluginManager.loadAllPlugins();

                File blockedParent = tempDir.resolve("blocked-parent-load-no-log.txt").toFile();
                assertTrue(blockedParent.createNewFile());
                setPrivateField(pluginManager, "pluginsDirectory", new File(blockedParent, "plugins"));
                pluginManager.loadAllPlugins();

                File emptyDirectory = tempDir.resolve("empty-load-all-no-log").toFile();
                assertTrue(emptyDirectory.mkdirs());
                setPrivateField(pluginManager, "pluginsDirectory", emptyDirectory);
                pluginManager.loadAllPlugins();

                File jarDirectory = tempDir.resolve("jar-load-all-no-log").toFile();
                assertTrue(jarDirectory.mkdirs());
                setPrivateField(pluginManager, "pluginsDirectory", jarDirectory);
                ConfigurableJarPlugin.configure("NoLogDirectoryLoadPlugin", false, false, false);
                createJar(
                        "jar-load-all-no-log/valid.jar",
                        createManifest("NoLogDirectoryLoadPlugin", ConfigurableJarPlugin.class.getName()),
                        null
                );
                File brokenJar = new File(jarDirectory, "invalid.jar");
                try (FileOutputStream fos = new FileOutputStream(brokenJar)) {
                    fos.write("broken".getBytes(StandardCharsets.UTF_8));
                }
                pluginManager.loadAllPlugins();

                File notADirectory = tempDir.resolve("load-plugins-file.txt").toFile();
                assertTrue(notADirectory.createNewFile());
                pluginManager.loadPlugins(notADirectory.getAbsolutePath());

                File invalidJarDirectory = tempDir.resolve("invalid-manual-load-no-log").toFile();
                assertTrue(invalidJarDirectory.mkdirs());
                File invalidJar = new File(invalidJarDirectory, "invalid.jar");
                try (FileOutputStream fos = new FileOutputStream(invalidJar)) {
                    fos.write("broken".getBytes(StandardCharsets.UTF_8));
                }
                pluginManager.loadPlugins(invalidJarDirectory.getAbsolutePath());

                pluginManager.loadPlugins(null);
            });
        }

        @Test
        @DisplayName("Should cover logger-disabled unload branches")
        void testLoggerDisabledUnloadBranches() throws Exception {
            TestUtils.withLoggingOffThrowing(DefaultPluginManager.class, () -> {
                Plugin closeSuccessPlugin = mock(Plugin.class);
                addMockPlugin("CloseSuccessPlugin", closeSuccessPlugin);
                when(closeSuccessPlugin.getState()).thenReturn(PluginStatus.DISABLED);
                Map<String, ClassLoader> loaders = getPrivateField(pluginManager, "pluginClassLoaders");
                loaders.put("CloseSuccessPlugin", new URLClassLoader(new URL[0]));
                pluginManager.unloadPlugin("CloseSuccessPlugin");

                Plugin closeFailurePlugin = mock(Plugin.class);
                addMockPlugin("CloseFailurePluginNoLog", closeFailurePlugin);
                when(closeFailurePlugin.getState()).thenReturn(PluginStatus.DISABLED);
                loaders.put("CloseFailurePluginNoLog", new ThrowingUrlClassLoader());
                pluginManager.unloadPlugin("CloseFailurePluginNoLog");

                Plugin brokenShutdownPlugin = mock(Plugin.class);
                addMockPlugin("BrokenShutdownPluginNoLog", brokenShutdownPlugin);
                when(brokenShutdownPlugin.getState()).thenReturn(PluginStatus.DISABLED);
                doThrow(new IllegalStateException("shutdown failed")).when(brokenShutdownPlugin).shutdown();
                pluginManager.unloadPlugin("BrokenShutdownPluginNoLog");
            });
        }

        @Test
        @DisplayName("Should cover logger-disabled loadPlugin branches")
        void testLoggerDisabledLoadPluginBranches() throws Exception {
            TestUtils.withLoggingOffThrowing(DefaultPluginManager.class, () -> {
                ConfigurableJarPlugin.configure("NoLogManualPlugin", false, false, false);
                pluginManager.loadPlugin(createPluginJar(
                        "no-log-manual-plugin.jar",
                        "NoLogManualPlugin",
                        ConfigurableJarPlugin.class
                ));

                ConfigurableJarPlugin.configure("NoLogAutoPlugin", true, false, false);
                pluginManager.loadPlugin(createPluginJar(
                        "no-log-auto-plugin.jar",
                        "NoLogAutoPlugin",
                        ConfigurableJarPlugin.class
                ));

                ConfigurableJarPlugin.configure("NoLogBrokenAutoPlugin", true, true, false);
                pluginManager.loadPlugin(createPluginJar(
                        "no-log-broken-auto-plugin.jar",
                        "NoLogBrokenAutoPlugin",
                        ConfigurableJarPlugin.class
                ));
            });
        }

        @Test
        @DisplayName("Should cover logger-disabled disableAllPlugins branches")
        void testLoggerDisabledDisableAllPluginsBranches() throws Exception {
            TestUtils.withLoggingOffThrowing(DefaultPluginManager.class, () -> {
                DefaultPluginManager plainManager = new DefaultPluginManager(
                        mock(ServiceRegistry.class),
                        mock(EventBus.class),
                        mockContext,
                        tempDir.resolve("plain-manager").toString()
                );
                Plugin plainPlugin = mock(Plugin.class);
                addMockPlugin(plainManager, "PlainEnabledPlugin", plainPlugin);
                setPluginEnabledState(plainManager, "PlainEnabledPlugin", true);
                Plugin plainDisabledPlugin = mock(Plugin.class);
                addMockPlugin(plainManager, "PlainDisabledPlugin", plainDisabledPlugin);
                plainManager.disableAllPlugins();

                DefaultPluginManager menuManager = new DefaultPluginManager(
                        mockServiceRegistry,
                        mockEventBus,
                        mockContext,
                        tempDir.resolve("menu-manager").toString()
                );
                Plugin menuPlugin = mock(Plugin.class, withSettings().extraInterfaces(MenuProvider.class));
                addMockPlugin(menuManager, "MenuEnabledPlugin", menuPlugin);
                setPluginEnabledState(menuManager, "MenuEnabledPlugin", true);
                menuManager.disableAllPlugins();

                ServiceRegistry failingRegistry = mock(ServiceRegistry.class);
                DefaultPluginManager unregisterFailureManager = new DefaultPluginManager(
                        failingRegistry,
                        mock(EventBus.class),
                        mockContext,
                        tempDir.resolve("unregister-failure-manager").toString()
                );
                Plugin unregisterFailurePlugin = mock(Plugin.class, withSettings().extraInterfaces(MenuProvider.class));
                addMockPlugin(unregisterFailureManager, "UnregisterFailureNoLogPlugin", unregisterFailurePlugin);
                setPluginEnabledState(unregisterFailureManager, "UnregisterFailureNoLogPlugin", true);
                doThrow(new IllegalStateException("unregister failed"))
                        .when(failingRegistry)
                        .unregister(MenuProvider.class, (MenuProvider) unregisterFailurePlugin);
                unregisterFailureManager.disableAllPlugins();

                DefaultPluginManager retryManager = new DefaultPluginManager(
                        mock(ServiceRegistry.class),
                        mock(EventBus.class),
                        mockContext,
                        tempDir.resolve("retry-manager").toString()
                );
                Plugin retryPlugin = mock(Plugin.class);
                addMockPlugin(retryManager, "RetryNoLogPlugin", retryPlugin);
                setPluginEnabledState(retryManager, "RetryNoLogPlugin", true);
                doThrow(new IllegalStateException("first failure"))
                        .doNothing()
                        .when(retryPlugin)
                        .disable();
                retryManager.disableAllPlugins();

                DefaultPluginManager forcedFailureManager = new DefaultPluginManager(
                        mock(ServiceRegistry.class),
                        mock(EventBus.class),
                        mockContext,
                        tempDir.resolve("forced-failure-manager").toString()
                );
                Plugin forcedFailurePlugin = mock(Plugin.class);
                addMockPlugin(forcedFailureManager, "ForcedFailureNoLogPlugin", forcedFailurePlugin);
                setPluginEnabledState(forcedFailureManager, "ForcedFailureNoLogPlugin", true);
                doThrow(new IllegalStateException("disable failed")).when(forcedFailurePlugin).disable();
                forcedFailureManager.disableAllPlugins();

                DefaultPluginManager criticalFailureManager = new DefaultPluginManager(
                        mock(ServiceRegistry.class),
                        mock(EventBus.class),
                        mockContext,
                        tempDir.resolve("critical-failure-manager").toString()
                );
                Plugin criticalFailurePlugin = mock(Plugin.class);
                addMockPlugin(criticalFailureManager, "CriticalFailureNoLogPlugin", criticalFailurePlugin);
                when(criticalFailurePlugin.getName()).thenReturn(null);
                criticalFailureManager.disableAllPlugins();

                pluginManager.resetAllPluginStates();
            });
        }

        @Test
        @DisplayName("Should cover logger-disabled enabled-plugin filtering")
        void testLoggerDisabledEnabledPluginFilteringBranch() throws Exception {
            TestUtils.withLoggingOffThrowing(DefaultPluginManager.class, () -> {
                Plugin brokenPlugin = mock(Plugin.class);
                addMockPlugin("BrokenFilterNoLogPlugin", brokenPlugin);
                when(brokenPlugin.getName()).thenReturn(null);

                assertTrue(pluginManager.getEnabledPlugins().isEmpty());
            });
        }

        @Test
        @DisplayName("Should cover logger-disabled enable and disable plugin branches")
        void testLoggerDisabledEnableAndDisablePluginBranches() throws Exception {
            TestUtils.withLoggingOffThrowing(DefaultPluginManager.class, () -> {
                DefaultPluginManager disableMenuManager = new DefaultPluginManager(
                        mockServiceRegistry,
                        mockEventBus,
                        mockContext,
                        tempDir.resolve("disable-menu-manager").toString()
                );
                Plugin disableMenuPlugin = mock(Plugin.class, withSettings().extraInterfaces(MenuProvider.class));
                addMockPlugin(disableMenuManager, "DisableMenuNoLogPlugin", disableMenuPlugin);
                when(disableMenuPlugin.getState()).thenReturn(PluginStatus.ENABLED);
                disableMenuManager.disablePlugin("DisableMenuNoLogPlugin");
                assertThrows(IllegalArgumentException.class, () -> disableMenuManager.disablePlugin("MissingNoLogPlugin"));

                Plugin alreadyDisabledPlugin = mock(Plugin.class);
                addMockPlugin(disableMenuManager, "AlreadyDisabledNoLogPlugin", alreadyDisabledPlugin);
                when(alreadyDisabledPlugin.getState()).thenReturn(PluginStatus.DISABLED);
                disableMenuManager.disablePlugin("AlreadyDisabledNoLogPlugin");

                Plugin failingDisablePlugin = mock(Plugin.class);
                addMockPlugin(disableMenuManager, "FailingDisableNoLogPlugin", failingDisablePlugin);
                when(failingDisablePlugin.getState()).thenReturn(PluginStatus.ENABLED);
                doThrow(new IllegalStateException("disable failed")).when(failingDisablePlugin).disable();
                assertThrows(RuntimeException.class,
                        () -> disableMenuManager.disablePlugin("FailingDisableNoLogPlugin"));

                DefaultPluginManager enableManager = new DefaultPluginManager(
                        mockServiceRegistry,
                        mockEventBus,
                        mockContext,
                        tempDir.resolve("enable-manager").toString()
                );
                Plugin alreadyEnabledPlugin = mock(Plugin.class);
                addMockPlugin(enableManager, "AlreadyEnabledNoLogPlugin", alreadyEnabledPlugin);
                when(alreadyEnabledPlugin.getState()).thenReturn(PluginStatus.ENABLED);
                when(alreadyEnabledPlugin.isEnabled()).thenReturn(true);
                enableManager.enablePlugin("AlreadyEnabledNoLogPlugin");

                Plugin enableMenuPlugin = mock(Plugin.class, withSettings().extraInterfaces(MenuProvider.class));
                addMockPlugin(enableManager, "EnableMenuNoLogPlugin", enableMenuPlugin);
                when(enableMenuPlugin.getState()).thenReturn(PluginStatus.DISABLED);
                when(enableMenuPlugin.isEnabled()).thenReturn(false);
                enableManager.enablePlugin("EnableMenuNoLogPlugin");

                Plugin failingEnablePlugin = mock(Plugin.class);
                addMockPlugin(enableManager, "FailingEnableNoLogPlugin", failingEnablePlugin);
                when(failingEnablePlugin.getState()).thenReturn(PluginStatus.DISABLED);
                when(failingEnablePlugin.isEnabled()).thenReturn(false);
                doThrow(new IllegalStateException("enable failed")).when(failingEnablePlugin).enable();
                assertThrows(RuntimeException.class,
                        () -> enableManager.enablePlugin("FailingEnableNoLogPlugin"));

                assertThrows(IllegalArgumentException.class,
                        () -> enableManager.enablePlugin("MissingEnableNoLogPlugin"));
            });
        }

        @Test
        @DisplayName("Should cover logger-disabled enablePluginByName branches")
        void testLoggerDisabledEnablePluginByNameBranches() throws Exception {
            TestUtils.withLoggingOffThrowing(DefaultPluginManager.class, () -> {
                DefaultPluginManager localManager = new DefaultPluginManager(
                        mockServiceRegistry,
                        mockEventBus,
                        mockContext,
                        tempDir.resolve("enable-by-name-manager").toString()
                );
                Plugin alreadyEnabledPlugin = mock(Plugin.class);
                addMockPlugin(localManager, "AlreadyEnabledByNameNoLogPlugin", alreadyEnabledPlugin);
                when(alreadyEnabledPlugin.getState()).thenReturn(PluginStatus.ENABLED);
                localManager.enablePluginByName("AlreadyEnabledByNameNoLogPlugin");

                Plugin notYetEnabledPlugin = mock(Plugin.class);
                addMockPlugin(localManager, "NotYetEnabledByNameNoLogPlugin", notYetEnabledPlugin);
                when(notYetEnabledPlugin.getState()).thenReturn(PluginStatus.DISABLED);
                when(notYetEnabledPlugin.isEnabled()).thenReturn(false);
                localManager.enablePluginByName("NotYetEnabledByNameNoLogPlugin");

                localManager.enablePluginByName("MissingByNameNoLogPlugin");
            });
        }

        @Test
        @DisplayName("Should cover logger-disabled plugin class discovery branches")
        void testLoggerDisabledFindPluginClassNameBranches() throws Exception {
            TestUtils.withLoggingOffThrowing(DefaultPluginManager.class, () -> {
                File validPropertiesJar = createJar(
                        "find-plugin-class-valid-no-log.jar",
                        createManifest("FindValidProperties", "manifest.PluginClass"),
                        "plugin.class=" + ConfigurableJarPlugin.class.getName() + "\n"
                );
                try (JarFile openedJar = new JarFile(validPropertiesJar)) {
                    assertEquals(
                            ConfigurableJarPlugin.class.getName(),
                            invokePrivateMethod("findPluginClassName", new Class<?>[]{JarFile.class}, openedJar)
                    );
                }

                File nullPropertyJar = createJar(
                        "find-plugin-class-null-property.jar",
                        createManifest("FindNullProperty", ConfigurableJarPlugin.class.getName()),
                        "other.property=value\n"
                );
                try (JarFile openedJar = new JarFile(nullPropertyJar)) {
                    assertEquals(
                            ConfigurableJarPlugin.class.getName(),
                            invokePrivateMethod("findPluginClassName", new Class<?>[]{JarFile.class}, openedJar)
                    );
                }

                File blankPropertiesJar = createJar(
                        "find-plugin-class-blank-no-log.jar",
                        createManifest("FindBlankProperties", ConfigurableJarPlugin.class.getName()),
                        "plugin.class=   \n"
                );
                try (JarFile openedJar = new JarFile(blankPropertiesJar)) {
                    assertEquals(
                            ConfigurableJarPlugin.class.getName(),
                            invokePrivateMethod("findPluginClassName", new Class<?>[]{JarFile.class}, openedJar)
                    );
                }

                File readableJar = createJar(
                        "find-plugin-class-io-no-log.jar",
                        createManifest("FindIoProperties", ConfigurableJarPlugin.class.getName()),
                        "plugin.class=" + ConfigurableJarPlugin.class.getName() + "\n"
                );
                try (JarFile openedJar = new UnreadablePropertiesJarFile(readableJar)) {
                    assertEquals(
                            ConfigurableJarPlugin.class.getName(),
                            invokePrivateMethod("findPluginClassName", new Class<?>[]{JarFile.class}, openedJar)
                    );
                }

                File manifestOnlyJar = createJar(
                        "find-plugin-class-manifest-only-no-log.jar",
                        createManifest("FindManifestOnly", ConfigurableJarPlugin.class.getName()),
                        null
                );
                try (JarFile openedJar = new JarFile(manifestOnlyJar)) {
                    assertEquals(
                            ConfigurableJarPlugin.class.getName(),
                            invokePrivateMethod("findPluginClassName", new Class<?>[]{JarFile.class}, openedJar)
                    );
                }

                Manifest manifest = createManifest("FindNoClass", "placeholder.PluginClass");
                manifest.getMainAttributes().remove(new Attributes.Name("Plugin-Class"));
                File noClassJar = createJar("find-plugin-class-none-no-log.jar", manifest, null);
                try (JarFile openedJar = new JarFile(noClassJar)) {
                    assertNull(invokePrivateMethod("findPluginClassName", new Class<?>[]{JarFile.class}, openedJar));
                }
            });
        }

        @Test
        @DisplayName("Should cover logger-disabled loadAndEnable branches")
        void testLoggerDisabledLoadAndEnableBranches() throws Exception {
            TestUtils.withLoggingOffThrowing(DefaultPluginManager.class, () -> {
                ConfigurableJarPlugin.configure("BrokenNoLogTargetPlugin", false, false, false);
                File brokenJar = createJar(
                        "load-and-enable-no-log/broken.jar",
                        createManifest("BrokenNoLogTargetPlugin", ConfigurableJarPlugin.class.getName()),
                        null
                );
                pluginManager.loadAndEnablePluginByName(
                        brokenJar.getParentFile().getAbsolutePath(),
                        "BrokenNoLogTargetPlugin"
                );

                DefaultPluginManager successManager = new DefaultPluginManager(
                        mockServiceRegistry,
                        mockEventBus,
                        mockContext,
                        tempDir.resolve("load-and-enable-success-manager").toString()
                );
                Plugin preloadedPlugin = mock(Plugin.class);
                addMockPlugin(successManager, "SuccessNoLogTargetPlugin", preloadedPlugin);
                when(preloadedPlugin.getState()).thenReturn(PluginStatus.DISABLED);
                when(preloadedPlugin.isEnabled()).thenReturn(false);
                ConfigurableJarPlugin.configure("SuccessNoLogTargetPlugin", false, false, false);
                File successJar = createJar(
                        "load-and-enable-no-log/success.jar",
                        createManifest("SuccessNoLogTargetPlugin", ConfigurableJarPlugin.class.getName()),
                        null
                );
                successManager.loadAndEnablePluginByName(
                        successJar.getParentFile().getAbsolutePath(),
                        "SuccessNoLogTargetPlugin"
                );
            });
        }
    }

    public static class ConfigurableJarPlugin extends AbstractPlugin {
        private static String configuredName = "JarPlugin";
        private static boolean autoEnable;
        private static boolean failEnable;
        private static boolean nullConfig;
        private static PluginContext lastContext;
        private static int initializeCalls;
        private static int enableCalls;

        public ConfigurableJarPlugin() {
            super(configuredName, "1.0.0", "Test plugin", "Test Author");
            if (!nullConfig) {
                PluginConfig config = new PluginConfig();
                config.setAutoEnable(autoEnable);
                setConfig(config);
            }
        }

        static void configure(String pluginName, boolean autoEnableValue, boolean failEnableValue,
                              boolean nullConfigValue) {
            configuredName = pluginName;
            autoEnable = autoEnableValue;
            failEnable = failEnableValue;
            nullConfig = nullConfigValue;
            lastContext = null;
            initializeCalls = 0;
            enableCalls = 0;
        }

        static void reset() {
            configure("JarPlugin", false, false, false);
        }

        @Override
        public void initialize(PluginContext context) {
            initializeCalls++;
            lastContext = context;
            super.initialize(context);
        }

        @Override
        public void enable() {
            enableCalls++;
            if (failEnable) {
                throw new IllegalStateException("enable failure");
            }
            super.enable();
        }

        @Override
        public PluginConfig getConfig() {
            if (nullConfig) {
                return null;
            }
            return super.getConfig();
        }
    }

    public static class NotAPlugin {
        public NotAPlugin() {
        }
    }

    private static final class ThrowingUrlClassLoader extends URLClassLoader {
        private ThrowingUrlClassLoader() {
            super(new URL[0]);
        }

        @Override
        public void close() throws IOException {
            throw new IOException("close failure");
        }
    }

    private static final class ThrowingFile extends File {
        private ThrowingFile(String pathname) {
            super(pathname);
        }

        @Override
        public boolean exists() {
            throw new IllegalStateException("exists failure");
        }
    }

    private static final class UnreadablePropertiesJarFile extends JarFile {
        private UnreadablePropertiesJarFile(File file) throws IOException {
            super(file);
        }

        @Override
        public InputStream getInputStream(java.util.zip.ZipEntry ze) throws IOException {
            if ("plugin.properties".equals(ze.getName())) {
                throw new IOException("Cannot read plugin.properties");
            }
            return new ByteArrayInputStream(new byte[0]);
        }
    }

}
