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

@Slf4j
public class DefaultPluginLoggingService implements PluginLoggingService {

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
        log.debug("Set log level for plugin {} to {}", pluginId, level);
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
            switch (level) {
                case ERROR:
                case FATAL:
                    System.err.println(formatted);
                    break;
                default:
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
        switch (level) {
            case TRACE:
                log.trace(slf4jMessage);
                break;
            case DEBUG:
                log.debug(slf4jMessage);
                break;
            case INFO:
                log.info(slf4jMessage);
                break;
            case WARN:
                log.warn(slf4jMessage);
                break;
            case ERROR:
            case FATAL:
                if (cause != null) {
                    log.error(slf4jMessage, cause);
                } else {
                    log.error(slf4jMessage);
                }
                break;
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
            log.debug("Cleared logs for plugin {}", pluginId);
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
        log.debug("Set console output for plugin {} to {}", pluginId, enabled);
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
            String path = filePath != null ? filePath : "logs/" + pluginId + ".log";
            File logFile = new File(path);
            logFile.getParentFile().mkdirs();
            PrintWriter writer = new PrintWriter(new FileWriter(logFile, true));
            fileWriters.put(pluginId, writer);
            log.debug("Enabled file output for plugin {} at {}", pluginId, path);
        } catch (Exception e) {
            log.error("Failed to set file output for plugin {}", pluginId, e);
        }
    }

    @Override
    public Map<String, Object> getStatistics(String pluginId) {
        return statistics.getOrDefault(pluginId, Collections.emptyMap());
    }

    private void updateStatistics(String pluginId, LogLevel level) {
        Map<String, Object> stats = statistics.computeIfAbsent(pluginId, k -> new ConcurrentHashMap<>());
        Map<String, Integer> counts = (Map<String, Integer>) stats.computeIfAbsent("counts", k -> new ConcurrentHashMap<>());
        counts.merge(level.name(), 1, Integer::sum);
        stats.put("lastLogTime", LocalDateTime.now().toString());
        stats.put("totalLogs", counts.values().stream().mapToInt(Integer::intValue).sum());
    }

    private String formatEntry(LogEntry entry) {
        return String.format("[%s] [%s] [%s] %s%s",
                entry.timestamp,
                entry.pluginId,
                entry.level,
                entry.message,
                entry.cause != null ? " - " + getStackTrace(entry.cause) : ""
        );
    }

    private String formatMessage(String pluginId, String message) {
        return String.format("[Plugin:%s] %s", pluginId, message);
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
    }

    private static class LogEntry {
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