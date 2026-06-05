package com.protonmail.landrevillejf.ide.plugin.service;

import java.util.List;
import java.util.Map;

/**
 * Caching service for plugins with support for TTL, eviction policies, and statistics.
 */
public interface PluginCacheService {

    /**
     * Eviction policy for cache entries.
     */
    enum EvictionPolicy {
        /** Least Recently Used */
        LRU,
        /** First In First Out */
        FIFO,
        /** Least Frequently Used */
        LFU
    }

    /**
     * Puts a value in the cache.
     *
     * @param pluginId the plugin identifier
     * @param key the cache key
     * @param value the value to cache
     */
    void put(String pluginId, String key, Object value);

    /**
     * Puts a value in the cache with a TTL.
     *
     * @param pluginId the plugin identifier
     * @param key the cache key
     * @param value the value to cache
     * @param ttlMillis the time to live in milliseconds
     */
    void put(String pluginId, String key, Object value, long ttlMillis);

    /**
     * Gets a value from the cache.
     *
     * @param pluginId the plugin identifier
     * @param key the cache key
     * @return the cached value, or null if not found
     */
    Object get(String pluginId, String key);

    /**
     * Gets a value from the cache with type casting.
     *
     * @param pluginId the plugin identifier
     * @param key the cache key
     * @param valueClass the expected value class
     * @return the cached value, or null if not found
     */
    <T> T get(String pluginId, String key, Class<T> valueClass);

    /**
     * Checks if a key exists in the cache.
     *
     * @param pluginId the plugin identifier
     * @param key the cache key
     * @return true if the key exists and is not expired
     */
    boolean containsKey(String pluginId, String key);

    /**
     * Removes a value from the cache.
     *
     * @param pluginId the plugin identifier
     * @param key the cache key
     */
    void remove(String pluginId, String key);

    /**
     * Clears all cache entries for a plugin.
     *
     * @param pluginId the plugin identifier
     */
    void clear(String pluginId);

    /**
     * Clears all cache entries globally.
     */
    void clearAll();

    /**
     * Gets the number of cached entries for a plugin.
     *
     * @param pluginId the plugin identifier
     * @return the number of cache entries
     */
    int size(String pluginId);

    /**
     * Sets the eviction policy for a plugin's cache.
     *
     * @param pluginId the plugin identifier
     * @param policy the eviction policy
     */
    void setEvictionPolicy(String pluginId, EvictionPolicy policy);

    /**
     * Sets the maximum cache size for a plugin.
     *
     * @param pluginId the plugin identifier
     * @param maxSize the maximum number of entries
     */
    void setMaxSize(String pluginId, int maxSize);

    /**
     * Gets cache statistics for a plugin.
     *
     * @param pluginId the plugin identifier
     * @return a map containing cache statistics (hits, misses, evictions, etc.)
     */
    Map<String, Object> getStatistics(String pluginId);

    /**
     * Resets cache statistics for a plugin.
     *
     * @param pluginId the plugin identifier
     */
    void resetStatistics(String pluginId);

    /**
     * Gets all keys in the cache for a plugin.
     *
     * @param pluginId the plugin identifier
     * @return list of cache keys
     */
    List<String> getKeys(String pluginId);
}

