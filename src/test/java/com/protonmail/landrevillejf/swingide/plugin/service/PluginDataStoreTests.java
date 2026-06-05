package com.protonmail.landrevillejf.swingide.plugin.service;

import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

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

    // Mock implementation
    public static class MockPluginDataStore implements PluginDataStore {
        private final Map<String, Map<String, Object>> storage = new java.util.HashMap<>();
        private final Map<String, Map<String, Object>> backups = new java.util.HashMap<>();

        @Override
        public void store(String pluginId, String key, Object data) {
            storage.computeIfAbsent(pluginId, k -> new java.util.HashMap<>()).put(key, data);
        }

        @Override
        public void store(String pluginId, String key, Object data, SerializationFormat format) {
            store(pluginId, key, data);
        }

        @Override
        public Object retrieve(String pluginId, String key) {
            return storage.getOrDefault(pluginId, new java.util.HashMap<>()).get(key);
        }

        @Override
        public <T> T retrieve(String pluginId, String key, Class<T> dataClass) {
            Object value = retrieve(pluginId, key);
            return dataClass.isInstance(value) ? (T) value : null;
        }

        @Override
        public boolean exists(String pluginId, String key) {
            return storage.getOrDefault(pluginId, new java.util.HashMap<>()).containsKey(key);
        }

        @Override
        public boolean delete(String pluginId, String key) {
            Map<String, Object> data = storage.get(pluginId);
            return data != null && data.remove(key) != null;
        }

        @Override
        public void clear(String pluginId) {
            storage.remove(pluginId);
        }

        @Override
        public List<String> getKeys(String pluginId) {
            return new java.util.ArrayList<>(storage.getOrDefault(pluginId, new java.util.HashMap<>()).keySet());
        }

        @Override
        public long getSize(String pluginId, String key) { return 0; }

        @Override
        public long getTotalSize(String pluginId) { return 0; }

        @Override
        public Map<String, Object> exportAllData(String pluginId) {
            return new java.util.HashMap<>(storage.getOrDefault(pluginId, new java.util.HashMap<>()));
        }

        @Override
        public void importAllData(String pluginId, Map<String, Object> data) {
            storage.put(pluginId, new java.util.HashMap<>(data));
        }

        @Override
        public String backup(String pluginId) {
            String backupId = "backup-" + System.currentTimeMillis();
            backups.put(backupId, new java.util.HashMap<>(storage.getOrDefault(pluginId, new java.util.HashMap<>())));
            return backupId;
        }

        @Override
        public boolean restore(String pluginId, String backupId) {
            if (backups.containsKey(backupId)) {
                storage.put(pluginId, new java.util.HashMap<>(backups.get(backupId)));
                return true;
            }
            return false;
        }

        @Override
        public List<Map<String, Object>> getBackups(String pluginId) { return java.util.Collections.emptyList(); }

        @Override
        public boolean deleteBackup(String pluginId, String backupId) { return true; }

        @Override
        public Map<String, Object> getStatistics(String pluginId) { return new java.util.HashMap<>(); }
    }
}

