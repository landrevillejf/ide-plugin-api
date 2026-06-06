package com.protonmail.landrevillejf.ide.plugin.service.impl;

import com.protonmail.landrevillejf.ide.plugin.service.PluginDataStore;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DefaultPluginDataStoreTest {

    private DefaultPluginDataStore dataStore;
    private static final String TEST_PLUGIN = "test-plugin";
    private static final String TEST_KEY = "test-key";
    private static final String TEST_VALUE = "test-value";

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        dataStore = new DefaultPluginDataStore(tempDir);
    }

    @AfterEach
    void tearDown() {
        dataStore.clear(TEST_PLUGIN);
    }

    @Test
    void store() {
        dataStore.store(TEST_PLUGIN, TEST_KEY, TEST_VALUE);

        assertTrue(dataStore.exists(TEST_PLUGIN, TEST_KEY));
        assertEquals(TEST_VALUE, dataStore.retrieve(TEST_PLUGIN, TEST_KEY));
    }

    @Test
    void testStore() {
        Map<String, String> complexData = Map.of("key1", "value1", "key2", "value2");

        dataStore.store(TEST_PLUGIN, "complex-key", complexData, PluginDataStore.SerializationFormat.JSON);

        assertTrue(dataStore.exists(TEST_PLUGIN, "complex-key"));

        @SuppressWarnings("unchecked")
        Map<String, String> retrieved = (Map<String, String>) dataStore.retrieve(TEST_PLUGIN, "complex-key");
        assertEquals("value1", retrieved.get("key1"));
        assertEquals("value2", retrieved.get("key2"));
    }

    @Test
    void retrieve() {
        dataStore.store(TEST_PLUGIN, TEST_KEY, TEST_VALUE);

        Object retrieved = dataStore.retrieve(TEST_PLUGIN, TEST_KEY);

        assertEquals(TEST_VALUE, retrieved);
    }

    @Test
    void testRetrieve() {
        dataStore.store(TEST_PLUGIN, TEST_KEY, TEST_VALUE);

        String retrieved = dataStore.retrieve(TEST_PLUGIN, TEST_KEY, String.class);

        assertEquals(TEST_VALUE, retrieved);
    }

    @Test
    void testRetrieveWithWrongType() {
        dataStore.store(TEST_PLUGIN, TEST_KEY, TEST_VALUE);

        Integer retrieved = dataStore.retrieve(TEST_PLUGIN, TEST_KEY, Integer.class);

        assertNull(retrieved);
    }

    @Test
    void exists() {
        assertFalse(dataStore.exists(TEST_PLUGIN, TEST_KEY));

        dataStore.store(TEST_PLUGIN, TEST_KEY, TEST_VALUE);

        assertTrue(dataStore.exists(TEST_PLUGIN, TEST_KEY));
    }

    @Test
    void delete() {
        dataStore.store(TEST_PLUGIN, TEST_KEY, TEST_VALUE);
        assertTrue(dataStore.exists(TEST_PLUGIN, TEST_KEY));

        boolean deleted = dataStore.delete(TEST_PLUGIN, TEST_KEY);

        assertTrue(deleted);
        assertFalse(dataStore.exists(TEST_PLUGIN, TEST_KEY));
    }

    @Test
    void deleteNonExistent() {
        boolean deleted = dataStore.delete(TEST_PLUGIN, "non-existent");

        assertFalse(deleted);
    }

    @Test
    void clear() {
        dataStore.store(TEST_PLUGIN, "key1", "value1");
        dataStore.store(TEST_PLUGIN, "key2", "value2");
        dataStore.store(TEST_PLUGIN, "key3", "value3");

        assertEquals(3, dataStore.getKeys(TEST_PLUGIN).size());

        dataStore.clear(TEST_PLUGIN);

        assertEquals(0, dataStore.getKeys(TEST_PLUGIN).size());
        assertNull(dataStore.retrieve(TEST_PLUGIN, "key1"));
    }

    @Test
    void getKeys() {
        dataStore.store(TEST_PLUGIN, "key1", "value1");
        dataStore.store(TEST_PLUGIN, "key2", "value2");
        dataStore.store(TEST_PLUGIN, "key3", "value3");

        List<String> keys = dataStore.getKeys(TEST_PLUGIN);

        assertEquals(3, keys.size());
        assertTrue(keys.contains("key1"));
        assertTrue(keys.contains("key2"));
        assertTrue(keys.contains("key3"));
    }

    @Test
    void getSize() {
        dataStore.store(TEST_PLUGIN, TEST_KEY, TEST_VALUE);

        long size = dataStore.getSize(TEST_PLUGIN, TEST_KEY);

        assertTrue(size > 0);
    }

    @Test
    void getTotalSize() {
        dataStore.store(TEST_PLUGIN, "key1", "value1");
        dataStore.store(TEST_PLUGIN, "key2", "value2");

        long totalSize = dataStore.getTotalSize(TEST_PLUGIN);

        assertTrue(totalSize > 0);
    }

    @Test
    void exportAllData() {
        dataStore.store(TEST_PLUGIN, "key1", "value1");
        dataStore.store(TEST_PLUGIN, "key2", 123);
        dataStore.store(TEST_PLUGIN, "key3", true);

        Map<String, Object> exported = dataStore.exportAllData(TEST_PLUGIN);

        assertEquals(3, exported.size());
        assertEquals("value1", exported.get("key1"));
        assertEquals(123, exported.get("key2"));
        assertEquals(true, exported.get("key3"));
    }

    @Test
    void importAllData() {
        Map<String, Object> data = new HashMap<>();
        data.put("importedKey1", "importedValue1");
        data.put("importedKey2", 456);
        data.put("importedKey3", false);

        dataStore.importAllData(TEST_PLUGIN, data);

        assertEquals("importedValue1", dataStore.retrieve(TEST_PLUGIN, "importedKey1"));
        assertEquals(456, dataStore.retrieve(TEST_PLUGIN, "importedKey2"));
        assertEquals(false, dataStore.retrieve(TEST_PLUGIN, "importedKey3"));
    }

    @Test
    void backup() throws Exception {
        dataStore.store(TEST_PLUGIN, "key1", "value1");
        dataStore.store(TEST_PLUGIN, "key2", "value2");

        String backupId = dataStore.backup(TEST_PLUGIN);

        assertNotNull(backupId);
        assertTrue(backupId.contains(TEST_PLUGIN));

        List<Map<String, Object>> backups = dataStore.getBackups(TEST_PLUGIN);
        assertEquals(1, backups.size());
        assertEquals(backupId, backups.get(0).get("backupId"));
    }

    @Test
    void restore() throws Exception {
        dataStore.store(TEST_PLUGIN, "key1", "original value");

        String backupId = dataStore.backup(TEST_PLUGIN);

        // Modify data
        dataStore.store(TEST_PLUGIN, "key1", "modified value");
        assertEquals("modified value", dataStore.retrieve(TEST_PLUGIN, "key1"));

        // Restore
        boolean restored = dataStore.restore(TEST_PLUGIN, backupId);

        assertTrue(restored);
        assertEquals("original value", dataStore.retrieve(TEST_PLUGIN, "key1"));
    }

    @Test
    void restoreNonExistentBackup() {
        boolean restored = dataStore.restore(TEST_PLUGIN, "non-existent-backup");

        assertFalse(restored);
    }

    @Test
    void getBackups() {
        // S'assurer qu'il y a des données avant le backup
        dataStore.store(TEST_PLUGIN, "key1", "value1");

        String backupId1 = dataStore.backup(TEST_PLUGIN);
        assertNotNull(backupId1, "First backup should be created");

        // Attendre un peu pour éviter les timestamps identiques
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        dataStore.store(TEST_PLUGIN, "key2", "value2");

        String backupId2 = dataStore.backup(TEST_PLUGIN);
        assertNotNull(backupId2, "Second backup should be created");

        List<Map<String, Object>> backups = dataStore.getBackups(TEST_PLUGIN);

        // Vérifier qu'il y a exactement 2 backups
        assertEquals(2, backups.size());

        // Vérifier que les IDs sont présents
        List<String> backupIds = backups.stream()
                .map(b -> (String) b.get("backupId"))
                .toList();
        assertTrue(backupIds.contains(backupId1), "First backup ID should be in the list");
        assertTrue(backupIds.contains(backupId2), "Second backup ID should be in the list");

        // Vérifier que chaque backup a les bonnes propriétés
        for (Map<String, Object> backup : backups) {
            assertNotNull(backup.get("backupId"));
            assertNotNull(backup.get("timestamp"));
            assertNotNull(backup.get("size"));
            assertNotNull(backup.get("dataSize"));
        }
    }

    @Test
    void deleteBackup() {
        // D'abord, stocker des données pour que le backup ait du contenu
        dataStore.store(TEST_PLUGIN, "key1", "value1");
        dataStore.store(TEST_PLUGIN, "key2", "value2");

        String backupId = dataStore.backup(TEST_PLUGIN);
        assertNotNull(backupId, "Backup should be created successfully");

        // Attendre un peu pour que le fichier soit écrit
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        List<Map<String, Object>> backups = dataStore.getBackups(TEST_PLUGIN);
        assertEquals(1, backups.size());

        boolean deleted = dataStore.deleteBackup(TEST_PLUGIN, backupId);
        assertTrue(deleted);

        backups = dataStore.getBackups(TEST_PLUGIN);
        assertTrue(backups.isEmpty());
    }

    @Test
    void deleteBackupNonExistent() {
        boolean deleted = dataStore.deleteBackup(TEST_PLUGIN, "non-existent");

        assertFalse(deleted);
    }

    @Test
    void getStatistics() {
        dataStore.store(TEST_PLUGIN, "key1", "value1");
        dataStore.store(TEST_PLUGIN, "key2", "value2");

        Map<String, Object> stats = dataStore.getStatistics(TEST_PLUGIN);

        assertNotNull(stats);
        assertTrue(stats.containsKey("totalSize"));
        assertTrue(stats.containsKey("keyCount"));
        assertTrue(stats.containsKey("backupCount"));

        assertEquals(2, stats.get("keyCount"));
        assertEquals(0, stats.get("backupCount"));
    }

    @Test
    void testMultiplePluginsIsolation() {
        String plugin1 = "plugin1";
        String plugin2 = "plugin2";

        dataStore.store(plugin1, "shared-key", "value from plugin1");
        dataStore.store(plugin2, "shared-key", "value from plugin2");

        assertEquals("value from plugin1", dataStore.retrieve(plugin1, "shared-key"));
        assertEquals("value from plugin2", dataStore.retrieve(plugin2, "shared-key"));

        dataStore.clear(plugin1);

        assertNull(dataStore.retrieve(plugin1, "shared-key"));
        assertEquals("value from plugin2", dataStore.retrieve(plugin2, "shared-key"));
    }

    @Test
    void testBinarySerialization() throws Exception {
        CustomSerializableObject obj = new CustomSerializableObject("test", 42);

        dataStore.store(TEST_PLUGIN, "binary-obj", obj, PluginDataStore.SerializationFormat.BINARY);

        CustomSerializableObject retrieved = dataStore.retrieve(TEST_PLUGIN, "binary-obj", CustomSerializableObject.class);

        assertNotNull(retrieved);
        assertEquals("test", retrieved.getName());
        assertEquals(42, retrieved.getValue());
    }

    @Test
    void testPropertiesFormat() {
        Map<String, String> props = new HashMap<>();
        props.put("prop1", "value1");
        props.put("prop2", "value2");

        dataStore.store(TEST_PLUGIN, "props", props, PluginDataStore.SerializationFormat.PROPERTIES);

        Properties retrieved = (Properties) dataStore.retrieve(TEST_PLUGIN, "props");

        assertEquals("value1", retrieved.getProperty("prop1"));
        assertEquals("value2", retrieved.getProperty("prop2"));
    }

    @Test
    void testXmlFormat() throws Exception {
        Map<String, Object> xmlData = new HashMap<>();
        xmlData.put("name", "Test");
        xmlData.put("value", 123);

        dataStore.store(TEST_PLUGIN, "xml-data", xmlData, PluginDataStore.SerializationFormat.XML);

        @SuppressWarnings("unchecked")
        Map<String, Object> retrieved = (Map<String, Object>) dataStore.retrieve(TEST_PLUGIN, "xml-data");

        assertEquals("Test", retrieved.get("name"));
        // XML peut désérialiser les nombres comme String
        assertEquals("123", retrieved.get("value").toString());
    }

    @Test
    void testOverwriteExistingKey() {
        dataStore.store(TEST_PLUGIN, TEST_KEY, "first value");
        assertEquals("first value", dataStore.retrieve(TEST_PLUGIN, TEST_KEY));

        dataStore.store(TEST_PLUGIN, TEST_KEY, "second value");
        assertEquals("second value", dataStore.retrieve(TEST_PLUGIN, TEST_KEY));
    }

    // Helper class for binary serialization test
    private static class CustomSerializableObject implements Serializable {
        private final String name;
        private final int value;

        CustomSerializableObject(String name, int value) {
            this.name = name;
            this.value = value;
        }

        String getName() { return name; }
        int getValue() { return value; }
    }
}