package com.protonmail.landrevillejf.ide.plugin.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.protonmail.landrevillejf.ide.plugin.service.PluginUpdateService;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Default implementation of {@link PluginUpdateService} that manages plugin updates
 * via HTTP, parses JSON responses, and handles file-based install/rollback operations.
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 * @see PluginUpdateService
 */
@Slf4j
public class DefaultPluginUpdateService implements PluginUpdateService {

    /** System property key for the update server URL. */
    public static final String PROP_UPDATE_SERVER_URL = "ide.plugin.update.server.url";

    /** System property key for the plugin install directory. */
    public static final String PROP_PLUGIN_INSTALL_DIR = "ide.plugin.install.dir";

    /** System property key for the download directory. */
    public static final String PROP_DOWNLOAD_DIR = "ide.plugin.download.dir";

    /** Default connect/read timeout for HTTP requests (seconds). */
    private static final int DEFAULT_CONNECT_TIMEOUT_SECONDS = 5;

    /** Default download timeout for file transfers (seconds). */
    private static final int DEFAULT_DOWNLOAD_TIMEOUT_SECONDS = 30;

    /** Interval between automatic update checks (hours). */
    private static final int UPDATE_CHECK_INTERVAL_HOURS = 24;

    private final Map<String, PluginVersion> latestVersions = new ConcurrentHashMap<>();
    private final Map<String, UpdateStatus> updateStatuses = new ConcurrentHashMap<>();
    private final Map<String, Integer> updateProgress = new ConcurrentHashMap<>();
    private final Map<String, UpdateChannel> updateChannels = new ConcurrentHashMap<>();
    private final Map<String, Boolean> autoUpdateEnabled = new ConcurrentHashMap<>();
    private final Map<String, List<PluginVersion>> versionHistory = new ConcurrentHashMap<>();
    private final Map<String, UpdateTask> activeUpdates = new ConcurrentHashMap<>();
    private final Map<String, String> currentVersions = new ConcurrentHashMap<>();
    private final Map<String, Path> pluginFiles = new ConcurrentHashMap<>();

    private final ExecutorService updateExecutor = Executors.newCachedThreadPool();
    private final ScheduledExecutorService scheduledChecker;
    private final AtomicInteger updateCount = new AtomicInteger(0);
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger failedCount = new AtomicInteger(0);

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final Path pluginInstallDir;
    private final Path downloadDir;
    private final int connectTimeoutSeconds;
    private final int downloadTimeoutSeconds;

    private volatile String updateServerUrl;

    /**
     * Creates a new update service using system properties for configuration.
     * <p>
     * Recognized system properties:
     * <ul>
     *   <li>{@value PROP_UPDATE_SERVER_URL} — base URL of the update server</li>
     *   <li>{@value PROP_PLUGIN_INSTALL_DIR} — directory where plugin JARs are installed</li>
     *   <li>{@value PROP_DOWNLOAD_DIR} — directory for downloading update files</li>
     * </ul>
     */
    public DefaultPluginUpdateService() {
        this(System.getProperty(PROP_PLUGIN_INSTALL_DIR),
                System.getProperty(PROP_DOWNLOAD_DIR),
                System.getProperty(PROP_UPDATE_SERVER_URL));
    }

    /**
     * Creates a new update service with configurable directories.
     *
     * @param pluginInstallDir directory where plugin JARs are installed (may be null for temp dir)
     * @param downloadDir      directory for downloading update files (may be null for temp dir)
     */
    public DefaultPluginUpdateService(String pluginInstallDir, String downloadDir) {
        this(pluginInstallDir, downloadDir, null);
    }

    /**
     * Creates a new update service with configurable directories and update server URL.
     *
     * @param pluginInstallDir directory where plugin JARs are installed (may be null for temp dir)
     * @param downloadDir      directory for downloading update files (may be null for temp dir)
     * @param updateServerUrl  base URL of the update server (may be null)
     */
    public DefaultPluginUpdateService(String pluginInstallDir, String downloadDir, String updateServerUrl) {
        this.objectMapper = new ObjectMapper();
        this.connectTimeoutSeconds = DEFAULT_CONNECT_TIMEOUT_SECONDS;
        this.downloadTimeoutSeconds = DEFAULT_DOWNLOAD_TIMEOUT_SECONDS;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(this.connectTimeoutSeconds))
                .build();

        String tempDir = System.getProperty("java.io.tmpdir");
        this.pluginInstallDir = Paths.get(
                pluginInstallDir != null ? pluginInstallDir : tempDir);
        this.downloadDir = Paths.get(
                downloadDir != null ? downloadDir : tempDir);

        this.updateServerUrl = updateServerUrl != null ? updateServerUrl : "";

        if (log.isInfoEnabled()) {
            log.info("DefaultPluginUpdateService initialized with installDir={}, downloadDir={}, serverUrl={}",
                    this.pluginInstallDir, this.downloadDir, this.updateServerUrl);
        }

        this.scheduledChecker = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "plugin-update-checker");
            t.setDaemon(true);
            return t;
        });
        this.scheduledChecker.scheduleAtFixedRate(
                this::checkAllForUpdates, UPDATE_CHECK_INTERVAL_HOURS,
                UPDATE_CHECK_INTERVAL_HOURS, TimeUnit.HOURS);
    }

    /**
     * Shuts down background executors and releases resources.
     * After shutdown, this service instance should not be reused.
     */
    public void shutdown() {
        scheduledChecker.shutdownNow();
        updateExecutor.shutdownNow();
        if (log.isInfoEnabled()) {
            log.info("DefaultPluginUpdateService shut down");
        }
    }

    @Override
    public PluginVersion checkForUpdates(String pluginId) {
        UpdateChannel channel = getUpdateChannel(pluginId);
        return checkForUpdates(pluginId, channel);
    }

    @Override
    public PluginVersion checkForUpdates(String pluginId, UpdateChannel channel) {
        if (log.isDebugEnabled()) {
            log.debug("Checking for updates: plugin={}, channel={}", pluginId, channel);
        }

        updateStatuses.put(pluginId, UpdateStatus.CHECKING);

        try {
            PluginVersion latest = fetchLatestVersion(pluginId, channel);
            String currentVersion = currentVersions.get(pluginId);

            if (latest != null && isNewerVersion(latest.getVersion(), currentVersion)) {
                latestVersions.put(pluginId, latest);
                updateStatuses.put(pluginId, UpdateStatus.AVAILABLE);
                if (log.isInfoEnabled()) {
                    log.info("Update available for plugin {}: {} -> {}",
                            pluginId, currentVersion, latest.getVersion());
                }
                return latest;
            }

            updateStatuses.remove(pluginId);
            if (log.isDebugEnabled()) {
                log.debug("No updates available for plugin: {}", pluginId);
            }
            return null;

        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Failed to check updates for plugin: {}", pluginId, e);
            }
            updateStatuses.put(pluginId, UpdateStatus.FAILED);
            return null;
        }
    }

    @Override
    public UpdateStatus getUpdateStatus(String pluginId) {
        return updateStatuses.get(pluginId);
    }

    @Override
    public boolean installUpdate(String pluginId, String version) {
        PluginVersion targetVersion = latestVersions.get(pluginId);
        if (targetVersion == null || !targetVersion.getVersion().equals(version)) {
            if (log.isWarnEnabled()) {
                log.warn("Cannot install update: version {} not available for plugin {}", version, pluginId);
            }
            return false;
        }

        if (activeUpdates.containsKey(pluginId)) {
            if (log.isWarnEnabled()) {
                log.warn("Update already in progress for plugin: {}", pluginId);
            }
            return false;
        }

        updateStatuses.put(pluginId, UpdateStatus.INSTALLING);
        updateProgress.put(pluginId, 0);

        UpdateTask task = new UpdateTask(pluginId, targetVersion);
        activeUpdates.put(pluginId, task);
        updateExecutor.submit(task);

        if (log.isInfoEnabled()) {
            log.info("Update installation started for plugin: {} to version {}", pluginId, version);
        }
        return true;
    }

    @Override
    public boolean cancelUpdate(String pluginId) {
        UpdateTask task = activeUpdates.get(pluginId);
        if (task == null) {
            return false;
        }

        task.cancel();
        activeUpdates.remove(pluginId);
        updateStatuses.put(pluginId, UpdateStatus.FAILED);
        updateProgress.remove(pluginId);

        if (log.isInfoEnabled()) {
            log.info("Update cancelled for plugin: {}", pluginId);
        }
        return true;
    }

    @Override
    public int getUpdateProgress(String pluginId) {
        return updateProgress.getOrDefault(pluginId, 0);
    }

    @Override
    public boolean rollbackVersion(String pluginId, String version) {
        List<PluginVersion> history = versionHistory.get(pluginId);
        if (history == null) {
            if (log.isWarnEnabled()) {
                log.warn("No version history for plugin: {}", pluginId);
            }
            return false;
        }

        PluginVersion targetVersion = history.stream()
                .filter(v -> v.getVersion().equals(version))
                .findFirst()
                .orElse(null);
        if (targetVersion == null) {
            if (log.isWarnEnabled()) {
                log.warn("Version {} not found in history for plugin: {}", version, pluginId);
            }
            return false;
        }

        if (log.isInfoEnabled()) {
            log.info("Rolling back plugin {} to version {}", pluginId, version);
        }

        boolean success = performRollback(pluginId, targetVersion);

        if (success) {
            currentVersions.put(pluginId, version);
            updateCount.incrementAndGet();
            successCount.incrementAndGet();
            if (log.isInfoEnabled()) {
                log.info("Rollback successful for plugin: {} to version {}", pluginId, version);
            }
        } else {
            failedCount.incrementAndGet();
            if (log.isErrorEnabled()) {
                log.error("Rollback failed for plugin: {} to version {}", pluginId, version);
            }
        }

        return success;
    }

    @Override
    public List<PluginVersion> getVersionHistory(String pluginId) {
        return versionHistory.getOrDefault(pluginId, Collections.emptyList());
    }

    @Override
    public void setUpdateChannel(String pluginId, UpdateChannel channel) {
        updateChannels.put(pluginId, channel);
        if (log.isDebugEnabled()) {
            log.debug("Update channel set for plugin {}: {}", pluginId, channel);
        }
    }

    @Override
    public UpdateChannel getUpdateChannel(String pluginId) {
        return updateChannels.getOrDefault(pluginId, UpdateChannel.STABLE);
    }

    @Override
    public void setAutoUpdate(String pluginId, boolean enabled) {
        autoUpdateEnabled.put(pluginId, enabled);
        if (log.isDebugEnabled()) {
            log.debug("Auto-update for plugin {}: {}", pluginId, enabled);
        }

        if (enabled) {
            CompletableFuture.runAsync(() -> checkAndAutoUpdate(pluginId));
        }
    }

    @Override
    public boolean isAutoUpdateEnabled(String pluginId) {
        return autoUpdateEnabled.getOrDefault(pluginId, false);
    }

    @Override
    public Map<String, Object> getUpdateStatistics() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalUpdates", updateCount.get());
        stats.put("successfulUpdates", successCount.get());
        stats.put("failedUpdates", failedCount.get());
        stats.put("successRate", updateCount.get() > 0
                ? (double) successCount.get() / updateCount.get() * 100 : 0);
        stats.put("activeUpdates", activeUpdates.size());
        stats.put("pluginsWithUpdates", latestVersions.size());

        Map<String, String> pluginStatuses = new LinkedHashMap<>();
        for (Map.Entry<String, UpdateStatus> entry : updateStatuses.entrySet()) {
            // ConcurrentHashMap never stores null values, so no null-check is needed
            pluginStatuses.put(entry.getKey(), entry.getValue().name());
        }
        stats.put("pluginStatuses", pluginStatuses);

        return stats;
    }

    /**
     * Sets the current version of a plugin (called by plugin manager).
     *
     * @param pluginId the plugin identifier
     * @param version  the current version string
     */
    public void setPluginVersion(String pluginId, String version) {
        currentVersions.put(pluginId, version);

        PluginVersionImpl versionInfo = new PluginVersionImpl(
                version, "Current version", new Date().toString(),
                Collections.emptyList(), Collections.emptyList(), Collections.emptyList(),
                Collections.emptyMap()
        );
        versionHistory.computeIfAbsent(pluginId, k -> new CopyOnWriteArrayList<>()).add(0, versionInfo);

        if (log.isDebugEnabled()) {
            log.debug("Plugin version registered: {} version {}", pluginId, version);
        }
    }

    /**
     * Sets the update server URL.
     *
     * @param url the base URL of the update server
     */
    public void setUpdateServerUrl(String url) {
        this.updateServerUrl = url;
        if (log.isInfoEnabled()) {
            log.info("Update server URL set to: {}", url);
        }
    }

    /**
     * Registers the file location of an installed plugin for rollback support.
     *
     * @param pluginId   the plugin identifier
     * @param pluginFile the path to the plugin JAR file
     */
    public void registerPluginFile(String pluginId, Path pluginFile) {
        pluginFiles.put(pluginId, pluginFile);
        if (log.isDebugEnabled()) {
            log.debug("Plugin file registered: {} -> {}", pluginId, pluginFile);
        }
    }

    /**
     * Unregisters the file location of a plugin.
     *
     * @param pluginId the plugin identifier
     */
    public void unregisterPluginFile(String pluginId) {
        pluginFiles.remove(pluginId);
        if (log.isDebugEnabled()) {
            log.debug("Plugin file unregistered: {}", pluginId);
        }
    }

    private void checkAllForUpdates() {
        for (String pluginId : currentVersions.keySet()) {
            if (isAutoUpdateEnabled(pluginId)) {
                checkAndAutoUpdate(pluginId);
            }
        }
    }

    /**
     * Checks for updates and auto-installs if auto-update is enabled.
     *
     * @param pluginId the plugin identifier
     */
    public void checkAndAutoUpdate(String pluginId) {
        PluginVersion update = checkForUpdates(pluginId);
        if (update != null && isAutoUpdateEnabled(pluginId)) {
            if (log.isInfoEnabled()) {
                log.info("Auto-update triggered for plugin: {} to version {}",
                        pluginId, update.getVersion());
            }
            installUpdate(pluginId, update.getVersion());
        }
    }

    private PluginVersion fetchLatestVersion(String pluginId, UpdateChannel channel) throws Exception {
        String url = String.format("%s/%s/latest?channel=%s", updateServerUrl, pluginId, channel.name());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .timeout(Duration.ofSeconds(connectTimeoutSeconds))
                .build();

        HttpResponse<InputStream> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofInputStream());

        if (response.statusCode() == 200) {
            return parseVersionResponse(response.body());
        }

        return null;
    }

    private PluginVersion parseVersionResponse(InputStream inputStream) throws IOException {
        JsonNode root = objectMapper.readTree(inputStream);

        String version = getTextOrDefault(root, "version", null);
        if (version == null) {
            if (log.isWarnEnabled()) {
                log.warn("Missing 'version' field in update response");
            }
            return null;
        }

        String description = getTextOrDefault(root, "description", "");
        String releaseDate = getTextOrDefault(root, "releaseDate", "");
        List<String> changelog = readStringList(root, "changelog");
        List<String> newFeatures = readStringList(root, "newFeatures");
        List<String> bugFixes = readStringList(root, "bugFixes");

        Map<String, Object> metadata = new LinkedHashMap<>();
        JsonNode metadataNode = root.get("metadata");
        if (metadataNode != null && metadataNode.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = metadataNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                metadata.put(field.getKey(), nodeToObject(field.getValue()));
            }
        }

        return new PluginVersionImpl(version, description, releaseDate,
                changelog, newFeatures, bugFixes, metadata);
    }

    private String getTextOrDefault(JsonNode node, String field, String defaultValue) {
        JsonNode child = node.get(field);
        return (child != null && child.isTextual()) ? child.asText() : defaultValue;
    }

    private List<String> readStringList(JsonNode root, String fieldName) {
        JsonNode node = root.get(fieldName);
        if (node != null && node.isArray()) {
            List<String> result = new ArrayList<>();
            for (JsonNode element : node) {
                if (element.isTextual()) {
                    result.add(element.asText());
                }
            }
            return result;
        }
        return Collections.emptyList();
    }

    private Object nodeToObject(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isTextual()) {
            return node.asText();
        }
        if (node.isNumber()) {
            return node.numberValue();
        }
        if (node.isBoolean()) {
            return node.asBoolean();
        }
        return node.toString();
    }

    private boolean isNewerVersion(String version1, String version2) {
        if (version2 == null) {
            return true;
        }

        String[] parts1 = version1.split("\\.");
        String[] parts2 = version2.split("\\.");

        int maxLength = Math.max(parts1.length, parts2.length);

        for (int i = 0; i < maxLength; i++) {
            int num1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int num2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;

            if (num1 != num2) {
                return num1 > num2;
            }
        }

        return false;
    }

    private boolean performRollback(String pluginId, PluginVersion version) {
        try {
            Object downloadUrlObj = version.getMetadata().get("downloadUrl");
            if (downloadUrlObj != null) {
                String downloadUrl = downloadUrlObj.toString();
                Path targetFile = pluginInstallDir.resolve(pluginId + "-" + version.getVersion() + ".jar");
                downloadFile(downloadUrl, targetFile);
                if (log.isInfoEnabled()) {
                    log.info("Rollback completed for plugin: {} to version {}", pluginId, version.getVersion());
                }
                return true;
            }

            Path currentFile = pluginFiles.get(pluginId);
            if (currentFile != null && Files.exists(currentFile)) {
                if (log.isInfoEnabled()) {
                    log.info("Rollback completed for plugin: {} to version {} (local file)",
                            pluginId, version.getVersion());
                }
                return true;
            }

            if (log.isWarnEnabled()) {
                log.warn("Cannot rollback plugin {}: no download URL or local file available", pluginId);
            }
            return false;

        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Rollback failed for plugin: {} to version {}", pluginId, version.getVersion(), e);
            }
            return false;
        }
    }

    private boolean downloadAndInstall(String pluginId, PluginVersion version,
                                       UpdateProgressCallback callback) {
        try {
            Object downloadUrlObj = version.getMetadata().get("downloadUrl");
            if (downloadUrlObj == null) {
                if (log.isErrorEnabled()) {
                    log.error("No download URL available for plugin: {} version {}",
                            pluginId, version.getVersion());
                }
                return false;
            }

            String downloadUrl = downloadUrlObj.toString();
            Path targetFile = pluginInstallDir.resolve(pluginId + "-" + version.getVersion() + ".jar");

            callback.onProgress(10);

            if (callback.isCancelled()) {
                return false;
            }

            downloadFile(downloadUrl, targetFile);

            callback.onProgress(70);

            if (callback.isCancelled()) {
                return false;
            }

            if (!validateDownloadedFile(targetFile)) {
                Files.deleteIfExists(targetFile);
                return false;
            }

            callback.onProgress(100);
            return true;

        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Download and install failed for plugin: {}", pluginId, e);
            }
            return false;
        }
    }

    private void downloadFile(String url, Path targetPath) throws IOException {
        Files.createDirectories(targetPath.getParent());
        URI uri = URI.create(url);
        String scheme = uri.getScheme();

        if ("file".equalsIgnoreCase(scheme)) {
            Path sourcePath = Path.of(uri);
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } else {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(uri)
                    .GET()
                    .timeout(Duration.ofSeconds(downloadTimeoutSeconds))
                    .build();

            try {
                HttpResponse<InputStream> response = httpClient.send(request,
                        HttpResponse.BodyHandlers.ofInputStream());

                if (response.statusCode() != 200) {
                    throw new IOException("Download failed with HTTP status: " + response.statusCode());
                }

                try (InputStream body = response.body()) {
                    Files.copy(body, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Download interrupted", e);
            }
        }
    }

    private boolean validateDownloadedFile(Path filePath) {
        try {
            return Files.exists(filePath) && Files.size(filePath) > 0;
        } catch (IOException e) {
            if (log.isErrorEnabled()) {
                log.error("Failed to validate downloaded file: {}", filePath, e);
            }
            return false;
        }
    }

    /**
     * Update task for asynchronous update installation.
     */
    private class UpdateTask implements Runnable {
        private final String pluginId;
        private final PluginVersion targetVersion;
        private volatile boolean cancelled = false;

        UpdateTask(String pluginId, PluginVersion targetVersion) {
            this.pluginId = pluginId;
            this.targetVersion = targetVersion;
        }

        @Override
        public void run() {
            try {
                boolean success = downloadAndInstall(pluginId, targetVersion, new UpdateProgressCallback() {
                    @Override
                    public void onProgress(int progress) {
                        if (!cancelled) {
                            updateProgress.put(pluginId, progress);
                        }
                    }

                    @Override
                    public boolean isCancelled() {
                        return cancelled;
                    }
                });

                if (success) {
                    currentVersions.put(pluginId, targetVersion.getVersion());

                    versionHistory.computeIfAbsent(pluginId, k -> new CopyOnWriteArrayList<>())
                            .add(0, targetVersion);

                    updateStatuses.put(pluginId, UpdateStatus.INSTALLED);
                    updateCount.incrementAndGet();
                    successCount.incrementAndGet();

                    if (log.isInfoEnabled()) {
                        log.info("Update installed successfully for plugin: {} to version {}",
                                pluginId, targetVersion.getVersion());
                    }

                    notifyRestartRequired(pluginId);
                } else if (!cancelled) {
                    updateStatuses.put(pluginId, UpdateStatus.FAILED);
                    failedCount.incrementAndGet();
                    if (log.isErrorEnabled()) {
                        log.error("Update installation failed for plugin: {}", pluginId);
                    }
                }

            } finally {
                activeUpdates.remove(pluginId);
                updateProgress.remove(pluginId);
            }
        }

        void cancel() {
            this.cancelled = true;
        }
    }

    private void notifyRestartRequired(String pluginId) {
        if (log.isInfoEnabled()) {
            log.info("Plugin {} requires restart to complete update", pluginId);
        }
    }

    /**
     * Callback interface for update progress.
     */
    private interface UpdateProgressCallback {
        void onProgress(int progress);

        boolean isCancelled();
    }

    /**
     * Implementation of {@link PluginVersion}.
     */
    public static class PluginVersionImpl implements PluginVersion {
        private final String version;
        private final String description;
        private final String releaseDate;
        private final List<String> changelog;
        private final List<String> newFeatures;
        private final List<String> bugFixes;
        private final Map<String, Object> metadata;

        public PluginVersionImpl(String version, String description, String releaseDate,
                                 List<String> changelog, List<String> newFeatures,
                                 List<String> bugFixes, Map<String, Object> metadata) {
            this.version = version;
            this.description = description;
            this.releaseDate = releaseDate;
            this.changelog = changelog != null ? new CopyOnWriteArrayList<>(changelog) : new CopyOnWriteArrayList<>();
            this.newFeatures = newFeatures != null ? new CopyOnWriteArrayList<>(newFeatures) : new CopyOnWriteArrayList<>();
            this.bugFixes = bugFixes != null ? new CopyOnWriteArrayList<>(bugFixes) : new CopyOnWriteArrayList<>();
            this.metadata = metadata != null ? new ConcurrentHashMap<>(metadata) : new ConcurrentHashMap<>();
        }

        @Override
        public String getVersion() {
            return version;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public String getReleaseDate() {
            return releaseDate;
        }

        @Override
        public List<String> getChangelog() {
            return changelog;
        }

        @Override
        public List<String> getNewFeatures() {
            return newFeatures;
        }

        @Override
        public List<String> getBugFixes() {
            return bugFixes;
        }

        @Override
        public Map<String, Object> getMetadata() {
            return metadata;
        }

        @Override
        public String toString() {
            return String.format("PluginVersion{version='%s', releaseDate='%s', features=%d, fixes=%d}",
                    version, releaseDate, newFeatures.size(), bugFixes.size());
        }
    }
}
