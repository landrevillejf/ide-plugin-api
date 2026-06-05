package com.protonmail.landrevillejf.ide.plugin.service;

/**
 * Plugin lifecycle listener interface for monitoring plugin state changes.
 */
public interface PluginLifecycleListener {

    /**
     * Called when a plugin is about to be loaded.
     *
     * @param pluginId the plugin identifier
     */
    default void onBeforeLoad(String pluginId) {
    }

    /**
     * Called when a plugin is loaded.
     *
     * @param pluginId the plugin identifier
     */
    default void onLoaded(String pluginId) {
    }

    /**
     * Called when a plugin load fails.
     *
     * @param pluginId the plugin identifier
     * @param error the error that occurred
     */
    default void onLoadFailed(String pluginId, Throwable error) {
    }

    /**
     * Called when a plugin is about to be initialized.
     *
     * @param pluginId the plugin identifier
     */
    default void onBeforeInitialize(String pluginId) {
    }

    /**
     * Called when a plugin is initialized.
     *
     * @param pluginId the plugin identifier
     */
    default void onInitialized(String pluginId) {
    }

    /**
     * Called when a plugin initialization fails.
     *
     * @param pluginId the plugin identifier
     * @param error the error that occurred
     */
    default void onInitializationFailed(String pluginId, Throwable error) {
    }

    /**
     * Called when a plugin is about to be enabled.
     *
     * @param pluginId the plugin identifier
     */
    default void onBeforeEnable(String pluginId) {
    }

    /**
     * Called when a plugin is enabled.
     *
     * @param pluginId the plugin identifier
     */
    default void onEnabled(String pluginId) {
    }

    /**
     * Called when a plugin enable fails.
     *
     * @param pluginId the plugin identifier
     * @param error the error that occurred
     */
    default void onEnableFailed(String pluginId, Throwable error) {
    }

    /**
     * Called when a plugin is about to be disabled.
     *
     * @param pluginId the plugin identifier
     */
    default void onBeforeDisable(String pluginId) {
    }

    /**
     * Called when a plugin is disabled.
     *
     * @param pluginId the plugin identifier
     */
    default void onDisabled(String pluginId) {
    }

    /**
     * Called when a plugin disable fails.
     *
     * @param pluginId the plugin identifier
     * @param error the error that occurred
     */
    default void onDisableFailed(String pluginId, Throwable error) {
    }

    /**
     * Called when a plugin is about to be unloaded.
     *
     * @param pluginId the plugin identifier
     */
    default void onBeforeUnload(String pluginId) {
    }

    /**
     * Called when a plugin is unloaded.
     *
     * @param pluginId the plugin identifier
     */
    default void onUnloaded(String pluginId) {
    }

    /**
     * Called when a plugin is about to be upgraded.
     *
     * @param pluginId the plugin identifier
     * @param oldVersion the old version
     * @param newVersion the new version
     */
    default void onBeforeUpgrade(String pluginId, String oldVersion, String newVersion) {
    }

    /**
     * Called when a plugin is upgraded.
     *
     * @param pluginId the plugin identifier
     * @param oldVersion the old version
     * @param newVersion the new version
     */
    default void onUpgraded(String pluginId, String oldVersion, String newVersion) {
    }

    /**
     * Called when a plugin encounters an error.
     *
     * @param pluginId the plugin identifier
     * @param error the error that occurred
     */
    default void onPluginError(String pluginId, Throwable error) {
    }

    /**
     * Called when a plugin state changes.
     *
     * @param pluginId the plugin identifier
     * @param oldState the previous state
     * @param newState the new state
     */
    default void onStateChanged(String pluginId, String oldState, String newState) {
    }
}

