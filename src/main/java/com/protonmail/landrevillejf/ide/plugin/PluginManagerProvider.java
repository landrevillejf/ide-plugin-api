package com.protonmail.landrevillejf.ide.plugin;

import com.protonmail.landrevillejf.swingide.core.bus.EventBus;
import com.protonmail.landrevillejf.swingide.core.registry.SimpleServiceRegistry;

import java.io.File;
import java.util.*;

public class PluginManagerProvider {
    private static PluginManager instance;

    public static PluginManager getInstance() {
        if (instance == null) {
            instance = createPluginManager();
        }
        return instance;
    }

    private static PluginManager createPluginManager() {
        // 1. Chercher via ServiceLoader
        ServiceLoader<PluginManager> loader = ServiceLoader.load(PluginManager.class);
        for (PluginManager pm : loader) {
            return pm; // Première implémentation trouvée
        }

        // 2. Si ServiceLoader trouve rien, créer un stub COMPLET avec un vrai contexte
        return createStubPluginManager();
    }

    private static PluginManager createStubPluginManager() {
        // Créer un vrai contexte pour le stub
        SimpleServiceRegistry serviceRegistry = new SimpleServiceRegistry();
        EventBus eventBus = new EventBus();
        PluginEventBus pluginEventBus = new PluginEventBus();
        File pluginDataDir = new File(System.getProperty("user.home"), ".swingide/plugins");

        PluginContext context = new DefaultPluginContext(
                serviceRegistry,
                pluginEventBus,
                eventBus,
                null,
                pluginDataDir,
                "stub-plugin-manager"
        );

        final PluginContext finalContext = context;

        return new PluginManager() {
            private final Map<String, Boolean> pluginStates = new HashMap<>();
            private final Map<String, Plugin> plugins = new HashMap<>();

            @Override
            public void loadPlugin(File pluginFile) throws Exception {
                System.out.println("Stub PluginManager: loadPlugin called but plugin module not available");
            }

            @Override
            public void unloadPlugin(String pluginId) {
                pluginStates.remove(pluginId);
                plugins.remove(pluginId);
            }

            @Override
            public PluginContext getPluginContext() {
                return finalContext;  // ← PLUS NULL !
            }

            @Override
            public boolean isPluginEnabled(String pluginId) {
                return pluginStates.getOrDefault(pluginId, false);
            }

            @Override
            public void enablePlugin(String pluginId) {
                pluginStates.put(pluginId, true);
                System.out.println("Stub PluginManager: Plugin " + pluginId + " marked as enabled");
            }

            @Override
            public void disablePlugin(String pluginId) {
                pluginStates.put(pluginId, false);
                System.out.println("Stub PluginManager: Plugin " + pluginId + " marked as disabled");
            }

            @Override
            public Map<String, Boolean> getAllPluginStates() {
                return new HashMap<>(pluginStates);
            }

            @Override
            public Plugin getPlugin(String pluginId) {
                return plugins.get(pluginId);
            }

            @Override
            public List<Plugin> getLoadedPlugins() {
                return new ArrayList<>(plugins.values());
            }

            @Override
            public List<Plugin> getEnabledPlugins() {
                List<Plugin> enabled = new ArrayList<>();
                for (Map.Entry<String, Plugin> entry : plugins.entrySet()) {
                    if (pluginStates.getOrDefault(entry.getKey(), false)) {
                        enabled.add(entry.getValue());
                    }
                }
                return enabled;
            }

            @Override
            public void shutdownAll() {
                pluginStates.clear();
                plugins.clear();
                System.out.println("Stub PluginManager: shutdownAll called");
            }

            @Override
            public void loadAllPlugins() {
                System.out.println("Stub PluginManager: loadAllPlugins called");
            }

            @Override
            public PluginStatus getPluginStatus(String pluginId) {
                if (!plugins.containsKey(pluginId)) {
                    return PluginStatus.UNLOADED;
                }
                return isPluginEnabled(pluginId) ? PluginStatus.ENABLED : PluginStatus.DISABLED;
            }

            @Override
            public void disableAllPlugins() {
                for (String pluginId : pluginStates.keySet()) {
                    pluginStates.put(pluginId, false);
                }
                System.out.println("Stub PluginManager: disableAllPlugins called");
            }

            @Override
            public void enablePluginByName(String pluginName) {
                enablePlugin(pluginName);
            }
        };
    }
}