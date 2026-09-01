package com.protonmail.landrevillejf.ide.plugin.service.impl;

import com.protonmail.landrevillejf.ide.plugin.service.PluginLoggingService;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Default implementation of {@link PluginLoggingService}.
 * <p>
 * Provides centralized logging with per-plugin log levels, console and file output,
 * and log statistics tracking. Uses thread-safe collections for concurrent access.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 * @see PluginLoggingService
 */
@Slf4j
public final class DefaultPluginLoggingService implements PluginLoggingService {

    private final Map<String, LogLevel> logLevels = new ConcurrentHashMap<>();
    private final Map<String, List<LogEntry>> logEntries = new ConcurrentHashMap<>();
    private final Map<String, Boolean> consoleOutput = new ConcurrentHashMap<>();
    private final Map<String, PrintWriter> fileWriters = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> statistics = new ConcurrentHashMap<>();
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public DefaultPluginLoggingService() {
        // Set default log level for all plugins
        Runtime.getRuntime().addShutdownHook(new Thread(this::closeAllWriters));
    }

    @Override
    public void setLogLevel(String pluginId, LogLevel level) {
        logLevels.put(pluginId, level);
        if (log.isDebugEnabled()) {
            log.debug("Set log level for plugin {} to {}", pluginId, level);
        }
    }

    @Override
    public LogLevel getLogLevel(String pluginId) {
        return logLevels.getOrDefault(pluginId, LogLevel.INFO);
    }

    @Override
    public void log(String pluginId, LogLevel level, String message) {
        log(pluginId, level, message, null);
    }

    @Override
    public void log(String pluginId, LogLevel level, String message, Throwable cause) {
        LogLevel minLevel = getLogLevel(pluginId);
        if (level.getLevel() < minLevel.getLevel()) {
            return;
        }

        LogEntry entry = new LogEntry(pluginId, level, message, cause);

        // Store log entry
        logEntries.computeIfAbsent(pluginId, k -> new CopyOnWriteArrayList<>()).add(entry);

        // Console output
        if (consoleOutput.getOrDefault(pluginId, true)) {
            String formatted = formatEntry(entry);
            if (level == LogLevel.ERROR || level == LogLevel.FATAL) {
                System.err.println(formatted);
            } else {
                System.out.println(formatted);
            }
        }

        // File output
        PrintWriter writer = fileWriters.get(pluginId);
        if (writer != null) {
            writer.println(formatEntry(entry));
            writer.flush();
        }

        // Update statistics
        updateStatistics(pluginId, level);

        // Also log through SLF4J
        String slf4jMessage = formatMessage(pluginId, message);
        if (level == LogLevel.TRACE) {
            if (log.isTraceEnabled()) {
                log.trace(slf4jMessage);
            }
        } else if (level == LogLevel.DEBUG) {
            if (log.isDebugEnabled()) {
                log.debug(slf4jMessage);
            }
        } else if (level == LogLevel.INFO) {
            if (log.isInfoEnabled()) {
                log.info(slf4jMessage);
            }
        } else if (level == LogLevel.WARN) {
            if (log.isWarnEnabled()) {
                log.warn(slf4jMessage);
            }
        } else {
            // ERROR and FATAL
            if (log.isErrorEnabled()) {
                if (cause != null) {
                    log.error(slf4jMessage, cause);
                } else {
                    log.error(slf4jMessage);
                }
            }
        }
    }

    @Override
    public void logf(String pluginId, LogLevel level, String format, Object... args) {
        String message = String.format(format, args);
        log(pluginId, level, message);
    }

    @Override
    public void clearLogs(String pluginId) {
        List<LogEntry> entries = logEntries.get(pluginId);
        if (entries != null) {
            entries.clear();
            if (log.isDebugEnabled()) {
                log.debug("Cleared logs for plugin {}", pluginId);
            }
        }
    }

    @Override
    public List<String> getRecentLogs(String pluginId, int maxLines) {
        List<LogEntry> entries = logEntries.get(pluginId);
        if (entries == null || entries.isEmpty()) {
            return Collections.emptyList();
        }

        List<String> recent = new ArrayList<>();
        int start = Math.max(0, entries.size() - maxLines);
        for (int i = start; i < entries.size(); i++) {
            recent.add(formatEntry(entries.get(i)));
        }
        return recent;
    }

    @Override
    public void setConsoleOutput(String pluginId, boolean enabled) {
        consoleOutput.put(pluginId, enabled);
        if (log.isDebugEnabled()) {
            log.debug("Set console output for plugin {} to {}", pluginId, enabled);
        }
    }

    @Override
    public void setFileOutput(String pluginId, boolean enabled, String filePath) {
        if (!enabled) {
            PrintWriter writer = fileWriters.remove(pluginId);
            if (writer != null) {
                writer.close();
            }
            return;
        }

        try {
            String path = (filePath != null) ? filePath : "logs/" + pluginId + ".log";
            File logFile = new File(path);
            File parentDir = logFile.getParentFile();
            if (parentDir != null && !parentDir.exists()
                    && !parentDir.mkdirs() && log.isWarnEnabled()) {
                log.warn("Failed to create directory: {}", parentDir);
            }
            PrintWriter writer = new PrintWriter(new FileWriter(logFile, true));
            fileWriters.put(pluginId, writer);
            if (log.isDebugEnabled()) {
                log.debug("Enabled file output for plugin {} at {}", pluginId, path);
            }
        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Failed to set file output for plugin {}", pluginId, e);
            }
        }
    }

    @Override
    public Map<String, Object> getStatistics(String pluginId) {
        Map<String, Object> stats = statistics.getOrDefault(pluginId, Collections.emptyMap());
        // Return a copy to avoid modification
        return new ConcurrentHashMap<>(stats);
    }

    private void updateStatistics(String pluginId, LogLevel level) {
        Map<String, Object> stats = statistics.computeIfAbsent(pluginId, k -> new ConcurrentHashMap<>());
        @SuppressWarnings("unchecked")
        Map<String, Integer> counts = (Map<String, Integer>) stats.computeIfAbsent("counts",
                k -> new ConcurrentHashMap<>());
        Integer newCount = counts.merge(level.name(), 1, Integer::sum);
        stats.put("lastLogTime", LocalDateTime.now().toString());

        int total = 0;
        for (Integer value : counts.values()) {
            total += value;
        }
        stats.put("totalLogs", total);
    }

    private String formatEntry(LogEntry entry) {
        StringBuilder sb = new StringBuilder();
        sb.append('[').append(entry.timestamp).append(']');
        sb.append(" [").append(entry.pluginId).append(']');
        sb.append(" [").append(entry.level).append(']');
        sb.append(' ').append(entry.message);
        if (entry.cause != null) {
            sb.append(" - ").append(getStackTrace(entry.cause));
        }
        return sb.toString();
    }

    private String formatMessage(String pluginId, String message) {
        return "[Plugin:" + pluginId + "] " + message;
    }

    private String getStackTrace(Throwable cause) {
        StringBuilder sb = new StringBuilder(cause.toString());
        for (StackTraceElement element : cause.getStackTrace()) {
            sb.append("\n\tat ").append(element);
        }
        return sb.toString();
    }

    private void closeAllWriters() {
        for (PrintWriter writer : fileWriters.values()) {
            writer.close();
        }
        fileWriters.clear();
    }

    private static final class LogEntry {
        final String timestamp;
        final String pluginId;
        final LogLevel level;
        final String message;
        final Throwable cause;

        LogEntry(String pluginId, LogLevel level, String message, Throwable cause) {
            this.timestamp = LocalDateTime.now().format(FORMATTER);
            this.pluginId = pluginId;
            this.level = level;
            this.message = message;
            this.cause = cause;
        }
    }
}