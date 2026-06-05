package com.protonmail.landrevillejf.ide.plugin.service;

import java.util.Map;

/**
 * Centralized logging service for plugins with configurable levels and output targets.
 * Provides structured logging capabilities for all plugins.
 */
public interface PluginLoggingService {

    /**
     * Log levels supported by the service.
     */
    enum LogLevel {
        TRACE(0),
        DEBUG(1),
        INFO(2),
        WARN(3),
        ERROR(4),
        FATAL(5);

        private final int level;

        LogLevel(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    }

    /**
     * Sets the minimum log level for a specific plugin.
     *
     * @param pluginId the plugin identifier
     * @param level the minimum log level
     */
    void setLogLevel(String pluginId, LogLevel level);

    /**
     * Gets the current log level for a plugin.
     *
     * @param pluginId the plugin identifier
     * @return the current log level
     */
    LogLevel getLogLevel(String pluginId);

    /**
     * Logs a message with the specified level.
     *
     * @param pluginId the plugin identifier
     * @param level the log level
     * @param message the message to log
     */
    void log(String pluginId, LogLevel level, String message);

    /**
     * Logs a message with the specified level and cause.
     *
     * @param pluginId the plugin identifier
     * @param level the log level
     * @param message the message to log
     * @param cause the throwable cause
     */
    void log(String pluginId, LogLevel level, String message, Throwable cause);

    /**
     * Logs a formatted message with arguments.
     *
     * @param pluginId the plugin identifier
     * @param level the log level
     * @param format the message format
     * @param args the formatting arguments
     */
    void logf(String pluginId, LogLevel level, String format, Object... args);

    /**
     * Clears all logs for a specific plugin.
     *
     * @param pluginId the plugin identifier
     */
    void clearLogs(String pluginId);

    /**
     * Retrieves recent logs for a plugin.
     *
     * @param pluginId the plugin identifier
     * @param maxLines the maximum number of recent lines to retrieve
     * @return a list of recent log entries
     */
    java.util.List<String> getRecentLogs(String pluginId, int maxLines);

    /**
     * Enables or disables console output for a plugin.
     *
     * @param pluginId the plugin identifier
     * @param enabled true to enable console output
     */
    void setConsoleOutput(String pluginId, boolean enabled);

    /**
     * Enables or disables file output for a plugin.
     *
     * @param pluginId the plugin identifier
     * @param enabled true to enable file output
     * @param filePath optional file path for logs
     */
    void setFileOutput(String pluginId, boolean enabled, String filePath);

    /**
     * Gets current logging statistics.
     *
     * @param pluginId the plugin identifier
     * @return a map containing log statistics
     */
    Map<String, Object> getStatistics(String pluginId);
}

