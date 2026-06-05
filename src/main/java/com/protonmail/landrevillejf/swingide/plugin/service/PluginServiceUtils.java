package com.protonmail.landrevillejf.swingide.plugin.service;

import com.protonmail.landrevillejf.swingide.plugin.ExtendedPluginContext;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for common plugin service operations.
 */
public final class PluginServiceUtils {

    private PluginServiceUtils() {
        // Utility class
    }

    /**
     * Creates a performance report for a plugin based on metrics.
     *
     * @param metricsService the metrics service
     * @param pluginId the plugin identifier
     * @return a performance report map
     */
    public static Map<String, Object> generatePerformanceReport(
            PluginMetricsService metricsService, String pluginId) {
        Map<String, Object> report = new HashMap<>();
        report.put("metrics", metricsService.getAllMetrics(pluginId));
        report.put("timestamp", System.currentTimeMillis());
        return report;
    }

    /**
     * Creates a health summary for a plugin.
     *
     * @param monitoringService the monitoring service
     * @param pluginId the plugin identifier
     * @return a health summary map
     */
    public static Map<String, Object> generateHealthSummary(
            PluginMonitoringService monitoringService, String pluginId) {
        PluginMonitoringService.HealthReport report = monitoringService.getHealthReport(pluginId);
        Map<String, Object> summary = new HashMap<>();
        summary.put("status", report.getStatus());
        summary.put("cpuUsage", report.getCpuUsage());
        summary.put("memoryUsage", report.getMemoryUsage());
        summary.put("threadCount", report.getThreadCount());
        summary.put("uptime", report.getUptime());
        summary.put("errorCount", report.getErrorCount());
        summary.put("warningCount", report.getWarningCount());
        summary.put("timestamp", System.currentTimeMillis());
        return summary;
    }

    /**
     * Backs up all plugin data.
     *
     * @param dataStore the data store service
     * @param pluginId the plugin identifier
     * @return the backup identifier
     */
    public static String backupPluginData(PluginDataStore dataStore, String pluginId) {
        return dataStore.backup(pluginId);
    }

    /**
     * Restores plugin data from backup.
     *
     * @param dataStore the data store service
     * @param pluginId the plugin identifier
     * @param backupId the backup identifier
     * @return true if restore succeeded
     */
    public static boolean restorePluginData(
            PluginDataStore dataStore, String pluginId, String backupId) {
        return dataStore.restore(pluginId, backupId);
    }

    /**
     * Gets all active alerts for a plugin.
     *
     * @param monitoringService the monitoring service
     * @param pluginId the plugin identifier
     * @return list of alerts for the plugin
     */
    public static java.util.List<PluginMonitoringService.Alert> getPluginAlerts(
            PluginMonitoringService monitoringService, String pluginId) {
        return monitoringService.getPluginAlerts(pluginId);
    }

    /**
     * Checks if a plugin has critical issues.
     *
     * @param monitoringService the monitoring service
     * @param pluginId the plugin identifier
     * @return true if critical alerts exist
     */
    public static boolean hasCriticalIssues(
            PluginMonitoringService monitoringService, String pluginId) {
        return getPluginAlerts(monitoringService, pluginId).stream()
                .anyMatch(alert -> alert.getSeverity() == PluginMonitoringService.AlertSeverity.CRITICAL);
    }

    /**
     * Clears all cache and metrics for a plugin.
     *
     * @param cacheService the cache service
     * @param metricsService the metrics service
     * @param pluginId the plugin identifier
     */
    public static void clearPluginState(
            PluginCacheService cacheService, PluginMetricsService metricsService, String pluginId) {
        cacheService.clear(pluginId);
        metricsService.resetMetrics(pluginId);
    }

    /**
     * Validates all plugin dependencies.
     *
     * @param dependencyResolver the dependency resolver
     * @param pluginId the plugin identifier
     * @return validation result
     */
    public static Map<String, Object> validateAllDependencies(
            PluginDependencyResolver dependencyResolver, String pluginId) {
        return dependencyResolver.validateDependencies(pluginId);
    }

    /**
     * Checks plugin permission compliance.
     *
     * @param permissionService the permission service
     * @param pluginId the plugin identifier
     * @param requiredPermissions required permission IDs
     * @return true if all permissions are available
     */
    public static boolean hasRequiredPermissions(
            PluginPermissionService permissionService, String pluginId,
            String... requiredPermissions) {
        return permissionService.hasAllPermissions(pluginId, requiredPermissions);
    }

    /**
     * Exports complete plugin diagnostic data.
     *
     * @param context the extended plugin context
     * @param pluginId the plugin identifier
     * @return complete diagnostic data
     */
    public static Map<String, Object> exportPluginDiagnostics(
            ExtendedPluginContext context, String pluginId) {
        Map<String, Object> diagnostics = new HashMap<>();

        // Health
        PluginMonitoringService monitoring = context.getMonitoringService();
        diagnostics.put("health", generateHealthSummary(monitoring, pluginId));

        // Performance
        PluginMetricsService metrics = context.getMetricsService();
        diagnostics.put("performance", generatePerformanceReport(metrics, pluginId));

        // Data store stats
        PluginDataStore dataStore = context.getDataStore();
        diagnostics.put("dataStoreStats", dataStore.getStatistics(pluginId));

        // Cache stats
        PluginCacheService cache = context.getCacheService();
        diagnostics.put("cacheStats", cache.getStatistics(pluginId));

        // Alerts
        diagnostics.put("alerts", monitoring.getPluginAlerts(pluginId));

        // Dependencies
        PluginDependencyResolver deps = context.getDependencyResolver();
        diagnostics.put("dependencies", deps.validateDependencies(pluginId));

        // Logs (recent)
        PluginLoggingService logger = context.getLoggingService();
        diagnostics.put("recentLogs", logger.getRecentLogs(pluginId, 50));

        diagnostics.put("timestamp", System.currentTimeMillis());
        return diagnostics;
    }

    /**
     * Cleanup utility for removing obsolete plugin data.
     *
     * @param cacheService the cache service
     * @param dataStore the data store
     * @param pluginId the plugin identifier
     */
    public static void cleanupObsoleteData(
            PluginCacheService cacheService, PluginDataStore dataStore, String pluginId) {
        cacheService.clear(pluginId);
        dataStore.clear(pluginId);
    }
}

