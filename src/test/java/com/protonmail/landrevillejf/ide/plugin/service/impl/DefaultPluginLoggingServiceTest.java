package com.protonmail.landrevillejf.ide.plugin.service.impl;

import com.protonmail.landrevillejf.ide.plugin.service.PluginLoggingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DefaultPluginLoggingServiceTest {

    private DefaultPluginLoggingService loggingService;
    private static final String TEST_PLUGIN = "test-plugin";
    private static final String TEST_PLUGIN_2 = "test-plugin-2";

    @BeforeEach
    void setUp() {
        loggingService = new DefaultPluginLoggingService();
    }

    @Test
    void setLogLevel() {
        loggingService.setLogLevel(TEST_PLUGIN, PluginLoggingService.LogLevel.DEBUG);

        assertEquals(PluginLoggingService.LogLevel.DEBUG, loggingService.getLogLevel(TEST_PLUGIN));
    }

    @Test
    void getLogLevel() {
        // Default level should be INFO
        assertEquals(PluginLoggingService.LogLevel.INFO, loggingService.getLogLevel(TEST_PLUGIN));

        loggingService.setLogLevel(TEST_PLUGIN, PluginLoggingService.LogLevel.ERROR);
        assertEquals(PluginLoggingService.LogLevel.ERROR, loggingService.getLogLevel(TEST_PLUGIN));
    }

    @Test
    void log() {
        loggingService.setLogLevel(TEST_PLUGIN, PluginLoggingService.LogLevel.DEBUG);

        loggingService.log(TEST_PLUGIN, PluginLoggingService.LogLevel.INFO, "Test message");

        List<String> logs = loggingService.getRecentLogs(TEST_PLUGIN, 10);
        assertFalse(logs.isEmpty());
        assertTrue(logs.get(0).contains("Test message"));
        assertTrue(logs.get(0).contains("INFO"));
    }

    @Test
    void logWithLevelBelowThreshold() {
        loggingService.setLogLevel(TEST_PLUGIN, PluginLoggingService.LogLevel.ERROR);

        loggingService.log(TEST_PLUGIN, PluginLoggingService.LogLevel.DEBUG, "This should not appear");

        List<String> logs = loggingService.getRecentLogs(TEST_PLUGIN, 10);
        // Should be empty because DEBUG < ERROR
        assertTrue(logs.stream().noneMatch(log -> log.contains("This should not appear")));
    }

    @Test
    void testLogWithThrowable() {
        RuntimeException testException = new RuntimeException("Test error");

        loggingService.log(TEST_PLUGIN, PluginLoggingService.LogLevel.ERROR, "Error message", testException);

        List<String> logs = loggingService.getRecentLogs(TEST_PLUGIN, 10);
        assertFalse(logs.isEmpty());
        assertTrue(logs.get(0).contains("Error message"));
        assertTrue(logs.get(0).contains("Test error"));
    }

    @Test
    void logf() {
        loggingService.setLogLevel(TEST_PLUGIN, PluginLoggingService.LogLevel.INFO);

        loggingService.logf(TEST_PLUGIN, PluginLoggingService.LogLevel.INFO,
                "User %s has %d messages", "john", 42);

        List<String> logs = loggingService.getRecentLogs(TEST_PLUGIN, 10);
        assertFalse(logs.isEmpty());
        assertTrue(logs.get(0).contains("User john has 42 messages"));
    }

    @Test
    void clearLogs() {
        loggingService.log(TEST_PLUGIN, PluginLoggingService.LogLevel.INFO, "Message 1");
        loggingService.log(TEST_PLUGIN, PluginLoggingService.LogLevel.INFO, "Message 2");

        assertFalse(loggingService.getRecentLogs(TEST_PLUGIN, 10).isEmpty());

        loggingService.clearLogs(TEST_PLUGIN);

        assertTrue(loggingService.getRecentLogs(TEST_PLUGIN, 10).isEmpty());
    }

    @Test
    void getRecentLogs() {
        for (int i = 1; i <= 20; i++) {
            loggingService.log(TEST_PLUGIN, PluginLoggingService.LogLevel.INFO, "Message " + i);
        }

        List<String> recent = loggingService.getRecentLogs(TEST_PLUGIN, 5);

        assertEquals(5, recent.size());
        assertTrue(recent.get(0).contains("Message 16"));
        assertTrue(recent.get(4).contains("Message 20"));
    }

    @Test
    void getRecentLogsForUnknownPlugin() {
        List<String> logs = loggingService.getRecentLogs("unknown-plugin", 10);

        assertTrue(logs.isEmpty());
    }

    @Test
    void setConsoleOutput() {
        // Should not throw exception
        loggingService.setConsoleOutput(TEST_PLUGIN, false);
        loggingService.setConsoleOutput(TEST_PLUGIN, true);

        loggingService.log(TEST_PLUGIN, PluginLoggingService.LogLevel.INFO, "Test with console disabled");
    }

    @Test
    void setFileOutput(@TempDir Path tempDir) throws IOException, InterruptedException {
        Path logFile = tempDir.resolve("test-plugin.log");

        loggingService.setFileOutput(TEST_PLUGIN, true, logFile.toString());

        loggingService.log(TEST_PLUGIN, PluginLoggingService.LogLevel.INFO, "File log message");

        // Wait a bit for file writing
        Thread.sleep(100);

        assertTrue(Files.exists(logFile));
        String content = Files.readString(logFile);
        assertTrue(content.contains("File log message"));
    }

    @Test
    void setFileOutputDisable(@TempDir Path tempDir) throws IOException, InterruptedException {
        Path logFile = tempDir.resolve("test-plugin.log");

        loggingService.setFileOutput(TEST_PLUGIN, true, logFile.toString());
        loggingService.log(TEST_PLUGIN, PluginLoggingService.LogLevel.INFO, "First message");

        loggingService.setFileOutput(TEST_PLUGIN, false, null);
        loggingService.log(TEST_PLUGIN, PluginLoggingService.LogLevel.INFO, "Second message");

        Thread.sleep(100);

        String content = Files.readString(logFile);
        assertTrue(content.contains("First message"));
        assertFalse(content.contains("Second message"));
    }

    @Test
    void getStatistics() {
        // Set log level to DEBUG to capture all messages
        loggingService.setLogLevel(TEST_PLUGIN, PluginLoggingService.LogLevel.DEBUG);

        loggingService.log(TEST_PLUGIN, PluginLoggingService.LogLevel.INFO, "Info message");
        loggingService.log(TEST_PLUGIN, PluginLoggingService.LogLevel.DEBUG, "Debug message");
        loggingService.log(TEST_PLUGIN, PluginLoggingService.LogLevel.ERROR, "Error message");
        loggingService.log(TEST_PLUGIN, PluginLoggingService.LogLevel.WARN, "Warn message");

        Map<String, Object> stats = loggingService.getStatistics(TEST_PLUGIN);

        assertNotNull(stats);
        assertTrue(stats.containsKey("counts"));
        assertTrue(stats.containsKey("lastLogTime"));
        assertTrue(stats.containsKey("totalLogs"));

        @SuppressWarnings("unchecked")
        Map<String, Integer> counts = (Map<String, Integer>) stats.get("counts");

        // Use getOrDefault to handle null values
        assertEquals(1, counts.getOrDefault("INFO", 0));
        assertEquals(1, counts.getOrDefault("DEBUG", 0));
        assertEquals(1, counts.getOrDefault("ERROR", 0));
        assertEquals(1, counts.getOrDefault("WARN", 0));
        assertEquals(4, stats.get("totalLogs"));
    }

    @Test
    void getStatisticsForUnknownPlugin() {
        Map<String, Object> stats = loggingService.getStatistics("unknown-plugin");

        assertTrue(stats.isEmpty());
    }

    @Test
    void multiplePluginsIsolation() {
        loggingService.setLogLevel(TEST_PLUGIN, PluginLoggingService.LogLevel.DEBUG);
        loggingService.setLogLevel(TEST_PLUGIN_2, PluginLoggingService.LogLevel.ERROR);

        loggingService.log(TEST_PLUGIN, PluginLoggingService.LogLevel.DEBUG, "Debug from plugin 1");
        loggingService.log(TEST_PLUGIN_2, PluginLoggingService.LogLevel.DEBUG, "Debug from plugin 2");
        loggingService.log(TEST_PLUGIN_2, PluginLoggingService.LogLevel.ERROR, "Error from plugin 2");

        List<String> logs1 = loggingService.getRecentLogs(TEST_PLUGIN, 10);
        List<String> logs2 = loggingService.getRecentLogs(TEST_PLUGIN_2, 10);

        assertTrue(logs1.stream().anyMatch(log -> log.contains("Debug from plugin 1")));
        assertFalse(logs2.stream().anyMatch(log -> log.contains("Debug from plugin 2")));
        assertTrue(logs2.stream().anyMatch(log -> log.contains("Error from plugin 2")));
    }

    @Test
    void logAllLevels() {
        loggingService.setLogLevel(TEST_PLUGIN, PluginLoggingService.LogLevel.TRACE);

        loggingService.log(TEST_PLUGIN, PluginLoggingService.LogLevel.TRACE, "TRACE message");
        loggingService.log(TEST_PLUGIN, PluginLoggingService.LogLevel.DEBUG, "DEBUG message");
        loggingService.log(TEST_PLUGIN, PluginLoggingService.LogLevel.INFO, "INFO message");
        loggingService.log(TEST_PLUGIN, PluginLoggingService.LogLevel.WARN, "WARN message");
        loggingService.log(TEST_PLUGIN, PluginLoggingService.LogLevel.ERROR, "ERROR message");
        loggingService.log(TEST_PLUGIN, PluginLoggingService.LogLevel.FATAL, "FATAL message");

        List<String> logs = loggingService.getRecentLogs(TEST_PLUGIN, 20);

        assertEquals(6, logs.size());
        assertTrue(logs.stream().anyMatch(log -> log.contains("TRACE")));
        assertTrue(logs.stream().anyMatch(log -> log.contains("DEBUG")));
        assertTrue(logs.stream().anyMatch(log -> log.contains("INFO")));
        assertTrue(logs.stream().anyMatch(log -> log.contains("WARN")));
        assertTrue(logs.stream().anyMatch(log -> log.contains("ERROR")));
        assertTrue(logs.stream().anyMatch(log -> log.contains("FATAL")));
    }

    @Test
    void defaultLogLevelIsInfo() {
        loggingService.log(TEST_PLUGIN, PluginLoggingService.LogLevel.DEBUG, "Debug message");
        loggingService.log(TEST_PLUGIN, PluginLoggingService.LogLevel.INFO, "Info message");

        List<String> logs = loggingService.getRecentLogs(TEST_PLUGIN, 10);

        assertFalse(logs.stream().anyMatch(log -> log.contains("Debug message")));
        assertTrue(logs.stream().anyMatch(log -> log.contains("Info message")));
    }
}