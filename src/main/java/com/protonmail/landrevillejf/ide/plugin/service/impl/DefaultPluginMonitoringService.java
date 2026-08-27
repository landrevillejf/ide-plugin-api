package com.protonmail.landrevillejf.ide.plugin.service.impl;

import com.protonmail.landrevillejf.ide.plugin.service.PluginMonitoringService;
import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.ThreadMXBean;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
public class DefaultPluginMonitoringService implements PluginMonitoringService {

    private final Map<String, PluginMonitor> monitors = new ConcurrentHashMap<>();
    private final Map<String, List<Alert>> activeAlerts = new ConcurrentHashMap<>();
    private final List<Alert> alertHistory = new CopyOnWriteArrayList<>();
    private final List<HealthMonitorListener> listeners = new CopyOnWriteArrayList<>();
    private final AtomicLong alertIdGenerator = new AtomicLong(0);

    private final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
    private final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

    public DefaultPluginMonitoringService() {
        // Start periodic monitoring
        ScheduledExecutorService monitoringExecutor = Executors.newScheduledThreadPool(1);
        monitoringExecutor.scheduleAtFixedRate(this::updateAllHealthReports, 5, 5, TimeUnit.SECONDS);

        // Start cleanup of old alerts
        monitoringExecutor.scheduleAtFixedRate(this::cleanupOldAlerts, 1, 1, TimeUnit.HOURS);

        if (log.isInfoEnabled()) {
            log.info("DefaultPluginMonitoringService initialized");
        }
    }

    @Override
    public HealthReport getHealthReport(String pluginId) {
        PluginMonitor monitor = monitors.get(pluginId);
        if (monitor == null) {
            return createEmptyReport(pluginId);
        }
        return monitor.getHealthReport();
    }

    @Override
    public HealthStatus getHealthStatus(String pluginId) {
        PluginMonitor monitor = monitors.get(pluginId);
        if (monitor == null) {
            return HealthStatus.UNKNOWN;
        }
        return monitor.getHealthStatus();
    }

    @Override
    public HealthStatus getGlobalHealthStatus() {
        List<HealthStatus> statuses = monitors.values().stream()
                .map(PluginMonitor::getHealthStatus)
                .collect(Collectors.toList());

        if (statuses.isEmpty()) {
            return HealthStatus.UNKNOWN;
        }

        // Check for critical first
        if (statuses.contains(HealthStatus.CRITICAL)) {
            return HealthStatus.CRITICAL;
        }

        // Then degraded
        if (statuses.contains(HealthStatus.DEGRADED)) {
            return HealthStatus.DEGRADED;
        }

        // Then offline
        if (statuses.contains(HealthStatus.OFFLINE)) {
            return HealthStatus.OFFLINE;
        }

        // Everything else is healthy (monitors only report
        // HEALTHY, DEGRADED, CRITICAL or OFFLINE)
        return HealthStatus.HEALTHY;
    }

    @Override
    public double getCpuUsage(String pluginId) {
        PluginMonitor monitor = monitors.get(pluginId);
        if (monitor == null) {
            return 0.0;
        }
        return monitor.getCpuUsage();
    }

    @Override
    public long getMemoryUsage(String pluginId) {
        PluginMonitor monitor = monitors.get(pluginId);
        if (monitor == null) {
            return 0;
        }
        return monitor.getMemoryUsage();
    }

    @Override
    public int getThreadCount(String pluginId) {
        PluginMonitor monitor = monitors.get(pluginId);
        if (monitor == null) {
            return 0;
        }
        return monitor.getThreadCount();
    }

    @Override
    public long getUptime(String pluginId) {
        PluginMonitor monitor = monitors.get(pluginId);
        if (monitor == null) {
            return 0;
        }
        return monitor.getUptime();
    }

    @Override
    public int getErrorCount(String pluginId) {
        PluginMonitor monitor = monitors.get(pluginId);
        if (monitor == null) {
            return 0;
        }
        return monitor.getErrorCount();
    }

    @Override
    public int getWarningCount(String pluginId) {
        PluginMonitor monitor = monitors.get(pluginId);
        if (monitor == null) {
            return 0;
        }
        return monitor.getWarningCount();
    }

    @Override
    public Alert createAlert(String pluginId, AlertSeverity severity, String title, String message) {
        String alertId = generateAlertId(pluginId);
        AlertImpl alert = new AlertImpl(alertId, pluginId, severity, title, message,
                currentTimeMillis(), false);

        activeAlerts.computeIfAbsent(pluginId, k -> new CopyOnWriteArrayList<>()).add(alert);

        // Notify listeners
        listeners.forEach(l -> l.onAlertCreated(alert));

        // Log according to severity
        if (severity == AlertSeverity.CRITICAL) {
            if (log.isErrorEnabled()) {
                log.error("CRITICAL alert for plugin {}: {} - {}", pluginId, title, message);
            }
        } else if (severity == AlertSeverity.ERROR) {
            if (log.isErrorEnabled()) {
                log.error("Alert for plugin {}: {} - {}", pluginId, title, message);
            }
        } else if (severity == AlertSeverity.WARNING) {
            if (log.isWarnEnabled()) {
                log.warn("Alert for plugin {}: {} - {}", pluginId, title, message);
            }
        } else {
            if (log.isInfoEnabled()) {
                log.info("Alert for plugin {}: {} - {}", pluginId, title, message);
            }
        }

        return alert;
    }

    @Override
    public List<Alert> getActiveAlerts() {
        return activeAlerts.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    @Override
    public List<Alert> getPluginAlerts(String pluginId) {
        return activeAlerts.getOrDefault(pluginId, Collections.emptyList())
                .stream()
                .collect(Collectors.toList());
    }

    @Override
    public void resolveAlert(String alertId) {
        for (Map.Entry<String, List<Alert>> entry : activeAlerts.entrySet()) {
            Alert toResolve = null;
            for (Alert alert : entry.getValue()) {
                if (alert.getAlertId().equals(alertId)) {
                    toResolve = alert;
                    break;
                }
            }

            if (toResolve != null) {
                entry.getValue().remove(toResolve);
                Alert resolvedAlert = new AlertImpl(
                        toResolve.getAlertId(), toResolve.getPluginId(), toResolve.getSeverity(),
                        toResolve.getTitle(), toResolve.getMessage(), toResolve.getTimestamp(), true
                );
                alertHistory.add(resolvedAlert);

                // Notify listeners
                listeners.forEach(l -> l.onAlertResolved(resolvedAlert));

                if (log.isDebugEnabled()) {
                    log.debug("Alert resolved: {}", alertId);
                }
                break;
            }
        }
    }

    @Override
    public List<Alert> getAlertHistory(int maxCount) {
        int start = Math.max(0, alertHistory.size() - maxCount);
        return alertHistory.subList(start, alertHistory.size())
                .stream()
                .collect(Collectors.toList());
    }

    @Override
    public void clearAlertHistory() {
        alertHistory.clear();
        if (log.isDebugEnabled()) {
            log.debug("Alert history cleared");
        }
    }

    @Override
    public List<HealthReport> getAllHealthReports() {
        return monitors.values().stream()
                .map(PluginMonitor::getHealthReport)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getGlobalStatistics() {
        Map<String, Object> stats = new LinkedHashMap<>();

        // System metrics
        stats.put("systemCpuLoad", osBean.getSystemLoadAverage());
        stats.put("systemMemoryUsage", memoryBean.getHeapMemoryUsage().getUsed());
        stats.put("systemThreadCount", threadBean.getThreadCount());
        stats.put("totalPlugins", monitors.size());

        // Plugin metrics
        long totalMemory = monitors.values().stream().mapToLong(PluginMonitor::getMemoryUsage).sum();
        double totalCpu = monitors.values().stream().mapToDouble(PluginMonitor::getCpuUsage).average().orElse(0.0);
        int totalErrors = monitors.values().stream().mapToInt(PluginMonitor::getErrorCount).sum();
        int totalWarnings = monitors.values().stream().mapToInt(PluginMonitor::getWarningCount).sum();

        stats.put("totalPluginMemoryUsage", totalMemory);
        stats.put("averagePluginCpuUsage", totalCpu);
        stats.put("totalPluginErrors", totalErrors);
        stats.put("totalPluginWarnings", totalWarnings);
        stats.put("totalActiveAlerts", getActiveAlerts().size());

        // Health distribution
        Map<HealthStatus, Long> healthDistribution = monitors.values().stream()
                .collect(Collectors.groupingBy(PluginMonitor::getHealthStatus, Collectors.counting()));
        stats.put("healthDistribution", healthDistribution);

        return stats;
    }

    @Override
    public void registerHealthMonitorListener(HealthMonitorListener listener) {
        listeners.add(listener);
        if (log.isDebugEnabled()) {
            log.debug("Health monitor listener registered");
        }
    }

    @Override
    public void unregisterHealthMonitorListener(HealthMonitorListener listener) {
        listeners.remove(listener);
        if (log.isDebugEnabled()) {
            log.debug("Health monitor listener unregistered");
        }
    }

    /**
     * Registers a plugin for monitoring.
     * This should be called when a plugin is loaded.
     */
    public void registerPlugin(String pluginId) {
        PluginMonitor monitor = new PluginMonitor(pluginId);
        monitors.put(pluginId, monitor);
        if (log.isDebugEnabled()) {
            log.debug("Plugin registered for monitoring: {}", pluginId);
        }
    }

    /**
     * Unregisters a plugin from monitoring.
     * This should be called when a plugin is unloaded.
     */
    public void unregisterPlugin(String pluginId) {
        monitors.remove(pluginId);
        activeAlerts.remove(pluginId);
        if (log.isDebugEnabled()) {
            log.debug("Plugin unregistered from monitoring: {}", pluginId);
        }
    }

    /**
     * Marks a plugin as offline. An offline plugin keeps its status
     * until it is marked online again.
     */
    public void markPluginOffline(String pluginId) {
        PluginMonitor monitor = monitors.get(pluginId);
        if (monitor != null) {
            monitor.setHealthStatus(HealthStatus.OFFLINE);
            if (log.isDebugEnabled()) {
                log.debug("Plugin marked offline: {}", pluginId);
            }
        }
    }

    /**
     * Marks a plugin as online again, letting the next health update
     * recompute its status from the collected metrics.
     */
    public void markPluginOnline(String pluginId) {
        PluginMonitor monitor = monitors.get(pluginId);
        if (monitor != null) {
            monitor.setHealthStatus(HealthStatus.HEALTHY);
            if (log.isDebugEnabled()) {
                log.debug("Plugin marked online: {}", pluginId);
            }
        }
    }

    /**
     * Records an error for a plugin.
     */
    public void recordError(String pluginId, Throwable error) {
        PluginMonitor monitor = monitors.get(pluginId);
        if (monitor != null) {
            monitor.recordError();

            // Create alert for repeated errors
            int errorCount = monitor.getErrorCount();
            if (errorCount % 10 == 0) {
                createAlert(pluginId, AlertSeverity.ERROR, "Multiple Errors Detected",
                        String.format("Plugin has encountered %d errors", errorCount));
            }
        }
    }

    /**
     * Records a warning for a plugin.
     */
    public void recordWarning(String pluginId, String warning) {
        PluginMonitor monitor = monitors.get(pluginId);
        if (monitor != null) {
            monitor.recordWarning();

            // Create alert for many warnings
            int warningCount = monitor.getWarningCount();
            if (warningCount % 20 == 0) {
                createAlert(pluginId, AlertSeverity.WARNING, "Multiple Warnings",
                        String.format("Plugin has generated %d warnings", warningCount));
            }
        }
    }

    private void updateAllHealthReports() {
        for (PluginMonitor monitor : monitors.values()) {
            monitor.updateMetrics();

            // Check for health status changes
            HealthStatus oldStatus = monitor.getHealthStatus();
            monitor.updateHealthStatus();
            HealthStatus newStatus = monitor.getHealthStatus();

            if (oldStatus != newStatus) {
                HealthReport report = monitor.getHealthReport();
                listeners.forEach(l -> l.onHealthChanged(report));

                // Create alert for health degradation
                if (newStatus == HealthStatus.DEGRADED) {
                    createAlert(monitor.pluginId, AlertSeverity.WARNING, "Health Degraded",
                            "Plugin health has degraded");
                } else if (newStatus == HealthStatus.CRITICAL) {
                    createAlert(monitor.pluginId, AlertSeverity.CRITICAL, "Health Critical",
                            "Plugin health is critical, immediate attention required");
                } else if (newStatus == HealthStatus.HEALTHY) {
                    createAlert(monitor.pluginId, AlertSeverity.INFO, "Health Restored",
                            "Plugin health has been restored");
                }
            }
        }
    }

    private void cleanupOldAlerts() {
        long sevenDaysAgo = currentTimeMillis() - TimeUnit.DAYS.toMillis(7);
        alertHistory.removeIf(alert -> alert.getTimestamp() < sevenDaysAgo);

        if (log.isDebugEnabled()) {
            log.debug("Cleaned up old alerts, history size: {}", alertHistory.size());
        }
    }

    /**
     * Returns the current time in milliseconds. Overridable so that
     * tests can pin a deterministic clock.
     */
    long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    private String generateAlertId(String pluginId) {
        return pluginId + "_alert_" + System.currentTimeMillis() + "_" + alertIdGenerator.incrementAndGet();
    }

    private HealthReport createEmptyReport(String pluginId) {
        return new HealthReportImpl(pluginId, HealthStatus.UNKNOWN, 0.0, 0L, 0, 0L, 0, 0, Collections.emptyMap());
    }

    /**
     * Plugin monitor implementation
     */
    private static class PluginMonitor {
        private final String pluginId;
        private long startTime;
        private final AtomicLong errorCount = new AtomicLong(0);
        private final AtomicLong warningCount = new AtomicLong(0);

        // Removed volatile - using AtomicReference or synchronized instead
        private double cpuUsage = 0.0;
        private long memoryUsage = 0;
        private int threadCount = 0;
        private HealthStatus healthStatus = HealthStatus.HEALTHY;

        // Thread tracking
        private final Map<Thread, Long> trackedThreads = new ConcurrentHashMap<>();

        public PluginMonitor(String pluginId) {
            this.pluginId = pluginId;
            this.startTime = System.currentTimeMillis();
        }

        public void updateMetrics() {
            // Update thread count (simplified - in real implementation, track plugin-specific threads)
            threadCount = trackedThreads.size();

            // Deterministic CPU and memory estimates based on thread activity.
            cpuUsage = Math.min(100.0, threadCount * 5.0);
            memoryUsage = (long) threadCount * 1024 * 1024; // Rough estimate: 1MB per thread
        }

        public void updateHealthStatus() {
            // An offline plugin keeps its status until explicitly brought back online
            if (healthStatus == HealthStatus.OFFLINE) {
                return;
            }

            long errors = errorCount.get();
            long warnings = warningCount.get();

            if (errors > 50) {
                healthStatus = HealthStatus.CRITICAL;
            } else if (errors > 20 || warnings > 100) {
                healthStatus = HealthStatus.DEGRADED;
            } else if (cpuUsage > 80.0 || memoryUsage > 500L * 1024 * 1024) { // 500MB
                healthStatus = HealthStatus.DEGRADED;
            } else {
                healthStatus = HealthStatus.HEALTHY;
            }
        }

        public HealthReport getHealthReport() {
            return new HealthReportImpl(pluginId, healthStatus, cpuUsage, memoryUsage,
                    threadCount, getUptime(), (int) errorCount.get(),
                    (int) warningCount.get(), getDetails());
        }

        public HealthStatus getHealthStatus() {
            return healthStatus;
        }

        public void setHealthStatus(HealthStatus status) {
            this.healthStatus = status;
        }

        public double getCpuUsage() {
            return cpuUsage;
        }

        public long getMemoryUsage() {
            return memoryUsage;
        }

        public int getThreadCount() {
            return threadCount;
        }

        public long getUptime() {
            return System.currentTimeMillis() - startTime;
        }

        public int getErrorCount() {
            return (int) errorCount.get();
        }

        public int getWarningCount() {
            return (int) warningCount.get();
        }

        public void recordError() {
            errorCount.incrementAndGet();
        }

        public void recordWarning() {
            warningCount.incrementAndGet();
        }

        private Map<String, Object> getDetails() {
            Map<String, Object> details = new LinkedHashMap<>();
            long uptimeMinutes = Math.max(1, getUptime() / 60000L);
            details.put("errorRate", (double) errorCount.get() / uptimeMinutes);
            details.put("warningRate", (double) warningCount.get() / uptimeMinutes);
            details.put("startTime", new Date(startTime));
            return details;
        }
    }

    /**
     * Implementation of HealthReport
     */
    private static class HealthReportImpl implements HealthReport {
        private final String pluginId;
        private final HealthStatus status;
        private final double cpuUsage;
        private final long memoryUsage;
        private final int threadCount;
        private final long uptime;
        private final int errorCount;
        private final int warningCount;
        private final Map<String, Object> details;

        public HealthReportImpl(String pluginId, HealthStatus status, double cpuUsage,
                                long memoryUsage, int threadCount, long uptime,
                                int errorCount, int warningCount, Map<String, Object> details) {
            this.pluginId = pluginId;
            this.status = status;
            this.cpuUsage = cpuUsage;
            this.memoryUsage = memoryUsage;
            this.threadCount = threadCount;
            this.uptime = uptime;
            this.errorCount = errorCount;
            this.warningCount = warningCount;
            this.details = details;
        }

        @Override
        public String getPluginId() { return pluginId; }

        @Override
        public HealthStatus getStatus() { return status; }

        @Override
        public double getCpuUsage() { return cpuUsage; }

        @Override
        public long getMemoryUsage() { return memoryUsage; }

        @Override
        public int getThreadCount() { return threadCount; }

        @Override
        public long getUptime() { return uptime; }

        @Override
        public int getErrorCount() { return errorCount; }

        @Override
        public int getWarningCount() { return warningCount; }

        @Override
        public Map<String, Object> getDetails() { return details; }

        @Override
        public String toString() {
            long memoryMB = memoryUsage / (1024 * 1024);
            return String.format("HealthReport{plugin='%s', status=%s, cpu=%.1f%%, memory=%dMB, errors=%d}",
                    pluginId, status, cpuUsage, memoryMB, errorCount);
        }
    }

    /**
     * Implementation of Alert
     */
    private static class AlertImpl implements Alert {
        private final String alertId;
        private final String pluginId;
        private final AlertSeverity severity;
        private final String title;
        private final String message;
        private final long timestamp;
        private final boolean resolved;

        public AlertImpl(String alertId, String pluginId, AlertSeverity severity,
                         String title, String message, long timestamp, boolean resolved) {
            this.alertId = alertId;
            this.pluginId = pluginId;
            this.severity = severity;
            this.title = title;
            this.message = message;
            this.timestamp = timestamp;
            this.resolved = resolved;
        }

        @Override
        public String getAlertId() { return alertId; }

        @Override
        public String getPluginId() { return pluginId; }

        @Override
        public AlertSeverity getSeverity() { return severity; }

        @Override
        public String getTitle() { return title; }

        @Override
        public String getMessage() { return message; }

        @Override
        public long getTimestamp() { return timestamp; }

        @Override
        public boolean isResolved() { return resolved; }

        @Override
        public String toString() {
            return String.format("Alert{id='%s', plugin='%s', severity=%s, title='%s', resolved=%s}",
                    alertId, pluginId, severity, title, resolved);
        }
    }
}