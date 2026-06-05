package com.protonmail.landrevillejf.ide.plugin.service;

import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * Tests for PluginCacheService interface
 */
@DisplayName("PluginCacheService Tests")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class PluginCacheServiceTests {

    private PluginCacheService cacheService;
    private static final String PLUGIN_ID = "test-plugin";
    private static final String CACHE_KEY = "test-key";
    private static final String CACHE_VALUE = "test-value";

    @BeforeEach
    void setUp() {
        cacheService = new MockPluginCacheService();
    }

    @Test
    @DisplayName("should put and get value from cache")
    void test_put_and_get_value() {
        cacheService.put(PLUGIN_ID, CACHE_KEY, CACHE_VALUE);

        Object cached = cacheService.get(PLUGIN_ID, CACHE_KEY);

        assertThat(cached).isEqualTo(CACHE_VALUE);
    }

    @Test
    @DisplayName("should return null for non-existent key")
    void test_get_non_existent_key() {
        Object cached = cacheService.get(PLUGIN_ID, "non-existent");

        assertThat(cached).isNull();
    }

    @Test
    @DisplayName("should put value with TTL")
    void test_put_with_ttl() {
        assertThatNoException().isThrownBy(() ->
            cacheService.put(PLUGIN_ID, CACHE_KEY, CACHE_VALUE, 3600000)
        );
    }

    @Test
    @DisplayName("should check if key exists")
    void test_contains_key() {
        cacheService.put(PLUGIN_ID, CACHE_KEY, CACHE_VALUE);

        boolean exists = cacheService.containsKey(PLUGIN_ID, CACHE_KEY);

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("should remove value from cache")
    void test_remove_value() {
        cacheService.put(PLUGIN_ID, CACHE_KEY, CACHE_VALUE);
        cacheService.remove(PLUGIN_ID, CACHE_KEY);

        Object cached = cacheService.get(PLUGIN_ID, CACHE_KEY);

        assertThat(cached).isNull();
    }

    @Test
    @DisplayName("should clear all cache entries for plugin")
    void test_clear_cache() {
        cacheService.put(PLUGIN_ID, CACHE_KEY, CACHE_VALUE);
        cacheService.clear(PLUGIN_ID);

        int size = cacheService.size(PLUGIN_ID);

        assertThat(size).isZero();
    }

    @Test
    @DisplayName("should get cache size")
    void test_get_cache_size() {
        cacheService.put(PLUGIN_ID, "key1", "value1");
        cacheService.put(PLUGIN_ID, "key2", "value2");

        int size = cacheService.size(PLUGIN_ID);

        assertThat(size).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("should set eviction policy")
    void test_set_eviction_policy() {
        assertThatNoException().isThrownBy(() ->
            cacheService.setEvictionPolicy(PLUGIN_ID, PluginCacheService.EvictionPolicy.LRU)
        );
    }

    @Test
    @DisplayName("should set max cache size")
    void test_set_max_size() {
        assertThatNoException().isThrownBy(() ->
            cacheService.setMaxSize(PLUGIN_ID, 100)
        );
    }

    @Test
    @DisplayName("should get cache statistics")
    void test_get_statistics() {
        cacheService.put(PLUGIN_ID, CACHE_KEY, CACHE_VALUE);

        Map<String, Object> stats = cacheService.getStatistics(PLUGIN_ID);

        assertThat(stats).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("should get cache keys")
    void test_get_keys() {
        cacheService.put(PLUGIN_ID, "key1", "value1");
        cacheService.put(PLUGIN_ID, "key2", "value2");

        List<String> keys = cacheService.getKeys(PLUGIN_ID);

        assertThat(keys).isNotNull().isNotEmpty();
    }

    // Mock implementation for testing
    public static class MockPluginCacheService implements PluginCacheService {
        private final Map<String, Map<String, Object>> cache = new java.util.HashMap<>();

        @Override
        public void put(String pluginId, String key, Object value) {
            cache.computeIfAbsent(pluginId, k -> new java.util.HashMap<>()).put(key, value);
        }

        @Override
        public void put(String pluginId, String key, Object value, long ttlMillis) {
            put(pluginId, key, value);
        }

        @Override
        public Object get(String pluginId, String key) {
            return cache.getOrDefault(pluginId, new java.util.HashMap<>()).get(key);
        }

        @Override
        public <T> T get(String pluginId, String key, Class<T> valueClass) {
            Object value = get(pluginId, key);
            return valueClass.isInstance(value) ? (T) value : null;
        }

        @Override
        public boolean containsKey(String pluginId, String key) {
            return cache.getOrDefault(pluginId, new java.util.HashMap<>()).containsKey(key);
        }

        @Override
        public void remove(String pluginId, String key) {
            cache.getOrDefault(pluginId, new java.util.HashMap<>()).remove(key);
        }

        @Override
        public void clear(String pluginId) {
            cache.remove(pluginId);
        }

        @Override
        public void clearAll() {
            cache.clear();
        }

        @Override
        public int size(String pluginId) {
            return cache.getOrDefault(pluginId, new java.util.HashMap<>()).size();
        }

        @Override
        public void setEvictionPolicy(String pluginId, EvictionPolicy policy) {}

        @Override
        public void setMaxSize(String pluginId, int maxSize) {}

        @Override
        public Map<String, Object> getStatistics(String pluginId) {
            Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("size", size(pluginId));
            stats.put("evictions", 0);
            return stats;
        }

        @Override
        public void resetStatistics(String pluginId) {}

        @Override
        public List<String> getKeys(String pluginId) {
            return new java.util.ArrayList<>(
                cache.getOrDefault(pluginId, new java.util.HashMap<>()).keySet()
            );
        }
    }
}

