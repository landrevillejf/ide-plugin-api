package com.protonmail.landrevillejf.ide.plugin.service;

import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * Tests for PluginDataStore interface
 */
@DisplayName("PluginDataStore Tests")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class PluginDataStoreTests {

    private PluginDataStore dataStore;
    private static final String PLUGIN_ID = "test-plugin";

    @BeforeEach
    void setUp() {
        dataStore = new MockPluginDataStore();
    }

    @Test
    @DisplayName("should store and retrieve data")
    void test_store_and_retrieve_data() {
        String testData = "test-value";

        dataStore.store(PLUGIN_ID, "key", testData);
        Object retrieved = dataStore.retrieve(PLUGIN_ID, "key");

        assertThat(retrieved).isEqualTo(testData);
    }

    @Test
    @DisplayName("should store with specific format")
    void test_store_with_format() {
        assertThatNoException().isThrownBy(() ->
            dataStore.store(PLUGIN_ID, "key", "data", PluginDataStore.SerializationFormat.JSON)
        );
    }

    @Test
    @DisplayName("should check if key exists")
    void test_exists() {
        dataStore.store(PLUGIN_ID, "key", "value");

        boolean exists = dataStore.exists(PLUGIN_ID, "key");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("should delete stored data")
    void test_delete() {
        dataStore.store(PLUGIN_ID, "key", "value");

        boolean deleted = dataStore.delete(PLUGIN_ID, "key");

        assertThat(deleted).isTrue();
        assertThat(dataStore.exists(PLUGIN_ID, "key")).isFalse();
    }

    @Test
    @DisplayName("should clear all data for plugin")
    void test_clear() {
        dataStore.store(PLUGIN_ID, "key1", "value1");
        dataStore.store(PLUGIN_ID, "key2", "value2");

        dataStore.clear(PLUGIN_ID);

        assertThat(dataStore.getKeys(PLUGIN_ID)).isEmpty();
    }

    @Test
    @DisplayName("should get all keys")
    void test_get_keys() {
        dataStore.store(PLUGIN_ID, "key1", "value1");
        dataStore.store(PLUGIN_ID, "key2", "value2");

        List<String> keys = dataStore.getKeys(PLUGIN_ID);

        assertThat(keys).isNotNull().hasSize(2);
    }

    @Test
    @DisplayName("should backup and restore data")
    void test_backup_and_restore() {
        dataStore.store(PLUGIN_ID, "key", "value");

        String backupId = dataStore.backup(PLUGIN_ID);
        dataStore.clear(PLUGIN_ID);

        boolean restored = dataStore.restore(PLUGIN_ID, backupId);

        assertThat(restored).isTrue();
        assertThat(dataStore.retrieve(PLUGIN_ID, "key")).isEqualTo("value");
    }

    // Dans PluginDataStoreTests.MockPluginDataStore, ajoutez ou modifiez :

    public static class MockPluginDataStore implements PluginDataStore {
        private final Map<String, Map<String, Object>> store = new HashMap<>();
        private final Map<String, Map<String, Map<String, Object>>> backups = new HashMap<>();
        private int backupCounter = 0;

        @Override
        public void store(String pluginId, String key, Object data) {
            store.computeIfAbsent(pluginId, k -> new HashMap<>()).put(key, data);
        }

        @Override
        public void store(String pluginId, String key, Object data, SerializationFormat format) {
            store(pluginId, key, data);
        }

        @Override
        public Object retrieve(String pluginId, String key) {
            Map<String, Object> pluginStore = store.get(pluginId);
            return pluginStore != null ? pluginStore.get(key) : null;
        }

        @Override
        public <T> T retrieve(String pluginId, String key, Class<T> dataClass) {
            Object data = retrieve(pluginId, key);
            return dataClass.isInstance(data) ? (T) data : null;
        }

        @Override
        public boolean exists(String pluginId, String key) {
            Map<String, Object> pluginStore = store.get(pluginId);
            return pluginStore != null && pluginStore.containsKey(key);
        }

        @Override
        public boolean delete(String pluginId, String key) {
            Map<String, Object> pluginStore = store.get(pluginId);
            if (pluginStore != null) {
                return pluginStore.remove(key) != null;
            }
            return false;
        }

        @Override
        public void clear(String pluginId) {
            Map<String, Object> pluginStore = store.get(pluginId);
            if (pluginStore != null) {
                pluginStore.clear();
            }
        }

        @Override
        public List<String> getKeys(String pluginId) {
            Map<String, Object> pluginStore = store.get(pluginId);
            return pluginStore != null ? new ArrayList<>(pluginStore.keySet()) : new ArrayList<>();
        }

        @Override
        public long getSize(String pluginId, String key) {
            return 0;
        }

        @Override
        public long getTotalSize(String pluginId) {
            return 0;
        }

        @Override
        public Map<String, Object> exportAllData(String pluginId) {
            Map<String, Object> pluginStore = store.get(pluginId);
            return pluginStore != null ? new HashMap<>(pluginStore) : new HashMap<>();
        }

        @Override
        public void importAllData(String pluginId, Map<String, Object> data) {
            store.put(pluginId, new HashMap<>(data));
        }

        @Override
        public String backup(String pluginId) {
            String backupId = pluginId + "_backup_" + System.currentTimeMillis() + "_" + (++backupCounter);
            Map<String, Object> backupData = exportAllData(pluginId);
            backups.computeIfAbsent(pluginId, k -> new HashMap<>()).put(backupId, backupData);
            return backupId;
        }

        @Override
        public boolean restore(String pluginId, String backupId) {
            Map<String, Map<String, Object>> pluginBackups = backups.get(pluginId);
            if (pluginBackups != null && pluginBackups.containsKey(backupId)) {
                Map<String, Object> backupData = pluginBackups.get(backupId);
                importAllData(pluginId, backupData);
                return true;
            }
            return false;
        }

        @Override
        public List<Map<String, Object>> getBackups(String pluginId) {
            Map<String, Map<String, Object>> pluginBackups = backups.get(pluginId);
            if (pluginBackups != null) {
                return pluginBackups.entrySet().stream()
                        .map(entry -> {
                            Map<String, Object> backupInfo = new HashMap<>();
                            backupInfo.put("id", entry.getKey());
                            backupInfo.put("size", entry.getValue().size());
                            return backupInfo;
                        })
                        .collect(Collectors.toList());
            }
            return new ArrayList<>();
        }

        @Override
        public boolean deleteBackup(String pluginId, String backupId) {
            Map<String, Map<String, Object>> pluginBackups = backups.get(pluginId);
            if (pluginBackups != null) {
                return pluginBackups.remove(backupId) != null;
            }
            return false;
        }

        @Override
        public Map<String, Object> getStatistics(String pluginId) {
            Map<String, Object> stats = new HashMap<>();
            Map<String, Object> pluginStore = store.get(pluginId);
            stats.put("storeSize", pluginStore != null ? pluginStore.size() : 0);
            stats.put("backupCount", backups.getOrDefault(pluginId, new HashMap<>()).size());
            return stats;
        }
    }
}

