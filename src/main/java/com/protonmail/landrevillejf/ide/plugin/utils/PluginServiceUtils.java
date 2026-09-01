package com.protonmail.landrevillejf.ide.plugin.utils;

import com.protonmail.landrevillejf.ide.plugin.ExtendedPluginContext;
import com.protonmail.landrevillejf.ide.plugin.service.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for common plugin service operations.
 * <p>
 * Provides convenience methods for generating performance reports, health summaries,
 * backup/restore, diagnostics export, and other cross-service operations.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
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
            final PluginMetricsService metricsService, final String pluginId) {
        final Map<String, Object> report = new HashMap<>();
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
            final PluginMonitoringService monitoringService, final String pluginId) {
        final PluginMonitoringService.HealthReport report = monitoringService.getHealthReport(pluginId);
        final Map<String, Object> summary = new HashMap<>();
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
    public static String backupPluginData(final PluginDataStore dataStore, final String pluginId) {
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
            final PluginDataStore dataStore, final String pluginId, final String backupId) {
        return dataStore.restore(pluginId, backupId);
    }

    /**
     * Gets all active alerts for a plugin.
     *
     * @param monitoringService the monitoring service
     * @param pluginId the plugin identifier
     * @return list of alerts for the plugin
     */
    public static List<PluginMonitoringService.Alert> getPluginAlerts(
            final PluginMonitoringService monitoringService, final String pluginId) {
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
            final PluginMonitoringService monitoringService, final String pluginId) {
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
            final PluginCacheService cacheService, final PluginMetricsService metricsService, final String pluginId) {
        cacheService.clear(pluginId);
        metricsService.resetMetrics(pluginId);
    }

    /**
     * Validates all plugin dependencies.
     *
     * @param resolver the dependency resolver (renamed from dependencyResolver to avoid long variable name)
     * @param pluginId the plugin identifier
     * @return validation result
     */
    public static Map<String, Object> validateAllDependencies(
            final PluginDependencyResolver resolver, final String pluginId) {
        return resolver.validateDependencies(pluginId);
    }

    /**
     * Checks plugin permission compliance.
     *
     * @param permissionService the permission service
     * @param pluginId the plugin identifier
     * @param permissions required permission IDs (renamed from requiredPermissions)
     * @return true if all permissions are available
     */
    public static boolean hasRequiredPermissions(
            final PluginPermissionService permissionService, final String pluginId,
            final String... permissions) {
        return permissionService.hasAllPermissions(pluginId, permissions);
    }

    /**
     * Exports complete plugin diagnostic data.
     *
     * @param context the extended plugin context
     * @param pluginId the plugin identifier
     * @return complete diagnostic data
     */
    public static Map<String, Object> exportPluginDiagnostics(
            final ExtendedPluginContext context, final String pluginId) {
        final Map<String, Object> diagnostics = new HashMap<>();

        // Health
        final PluginMonitoringService monitoring = context.getMonitoringService();
        diagnostics.put("health", generateHealthSummary(monitoring, pluginId));

        // Performance
        final PluginMetricsService metrics = context.getMetricsService();
        diagnostics.put("performance", generatePerformanceReport(metrics, pluginId));

        // Data store stats
        final PluginDataStore dataStore = context.getDataStore();
        diagnostics.put("dataStoreStats", dataStore.getStatistics(pluginId));

        // Cache stats
        final PluginCacheService cache = context.getCacheService();
        diagnostics.put("cacheStats", cache.getStatistics(pluginId));

        // Alerts
        diagnostics.put("alerts", monitoring.getPluginAlerts(pluginId));

        // Dependencies
        final PluginDependencyResolver resolver = context.getDependencyResolver();
        diagnostics.put("dependencies", resolver.validateDependencies(pluginId));

        // Logs (recent)
        final PluginLoggingService logger = context.getLoggingService();
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
            final PluginCacheService cacheService, final PluginDataStore dataStore, final String pluginId) {
        cacheService.clear(pluginId);
        dataStore.clear(pluginId);
    }
}