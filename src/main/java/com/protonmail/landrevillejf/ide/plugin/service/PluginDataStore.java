package com.protonmail.landrevillejf.ide.plugin.service;

import java.util.List;
import java.util.Map;

/**
 * Plugin data persistence service for storing and retrieving plugin-specific data.
 * <p>
 * Supports multiple serialization formats, data backup/restore, and storage statistics.
 * Each plugin has its own isolated data namespace.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
public interface PluginDataStore {

    /**
     * Data serialization format.
     */
    enum SerializationFormat {
        JSON,
        XML,
        BINARY,
        PROPERTIES
    }

    /**
     * Stores data to persistent storage.
     *
     * @param pluginId the plugin identifier
     * @param key the data key
     * @param data the data to store
     */
    void store(String pluginId, String key, Object data);

    /**
     * Stores data with a specific format.
     *
     * @param pluginId the plugin identifier
     * @param key the data key
     * @param data the data to store
     * @param format the serialization format
     */
    void store(String pluginId, String key, Object data, SerializationFormat format);

    /**
     * Retrieves stored data.
     *
     * @param pluginId the plugin identifier
     * @param key the data key
     * @return the stored data, or null if not found
     */
    Object retrieve(String pluginId, String key);

    /**
     * Retrieves stored data with type casting.
     *
     * @param pluginId the plugin identifier
     * @param key the data key
     * @param dataClass the expected data class
     * @return the stored data, or null if not found
     */
    <T> T retrieve(String pluginId, String key, Class<T> dataClass);

    /**
     * Checks if data exists.
     *
     * @param pluginId the plugin identifier
     * @param key the data key
     * @return true if data exists
     */
    boolean exists(String pluginId, String key);

    /**
     * Deletes stored data.
     *
     * @param pluginId the plugin identifier
     * @param key the data key
     * @return true if data was deleted
     */
    boolean delete(String pluginId, String key);

    /**
     * Clears all data for a plugin.
     *
     * @param pluginId the plugin identifier
     */
    void clear(String pluginId);

    /**
     * Gets all data keys for a plugin.
     *
     * @param pluginId the plugin identifier
     * @return list of data keys
     */
    List<String> getKeys(String pluginId);

    /**
     * Gets the size of stored data for a key.
     *
     * @param pluginId the plugin identifier
     * @param key the data key
     * @return the size in bytes
     */
    long getSize(String pluginId, String key);

    /**
     * Gets total storage used by a plugin.
     *
     * @param pluginId the plugin identifier
     * @return the total size in bytes
     */
    long getTotalSize(String pluginId);

    /**
     * Exports all plugin data as a map.
     *
     * @param pluginId the plugin identifier
     * @return a map of all stored data
     */
    Map<String, Object> exportAllData(String pluginId);

    /**
     * Imports data from a map.
     *
     * @param pluginId the plugin identifier
     * @param data the data to import
     */
    void importAllData(String pluginId, Map<String, Object> data);

    /**
     * Backs up all data for a plugin.
     *
     * @param pluginId the plugin identifier
     * @return the backup identifier
     */
    String backup(String pluginId);

    /**
     * Restores data from a backup.
     *
     * @param pluginId the plugin identifier
     * @param backupId the backup identifier
     * @return true if restore was successful
     */
    boolean restore(String pluginId, String backupId);

    /**
     * Gets all available backups for a plugin.
     *
     * @param pluginId the plugin identifier
     * @return list of backup identifiers with metadata
     */
    List<Map<String, Object>> getBackups(String pluginId);

    /**
     * Deletes a backup.
     *
     * @param pluginId the plugin identifier
     * @param backupId the backup identifier
     * @return true if backup was deleted
     */
    boolean deleteBackup(String pluginId, String backupId);

    /**
     * Gets data store statistics.
     *
     * @param pluginId the plugin identifier
     * @return a map containing storage statistics
     */
    Map<String, Object> getStatistics(String pluginId);
}

