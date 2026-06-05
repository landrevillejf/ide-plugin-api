package com.protonmail.landrevillejf.swingide.plugin.service;

import org.junit.jupiter.api.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * Tests for PluginLoggingService interface
 */
@DisplayName("PluginLoggingService Tests")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class PluginLoggingServiceTests {

    private PluginLoggingService loggingService;
    private static final String PLUGIN_ID = "test-plugin";

    @BeforeEach
    void setUp() {
        loggingService = new MockPluginLoggingService();
    }

    @Test
    @DisplayName("should set and get log level for plugin")
    void test_set_and_get_log_level() {
        loggingService.setLogLevel(PLUGIN_ID, PluginLoggingService.LogLevel.DEBUG);

        PluginLoggingService.LogLevel level = loggingService.getLogLevel(PLUGIN_ID);

        assertThat(level).isEqualTo(PluginLoggingService.LogLevel.DEBUG);
    }

    @Test
    @DisplayName("should log messages at different levels")
    void test_log_messages_at_different_levels() {
        assertThatNoException().isThrownBy(() -> {
            loggingService.log(PLUGIN_ID, PluginLoggingService.LogLevel.INFO, "Info message");
            loggingService.log(PLUGIN_ID, PluginLoggingService.LogLevel.DEBUG, "Debug message");
            loggingService.log(PLUGIN_ID, PluginLoggingService.LogLevel.ERROR, "Error message");
        });
    }

    @Test
    @DisplayName("should log messages with throwable")
    void test_log_messages_with_throwable() {
        Throwable throwable = new RuntimeException("Test error");

        assertThatNoException().isThrownBy(() ->
            loggingService.log(PLUGIN_ID, PluginLoggingService.LogLevel.ERROR, "Error occurred", throwable)
        );
    }

    @Test
    @DisplayName("should support formatted logging")
    void test_formatted_logging() {
        assertThatNoException().isThrownBy(() ->
            loggingService.logf(PLUGIN_ID, PluginLoggingService.LogLevel.INFO, "User %s logged in", "john")
        );
    }

    @Test
    @DisplayName("should retrieve recent logs")
    void test_retrieve_recent_logs() {
        loggingService.log(PLUGIN_ID, PluginLoggingService.LogLevel.INFO, "Message 1");
        loggingService.log(PLUGIN_ID, PluginLoggingService.LogLevel.INFO, "Message 2");

        java.util.List<String> recentLogs = loggingService.getRecentLogs(PLUGIN_ID, 10);

        assertThat(recentLogs).isNotNull();
    }

    @Test
    @DisplayName("should clear logs for plugin")
    void test_clear_logs() {
        loggingService.log(PLUGIN_ID, PluginLoggingService.LogLevel.INFO, "Message");

        assertThatNoException().isThrownBy(() -> loggingService.clearLogs(PLUGIN_ID));
    }

    @Test
    @DisplayName("should get logging statistics")
    void test_get_logging_statistics() {
        loggingService.log(PLUGIN_ID, PluginLoggingService.LogLevel.INFO, "Message");

        Map<String, Object> stats = loggingService.getStatistics(PLUGIN_ID);

        assertThat(stats).isNotNull().isNotEmpty();
    }

    // Mock implementation for testing
    public static class MockPluginLoggingService implements PluginLoggingService {
        private final Map<String, LogLevel> logLevels = new java.util.HashMap<>();
        private final Map<String, java.util.List<String>> logs = new java.util.HashMap<>();

        @Override
        public void setLogLevel(String pluginId, LogLevel level) {
            logLevels.put(pluginId, level);
        }

        @Override
        public LogLevel getLogLevel(String pluginId) {
            return logLevels.getOrDefault(pluginId, LogLevel.INFO);
        }

        @Override
        public void log(String pluginId, LogLevel level, String message) {
            logs.computeIfAbsent(pluginId, k -> new java.util.ArrayList<>())
                .add("[" + level + "] " + message);
        }

        @Override
        public void log(String pluginId, LogLevel level, String message, Throwable cause) {
            log(pluginId, level, message + " - " + cause.getMessage());
        }

        @Override
        public void logf(String pluginId, LogLevel level, String format, Object... args) {
            log(pluginId, level, String.format(format, args));
        }

        @Override
        public void clearLogs(String pluginId) {
            logs.remove(pluginId);
        }

        @Override
        public java.util.List<String> getRecentLogs(String pluginId, int maxLines) {
            return logs.getOrDefault(pluginId, java.util.Collections.emptyList());
        }

        @Override
        public void setConsoleOutput(String pluginId, boolean enabled) {}

        @Override
        public void setFileOutput(String pluginId, boolean enabled, String filePath) {}

        @Override
        public Map<String, Object> getStatistics(String pluginId) {
            Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("logCount", logs.getOrDefault(pluginId, java.util.Collections.emptyList()).size());
            return stats;
        }
    }
}

