package com.protonmail.landrevillejf.ide.plugin.service.impl;

import com.protonmail.landrevillejf.ide.plugin.service.PluginUpdateService;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class DefaultPluginUpdateService implements PluginUpdateService {

    private final Map<String, PluginVersion> latestVersions = new ConcurrentHashMap<>();
    private final Map<String, UpdateStatus> updateStatuses = new ConcurrentHashMap<>();
    private final Map<String, Integer> updateProgress = new ConcurrentHashMap<>();
    private final Map<String, UpdateChannel> updateChannels = new ConcurrentHashMap<>();
    private final Map<String, Boolean> autoUpdateEnabled = new ConcurrentHashMap<>();
    private final Map<String, List<PluginVersion>> versionHistory = new ConcurrentHashMap<>();
    private final Map<String, UpdateTask> activeUpdates = new ConcurrentHashMap<>();
    private final Map<String, String> currentVersions = new ConcurrentHashMap<>();

    private final ExecutorService updateExecutor = Executors.newCachedThreadPool();
    private final AtomicInteger updateCount = new AtomicInteger(0);
    private final AtomicInteger successCount = new AtomicInteger(0);
    private final AtomicInteger failedCount = new AtomicInteger(0);

    private String updateServerUrl = "https://api.ide.com/plugins";

    public DefaultPluginUpdateService() {
        if (log.isInfoEnabled()) {
            log.info("DefaultPluginUpdateService initialized");
        }

        // Start background update checker
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::checkAllForUpdates, 24, 24, TimeUnit.HOURS);
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

            updateStatuses.put(pluginId, null);
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

        // Perform rollback
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
            // Schedule immediate check
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
        stats.put("successRate", updateCount.get() > 0 ?
                (double) successCount.get() / updateCount.get() * 100 : 0);
        stats.put("activeUpdates", activeUpdates.size());
        stats.put("pluginsWithUpdates", latestVersions.size());

        // Per plugin status
        Map<String, String> pluginStatuses = new LinkedHashMap<>();
        for (Map.Entry<String, UpdateStatus> entry : updateStatuses.entrySet()) {
            if (entry.getValue() != null) {
                pluginStatuses.put(entry.getKey(), entry.getValue().name());
            }
        }
        stats.put("pluginStatuses", pluginStatuses);

        return stats;
    }

    /**
     * Sets the current version of a plugin (called by plugin manager).
     */
    public void setPluginVersion(String pluginId, String version) {
        currentVersions.put(pluginId, version);

        // Add to version history
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
     */
    public void setUpdateServerUrl(String url) {
        this.updateServerUrl = url;
        if (log.isInfoEnabled()) {
            log.info("Update server URL set to: {}", url);
        }
    }

    private void checkAllForUpdates() {
        for (String pluginId : currentVersions.keySet()) {
            if (isAutoUpdateEnabled(pluginId)) {
                checkAndAutoUpdate(pluginId);
            }
        }
    }

    public void checkAndAutoUpdate(String pluginId) {
        PluginVersion update = checkForUpdates(pluginId);
        if (update != null && isAutoUpdateEnabled(pluginId)) {
            if (log.isInfoEnabled()) {
                log.info("Auto-update triggered for plugin: {} to version {}", pluginId, update.getVersion());
            }
            installUpdate(pluginId, update.getVersion());
        }
    }

    private PluginVersion fetchLatestVersion(String pluginId, UpdateChannel channel) throws Exception {
        URL url = new URL(String.format("%s/%s/latest?channel=%s", updateServerUrl, pluginId, channel.name()));
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
            // In a real implementation, parse JSON response
            // For now, return a mock response
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                // Parse JSON here
                return parseVersionResponse(reader);
            }
        }

        return null;
    }

    private PluginVersion parseVersionResponse(BufferedReader reader) throws IOException {
        // Mock implementation - in real code, parse JSON
        return new PluginVersionImpl(
                "1.1.0",
                "Added new features and bug fixes",
                "2024-01-15",
                Arrays.asList("Fixed memory leak", "Improved performance"),
                Arrays.asList("New API endpoints", "Better error handling"),
                Arrays.asList("Crash on startup", "UI glitches"),
                Collections.singletonMap("downloadUrl", "https://example.com/plugin.jar")
        );
    }

    private boolean isNewerVersion(String version1, String version2) {
        if (version2 == null) return true;

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
        // Mock implementation - in real code, download and install previous version
        try {
            // Simulate rollback
            Thread.sleep(1000);
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    private boolean downloadAndInstall(String pluginId, PluginVersion version,
                                       UpdateProgressCallback callback) {
        // Mock implementation - in real code, download from URL and install
        try {
            for (int i = 0; i <= 100; i += 10) {
                if (callback.isCancelled()) {
                    return false;
                }
                callback.onProgress(i);
                Thread.sleep(100);
            }
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * Update task for asynchronous update installation
     */
    private class UpdateTask implements Runnable {
        private final String pluginId;
        private final PluginVersion targetVersion;
        private volatile boolean cancelled = false;

        public UpdateTask(String pluginId, PluginVersion targetVersion) {
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

                if (success && !cancelled) {
                    // Update current version
                    currentVersions.put(pluginId, targetVersion.getVersion());

                    // Add to history
                    versionHistory.computeIfAbsent(pluginId, k -> new CopyOnWriteArrayList<>())
                            .add(0, targetVersion);

                    updateStatuses.put(pluginId, UpdateStatus.INSTALLED);
                    updateCount.incrementAndGet();
                    successCount.incrementAndGet();

                    if (log.isInfoEnabled()) {
                        log.info("Update installed successfully for plugin: {} to version {}",
                                pluginId, targetVersion.getVersion());
                    }

                    // Notify that plugin needs restart
                    notifyRestartRequired(pluginId);
                } else if (!cancelled) {
                    updateStatuses.put(pluginId, UpdateStatus.FAILED);
                    failedCount.incrementAndGet();
                    if (log.isErrorEnabled()) {
                        log.error("Update installation failed for plugin: {}", pluginId);
                    }
                }

            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error("Error during update installation for plugin: {}", pluginId, e);
                }
                updateStatuses.put(pluginId, UpdateStatus.FAILED);
                failedCount.incrementAndGet();
            } finally {
                activeUpdates.remove(pluginId);
                updateProgress.remove(pluginId);
            }
        }

        public void cancel() {
            this.cancelled = true;
        }
    }

    private void notifyRestartRequired(String pluginId) {
        if (log.isInfoEnabled()) {
            log.info("Plugin {} requires restart to complete update", pluginId);
        }
        // In real implementation, show notification to user
    }

    /**
     * Callback interface for update progress
     */
    private interface UpdateProgressCallback {
        void onProgress(int progress);
        boolean isCancelled();
    }

    /**
     * Implementation of PluginVersion
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
        public String getVersion() { return version; }

        @Override
        public String getDescription() { return description; }

        @Override
        public String getReleaseDate() { return releaseDate; }

        @Override
        public List<String> getChangelog() { return changelog; }

        @Override
        public List<String> getNewFeatures() { return newFeatures; }

        @Override
        public List<String> getBugFixes() { return bugFixes; }

        @Override
        public Map<String, Object> getMetadata() { return metadata; }

        @Override
        public String toString() {
            return String.format("PluginVersion{version='%s', releaseDate='%s', features=%d, fixes=%d}",
                    version, releaseDate, newFeatures.size(), bugFixes.size());
        }
    }
}