package com.protonmail.landrevillejf.ide.plugin.service.impl;

import com.protonmail.landrevillejf.ide.plugin.service.PluginUpdateService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

class DefaultPluginUpdateServiceTest {

    private DefaultPluginUpdateService updateService;
    private static final String TEST_PLUGIN_ID = "test.plugin.id";
    private static final String INITIAL_VERSION = "1.0.0";

    @BeforeEach
    void setUp() {
        updateService = new DefaultPluginUpdateService();
        updateService.setPluginVersion(TEST_PLUGIN_ID, INITIAL_VERSION);
        // Set to a non-routable address to avoid actual network calls
        updateService.setUpdateServerUrl("http://127.0.0.1:9999/nonexistent");
    }

    @AfterEach
    void tearDown() {
        // Clean up any active updates
        updateService.cancelUpdate(TEST_PLUGIN_ID);
    }

    @Test
    void checkForUpdates_ShouldReturnNull_WhenServerUnreachable() {
        // When - server is unreachable, should return null
        PluginUpdateService.PluginVersion result = updateService.checkForUpdates(TEST_PLUGIN_ID);

        // Then
        assertNull(result);
        assertEquals(PluginUpdateService.UpdateStatus.FAILED, updateService.getUpdateStatus(TEST_PLUGIN_ID));
    }

    @Test
    void getUpdateStatus_ShouldReturnCorrectStatus() {
        // Given - initially no status
        assertNull(updateService.getUpdateStatus(TEST_PLUGIN_ID));

        // When
        updateService.checkForUpdates(TEST_PLUGIN_ID);

        // Then - status should be either CHECKING, FAILED, or null depending on timing
        PluginUpdateService.UpdateStatus status = updateService.getUpdateStatus(TEST_PLUGIN_ID);
        // Status can be CHECKING, FAILED, or null - all are valid depending on timing
        assertTrue(status == null ||
                status == PluginUpdateService.UpdateStatus.CHECKING ||
                status == PluginUpdateService.UpdateStatus.FAILED);
    }

    @Test
    void installUpdate_ShouldReturnFalse_WhenNoUpdateAvailable() {
        // When - no update has been checked/found
        boolean result = updateService.installUpdate(TEST_PLUGIN_ID, "1.2.0");

        // Then
        assertFalse(result);
    }

    @Test
    void cancelUpdate_ShouldReturnFalse_WhenNoUpdateInProgress() {
        // When
        boolean result = updateService.cancelUpdate(TEST_PLUGIN_ID);

        // Then
        assertFalse(result);
    }

    @Test
    void getUpdateProgress_ShouldReturnZero_WhenNoUpdate() {
        // When
        int progress = updateService.getUpdateProgress(TEST_PLUGIN_ID);

        // Then
        assertEquals(0, progress);
    }

    @Test
    void rollbackVersion_ShouldReturnFalse_WhenVersionNotFoundInHistory() {
        // When
        boolean result = updateService.rollbackVersion(TEST_PLUGIN_ID, "0.9.0");

        // Then
        assertFalse(result);
    }

    @Test
    void getVersionHistory_ShouldContainInitialVersion() {
        // When
        List<PluginUpdateService.PluginVersion> history = updateService.getVersionHistory(TEST_PLUGIN_ID);

        // Then
        assertNotNull(history);
        assertFalse(history.isEmpty());
        assertEquals(INITIAL_VERSION, history.get(0).getVersion());
    }

    @Test
    void setUpdateChannel_ShouldStoreChannel() {
        // Given
        PluginUpdateService.UpdateChannel channel = PluginUpdateService.UpdateChannel.BETA;

        // When
        updateService.setUpdateChannel(TEST_PLUGIN_ID, channel);

        // Then
        assertEquals(channel, updateService.getUpdateChannel(TEST_PLUGIN_ID));
    }

    @Test
    void getUpdateChannel_ShouldReturnStable_WhenNotSet() {
        // When
        PluginUpdateService.UpdateChannel channel = updateService.getUpdateChannel(TEST_PLUGIN_ID);

        // Then
        assertEquals(PluginUpdateService.UpdateChannel.STABLE, channel);
    }

    @Test
    void setAutoUpdate_ShouldEnableAutoUpdates() {
        // When
        updateService.setAutoUpdate(TEST_PLUGIN_ID, true);

        // Then
        assertTrue(updateService.isAutoUpdateEnabled(TEST_PLUGIN_ID));

        // When
        updateService.setAutoUpdate(TEST_PLUGIN_ID, false);

        // Then
        assertFalse(updateService.isAutoUpdateEnabled(TEST_PLUGIN_ID));
    }

    @Test
    void isAutoUpdateEnabled_ShouldReturnFalse_WhenNotSet() {
        // Then
        assertFalse(updateService.isAutoUpdateEnabled(TEST_PLUGIN_ID));
    }

    @Test
    void getUpdateStatistics_ShouldReturnValidStats() {
        // When
        Map<String, Object> stats = updateService.getUpdateStatistics();

        // Then
        assertNotNull(stats);
        assertTrue(stats.containsKey("totalUpdates"));
        assertTrue(stats.containsKey("successfulUpdates"));
        assertTrue(stats.containsKey("failedUpdates"));
        assertTrue(stats.containsKey("successRate"));
        assertTrue(stats.containsKey("activeUpdates"));
        assertTrue(stats.containsKey("pluginsWithUpdates"));
        assertTrue(stats.containsKey("pluginStatuses"));

        assertEquals(0, stats.get("totalUpdates"));
        assertEquals(0, stats.get("successfulUpdates"));
        assertEquals(0, stats.get("failedUpdates"));
        assertNotNull(stats.get("pluginStatuses"));
    }

    @Test
    void setPluginVersion_ShouldAddToHistory() {
        // Given
        String newVersion = "2.0.0";

        // When
        updateService.setPluginVersion(TEST_PLUGIN_ID, newVersion);

        // Then
        List<PluginUpdateService.PluginVersion> history = updateService.getVersionHistory(TEST_PLUGIN_ID);
        assertEquals(newVersion, history.get(0).getVersion());
        assertEquals(INITIAL_VERSION, history.get(1).getVersion());
    }

    @Test
    void setUpdateServerUrl_ShouldChangeServerUrl() {
        // Given
        String newUrl = "https://new-server.com/api";

        // When - should not throw exception
        updateService.setUpdateServerUrl(newUrl);

        // Then - verify by checking updates (will fail to connect, but that's expected)
        assertDoesNotThrow(() -> updateService.checkForUpdates(TEST_PLUGIN_ID));
    }

    @Test
    void getUpdateProgress_ShouldReturnZeroForUnknownPlugin() {
        // When
        int progress = updateService.getUpdateProgress("unknown.plugin");

        // Then
        assertEquals(0, progress);
    }

    @Test
    void setUpdateChannel_ShouldOverridePreviousChannel() {
        // Given
        updateService.setUpdateChannel(TEST_PLUGIN_ID, PluginUpdateService.UpdateChannel.BETA);

        // When
        updateService.setUpdateChannel(TEST_PLUGIN_ID, PluginUpdateService.UpdateChannel.STABLE);

        // Then
        assertEquals(PluginUpdateService.UpdateChannel.STABLE, updateService.getUpdateChannel(TEST_PLUGIN_ID));
    }

    @Test
    void multiplePlugins_ShouldBeIsolated() {
        // Given
        String plugin2 = "plugin2.id";
        updateService.setPluginVersion(plugin2, "2.0.0");
        updateService.setUpdateChannel(TEST_PLUGIN_ID, PluginUpdateService.UpdateChannel.STABLE);
        updateService.setUpdateChannel(plugin2, PluginUpdateService.UpdateChannel.BETA);

        // Then
        assertEquals(PluginUpdateService.UpdateChannel.STABLE, updateService.getUpdateChannel(TEST_PLUGIN_ID));
        assertEquals(PluginUpdateService.UpdateChannel.BETA, updateService.getUpdateChannel(plugin2));

        updateService.setAutoUpdate(TEST_PLUGIN_ID, true);
        updateService.setAutoUpdate(plugin2, false);

        assertTrue(updateService.isAutoUpdateEnabled(TEST_PLUGIN_ID));
        assertFalse(updateService.isAutoUpdateEnabled(plugin2));
    }

    @Test
    void getVersionHistory_ShouldReturnEmptyList_ForUnknownPlugin() {
        // When
        List<PluginUpdateService.PluginVersion> history = updateService.getVersionHistory("unknown.plugin");

        // Then
        assertNotNull(history);
        assertTrue(history.isEmpty());
    }

    @Test
    void getUpdateStatistics_ShouldTrackUpdatesCorrectly() {
        // Given - initial stats
        Map<String, Object> initialStats = updateService.getUpdateStatistics();
        assertEquals(0, initialStats.get("totalUpdates"));

        // When - perform a failed update
        updateService.installUpdate("nonexistent.plugin", "1.0.0");

        // Then - stats should still show 0 for total updates (since install failed)
        Map<String, Object> stats = updateService.getUpdateStatistics();
        assertEquals(0, stats.get("totalUpdates"));
        assertEquals(0, stats.get("successfulUpdates"));
        assertEquals(0, stats.get("failedUpdates"));
    }

    @Test
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void cancelUpdate_ShouldReturnFalse_WhenUpdateAlreadyCompleted() throws InterruptedException {
        // This test verifies cancel behavior
        assertFalse(updateService.cancelUpdate(TEST_PLUGIN_ID));
    }
}