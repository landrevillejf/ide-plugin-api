package com.protonmail.landrevillejf.ide.plugin.service;

import java.util.List;
import java.util.Map;

/**
 * Plugin update and versioning management service.
 * <p>
 * Provides update checking, installation, rollback, and version history management
 * with support for multiple update channels (stable, beta, development).
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
public interface PluginUpdateService {

    /**
     * Update channel type.
     */
    enum UpdateChannel {
        STABLE,
        BETA,
        DEVELOPMENT
    }

    /**
     * Update status.
     */
    enum UpdateStatus {
        AVAILABLE,
        CHECKING,
        INSTALLING,
        INSTALLED,
        FAILED
    }

    /**
     * Represents a plugin version.
     */
    interface PluginVersion {
        String getVersion();
        String getDescription();
        String getReleaseDate();
        List<String> getChangelog();
        List<String> getNewFeatures();
        List<String> getBugFixes();
        Map<String, Object> getMetadata();
    }

    /**
     * Checks for available updates for a plugin.
     *
     * @param pluginId the plugin identifier
     * @return the latest available version, or null if none available
     */
    PluginVersion checkForUpdates(String pluginId);

    /**
     * Checks for available updates for a plugin on a specific channel.
     *
     * @param pluginId the plugin identifier
     * @param channel the update channel
     * @return the latest available version, or null if none available
     */
    PluginVersion checkForUpdates(String pluginId, UpdateChannel channel);

    /**
     * Gets the update status for a plugin.
     *
     * @param pluginId the plugin identifier
     * @return the update status
     */
    UpdateStatus getUpdateStatus(String pluginId);

    /**
     * Installs an update for a plugin.
     *
     * @param pluginId the plugin identifier
     * @param version the version to install
     * @return true if the update started successfully
     */
    boolean installUpdate(String pluginId, String version);

    /**
     * Cancels an ongoing update.
     *
     * @param pluginId the plugin identifier
     * @return true if the cancellation was successful
     */
    boolean cancelUpdate(String pluginId);

    /**
     * Gets the update progress for a plugin.
     *
     * @param pluginId the plugin identifier
     * @return the progress percentage (0-100)
     */
    int getUpdateProgress(String pluginId);

    /**
     * Rollbacks a plugin to a previous version.
     *
     * @param pluginId the plugin identifier
     * @param version the version to rollback to
     * @return true if rollback was successful
     */
    boolean rollbackVersion(String pluginId, String version);

    /**
     * Gets version history for a plugin.
     *
     * @param pluginId the plugin identifier
     * @return list of previous versions
     */
    List<PluginVersion> getVersionHistory(String pluginId);

    /**
     * Sets the update channel for a plugin.
     *
     * @param pluginId the plugin identifier
     * @param channel the update channel
     */
    void setUpdateChannel(String pluginId, UpdateChannel channel);

    /**
     * Gets the update channel for a plugin.
     *
     * @param pluginId the plugin identifier
     * @return the update channel
     */
    UpdateChannel getUpdateChannel(String pluginId);

    /**
     * Enables or disables auto-updates for a plugin.
     *
     * @param pluginId the plugin identifier
     * @param enabled true to enable auto-updates
     */
    void setAutoUpdate(String pluginId, boolean enabled);

    /**
     * Gets auto-update status for a plugin.
     *
     * @param pluginId the plugin identifier
     * @return true if auto-update is enabled
     */
    boolean isAutoUpdateEnabled(String pluginId);

    /**
     * Gets update statistics.
     *
     * @return a map containing update statistics
     */
    Map<String, Object> getUpdateStatistics();
}

