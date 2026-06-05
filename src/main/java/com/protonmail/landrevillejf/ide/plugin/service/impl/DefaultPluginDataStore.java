package com.protonmail.landrevillejf.ide.plugin.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.protonmail.landrevillejf.ide.plugin.service.PluginDataStore;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Slf4j
public class DefaultPluginDataStore implements PluginDataStore {

    private final Path dataRoot;
    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final XmlMapper xmlMapper = new XmlMapper();
    private final Map<String, PluginStorage> pluginStorages = new ConcurrentHashMap<>();
    private final Map<String, List<BackupInfo>> backups = new ConcurrentHashMap<>();
    private final AtomicLong backupIdGenerator = new AtomicLong(0);

    public DefaultPluginDataStore() {
        String userHome = System.getProperty("user.home");
        this.dataRoot = Paths.get(userHome, ".swingide", "plugin-data");

        try {
            Files.createDirectories(dataRoot);
            log.info("PluginDataStore initialized at: {}", dataRoot);
        } catch (IOException e) {
            log.error("Failed to create plugin data directory", e);
            throw new RuntimeException("Could not initialize plugin data store", e);
        }
    }

    public DefaultPluginDataStore(Path dataRoot) {
        this.dataRoot = dataRoot;

        try {
            Files.createDirectories(dataRoot);
            log.info("PluginDataStore initialized at: {}", dataRoot);
        } catch (IOException e) {
            log.error("Failed to create plugin data directory", e);
            throw new RuntimeException("Could not initialize plugin data store", e);
        }
    }

    @Override
    public void store(String pluginId, String key, Object data) {
        store(pluginId, key, data, SerializationFormat.JSON);
    }

    @Override
    public void store(String pluginId, String key, Object data, SerializationFormat format) {
        PluginStorage storage = getOrCreateStorage(pluginId);
        storage.store(key, data, format);
        log.debug("Data stored: plugin={}, key={}, format={}", pluginId, key, format);
    }

    @Override
    public Object retrieve(String pluginId, String key) {
        PluginStorage storage = pluginStorages.get(pluginId);
        if (storage == null) {
            return null;
        }

        Object data = storage.retrieve(key);
        if (data != null) {
            log.debug("Data retrieved: plugin={}, key={}", pluginId, key);
        }
        return data;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T retrieve(String pluginId, String key, Class<T> dataClass) {
        Object data = retrieve(pluginId, key);
        if (data != null && dataClass.isAssignableFrom(data.getClass())) {
            return (T) data;
        }
        return null;
    }

    @Override
    public boolean exists(String pluginId, String key) {
        PluginStorage storage = pluginStorages.get(pluginId);
        return storage != null && storage.exists(key);
    }

    @Override
    public boolean delete(String pluginId, String key) {
        PluginStorage storage = pluginStorages.get(pluginId);
        if (storage == null) {
            return false;
        }

        boolean deleted = storage.delete(key);
        if (deleted) {
            log.debug("Data deleted: plugin={}, key={}", pluginId, key);
        }
        return deleted;
    }

    @Override
    public void clear(String pluginId) {
        PluginStorage storage = pluginStorages.get(pluginId);
        if (storage != null) {
            storage.clear();
            log.debug("All data cleared for plugin: {}", pluginId);
        }
    }

    @Override
    public List<String> getKeys(String pluginId) {
        PluginStorage storage = pluginStorages.get(pluginId);
        if (storage == null) {
            return Collections.emptyList();
        }
        return storage.getKeys();
    }

    @Override
    public long getSize(String pluginId, String key) {
        PluginStorage storage = pluginStorages.get(pluginId);
        if (storage == null) {
            return 0;
        }
        return storage.getSize(key);
    }

    @Override
    public long getTotalSize(String pluginId) {
        PluginStorage storage = pluginStorages.get(pluginId);
        if (storage == null) {
            return 0;
        }
        return storage.getTotalSize();
    }

    @Override
    public Map<String, Object> exportAllData(String pluginId) {
        PluginStorage storage = pluginStorages.get(pluginId);
        if (storage == null) {
            return Collections.emptyMap();
        }

        Map<String, Object> export = new LinkedHashMap<>();
        for (String key : storage.getKeys()) {
            export.put(key, storage.retrieve(key));
        }

        log.debug("Data exported for plugin: {} ({} entries)", pluginId, export.size());
        return export;
    }

    @Override
    public void importAllData(String pluginId, Map<String, Object> data) {
        PluginStorage storage = getOrCreateStorage(pluginId);

        for (Map.Entry<String, Object> entry : data.entrySet()) {
            storage.store(entry.getKey(), entry.getValue(), SerializationFormat.JSON);
        }

        log.debug("Data imported for plugin: {} ({} entries)", pluginId, data.size());
    }

    @Override
    public String backup(String pluginId) {
        PluginStorage storage = pluginStorages.get(pluginId);
        if (storage == null) {
            log.warn("No storage found for plugin: {}", pluginId);
            return null;
        }

        String backupId = generateBackupId(pluginId);
        Path backupFile = getBackupPath(pluginId, backupId);

        try {
            storage.backup(backupFile);

            BackupInfo info = new BackupInfo(backupId, System.currentTimeMillis(),
                    Files.size(backupFile), storage.getTotalSize());
            backups.computeIfAbsent(pluginId, k -> new ArrayList<>()).add(info);

            // Keep only last 10 backups
            List<BackupInfo> pluginBackups = backups.get(pluginId);
            if (pluginBackups.size() > 10) {
                BackupInfo oldest = pluginBackups.remove(0);
                deleteBackupFile(pluginId, oldest.id);
            }

            log.info("Backup created for plugin: {}, backupId: {}, size: {} bytes",
                    pluginId, backupId, info.size);

            return backupId;

        } catch (IOException e) {
            log.error("Failed to create backup for plugin: {}", pluginId, e);
            return null;
        }
    }

    @Override
    public boolean restore(String pluginId, String backupId) {
        Path backupFile = getBackupPath(pluginId, backupId);

        if (!Files.exists(backupFile)) {
            log.warn("Backup not found: plugin={}, backupId={}", pluginId, backupId);
            return false;
        }

        PluginStorage storage = getOrCreateStorage(pluginId);

        try {
            storage.restore(backupFile);
            log.info("Backup restored for plugin: {}, backupId: {}", pluginId, backupId);
            return true;
        } catch (IOException e) {
            log.error("Failed to restore backup for plugin: {}", pluginId, e);
            return false;
        }
    }

    @Override
    public List<Map<String, Object>> getBackups(String pluginId) {
        List<BackupInfo> pluginBackups = backups.get(pluginId);
        if (pluginBackups == null || pluginBackups.isEmpty()) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (BackupInfo info : pluginBackups) {
            Map<String, Object> backupInfo = new LinkedHashMap<>();
            backupInfo.put("backupId", info.id);
            backupInfo.put("timestamp", info.timestamp);
            backupInfo.put("size", info.size);
            backupInfo.put("dataSize", info.dataSize);
            result.add(backupInfo);
        }

        return result;
    }

    @Override
    public boolean deleteBackup(String pluginId, String backupId) {
        List<BackupInfo> pluginBackups = backups.get(pluginId);
        if (pluginBackups == null) {
            return false;
        }

        boolean removed = pluginBackups.removeIf(info -> info.id.equals(backupId));
        if (removed) {
            deleteBackupFile(pluginId, backupId);
            log.debug("Backup deleted: plugin={}, backupId={}", pluginId, backupId);
        }

        return removed;
    }

    @Override
    public Map<String, Object> getStatistics(String pluginId) {
        PluginStorage storage = pluginStorages.get(pluginId);
        if (storage == null) {
            Map<String, Object> empty = new LinkedHashMap<>();
            empty.put("totalSize", 0);
            empty.put("keyCount", 0);
            return empty;
        }

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalSize", storage.getTotalSize());
        stats.put("keyCount", storage.getKeys().size());
        stats.put("backupCount", backups.getOrDefault(pluginId, Collections.emptyList()).size());

        return stats;
    }

    private PluginStorage getOrCreateStorage(String pluginId) {
        return pluginStorages.computeIfAbsent(pluginId, k -> new PluginStorage(pluginId));
    }

    private String generateBackupId(String pluginId) {
        return pluginId + "_backup_" + System.currentTimeMillis() + "_" + backupIdGenerator.incrementAndGet();
    }

    private Path getBackupPath(String pluginId, String backupId) {
        Path pluginDir = dataRoot.resolve(pluginId);
        Path backupsDir = pluginDir.resolve("backups");
        return backupsDir.resolve(backupId + ".zip");
    }

    private void deleteBackupFile(String pluginId, String backupId) {
        try {
            Path backupFile = getBackupPath(pluginId, backupId);
            Files.deleteIfExists(backupFile);
        } catch (IOException e) {
            log.warn("Failed to delete backup file: plugin={}, backupId={}", pluginId, backupId, e);
        }
    }

    /**
     * Plugin storage implementation
     */
    private class PluginStorage {
        private final String pluginId;
        private final Path storagePath;
        private final Map<String, StoredData> dataMap = new ConcurrentHashMap<>();

        public PluginStorage(String pluginId) {
            this.pluginId = pluginId;
            this.storagePath = dataRoot.resolve(pluginId);

            try {
                Files.createDirectories(storagePath);
                loadMetadata();
            } catch (IOException e) {
                log.error("Failed to initialize storage for plugin: {}", pluginId, e);
            }
        }

        public void store(String key, Object data, SerializationFormat format) {
            try {
                byte[] serialized = serialize(data, format);
                Path dataFile = getDataFile(key);

                // Write data
                Files.write(dataFile, serialized);

                // Store metadata
                StoredData storedData = new StoredData(key, format, serialized.length, System.currentTimeMillis());
                dataMap.put(key, storedData);

                // Save metadata
                saveMetadata();

            } catch (Exception e) {
                log.error("Failed to store data for plugin: {}, key: {}", pluginId, key, e);
                throw new RuntimeException("Failed to store data", e);
            }
        }

        public Object retrieve(String key) {
            StoredData metadata = dataMap.get(key);
            if (metadata == null) {
                return null;
            }

            try {
                Path dataFile = getDataFile(key);
                if (!Files.exists(dataFile)) {
                    dataMap.remove(key);
                    return null;
                }

                byte[] data = Files.readAllBytes(dataFile);
                return deserialize(data, metadata.format);

            } catch (Exception e) {
                log.error("Failed to retrieve data for plugin: {}, key: {}", pluginId, key, e);
                return null;
            }
        }

        public boolean exists(String key) {
            StoredData metadata = dataMap.get(key);
            if (metadata == null) {
                return false;
            }

            Path dataFile = getDataFile(key);
            return Files.exists(dataFile);
        }

        public boolean delete(String key) {
            StoredData metadata = dataMap.remove(key);
            if (metadata == null) {
                return false;
            }

            try {
                Path dataFile = getDataFile(key);
                Files.deleteIfExists(dataFile);
                saveMetadata();
                return true;
            } catch (IOException e) {
                log.error("Failed to delete data for plugin: {}, key: {}", pluginId, key, e);
                return false;
            }
        }

        public void clear() {
            try {
                for (String key : dataMap.keySet()) {
                    Path dataFile = getDataFile(key);
                    Files.deleteIfExists(dataFile);
                }
                dataMap.clear();
                saveMetadata();
            } catch (IOException e) {
                log.error("Failed to clear data for plugin: {}", pluginId, e);
            }
        }

        public List<String> getKeys() {
            return new ArrayList<>(dataMap.keySet());
        }

        public long getSize(String key) {
            StoredData metadata = dataMap.get(key);
            return metadata != null ? metadata.size : 0;
        }

        public long getTotalSize() {
            return dataMap.values().stream().mapToLong(d -> d.size).sum();
        }

        public void backup(Path backupFile) throws IOException {
            Path backupsDir = backupFile.getParent();
            Files.createDirectories(backupsDir);

            try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(backupFile))) {
                // Add metadata
                ZipEntry metadataEntry = new ZipEntry("metadata.json");
                zos.putNextEntry(metadataEntry);
                byte[] metadataBytes = jsonMapper.writeValueAsBytes(dataMap);
                zos.write(metadataBytes);
                zos.closeEntry();

                // Add data files
                for (String key : dataMap.keySet()) {
                    Path dataFile = getDataFile(key);
                    if (Files.exists(dataFile)) {
                        ZipEntry dataEntry = new ZipEntry("data/" + key);
                        zos.putNextEntry(dataEntry);
                        Files.copy(dataFile, zos);
                        zos.closeEntry();
                    }
                }
            }
        }

        public void restore(Path backupFile) throws IOException {
            try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(backupFile))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    if (entry.getName().equals("metadata.json")) {
                        byte[] metadataBytes = zis.readAllBytes();
                        @SuppressWarnings("unchecked")
                        Map<String, StoredData> restored = jsonMapper.readValue(metadataBytes, Map.class);
                        // Restore metadata
                        dataMap.clear();
                        for (Map.Entry<String, StoredData> e : restored.entrySet()) {
                            dataMap.put(e.getKey(), e.getValue());
                        }
                    } else if (entry.getName().startsWith("data/")) {
                        String key = entry.getName().substring(5);
                        Path dataFile = getDataFile(key);
                        Files.copy(zis, dataFile, StandardCopyOption.REPLACE_EXISTING);
                    }
                    zis.closeEntry();
                }
                saveMetadata();
            }
        }

        private byte[] serialize(Object data, SerializationFormat format) throws Exception {
            switch (format) {
                case JSON:
                    return jsonMapper.writeValueAsBytes(data);
                case XML:
                    return xmlMapper.writeValueAsBytes(data);
                case PROPERTIES:
                    if (data instanceof Map) {
                        Properties props = new Properties();
                        props.putAll((Map<?, ?>) data);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        props.store(baos, null);
                        return baos.toByteArray();
                    }
                    throw new IllegalArgumentException("Properties format requires Map data");
                case BINARY:
                    if (data instanceof Serializable) {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ObjectOutputStream oos = new ObjectOutputStream(baos);
                        oos.writeObject(data);
                        oos.close();
                        return baos.toByteArray();
                    }
                    throw new IllegalArgumentException("Binary format requires Serializable data");
                default:
                    throw new IllegalArgumentException("Unsupported format: " + format);
            }
        }

        private Object deserialize(byte[] data, SerializationFormat format) throws Exception {
            switch (format) {
                case JSON:
                    return jsonMapper.readValue(data, Object.class);
                case XML:
                    return xmlMapper.readValue(data, Object.class);
                case PROPERTIES:
                    Properties props = new Properties();
                    props.load(new ByteArrayInputStream(data));
                    return props;
                case BINARY:
                    ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
                    return ois.readObject();
                default:
                    throw new IllegalArgumentException("Unsupported format: " + format);
            }
        }

        private Path getDataFile(String key) {
            String safeKey = key.replaceAll("[^a-zA-Z0-9.-]", "_");
            return storagePath.resolve(safeKey + ".dat");
        }

        private Path getMetadataFile() {
            return storagePath.resolve("metadata.json");
        }

        private void saveMetadata() throws IOException {
            Path metadataFile = getMetadataFile();
            byte[] metadataBytes = jsonMapper.writeValueAsBytes(dataMap);
            Files.write(metadataFile, metadataBytes);
        }

        private void loadMetadata() throws IOException {
            Path metadataFile = getMetadataFile();
            if (Files.exists(metadataFile)) {
                byte[] metadataBytes = Files.readAllBytes(metadataFile);
                @SuppressWarnings("unchecked")
                Map<String, StoredData> loaded = jsonMapper.readValue(metadataBytes, Map.class);
                dataMap.clear();
                dataMap.putAll(loaded);
            }
        }
    }

    /**
     * Stored data metadata
     */
    private static class StoredData {
        public String key;
        public SerializationFormat format;
        public long size;
        public long timestamp;

        public StoredData() {}

        public StoredData(String key, SerializationFormat format, long size, long timestamp) {
            this.key = key;
            this.format = format;
            this.size = size;
            this.timestamp = timestamp;
        }
    }

    /**
     * Backup information
     */
    private static class BackupInfo {
        final String id;
        final long timestamp;
        final long size;
        final long dataSize;

        BackupInfo(String id, long timestamp, long size, long dataSize) {
            this.id = id;
            this.timestamp = timestamp;
            this.size = size;
            this.dataSize = dataSize;
        }
    }
}