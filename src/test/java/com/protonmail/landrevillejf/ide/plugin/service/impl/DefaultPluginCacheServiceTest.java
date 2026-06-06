package com.protonmail.landrevillejf.ide.plugin.service.impl;

import com.protonmail.landrevillejf.ide.plugin.service.PluginCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.TimeUnit;

class DefaultPluginCacheServiceTest {

    private DefaultPluginCacheService cacheService;
    private static final String TEST_PLUGIN = "test-plugin";
    private static final String TEST_KEY = "test-key";
    private static final String TEST_VALUE = "test-value";

    @BeforeEach
    void setUp() {
        cacheService = new DefaultPluginCacheService();
    }

    @Test
    void put() {
        cacheService.put(TEST_PLUGIN, TEST_KEY, TEST_VALUE);

        assertTrue(cacheService.containsKey(TEST_PLUGIN, TEST_KEY));
        assertEquals(TEST_VALUE, cacheService.get(TEST_PLUGIN, TEST_KEY));
    }

    @Test
    void testPut() {
        // Test put with TTL
        cacheService.put(TEST_PLUGIN, TEST_KEY, TEST_VALUE, 100); // 100ms TTL

        assertTrue(cacheService.containsKey(TEST_PLUGIN, TEST_KEY));
        assertEquals(TEST_VALUE, cacheService.get(TEST_PLUGIN, TEST_KEY));

        // Wait for TTL to expire
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        assertFalse(cacheService.containsKey(TEST_PLUGIN, TEST_KEY));
    }

    @Test
    void testPutWithDifferentPlugins() {
        String plugin1 = "plugin1";
        String plugin2 = "plugin2";
        String key = "shared-key";

        cacheService.put(plugin1, key, "value1");
        cacheService.put(plugin2, key, "value2");

        assertEquals("value1", cacheService.get(plugin1, key));
        assertEquals("value2", cacheService.get(plugin2, key));
    }

    @Test
    void testPutOverwrite() {
        cacheService.put(TEST_PLUGIN, TEST_KEY, "old-value");
        cacheService.put(TEST_PLUGIN, TEST_KEY, "new-value");

        assertEquals("new-value", cacheService.get(TEST_PLUGIN, TEST_KEY));
    }

    @Test
    void get() {
        // Test get non-existent key
        assertNull(cacheService.get(TEST_PLUGIN, "non-existent"));

        // Test get existing key
        cacheService.put(TEST_PLUGIN, TEST_KEY, TEST_VALUE);
        assertEquals(TEST_VALUE, cacheService.get(TEST_PLUGIN, TEST_KEY));
    }

    @Test
    void testGet() {
        // Test typed get with matching type
        cacheService.put(TEST_PLUGIN, TEST_KEY, "string-value");
        String value = cacheService.get(TEST_PLUGIN, TEST_KEY, String.class);
        assertEquals("string-value", value);

        // Test typed get with non-matching type
        Integer intValue = cacheService.get(TEST_PLUGIN, TEST_KEY, Integer.class);
        assertNull(intValue);

        // Test typed get with complex object
        Map<String, String> map = Map.of("key", "value");
        cacheService.put(TEST_PLUGIN, "map-key", map);
        Map<String, String> retrieved = cacheService.get(TEST_PLUGIN, "map-key", Map.class);
        assertEquals(map, retrieved);
    }

    @Test
    void containsKey() {
        assertFalse(cacheService.containsKey(TEST_PLUGIN, TEST_KEY));

        cacheService.put(TEST_PLUGIN, TEST_KEY, TEST_VALUE);
        assertTrue(cacheService.containsKey(TEST_PLUGIN, TEST_KEY));
    }

    @Test
    void remove() {
        cacheService.put(TEST_PLUGIN, TEST_KEY, TEST_VALUE);
        assertTrue(cacheService.containsKey(TEST_PLUGIN, TEST_KEY));

        cacheService.remove(TEST_PLUGIN, TEST_KEY);
        assertFalse(cacheService.containsKey(TEST_PLUGIN, TEST_KEY));
    }

    @Test
    void clear() {
        cacheService.put(TEST_PLUGIN, "key1", "value1");
        cacheService.put(TEST_PLUGIN, "key2", "value2");
        cacheService.put(TEST_PLUGIN, "key3", "value3");

        assertEquals(3, cacheService.size(TEST_PLUGIN));

        cacheService.clear(TEST_PLUGIN);
        assertEquals(0, cacheService.size(TEST_PLUGIN));
        assertNull(cacheService.get(TEST_PLUGIN, "key1"));
    }

    @Test
    void clearAll() {
        cacheService.put("plugin1", "key1", "value1");
        cacheService.put("plugin2", "key2", "value2");
        cacheService.put("plugin3", "key3", "value3");

        cacheService.clearAll();

        assertEquals(0, cacheService.size("plugin1"));
        assertEquals(0, cacheService.size("plugin2"));
        assertEquals(0, cacheService.size("plugin3"));
    }

    @Test
    void size() {
        assertEquals(0, cacheService.size(TEST_PLUGIN));

        cacheService.put(TEST_PLUGIN, "key1", "value1");
        assertEquals(1, cacheService.size(TEST_PLUGIN));

        cacheService.put(TEST_PLUGIN, "key2", "value2");
        assertEquals(2, cacheService.size(TEST_PLUGIN));

        cacheService.remove(TEST_PLUGIN, "key1");
        assertEquals(1, cacheService.size(TEST_PLUGIN));
    }

    @Test
    void setEvictionPolicy() {
        cacheService.setEvictionPolicy(TEST_PLUGIN, PluginCacheService.EvictionPolicy.LRU);
        cacheService.put(TEST_PLUGIN, "key1", "value1");
        cacheService.put(TEST_PLUGIN, "key2", "value2");
        cacheService.put(TEST_PLUGIN, "key3", "value3");

        // Access key1 to make it most recent in LRU
        cacheService.get(TEST_PLUGIN, "key1");

        // Verify policy was set (indirectly by checking statistics)
        Map<String, Object> stats = cacheService.getStatistics(TEST_PLUGIN);
        assertEquals("LRU", stats.get("evictionPolicy"));
    }

    @Test
    void setMaxSize() {
        cacheService.setMaxSize(TEST_PLUGIN, 2);

        cacheService.put(TEST_PLUGIN, "key1", "value1");
        cacheService.put(TEST_PLUGIN, "key2", "value2");
        cacheService.put(TEST_PLUGIN, "key3", "value3");

        // With max size 2, only 2 entries should remain
        assertEquals(2, cacheService.size(TEST_PLUGIN));

        // The oldest entry (key1) should be evicted
        assertFalse(cacheService.containsKey(TEST_PLUGIN, "key1"));
    }

    @Test
    void getStatistics() {
        cacheService.put(TEST_PLUGIN, "key1", "value1");
        cacheService.get(TEST_PLUGIN, "key1"); // hit
        cacheService.get(TEST_PLUGIN, "key1"); // hit
        cacheService.get(TEST_PLUGIN, "non-existent"); // miss
        cacheService.get(TEST_PLUGIN, "non-existent2"); // miss

        Map<String, Object> stats = cacheService.getStatistics(TEST_PLUGIN);

        assertNotNull(stats);
        assertTrue(stats.containsKey("size"));
        assertTrue(stats.containsKey("maxSize"));
        assertTrue(stats.containsKey("hits"));
        assertTrue(stats.containsKey("misses"));
        assertTrue(stats.containsKey("hitRatio"));
        assertTrue(stats.containsKey("evictionPolicy"));

        assertEquals(1, stats.get("size"));
        assertEquals(1000, stats.get("maxSize")); // default max size
        assertEquals(2L, stats.get("hits"));
        assertEquals(2L, stats.get("misses"));
    }

    @Test
    void resetStatistics() {
        cacheService.put(TEST_PLUGIN, "key1", "value1");
        cacheService.get(TEST_PLUGIN, "key1");
        cacheService.get(TEST_PLUGIN, "key1");

        Map<String, Object> beforeReset = cacheService.getStatistics(TEST_PLUGIN);
        assertEquals(2L, beforeReset.get("hits"));

        cacheService.resetStatistics(TEST_PLUGIN);

        Map<String, Object> afterReset = cacheService.getStatistics(TEST_PLUGIN);
        assertEquals(0L, afterReset.get("hits"));
        assertEquals(0L, afterReset.get("misses"));
    }

    @Test
    void getKeys() {
        cacheService.put(TEST_PLUGIN, "key1", "value1");
        cacheService.put(TEST_PLUGIN, "key2", "value2");
        cacheService.put(TEST_PLUGIN, "key3", "value3");

        List<String> keys = cacheService.getKeys(TEST_PLUGIN);

        assertEquals(3, keys.size());
        assertTrue(keys.contains("key1"));
        assertTrue(keys.contains("key2"));
        assertTrue(keys.contains("key3"));
    }

    @Test
    void testLRUEviction() {
        cacheService.setEvictionPolicy(TEST_PLUGIN, PluginCacheService.EvictionPolicy.LRU);
        cacheService.setMaxSize(TEST_PLUGIN, 2);

        cacheService.put(TEST_PLUGIN, "key1", "value1");
        cacheService.put(TEST_PLUGIN, "key2", "value2");

        // Access key1 to make it most recent
        cacheService.get(TEST_PLUGIN, "key1");

        // Add key3, should evict key2 (least recently used)
        cacheService.put(TEST_PLUGIN, "key3", "value3");

        assertTrue(cacheService.containsKey(TEST_PLUGIN, "key1"));
        assertFalse(cacheService.containsKey(TEST_PLUGIN, "key2"));
        assertTrue(cacheService.containsKey(TEST_PLUGIN, "key3"));
    }

    @Test
    void testTTLExpiration() throws InterruptedException {
        cacheService.put(TEST_PLUGIN, "short-lived", "temp", 50);
        cacheService.put(TEST_PLUGIN, "long-lived", "persistent", 500);

        assertTrue(cacheService.containsKey(TEST_PLUGIN, "short-lived"));

        Thread.sleep(100);

        // Short-lived should expire
        assertFalse(cacheService.containsKey(TEST_PLUGIN, "short-lived"));
        assertTrue(cacheService.containsKey(TEST_PLUGIN, "long-lived"));
    }

    @Test
    void testNoTTLExpiration() {
        cacheService.put(TEST_PLUGIN, "key", "value", -1);

        // Should never expire
        assertTrue(cacheService.containsKey(TEST_PLUGIN, "key"));
    }

    @Test
    void testCacheIsolation() {
        String pluginA = "pluginA";
        String pluginB = "pluginB";

        cacheService.put(pluginA, "key", "valueA");
        cacheService.put(pluginB, "key", "valueB");

        assertEquals("valueA", cacheService.get(pluginA, "key"));
        assertEquals("valueB", cacheService.get(pluginB, "key"));

        cacheService.clear(pluginA);

        assertNull(cacheService.get(pluginA, "key"));
        assertEquals("valueB", cacheService.get(pluginB, "key"));
    }

    @Test
    void testNullValues() {
        cacheService.put(TEST_PLUGIN, "key", null);

        assertTrue(cacheService.containsKey(TEST_PLUGIN, "key"));
        assertNull(cacheService.get(TEST_PLUGIN, "key"));
    }
}