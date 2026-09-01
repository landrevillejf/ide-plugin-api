package com.protonmail.landrevillejf.ide.plugin.service;

import java.util.List;
import java.util.Map;

/**
 * Global plugin monitoring and analytics service.
 * <p>
 * Provides health reporting, resource usage tracking (CPU, memory, threads),
 * alerting, and comprehensive monitoring across all loaded plugins.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
public interface PluginMonitoringService {

    /**
     * Plugin health status.
     */
    enum HealthStatus {
        HEALTHY,
        DEGRADED,
        CRITICAL,
        OFFLINE,
        UNKNOWN
    }

    /**
     * Alert severity level.
     */
    enum AlertSeverity {
        INFO,
        WARNING,
        ERROR,
        CRITICAL
    }

    /**
     * Represents a plugin health report.
     */
    interface HealthReport {
        String getPluginId();
        HealthStatus getStatus();
        double getCpuUsage();
        long getMemoryUsage();
        int getThreadCount();
        long getUptime();
        int getErrorCount();
        int getWarningCount();
        Map<String, Object> getDetails();
    }

    /**
     * Represents an alert.
     */
    interface Alert {
        String getAlertId();
        String getPluginId();
        AlertSeverity getSeverity();
        String getTitle();
        String getMessage();
        long getTimestamp();
        boolean isResolved();
    }

    /**
     * Gets health report for a plugin.
     *
     * @param pluginId the plugin identifier
     * @return the health report
     */
    HealthReport getHealthReport(String pluginId);

    /**
     * Gets health status for a plugin.
     *
     * @param pluginId the plugin identifier
     * @return the health status
     */
    HealthStatus getHealthStatus(String pluginId);

    /**
     * Gets global health status for all plugins.
     *
     * @return the overall health status
     */
    HealthStatus getGlobalHealthStatus();

    /**
     * Gets CPU usage for a plugin.
     *
     * @param pluginId the plugin identifier
     * @return the CPU usage percentage
     */
    double getCpuUsage(String pluginId);

    /**
     * Gets memory usage for a plugin.
     *
     * @param pluginId the plugin identifier
     * @return the memory usage in bytes
     */
    long getMemoryUsage(String pluginId);

    /**
     * Gets thread count for a plugin.
     *
     * @param pluginId the plugin identifier
     * @return the number of threads
     */
    int getThreadCount(String pluginId);

    /**
     * Gets uptime for a plugin.
     *
     * @param pluginId the plugin identifier
     * @return the uptime in milliseconds
     */
    long getUptime(String pluginId);

    /**
     * Gets error count for a plugin.
     *
     * @param pluginId the plugin identifier
     * @return the number of errors
     */
    int getErrorCount(String pluginId);

    /**
     * Gets warning count for a plugin.
     *
     * @param pluginId the plugin identifier
     * @return the number of warnings
     */
    int getWarningCount(String pluginId);

    /**
     * Creates an alert.
     *
     * @param pluginId the plugin identifier
     * @param severity the alert severity
     * @param title the alert title
     * @param message the alert message
     * @return the alert
     */
    Alert createAlert(String pluginId, AlertSeverity severity, String title, String message);

    /**
     * Gets all active alerts.
     *
     * @return list of active alerts
     */
    List<Alert> getActiveAlerts();

    /**
     * Gets alerts for a plugin.
     *
     * @param pluginId the plugin identifier
     * @return list of alerts for the plugin
     */
    List<Alert> getPluginAlerts(String pluginId);

    /**
     * Resolves an alert.
     *
     * @param alertId the alert identifier
     */
    void resolveAlert(String alertId);

    /**
     * Gets alert history.
     *
     * @param maxCount the maximum number of records to retrieve
     * @return list of resolved alerts
     */
    List<Alert> getAlertHistory(int maxCount);

    /**
     * Clears alert history.
     */
    void clearAlertHistory();

    /**
     * Gets comprehensive health reports for all plugins.
     *
     * @return list of health reports
     */
    List<HealthReport> getAllHealthReports();

    /**
     * Gets global monitoring statistics.
     *
     * @return a map containing global statistics
     */
    Map<String, Object> getGlobalStatistics();

    /**
     * Registers a health monitor listener.
     *
     * @param listener the listener to register
     */
    void registerHealthMonitorListener(HealthMonitorListener listener);

    /**
     * Unregisters a health monitor listener.
     *
     * @param listener the listener to unregister
     */
    void unregisterHealthMonitorListener(HealthMonitorListener listener);

    /**
     * Listener for health monitoring events.
     */
    interface HealthMonitorListener {
        /**
         * Called when plugin health changes.
         *
         * @param report the new health report
         */
        void onHealthChanged(HealthReport report);

        /**
         * Called when an alert is created.
         *
         * @param alert the alert
         */
        void onAlertCreated(Alert alert);

        /**
         * Called when an alert is resolved.
         *
         * @param alert the resolved alert
         */
        void onAlertResolved(Alert alert);
    }
}

