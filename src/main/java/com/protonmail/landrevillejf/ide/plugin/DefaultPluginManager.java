package com.protonmail.landrevillejf.ide.plugin;

import com.protonmail.landrevillejf.ide.plugin.events.*;
import com.protonmail.landrevillejf.swingide.core.bus.EventBus;
import com.protonmail.landrevillejf.swingide.core.registry.ServiceRegistry;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

@Slf4j
public final class DefaultPluginManager implements PluginManager {
    private final ServiceRegistry serviceRegistry;
    private final EventBus eventBus;
    private final Map<String, Plugin> plugins = new HashMap<>();
    private final Map<String, ClassLoader> pluginClassLoaders = new HashMap<>();
    private File pluginsDirectory;
    @Getter
    private final PluginContext context;
    private final List<File> temporaryPluginFiles = new ArrayList<>();
    private final Map<String, Boolean> pluginEnabledStates = new HashMap<>();
    private static final String PLUGIN_PROPERTIES = "plugin.properties";
    private static final String PLUGIN_CLASS_ATTRIBUTE = "plugin.class";
    private static final String MANIFEST_PLUGIN_CLASS = "Plugin-Class";

    public DefaultPluginManager(ServiceRegistry serviceRegistry,
                                EventBus eventBus,
                                PluginContext context,
                                String pluginsDirectoryPath) {
        this.serviceRegistry = serviceRegistry;
        this.eventBus = eventBus;
        this.context = context;
        // Éviter NPE en vérifiant d'abord
        if (pluginsDirectoryPath != null) {
            setPluginsDirectory(pluginsDirectoryPath);
        } else {
            this.pluginsDirectory = null;
        }
    }

    public void setPluginsDirectory(String path) {
        if (path == null) {
            this.pluginsDirectory = null;
            return;
        }
        this.pluginsDirectory = new File(path);
        if (!pluginsDirectory.exists()) {
            boolean created = pluginsDirectory.mkdirs();
            if (!created && log.isWarnEnabled()) {
                log.warn("Failed to create plugins directory: {}", path);
            }
        }
    }

    private PluginDescriptor loadDescriptor(File jarFile) throws Exception {
        try (JarFile jar = new JarFile(jarFile)) {
            Manifest manifest = jar.getManifest();

            PluginDescriptor descriptor = new PluginDescriptor();
            descriptor.setId(manifest.getMainAttributes().getValue("Plugin-Id"));
            descriptor.setName(manifest.getMainAttributes().getValue("Plugin-Name"));
            descriptor.setVersion(manifest.getMainAttributes().getValue("Plugin-Version"));
            descriptor.setMainClass(manifest.getMainAttributes().getValue("Plugin-Class"));
            descriptor.setDescription(manifest.getMainAttributes().getValue("Plugin-Description"));
            descriptor.setAuthor(manifest.getMainAttributes().getValue("Plugin-Author"));

            return descriptor;
        }
    }

    public Map<String, Plugin> getPlugins() {
        synchronized (plugins) {
            return new HashMap<>(plugins);
        }
    }

    @Override
    public void shutdownAll() {
        if (log.isDebugEnabled()) {
            log.debug("Shutdown all plugins...");
        }
        unloadAllPlugins();
        cleanupTemporaryFiles();
        eventBus.shutdown();
    }

    private void cleanupTemporaryFiles() {
        for (File tempFile : temporaryPluginFiles) {
            try {
                if (tempFile.exists()) {
                    boolean deleted = tempFile.delete();
                    if (deleted && log.isDebugEnabled()) {
                        log.debug("Deleted temporary plugin file: {}", tempFile.getName());
                    }
                }
            } catch (Exception e) {
                if (log.isWarnEnabled()) {
                    log.warn("Could not delete temporary file {}: {}",
                            tempFile.getName(), e.getMessage());
                }
            }
        }
        temporaryPluginFiles.clear();
    }

    @Override
    public void loadAllPlugins() {
        loadPluginsFromDirectory();
        if (log.isDebugEnabled()) {
            log.debug("Total plugins loaded: {} ({} from classpath, {} from directory)",
                    plugins.size(), temporaryPluginFiles.size(),
                    plugins.size() - temporaryPluginFiles.size());
        }
    }

    private void loadPluginsFromDirectory() {
        if (pluginsDirectory == null) {
            if (log.isWarnEnabled()) {
                log.warn("No external plugins directory configured");
            }
            return;
        }

        if (!pluginsDirectory.exists()) {
            if (log.isDebugEnabled()) {
                log.debug("Creating external plugins directory: {}", pluginsDirectory);
            }
            boolean created = pluginsDirectory.mkdirs();
            if (!created && log.isWarnEnabled()) {
                log.warn("Failed to create plugins directory: {}", pluginsDirectory);
            }
            return;
        }

        File[] jarFiles = pluginsDirectory.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jarFiles == null || jarFiles.length == 0) {
            if (log.isDebugEnabled()) {
                log.debug("No plugins found in external directory: {}", pluginsDirectory);
            }
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("Loading {} plugins from external directory: {}",
                    jarFiles.length, pluginsDirectory);
        }

        for (File jarFile : jarFiles) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Loading external plugin: {}", jarFile.getName());
                }
                loadPlugin(jarFile);
            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error("Error loading external plugin {}: {}",
                            jarFile.getName(), e.getMessage());
                }
            }
        }
    }

    public void loadPlugins(String pluginsDirectory) {
        // Vérifier si le paramètre est null
        if (pluginsDirectory == null) {
            if (log.isWarnEnabled()) {
                log.warn("Cannot load plugins from null directory");
            }
            return;
        }

        File pluginsDir = new File(pluginsDirectory);
        if (!pluginsDir.exists() || !pluginsDir.isDirectory()) {
            if (log.isErrorEnabled()) {
                log.error("Plugin directory doesn't exist: {}", pluginsDirectory);
            }
            return;
        }

        File[] jarFiles = pluginsDir.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jarFiles == null) {
            return;
        }

        for (File jarFile : jarFiles) {
            try {
                loadPlugin(jarFile);
            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error("Error loading plugin: {} {}", jarFile.getName(), e.getMessage());
                }
            }
        }
    }

    public void unloadPlugin(String pluginName) {
        synchronized (plugins) {
            Plugin plugin = plugins.get(pluginName);
            if (plugin != null) {
                try {
                    plugin.shutdown();
                    plugins.remove(pluginName);

                    ClassLoader cl = pluginClassLoaders.remove(pluginName);
                    if (cl instanceof URLClassLoader) {
                        try {
                            ((URLClassLoader) cl).close();
                            if (log.isDebugEnabled()) {
                                log.debug("Closed classloader for plugin: {}", pluginName);
                            }
                        } catch (IOException e) {
                            if (log.isWarnEnabled()) {
                                log.warn("Error closing classloader for plugin {}: {}", pluginName, e.getMessage());
                            }
                        }
                    }

                    eventBus.publish(new PluginUnloadedEvent(
                            "PluginManager",
                            pluginName,
                            plugin.getClass().getSimpleName()
                    ));

                    if (log.isDebugEnabled()) {
                        log.debug("Plugin unloaded: {}", pluginName);
                    }
                } catch (Exception e) {
                    if (log.isErrorEnabled()) {
                        log.error("Error unloading plugin: {} {}", pluginName, e.getMessage());
                    }
                }
            }
        }
    }

    public void unloadAllPlugins() {
        synchronized (plugins) {
            for (String pluginName : new ArrayList<>(plugins.keySet())) {
                unloadPlugin(pluginName);
            }
        }
    }

    public void loadPlugin(File jarFile) throws Exception {
        JarFile jar = null;
        URLClassLoader classLoader = null;

        try {
            jar = new JarFile(jarFile);
            Manifest manifest = jar.getManifest();
            if (manifest == null) {
                throw new IllegalArgumentException("Fichier JAR sans manifest: " + jarFile.getName());
            }

            PluginDescriptor descriptor = loadDescriptor(jarFile);
            String pluginName = descriptor.getName();
            String pluginClass = descriptor.getMainClass();

            File pluginDataDir = new File(context.getPluginDataPath(), pluginName);

            URL[] urls = { jarFile.toURI().toURL() };

            classLoader = new URLClassLoader(urls, getClass().getClassLoader());

            Class<?> clazz = classLoader.loadClass(pluginClass);
            Plugin plugin = (Plugin) clazz.getDeclaredConstructor().newInstance();

            PluginContext pluginContext;
            if (this.context instanceof DefaultExtendedPluginContext) {
                // Si le contexte principal est étendu, créer un contexte étendu pour le plugin
                DefaultExtendedPluginContext mainExt = (DefaultExtendedPluginContext) this.context;
                pluginContext = new DefaultExtendedPluginContext(
                        serviceRegistry,
                        new PluginEventBus(),
                        eventBus,
                        this,
                        pluginDataDir,
                        pluginName,
                        plugin,
                        mainExt.getServiceLocator()
                );
            } else {
                // Fallback pour la rétrocompatibilité
                pluginContext = new DefaultPluginContext(
                        serviceRegistry,
                        new PluginEventBus(),
                        eventBus,
                        this,
                        pluginDataDir,
                        pluginName
                );
            }

            pluginClassLoaders.put(pluginName, classLoader);

            plugin.initialize(pluginContext);

            PluginConfig config = plugin.getConfig();
            boolean autoEnable = config != null && config.isAutoEnable();

            if (autoEnable) {
                try {
                    plugin.enable();
                    pluginEnabledStates.put(pluginName, true);
                    if (log.isDebugEnabled()) {
                        log.debug("Plugin auto-enabled: {} v{} (State: {})",
                                pluginName, plugin.getVersion(), plugin.getState());
                    }
                } catch (Exception e) {
                    if (log.isErrorEnabled()) {
                        log.error("Auto-enable failed for '{}': {}", pluginName, e.getMessage());
                    }
                    pluginEnabledStates.put(pluginName, false);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Plugin loaded: {} v{} (State: {})",
                            pluginName, plugin.getVersion(), plugin.getState());
                }
            }

            synchronized (plugins) {
                plugins.put(pluginName, plugin);
            }

        } catch (Exception e) {
            if (classLoader != null) {
                try {
                    classLoader.close();
                } catch (IOException ex) {
                    if (log.isWarnEnabled()) {
                        log.warn("Error closing classloader after load failure", ex);
                    }
                }
            }
            throw e;
        } finally {
            if (jar != null) {
                try {
                    jar.close();
                } catch (IOException e) {
                    if (log.isWarnEnabled()) {
                        log.warn("Error closing jar file", e);
                    }
                }
            }
        }
    }

    @Override
    public Map<String, Boolean> getAllPluginStates() {
        return new HashMap<>(pluginEnabledStates);
    }

    @Override
    public Plugin getPlugin(String pluginId) {
        synchronized (plugins) {
            return plugins.get(pluginId);
        }
    }

    @Override
    public List<Plugin> getLoadedPlugins() {
        synchronized (plugins) {
            return new ArrayList<>(plugins.values());
        }
    }

    @Override
    public PluginStatus getPluginStatus(String pluginName) {
        Plugin plugin = findPluginByName(pluginName);
        if (plugin == null) {
            return PluginStatus.UNLOADED;
        }
        return plugin.getState();
    }

    @Override
    public void disableAllPlugins() {
        if (log.isDebugEnabled()) {
            log.debug("=== DISABLING ALL PLUGINS ===");
        }

        // Faire une copie synchronisée pour éviter les modifications concurrentes
        List<Map.Entry<String, Plugin>> entries;
        synchronized (plugins) {
            entries = new ArrayList<>(plugins.entrySet());
        }

        try {
            int totalPlugins = entries.size();
            int disabledCount = 0;
            int errorCount = 0;

            if (log.isDebugEnabled()) {
                log.debug("Found {} plugins to disable", totalPlugins);
            }

            for (Map.Entry<String, Plugin> entry : entries) {
                String pluginName = entry.getKey();
                Plugin plugin = entry.getValue();

                try {
                    if (isPluginEnabled(pluginName)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Disabling plugin: {} v{}", pluginName, plugin.getVersion());
                        }

                        plugin.disable();
                        pluginEnabledStates.put(pluginName, false);

                        if (plugin instanceof MenuProvider) {
                            try {
                                serviceRegistry.unregister(MenuProvider.class, (MenuProvider) plugin);
                                if (log.isDebugEnabled()) {
                                    log.debug("Unregistered MenuProvider for plugin: {}", pluginName);
                                }
                            } catch (Exception e) {
                                if (log.isWarnEnabled()) {
                                    log.warn("Failed to unregister MenuProvider for plugin {}: {}",
                                            pluginName, e.getMessage());
                                }
                            }
                        }

                        disabledCount++;
                        if (log.isDebugEnabled()) {
                            log.debug("Successfully disabled plugin: {} v{}", pluginName, plugin.getVersion());
                        }
                    } else {
                        if (log.isDebugEnabled()) {
                            log.debug("Plugin {} is already disabled", pluginName);
                        }
                    }

                } catch (Exception e) {
                    errorCount++;
                    if (log.isErrorEnabled()) {
                        log.error("Error disabling plugin {}: {}", pluginName, e.getMessage());
                    }
                }
            }

            if (log.isDebugEnabled()) {
                log.debug("=== DISABLE ALL PLUGINS COMPLETE ===");
                log.debug("Total plugins: {}", totalPlugins);
                log.debug("Successfully disabled: {}", disabledCount);
                log.debug("Errors: {}", errorCount);
            }

            verifyAllPluginsDisabled();

        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Critical error in disableAllPlugins(): {}", e.getMessage(), e);
            }
        }
    }

    private void verifyAllPluginsDisabled() {
        if (log.isDebugEnabled()) {
            log.debug("Verifying all plugins are disabled...");
        }

        List<Map.Entry<String, Plugin>> entries;
        synchronized (plugins) {
            entries = new ArrayList<>(plugins.entrySet());
        }

        int stillEnabled = 0;
        for (Map.Entry<String, Plugin> entry : entries) {
            String pluginName = entry.getKey();

            if (isPluginEnabled(pluginName)) {
                stillEnabled++;
                if (log.isWarnEnabled()) {
                    log.warn("Plugin {} is still enabled after disableAllPlugins()", pluginName);
                }

                try {
                    Plugin plugin = entry.getValue();
                    plugin.disable();
                    pluginEnabledStates.put(pluginName, false);
                    if (log.isDebugEnabled()) {
                        log.debug("Forced disable of plugin: {}", pluginName);
                    }
                } catch (Exception e) {
                    if (log.isErrorEnabled()) {
                        log.error("Could not force disable plugin {}: {}", pluginName, e.getMessage());
                    }
                }
            }
        }

        if (stillEnabled == 0) {
            if (log.isDebugEnabled()) {
                log.debug("✓ All plugins are successfully disabled");
            }
        } else {
            if (log.isWarnEnabled()) {
                log.warn("{} plugins were still enabled after disableAllPlugins()", stillEnabled);
            }
        }
    }

    public void resetAllPluginStates() {
        if (log.isDebugEnabled()) {
            log.debug("Resetting all plugin states to DISABLED");
        }
        pluginEnabledStates.replaceAll((n, v) -> false);
        if (log.isDebugEnabled()) {
            log.debug("Reset {} plugin states", pluginEnabledStates.size());
        }
    }

    @Override
    public boolean isPluginEnabled(String pluginName) {
        Plugin plugin = findPluginByName(pluginName);
        if (plugin == null) {
            return false;
        }

        if (pluginEnabledStates.containsKey(pluginName)) {
            return pluginEnabledStates.get(pluginName);
        }

        return plugin.isEnabled();
    }

    @Override
    public List<Plugin> getEnabledPlugins() {
        List<Plugin> allPlugins;
        synchronized (plugins) {
            allPlugins = new ArrayList<>(plugins.values());
        }

        return allPlugins.stream()
                .filter(plugin -> {
                    try {
                        String pluginName = plugin.getName();
                        return isPluginEnabled(pluginName);
                    } catch (Exception e) {
                        if (log.isErrorEnabled()) {
                            log.error("Error checking if plugin {} is enabled: {}",
                                    plugin.getName(), e.getMessage());
                        }
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public void disablePlugin(String pluginName) {
        Plugin plugin = findPluginByName(pluginName);
        if (plugin != null) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("=== DISABLE PLUGIN ===");
                    log.debug("Plugin: {} v{}", pluginName, plugin.getVersion());
                }

                // Vérifier l'état avant
                PluginStatus currentState = plugin.getState();
                if (currentState == PluginStatus.DISABLED) {
                    if (log.isDebugEnabled()) {
                        log.debug("Plugin {} is already disabled", pluginName);
                    }
                    pluginEnabledStates.put(pluginName, false);
                    return;
                }

                plugin.disable();
                pluginEnabledStates.put(pluginName, false);

                if (plugin instanceof MenuProvider) {
                    serviceRegistry.unregister(MenuProvider.class, (MenuProvider) plugin);
                    if (log.isDebugEnabled()) {
                        log.debug("MenuProvider unregistered for plugin: {}", pluginName);
                    }
                }

                eventBus.publish(new PluginMenuRemovedEvent(pluginName, plugin.getClass().getSimpleName()));
                if (log.isDebugEnabled()) {
                    log.debug("Plugin {} v{} disabled successfully", pluginName, plugin.getVersion());
                }
                eventBus.publish(new PluginDisabledEvent(plugin.getClass().getSimpleName()));

            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error("Error disabling plugin {}: {}", pluginName, e.getMessage(), e);
                }
                throw new RuntimeException("Failed to disable plugin: " + pluginName, e);
            }
        } else {
            if (log.isWarnEnabled()) {
                log.warn("Plugin not found: {}", pluginName);
            }
            throw new IllegalArgumentException("Plugin not found: " + pluginName);
        }
    }

    @Override
    public void enablePlugin(String pluginName) {
        // Chercher d'abord par la clé exacte dans la map
        Plugin plugin = plugins.get(pluginName);

        // Si non trouvé, essayer par le nom
        if (plugin == null) {
            plugin = findPluginByName(pluginName);
        }

        if (plugin != null) {
            try {
                if (log.isDebugEnabled()) {
                    log.debug("=== ENABLE PLUGIN ===");
                    log.debug("Plugin: {} v{}", pluginName, plugin.getVersion());
                }

                if (isPluginEnabled(pluginName)) {
                    if (log.isWarnEnabled()) {
                        log.warn("Plugin {} v{} is already enabled", pluginName, plugin.getVersion());
                    }
                    return;
                }

                plugin.enable();
                pluginEnabledStates.put(pluginName, true);

                if (plugin instanceof MenuProvider) {
                    MenuProvider menuProvider = (MenuProvider) plugin;
                    serviceRegistry.register(MenuProvider.class, menuProvider);
                    if (log.isDebugEnabled()) {
                        log.debug("MenuProvider registered for plugin: {}", pluginName);
                    }
                }

                eventBus.publish(new PluginMenuAddedEvent(pluginName, plugin.getClass().getSimpleName()));
                if (log.isDebugEnabled()) {
                    log.debug("Plugin {} v{} enabled successfully", pluginName, plugin.getVersion());
                }
                eventBus.publish(new PluginEnabledEvent(plugin.getClass().getSimpleName()));

            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error("Error enabling plugin {}: {}", pluginName, e.getMessage(), e);
                }
                pluginEnabledStates.put(pluginName, false);
                throw new RuntimeException("Failed to enable plugin: " + pluginName, e);
            }
        } else {
            if (log.isWarnEnabled()) {
                log.warn("Plugin not found: {}", pluginName);
            }
            throw new IllegalArgumentException("Plugin not found: " + pluginName);
        }
    }

    @Override
    public void enablePluginByName(String pluginName) {
        Plugin plugin = findPluginByName(pluginName);
        if (plugin != null) {
            if (plugin.getState() != PluginStatus.ENABLED) {
                enablePlugin(plugin.getName());
                if (log.isDebugEnabled()) {
                    log.debug("Plugin {} enabled successfully.", pluginName);
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Plugin {} is already enabled.", pluginName);
                }
            }
        } else {
            if (log.isWarnEnabled()) {
                log.warn("Plugin with name '{}' not found.", pluginName);
            }
        }
    }

    private Plugin findPluginByName(String pluginName) {
        for (Plugin plugin : plugins.values()) {
            if (plugin.getName().equals(pluginName)) {
                return plugin;
            }
        }
        return null;
    }

    private String findPluginClassName(JarFile jarFile) throws Exception {
        JarEntry entry = jarFile.getJarEntry(PLUGIN_PROPERTIES);
        if (entry != null) {
            try (InputStream is = jarFile.getInputStream(entry)) {
                Properties properties = new Properties();
                properties.load(is);
                String className = properties.getProperty(PLUGIN_CLASS_ATTRIBUTE);
                if (className != null && !className.isBlank()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Found plugin class in properties file {}: {}", PLUGIN_PROPERTIES, className);
                    }
                    return className;
                } else {
                    if (log.isErrorEnabled()) {
                        log.error("Property {} in {} is missing or empty.", PLUGIN_CLASS_ATTRIBUTE, PLUGIN_PROPERTIES);
                    }
                }
            } catch (IOException e) {
                if (log.isErrorEnabled()) {
                    log.error("Failed to read {} from JAR: {}", PLUGIN_PROPERTIES, e.getMessage(), e);
                }
            }
        }

        String mainClass = jarFile.getManifest().getMainAttributes().getValue(MANIFEST_PLUGIN_CLASS);
        if (mainClass != null && !mainClass.isBlank()) {
            if (log.isDebugEnabled()) {
                log.debug("Found plugin class in manifest: {}", mainClass);
            }
            return mainClass;
        }

        if (log.isWarnEnabled()) {
            log.warn("No plugin class found in JAR: {} Checked both {} and manifest attribute {}.",
                    jarFile.getName(), PLUGIN_PROPERTIES, MANIFEST_PLUGIN_CLASS);
        }
        return null;
    }

    public void loadAndEnablePluginByName(String pluginsDir, String pluginName) {
        File dir = new File(pluginsDir);
        File[] jarFiles = dir.listFiles(file -> file.getName().endsWith(".jar"));

        if (jarFiles != null) {
            for (File jar : jarFiles) {
                try (JarFile jarFile = new JarFile(jar)) {
                    String pluginClassName = findPluginClassName(jarFile);

                    if (pluginClassName != null) {
                        URL[] urls = {jar.toURI().toURL()};
                        URLClassLoader loader = new URLClassLoader(urls, this.getClass().getClassLoader());
                        Class<?> clazz = loader.loadClass(pluginClassName);

                        if (Plugin.class.isAssignableFrom(clazz)) {
                            Plugin plugin = (Plugin) clazz.getDeclaredConstructor().newInstance();

                            // ← CRÉER LE CONTEXTE AVEC LE PLUGIN
                            PluginContext pluginContext = new DefaultPluginContext(
                                    serviceRegistry,
                                    new PluginEventBus(),
                                    eventBus,
                                    this,
                                    new File(context.getPluginDataPath(), plugin.getName()),
                                    plugin.getName()
                            );

                            plugin.initialize(pluginContext);

                            if (plugin.getName().equals(pluginName)) {
                                enablePlugin(plugin.getName());
                                plugins.put(plugin.getName(), plugin);
                                pluginClassLoaders.put(plugin.getName(), loader);
                                if (log.isDebugEnabled()) {
                                    log.debug("Successfully loaded and enabled plugin: {} v{}",
                                            plugin.getName(), plugin.getVersion());
                                }
                                return;
                            } else {
                                try {
                                    loader.close();
                                } catch (IOException e) {
                                    if (log.isWarnEnabled()) {
                                        log.warn("Error closing classloader for plugin {}", plugin.getName(), e);
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    if (log.isErrorEnabled()) {
                        log.error("Failed to load plugin from {}: {}", jar.getName(), e.getMessage(), e);
                    }
                }
            }
        }

        if (log.isWarnEnabled()) {
            log.warn("Plugin with name '{}' not found in directory '{}'.", pluginName, pluginsDir);
        }
    }

    @Override
    public PluginContext getPluginContext() {
        return context;
    }
}