package com.protonmail.landrevillejf.ide.plugin;

import java.io.File;
import java.util.List;
import java.util.Map;

public interface PluginManager {
    void loadPlugin(File pluginFile) throws Exception;
    void unloadPlugin(String pluginId);
    PluginContext getPluginContext();
    boolean isPluginEnabled(String pluginId);

    void enablePlugin(String pluginId);
    void disablePlugin(String pluginId);

    Map<String, Boolean> getAllPluginStates();

    Plugin getPlugin(String pluginId);
    List<Plugin> getLoadedPlugins();
    List<Plugin> getEnabledPlugins();
    void shutdownAll();
    void loadAllPlugins();

    PluginStatus getPluginStatus(String pluginId);

    void disableAllPlugins();

    void enablePluginByName(String pluginName);
}