package com.protonmail.landrevillejf.ide.plugin;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Manager interface for plugin lifecycle operations.
 * <p>
 * This interface defines the contract for managing plugins throughout their
 * lifecycle, including loading, enabling, disabling, and unloading.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
public interface PluginManager {
    /**
     * Loads a plugin from a JAR file.
     *
     * @param pluginFile the JAR file containing the plugin
     * @throws Exception if the plugin cannot be loaded
     */
    void loadPlugin(File pluginFile) throws Exception;

    /**
     * Unloads a plugin by its identifier.
     *
     * @param pluginId the unique identifier of the plugin to unload
     */
    void unloadPlugin(String pluginId);

    /**
     * Returns the plugin context for this manager.
     *
     * @return the plugin context
     */
    PluginContext getPluginContext();

    /**
     * Checks if a plugin is currently enabled.
     *
     * @param pluginId the unique identifier of the plugin
     * @return {@code true} if the plugin is enabled, {@code false} otherwise
     */
    boolean isPluginEnabled(String pluginId);

    /**
     * Enables a plugin by its identifier.
     *
     * @param pluginId the unique identifier of the plugin to enable
     */
    void enablePlugin(String pluginId);

    /**
     * Disables a plugin by its identifier.
     *
     * @param pluginId the unique identifier of the plugin to disable
     */
    void disablePlugin(String pluginId);

    /**
     * Returns the enabled/disabled state of all plugins.
     *
     * @return a map of plugin IDs to their enabled state
     */
    Map<String, Boolean> getAllPluginStates();

    /**
     * Returns a plugin by its identifier.
     *
     * @param pluginId the unique identifier of the plugin
     * @return the plugin, or {@code null} if not found
     */
    Plugin getPlugin(String pluginId);

    /**
     * Returns all loaded plugins.
     *
     * @return a list of all loaded plugins
     */
    List<Plugin> getLoadedPlugins();

    /**
     * Returns all currently enabled plugins.
     *
     * @return a list of all enabled plugins
     */
    List<Plugin> getEnabledPlugins();

    /**
     * Shuts down all plugins and cleans up resources.
     */
    void shutdownAll();

    /**
     * Loads all plugins from the configured plugins directory.
     */
    void loadAllPlugins();

    /**
     * Returns the status of a plugin.
     *
     * @param pluginId the unique identifier of the plugin
     * @return the current status of the plugin
     */
    PluginStatus getPluginStatus(String pluginId);

    /**
     * Disables all currently enabled plugins.
     */
    void disableAllPlugins();

    /**
     * Enables a plugin by its name.
     *
     * @param pluginName the name of the plugin to enable
     */
    void enablePluginByName(String pluginName);
}