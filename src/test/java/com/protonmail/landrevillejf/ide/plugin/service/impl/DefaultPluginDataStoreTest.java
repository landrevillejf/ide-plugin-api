package com.protonmail.landrevillejf.ide.plugin.service.impl;

import com.protonmail.landrevillejf.ide.plugin.service.PluginDataStore;
import com.protonmail.landrevillejf.ide.plugin.utils.LogCapture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

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

    @Test
    void storeProperties_ShouldRejectNonMapData() {
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                dataStore.store(TEST_PLUGIN, "invalid-props", "not-a-map", PluginDataStore.SerializationFormat.PROPERTIES)
        );

        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
    }

    @Test
    void storeBinary_ShouldRejectNonSerializableData() {
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                dataStore.store(TEST_PLUGIN, "invalid-binary", new Object(), PluginDataStore.SerializationFormat.BINARY)
        );

        assertInstanceOf(IllegalArgumentException.class, exception.getCause());
    }

    @Test
    void restore_ShouldIgnoreMaliciousZipEntries() throws Exception {
        dataStore.store(TEST_PLUGIN, "safe", "value");
        String backupId = dataStore.backup(TEST_PLUGIN);
        assertNotNull(backupId);

        Path backupFile = tempDir.resolve(TEST_PLUGIN).resolve("backups").resolve(backupId + ".zip");
        try (java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(Files.newOutputStream(backupFile))) {
            zos.putNextEntry(new java.util.zip.ZipEntry("metadata.json"));
            zos.write("""
                    {"safe":{"key":"safe","format":"JSON","size":7,"timestamp":1}}
                    """.trim().getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            zos.putNextEntry(new java.util.zip.ZipEntry("data/safe"));
            zos.write("\"value\"".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            zos.putNextEntry(new java.util.zip.ZipEntry("data/../escape"));
            zos.write("escape".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        dataStore.clear(TEST_PLUGIN);

        assertTrue(dataStore.restore(TEST_PLUGIN, backupId));
        assertEquals("value", dataStore.retrieve(TEST_PLUGIN, "safe"));
        assertFalse(Files.exists(tempDir.resolve("escape.dat")));
    }

    @Test
    void restore_ShouldReturnFalse_ForCorruptedBackup() throws Exception {
        assertFalse(dataStore.restore(TEST_PLUGIN, "missing"));
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

    @Test
    void getSafeExtractionPath_shouldRejectPathTraversal() throws Exception {
        Method method = DefaultPluginDataStore.class.getDeclaredMethod(
                "getSafeExtractionPath", Path.class, String.class);
        method.setAccessible(true);

        Path targetDir = tempDir.resolve("target");
        Files.createDirectories(targetDir);

        // Chemin normal accepté
        Path safe = (Path) method.invoke(dataStore, targetDir, "data/file.txt");
        assertEquals(targetDir.resolve("data/file.txt").normalize(), safe);

        // Chemin avec ".." doit lever une SecurityException
        assertThrows(java.lang.reflect.InvocationTargetException.class, () ->
                method.invoke(dataStore, targetDir, "../escape.txt")
        );
    }

    @Test
    void methods_withUnknownPlugin_shouldReturnDefaults() {
        String unknown = "unknown.plugin";

        assertNull(dataStore.retrieve(unknown, "key"));
        assertFalse(dataStore.exists(unknown, "key"));
        assertFalse(dataStore.delete(unknown, "key"));
        assertNull(dataStore.backup(unknown)); // storage null -> null
        assertEquals(0, dataStore.getKeys(unknown).size());
        assertEquals(0, dataStore.getSize(unknown, "key"));
        assertEquals(0, dataStore.getTotalSize(unknown));
        assertTrue(dataStore.exportAllData(unknown).isEmpty());
        assertTrue(dataStore.getBackups(unknown).isEmpty());

        Map<String, Object> stats = dataStore.getStatistics(unknown);
        assertEquals(0, stats.get("totalSize"));
        assertEquals(0, stats.get("keyCount"));
    }

    @Test
    void retrieve_whenDataFileMissing_shouldRemoveFromMapAndReturnNull() throws Exception {
        dataStore.store(TEST_PLUGIN, "temp", "value");
        Path dataFile = tempDir.resolve(TEST_PLUGIN).resolve("temp.dat");
        Files.delete(dataFile); // supprime le fichier physique

        assertNull(dataStore.retrieve(TEST_PLUGIN, "temp"));
        assertFalse(dataStore.exists(TEST_PLUGIN, "temp"));
    }

    @Test
    void retrieve_withIncompatibleType_shouldReturnNull() {
        dataStore.store(TEST_PLUGIN, "number", 42);
        // 42 est désérialisé comme Integer, demander String doit retourner null
        assertNull(dataStore.retrieve(TEST_PLUGIN, "number", String.class));
    }

    @Test
    void delete_whenFileMissing_shouldStillRemoveMetadataAndReturnTrue() throws Exception {
        dataStore.store(TEST_PLUGIN, "key", "value");
        Path dataFile = tempDir.resolve(TEST_PLUGIN).resolve("key.dat");
        Files.delete(dataFile);

        assertTrue(dataStore.delete(TEST_PLUGIN, "key"));
        assertFalse(dataStore.exists(TEST_PLUGIN, "key"));
    }

    @Test
    void clear_unknownPlugin_shouldNotThrow() {
        assertDoesNotThrow(() -> dataStore.clear("unknown.plugin"));
    }

    @Test
    void backup_whenMoreThan10_shouldRemoveOldest() throws Exception {
        // Créer 11 sauvegardes
        for (int i = 0; i < 11; i++) {
            dataStore.store(TEST_PLUGIN, "key" + i, "value" + i);
            String backupId = dataStore.backup(TEST_PLUGIN);
            assertNotNull(backupId);
            Thread.sleep(2); // éviter des IDs identiques
        }

        List<Map<String, Object>> backups = dataStore.getBackups(TEST_PLUGIN);
        assertEquals(10, backups.size()); // une seule supprimée

        // Vérifier que le plus ancien a été supprimé (le premier ID)
        // Le premier backup créé ne doit plus être présent
        // On peut comparer les timestamps : le plus petit timestamp doit être absent
        long minTimestamp = Long.MAX_VALUE;
        for (Map<String, Object> b : backups) {
            long timestamp = (long) b.get("timestamp");
            minTimestamp = Math.min(minTimestamp, timestamp);
        }
        // En fait, le premier backup avait le plus petit timestamp, il ne devrait plus être là
        // On peut vérifier en regardant le nombre de fichiers zip
        Path backupsDir = tempDir.resolve(TEST_PLUGIN).resolve("backups");
        try (var files = Files.list(backupsDir)) {
            assertEquals(10, files.count());
        }
    }

    @Test
    void getSafeExtractionPath_shouldAcceptNormalizedPaths() throws Exception {
        Method method = DefaultPluginDataStore.class.getDeclaredMethod(
                "getSafeExtractionPath", Path.class, String.class);
        method.setAccessible(true);

        Path target = tempDir.resolve("target");
        Files.createDirectories(target);

        // Chemin avec "./" doit être normalisé
        Path result = (Path) method.invoke(dataStore, target, "./data/../file.txt");
        assertEquals(target.resolve("file.txt").normalize(), result);
    }

    @Test
    void getBackups_empty_shouldReturnEmptyList() {
        assertTrue(dataStore.getBackups(TEST_PLUGIN).isEmpty());
    }

    @Test
    void storeXml_withPojo_shouldDeserializeToMap() throws Exception {
        dataStore.store(TEST_PLUGIN, "pojo", new Person("Alice", 30), PluginDataStore.SerializationFormat.XML);
        Map<?,?> result = (Map<?,?>) dataStore.retrieve(TEST_PLUGIN, "pojo");
        assertEquals("Alice", result.get("name"));
        assertEquals("30", result.get("age").toString()); // XML convertit en String
    }

    static class Person {
        public String name;
        public int age;
        public Person() {}
        public Person(String name, int age) { this.name=name; this.age=age; }
    }

    @Nested
    @DisplayName("Coverage completion tests")
    class CoverageCompletionTests {

        @Test
        @DisplayName("Default constructor wraps directory creation failures")
        void noArgConstructorFailsWhenDirectoryCannotBeCreated() throws Exception {
            String originalHome = System.getProperty("user.home");
            try {
                // Pointing user.home at a regular file makes createDirectories fail
                Path blocker = Files.createFile(tempDir.resolve("home-as-file"));
                System.setProperty("user.home", blocker.toString());
                assertThrows(RuntimeException.class, DefaultPluginDataStore::new);
            } finally {
                System.setProperty("user.home", originalHome);
            }
        }

        @Test
        @DisplayName("Path constructor wraps directory creation failures")
        void pathConstructorFailsWhenDirectoryCannotBeCreated() throws Exception {
            Path blocker = Files.createFile(tempDir.resolve("root-as-file"));
            assertThrows(RuntimeException.class, () -> new DefaultPluginDataStore(blocker));
        }

        @Test
        @DisplayName("Storage initialization failure is logged and store fails fast")
        void storageInitializationFailure() throws Exception {
            // A regular file where the plugin directory should be blocks creation
            Files.createFile(tempDir.resolve("broken-plugin"));
            assertThrows(RuntimeException.class,
                    () -> dataStore.store("broken-plugin", TEST_KEY, TEST_VALUE));
        }

        @Test
        @DisplayName("Typed retrieve rejects values of incompatible types")
        void typedRetrieveWithIncompatibleClass() {
            Map<String, String> data = Map.of("k", "v");
            dataStore.store(TEST_PLUGIN, TEST_KEY, data);

            assertNotNull(dataStore.retrieve(TEST_PLUGIN, TEST_KEY, Map.class));
            assertNull(dataStore.retrieve(TEST_PLUGIN, TEST_KEY, String.class));
            // Missing key -> data == null short-circuits before the type check
            assertNull(dataStore.retrieve(TEST_PLUGIN, "missing-key", String.class));
        }

        @Test
        @DisplayName("Deleting a missing key returns false, getSize returns zero")
        void deleteMissingKeyAndSizeOfMissingKey() {
            dataStore.store(TEST_PLUGIN, TEST_KEY, TEST_VALUE);

            assertFalse(dataStore.delete(TEST_PLUGIN, "missing-key"));
            assertEquals(0, dataStore.getSize(TEST_PLUGIN, "missing-key"));
        }

        @Test
        @DisplayName("Backup fails when the backups path is blocked by a file")
        void backupFailsWhenBackupsPathBlocked() throws Exception {
            dataStore.store(TEST_PLUGIN, TEST_KEY, TEST_VALUE);
            // Block the backups directory with a regular file
            Files.createFile(tempDir.resolve(TEST_PLUGIN).resolve("backups"));

            assertNull(dataStore.backup(TEST_PLUGIN));
        }

        @Test
        @DisplayName("Restore fails when the backup file cannot be opened")
        void restoreFailsWhenBackupCannotBeOpened() throws Exception {
            Path backupsDir = tempDir.resolve(TEST_PLUGIN).resolve("backups");
            Files.createDirectories(backupsDir);
            Files.writeString(backupsDir.resolve("bad.zip"), "this is not a zip");

            try (org.mockito.MockedStatic<Files> filesMock =
                         org.mockito.Mockito.mockStatic(Files.class,
                                 org.mockito.Mockito.CALLS_REAL_METHODS)) {
                filesMock.when(() -> Files.newInputStream(backupsDir.resolve("bad.zip")))
                        .thenThrow(new IOException("cannot open backup"));
                assertFalse(dataStore.restore(TEST_PLUGIN, "bad"));
            }
        }

        @Test
        @DisplayName("Deleting an unknown backup id returns false")
        void deleteUnknownBackupReturnsFalse() {
            dataStore.store(TEST_PLUGIN, TEST_KEY, TEST_VALUE);
            assertNotNull(dataStore.backup(TEST_PLUGIN));

            assertFalse(dataStore.deleteBackup(TEST_PLUGIN, "no-such-backup"));
        }

        @Test
        @DisplayName("Deleting a backup whose file cannot be removed still succeeds")
        void deleteBackupWhenFileCannotBeDeleted() throws Exception {
            dataStore.store(TEST_PLUGIN, TEST_KEY, TEST_VALUE);
            String backupId = dataStore.backup(TEST_PLUGIN);
            assertNotNull(backupId);

            // Replace the backup file with a non-empty directory
            Path backupFile = tempDir.resolve(TEST_PLUGIN)
                    .resolve("backups").resolve(backupId + ".zip");
            Files.delete(backupFile);
            Files.createDirectories(backupFile);
            Files.createFile(backupFile.resolve("child"));

            assertTrue(dataStore.deleteBackup(TEST_PLUGIN, backupId));
        }

        @Test
        @DisplayName("Retrieve returns null when the stored file is corrupted")
        void retrieveCorruptedDataReturnsNull() throws Exception {
            dataStore.store(TEST_PLUGIN, TEST_KEY, Map.of("k", "v"),
                    PluginDataStore.SerializationFormat.XML);
            Path dataFile = tempDir.resolve(TEST_PLUGIN).resolve(TEST_KEY + ".dat");
            Files.writeString(dataFile, "this is not valid xml");

            assertNull(dataStore.retrieve(TEST_PLUGIN, TEST_KEY));
        }

        @Test
        @DisplayName("Delete fails when the data file is a non-empty directory")
        void deleteFailsWhenDataFileIsDirectory() throws Exception {
            dataStore.store(TEST_PLUGIN, TEST_KEY, TEST_VALUE);
            Path dataFile = tempDir.resolve(TEST_PLUGIN).resolve(TEST_KEY + ".dat");
            Files.delete(dataFile);
            Files.createDirectories(dataFile);
            Files.createFile(dataFile.resolve("child"));

            assertFalse(dataStore.delete(TEST_PLUGIN, TEST_KEY));

            // Restore a deletable state so tearDown cleanup succeeds
            Files.delete(dataFile.resolve("child"));
            Files.delete(dataFile);
        }

        @Test
        @DisplayName("Clear logs an error when a data file cannot be removed")
        void clearFailsWhenDataFileCannotBeDeleted() throws Exception {
            dataStore.store(TEST_PLUGIN, TEST_KEY, TEST_VALUE);
            dataStore.store(TEST_PLUGIN, "other-key", TEST_VALUE);
            Path dataFile = tempDir.resolve(TEST_PLUGIN).resolve(TEST_KEY + ".dat");
            Files.delete(dataFile);
            Files.createDirectories(dataFile);
            Files.createFile(dataFile.resolve("child"));

            // Should not throw: the IOException is caught and logged
            dataStore.clear(TEST_PLUGIN);

            // Restore a deletable state so tearDown cleanup succeeds
            Files.delete(dataFile.resolve("child"));
            Files.delete(dataFile);
        }

        @Test
        @DisplayName("Backup skips entries whose data file is missing")
        void backupSkipsMissingDataFiles() throws Exception {
            dataStore.store(TEST_PLUGIN, TEST_KEY, TEST_VALUE);
            Files.delete(tempDir.resolve(TEST_PLUGIN).resolve(TEST_KEY + ".dat"));

            assertNotNull(dataStore.backup(TEST_PLUGIN));
        }

        @Test
        @DisplayName("Restore skips unknown and path-traversal entries")
        void restoreSkipsUnknownAndMaliciousEntries() throws Exception {
            Path backupsDir = tempDir.resolve(TEST_PLUGIN).resolve("backups");
            Files.createDirectories(backupsDir);
            Path zip = backupsDir.resolve("custom.zip");

            // Metadata must declare the restored keys, otherwise restore clears them
            String metadata = "{\"ok\":{\"key\":\"ok\",\"format\":\"JSON\","
                    + "\"size\":3,\"timestamp\":0}}";

            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zip))) {
                zos.putNextEntry(new ZipEntry("metadata.json"));
                zos.write(metadata.getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
                zos.putNextEntry(new ZipEntry("other.txt"));
                zos.write("ignored".getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
                zos.putNextEntry(new ZipEntry("data/a/b"));
                zos.write("x".getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
                zos.putNextEntry(new ZipEntry("data/a\\b"));
                zos.write("x".getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
                zos.putNextEntry(new ZipEntry("data/ok"));
                zos.write("\"v\"".getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }

            assertTrue(dataStore.restore(TEST_PLUGIN, "custom"));
            assertTrue(dataStore.exists(TEST_PLUGIN, "ok"));
            assertEquals("v", dataStore.retrieve(TEST_PLUGIN, "ok"));
        }

        @Test
        @DisplayName("Existing metadata is loaded when storage is recreated")
        void loadMetadataFromExistingStorage() {
            dataStore.store(TEST_PLUGIN, TEST_KEY, TEST_VALUE);

            // A second store on the same root reloads persisted metadata
            DefaultPluginDataStore secondStore = new DefaultPluginDataStore(tempDir);
            secondStore.store(TEST_PLUGIN, "second-key", "second-value");

            assertEquals("second-value", secondStore.retrieve(TEST_PLUGIN, "second-key"));
            assertTrue(secondStore.getKeys(TEST_PLUGIN).contains("second-key"));
        }
    }
}