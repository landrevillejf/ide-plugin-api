package com.protonmail.landrevillejf.ide.plugin.service.impl;

import com.protonmail.landrevillejf.ide.plugin.service.PluginLoggingService.LogLevel;
import com.protonmail.landrevillejf.ide.plugin.utils.LogCapture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Mutation-killing tests for {@link DefaultPluginLoggingService}.
 */
@DisplayName("DefaultPluginLoggingService mutation tests")
class DefaultPluginLoggingServiceMutationTest {

    private static final String P = "log-plugin";

    private DefaultPluginLoggingService service;

    @BeforeEach
    void setUp() {
        service = new DefaultPluginLoggingService();
    }

    @Nested
    @DisplayName("log levels")
    class LevelTests {

        @Test
        @DisplayName("default level is INFO and can be changed")
        void levelDefaults() {
            assertThat(service.getLogLevel(P)).isEqualTo(LogLevel.INFO);

            try (LogCapture capture = LogCapture.attach(DefaultPluginLoggingService.class)) {
                service.setLogLevel(P, LogLevel.DEBUG);
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("Set log level") && m.contains("DEBUG"));
            }
            assertThat(service.getLogLevel(P)).isEqualTo(LogLevel.DEBUG);
        }

        @Test
        @DisplayName("messages below the configured level are dropped")
        void levelFiltering() {
            service.setLogLevel(P, LogLevel.WARN);
            service.setConsoleOutput(P, false);

            service.log(P, LogLevel.INFO, "dropped");
            service.log(P, LogLevel.WARN, "kept");     // boundary: equal level passes
            service.log(P, LogLevel.ERROR, "also kept");

            List<String> logs = service.getRecentLogs(P, 10);
            assertThat(logs).hasSize(2);
            assertThat(logs.get(0)).contains("[WARN] kept");
            assertThat(logs.get(1)).contains("[ERROR] also kept");
        }
    }

    @Nested
    @DisplayName("console output")
    class ConsoleTests {

        @Test
        @DisplayName("INFO goes to stdout, ERROR and FATAL go to stderr")
        void consoleRouting() {
            service.setConsoleOutput(P, true);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayOutputStream err = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            PrintStream originalErr = System.err;
            try {
                System.setOut(new PrintStream(out, true));
                System.setErr(new PrintStream(err, true));
                service.log(P, LogLevel.INFO, "to-out");
                service.log(P, LogLevel.ERROR, "to-err");
                service.log(P, LogLevel.FATAL, "to-err-too");
            } finally {
                System.setOut(originalOut);
                System.setErr(originalErr);
            }

            assertThat(out.toString()).contains("[log-plugin] [INFO] to-out");
            assertThat(err.toString())
                    .contains("[log-plugin] [ERROR] to-err")
                    .contains("[log-plugin] [FATAL] to-err-too");
        }

        @Test
        @DisplayName("console output can be disabled per plugin")
        void consoleDisabled() {
            service.setConsoleOutput(P, false);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;
            try {
                System.setOut(new PrintStream(out, true));
                service.log(P, LogLevel.INFO, "silent");
            } finally {
                System.setOut(originalOut);
            }

            assertThat(out.toString()).doesNotContain("silent");
            assertThat(service.getRecentLogs(P, 10)).hasSize(1);
        }

        @Test
        @DisplayName("setConsoleOutput logs the change")
        void consoleToggleLogs() {
            try (LogCapture capture = LogCapture.attach(DefaultPluginLoggingService.class)) {
                service.setConsoleOutput(P, false);
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("Set console output") && m.contains("false"));
            }
        }
    }

    @Nested
    @DisplayName("recent logs")
    class RecentLogTests {

        @Test
        @DisplayName("recent logs respect the maxLines window")
        void window() {
            service.setConsoleOutput(P, false);
            service.log(P, LogLevel.INFO, "one");
            service.log(P, LogLevel.INFO, "two");
            service.log(P, LogLevel.INFO, "three");

            List<String> window = service.getRecentLogs(P, 2);
            assertThat(window).hasSize(2);
            assertThat(window.get(0)).contains("] two");
            assertThat(window.get(1)).contains("] three");
            assertThat(window.get(1)).contains("[log-plugin] [INFO]");
        }

        @Test
        @DisplayName("entries with a cause embed the stack trace")
        void stackTraceFormatting() {
            service.setConsoleOutput(P, false);
            service.log(P, LogLevel.ERROR, "failed", new RuntimeException("boom"));

            List<String> logs = service.getRecentLogs(P, 10);
            assertThat(logs).hasSize(1);
            assertThat(logs.get(0))
                    .contains("[ERROR] failed - java.lang.RuntimeException: boom")
                    .contains("\n\tat ");
        }

        @Test
        @DisplayName("logf formats the message")
        void logf() {
            service.setConsoleOutput(P, false);
            service.logf(P, LogLevel.INFO, "answer=%d", 42);
            assertThat(service.getRecentLogs(P, 10).get(0)).contains("answer=42");
        }

        @Test
        @DisplayName("unknown or cleared plugins yield empty lists")
        void emptyCases() {
            assertThat(service.getRecentLogs("ghost", 5)).isEmpty();

            service.setConsoleOutput(P, false);
            service.log(P, LogLevel.INFO, "x");
            try (LogCapture capture = LogCapture.attach(DefaultPluginLoggingService.class)) {
                service.clearLogs(P);
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("Cleared logs"));
            }
            assertThat(service.getRecentLogs(P, 5)).isEmpty();

            // clearing an unknown plugin is a silent no-op
            service.clearLogs("ghost");
        }
    }

    @Nested
    @DisplayName("file output")
    class FileOutputTests {

        @TempDir
        Path tempDir;

        @SuppressWarnings("unchecked")
        private Map<String, PrintWriter> writers() throws Exception {
            Field field = DefaultPluginLoggingService.class.getDeclaredField("fileWriters");
            field.setAccessible(true);
            return (Map<String, PrintWriter>) field.get(service);
        }

        @Test
        @DisplayName("enabling file output appends formatted lines")
        void fileOutput() throws Exception {
            Path logFile = tempDir.resolve("plugin.log");
            service.setConsoleOutput(P, false);

            try (LogCapture capture = LogCapture.attach(DefaultPluginLoggingService.class)) {
                service.setFileOutput(P, true, logFile.toString());
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("Enabled file output")
                                && m.contains(logFile.toString()));
            }
            assertThat(writers()).containsKey(P);

            service.log(P, LogLevel.INFO, "filed");
            assertThat(Files.readString(logFile)).contains("[INFO] filed");

            service.setFileOutput(P, false, null);
            assertThat(writers()).isEmpty();

            service.log(P, LogLevel.INFO, "not-filed");
            assertThat(Files.readString(logFile)).doesNotContain("not-filed");

            // disabling twice is a silent no-op
            service.setFileOutput(P, false, null);
        }

        @Test
        @DisplayName("null file path falls back to the default logs directory")
        void defaultPath() throws Exception {
            String pluginId = "log-default-path";
            service.setConsoleOutput(pluginId, false);
            service.setFileOutput(pluginId, true, null);
            assertThat(writers()).containsKey(pluginId);

            service.log(pluginId, LogLevel.INFO, "default-filed");
            Path defaultFile = Path.of("logs", pluginId + ".log");
            try {
                assertThat(Files.readString(defaultFile)).contains("default-filed");
            } finally {
                service.setFileOutput(pluginId, false, null);
                Files.deleteIfExists(defaultFile);
            }
        }

        @Test
        @DisplayName("a path without parent directory is written to the working directory")
        void bareFile() throws Exception {
            String pluginId = "log-bare";
            service.setConsoleOutput(pluginId, false);
            File bare = new File("bare-logging-test.log");

            service.setFileOutput(pluginId, true, bare.getName());
            try {
                service.log(pluginId, LogLevel.INFO, "bare-filed");
                assertThat(Files.readString(bare.toPath())).contains("bare-filed");
            } finally {
                service.setFileOutput(pluginId, false, null);
                Files.deleteIfExists(bare.toPath());
            }
        }

        @Test
        @DisplayName("uncreatable parent directory warns and fails")
        void mkdirFailure() throws Exception {
            Path blocker = tempDir.resolve("blocker");
            Files.createFile(blocker);
            String impossible = blocker.resolve("sub").resolve("x.log").toString();

            try (LogCapture capture = LogCapture.attach(DefaultPluginLoggingService.class)) {
                service.setFileOutput(P, true, impossible);
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("Failed to create directory"))
                        .anyMatch(m -> m.contains("Failed to set file output"));
            }
            assertThat(writers()).doesNotContainKey(P);
        }

        @Test
        @DisplayName("pointing at a directory fails with an error log")
        void directoryAsFile() throws Exception {
            try (LogCapture capture = LogCapture.attach(DefaultPluginLoggingService.class)) {
                service.setFileOutput(P, true, tempDir.toString());
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("Failed to set file output"));
            }
            assertThat(writers()).doesNotContainKey(P);
        }

        @Test
        @DisplayName("closeAllWriters closes and clears every writer")
        void closeAllWriters() throws Exception {
            Path logFile = tempDir.resolve("close.log");
            service.setFileOutput(P, true, logFile.toString());
            PrintWriter writer = writers().get(P);
            assertThat(writer).isNotNull();

            Method closeAll = DefaultPluginLoggingService.class
                    .getDeclaredMethod("closeAllWriters");
            closeAll.setAccessible(true);
            closeAll.invoke(service);

            // a write on the closed writer silently fails, then checkError reports it
            writer.println("after-close");
            assertThat(writer.checkError()).isTrue();
            assertThat(Files.readString(logFile)).doesNotContain("after-close");
            assertThat(writers()).isEmpty();
        }
    }

    @Nested
    @DisplayName("statistics and SLF4J routing")
    class StatisticsTests {

        @Test
        @DisplayName("statistics count every level")
        void statistics() {
            service.setConsoleOutput(P, false);
            service.log(P, LogLevel.INFO, "a");
            service.log(P, LogLevel.INFO, "b");
            service.log(P, LogLevel.ERROR, "c");

            Map<String, Object> stats = service.getStatistics(P);
            assertThat(stats).containsKey("lastLogTime");
            assertThat(stats).containsEntry("totalLogs", 3);

            @SuppressWarnings("unchecked")
            Map<String, Integer> counts = (Map<String, Integer>) stats.get("counts");
            assertThat(counts).containsEntry("INFO", 2).containsEntry("ERROR", 1);
        }

        @Test
        @DisplayName("statistics of an unknown plugin are empty")
        void statisticsUnknown() {
            assertThat(service.getStatistics("ghost")).isEmpty();
        }

        @Test
        @DisplayName("every level is routed through SLF4J with the plugin prefix")
        void slf4jRouting() {
            service.setConsoleOutput(P, false);

            try (LogCapture capture = LogCapture.attach(DefaultPluginLoggingService.class)) {
                service.setLogLevel(P, LogLevel.TRACE);
                service.log(P, LogLevel.TRACE, "t");
                service.log(P, LogLevel.DEBUG, "d");
                service.log(P, LogLevel.INFO, "i");
                service.log(P, LogLevel.WARN, "w");
                service.log(P, LogLevel.ERROR, "e");
                service.log(P, LogLevel.FATAL, "f", new IllegalStateException("cause"));

                List<String> messages = capture.formattedMessages();
                assertThat(messages).contains(
                        "[Plugin:log-plugin] t",
                        "[Plugin:log-plugin] d",
                        "[Plugin:log-plugin] i",
                        "[Plugin:log-plugin] w",
                        "[Plugin:log-plugin] e",
                        "[Plugin:log-plugin] f");

                // the cause must be attached only when provided
                assertThat(capture.events())
                        .filteredOn(ev -> "[Plugin:log-plugin] f".equals(ev.getFormattedMessage()))
                        .allSatisfy(ev -> assertThat(ev.getThrowableProxy()).isNotNull());
                assertThat(capture.events())
                        .filteredOn(ev -> "[Plugin:log-plugin] e".equals(ev.getFormattedMessage()))
                        .allSatisfy(ev -> assertThat(ev.getThrowableProxy()).isNull());
            }
        }
    }
}
