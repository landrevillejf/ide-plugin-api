package com.protonmail.landrevillejf.ide.plugin.service.impl;

import ch.qos.logback.classic.Level;
import com.protonmail.landrevillejf.ide.plugin.service.PluginMonitoringService.Alert;
import com.protonmail.landrevillejf.ide.plugin.service.PluginMonitoringService.AlertSeverity;
import com.protonmail.landrevillejf.ide.plugin.service.PluginMonitoringService.HealthMonitorListener;
import com.protonmail.landrevillejf.ide.plugin.service.PluginMonitoringService.HealthReport;
import com.protonmail.landrevillejf.ide.plugin.service.PluginMonitoringService.HealthStatus;
import com.protonmail.landrevillejf.ide.plugin.utils.LogCapture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.assertj.core.api.Assertions.within;

/**
 * Mutation-killing tests for {@link DefaultPluginMonitoringService}.
 */
@DisplayName("DefaultPluginMonitoringService mutation tests")
class DefaultPluginMonitoringServiceMutationTest {

    private static final String P = "mon-plugin";
    private static final long NOW = 1_700_000_000_000L;

    /** Service whose clock is pinned so time based branches are testable. */
    private static final class FixedClock extends DefaultPluginMonitoringService {
        @Override
        long currentTimeMillis() {
            return NOW;
        }
    }

    private FixedClock service;

    @BeforeEach
    void setUp() {
        service = new FixedClock();
    }

    // ------------------------------------------------------------------
    // Reflection helpers
    // ------------------------------------------------------------------

    private static Object serviceField(Object target, String name) throws Exception {
        Field field = DefaultPluginMonitoringService.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }

    private Object monitor(String pluginId) throws Exception {
        return ((Map<?, ?>) serviceField(service, "monitors")).get(pluginId);
    }

    private static Object monitorField(Object monitor, String name) throws Exception {
        Field field = monitor.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(monitor);
    }

    private static void setMonitorField(Object monitor, String name, Object value) throws Exception {
        Field field = monitor.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(monitor, value);
    }

    private static void invokeMonitor(Object monitor, String method) throws Exception {
        Method m = monitor.getClass().getMethod(method);
        m.setAccessible(true);
        m.invoke(monitor);
    }

    private void updateAll() throws Exception {
        Method m = DefaultPluginMonitoringService.class.getDeclaredMethod("updateAllHealthReports");
        m.setAccessible(true);
        m.invoke(service);
    }

    private void cleanup() throws Exception {
        Method m = DefaultPluginMonitoringService.class.getDeclaredMethod("cleanupOldAlerts");
        m.setAccessible(true);
        m.invoke(service);
    }

    private static void errors(Object monitor, long value) throws Exception {
        ((AtomicLong) monitorField(monitor, "errorCount")).set(value);
    }

    private static void warnings(Object monitor, long value) throws Exception {
        ((AtomicLong) monitorField(monitor, "warningCount")).set(value);
    }

    private void setStatus(String pluginId, HealthStatus status) throws Exception {
        Object monitor = monitor(pluginId);
        Method m = monitor.getClass().getMethod("setHealthStatus", HealthStatus.class);
        m.setAccessible(true);
        m.invoke(monitor, status);
    }

    @SuppressWarnings("unchecked")
    private void trackThreads(Object monitor, int count) throws Exception {
        Map<Thread, Long> tracked = (Map<Thread, Long>) monitorField(monitor, "trackedThreads");
        for (int i = 0; i < count; i++) {
            tracked.put(new Thread(), 0L);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Alert> alertHistory() throws Exception {
        return (List<Alert>) serviceField(service, "alertHistory");
    }

    private Alert newHistoryAlert(String id, long timestamp) throws Exception {
        Class<?> cls = Class.forName(DefaultPluginMonitoringService.class.getName() + "$AlertImpl");
        Constructor<?> constructor = cls.getDeclaredConstructor(String.class, String.class,
                AlertSeverity.class, String.class, String.class, long.class, boolean.class);
        constructor.setAccessible(true);
        return (Alert) constructor.newInstance(id, P, AlertSeverity.INFO, "title", "message", timestamp, true);
    }

    private List<String> alertTitles(String pluginId) {
        return service.getPluginAlerts(pluginId).stream().map(Alert::getTitle).toList();
    }

    private List<String> alertMessages(String pluginId) {
        return service.getPluginAlerts(pluginId).stream().map(Alert::getMessage).toList();
    }

    private static final class RecordingListener implements HealthMonitorListener {
        final List<HealthReport> healthReports = new CopyOnWriteArrayList<>();
        final List<Alert> created = new CopyOnWriteArrayList<>();
        final List<Alert> resolved = new CopyOnWriteArrayList<>();

        @Override
        public void onHealthChanged(HealthReport report) {
            healthReports.add(report);
        }

        @Override
        public void onAlertCreated(Alert alert) {
            created.add(alert);
        }

        @Override
        public void onAlertResolved(Alert alert) {
            resolved.add(alert);
        }
    }

    // ------------------------------------------------------------------
    // Construction and unknown-plugin guards
    // ------------------------------------------------------------------

    @Test
    @DisplayName("constructor logs its initialization")
    void constructorLogs() {
        try (LogCapture capture = LogCapture.attach(DefaultPluginMonitoringService.class)) {
            new FixedClock();
            assertThat(capture.formattedMessages())
                    .contains("DefaultPluginMonitoringService initialized");
        }
    }

    @Test
    @DisplayName("unknown plugins report empty values")
    void unknownPluginReturnsEmptyValues() {
        assertThat(service.getHealthStatus("ghost")).isEqualTo(HealthStatus.UNKNOWN);
        assertThat(service.getCpuUsage("ghost")).isZero();
        assertThat(service.getMemoryUsage("ghost")).isZero();
        assertThat(service.getThreadCount("ghost")).isZero();
        assertThat(service.getUptime("ghost")).isZero();
        assertThat(service.getErrorCount("ghost")).isZero();
        assertThat(service.getWarningCount("ghost")).isZero();

        HealthReport report = service.getHealthReport("ghost");
        assertThat(report.getPluginId()).isEqualTo("ghost");
        assertThat(report.getStatus()).isEqualTo(HealthStatus.UNKNOWN);
        assertThat(report.getCpuUsage()).isZero();
        assertThat(report.getMemoryUsage()).isZero();
        assertThat(report.getThreadCount()).isZero();
        assertThat(report.getUptime()).isZero();
        assertThat(report.getErrorCount()).isZero();
        assertThat(report.getWarningCount()).isZero();
        assertThat(report.getDetails()).isEmpty();
    }

    // ------------------------------------------------------------------
    // Registration lifecycle
    // ------------------------------------------------------------------

    @Test
    @DisplayName("registering and unregistering plugins manages monitors and alerts")
    void registrationLifecycle() {
        try (LogCapture capture = LogCapture.attach(DefaultPluginMonitoringService.class)) {
            service.registerPlugin(P);
            service.unregisterPlugin("ghost");
            assertThat(capture.formattedMessages())
                    .anyMatch(m -> m.contains("registered for monitoring: " + P));
        }

        assertThat(service.getHealthStatus(P)).isEqualTo(HealthStatus.HEALTHY);
        HealthReport report = service.getHealthReport(P);
        assertThat(report.getPluginId()).isEqualTo(P);
        assertThat(report.getUptime()).isGreaterThanOrEqualTo(0);
        assertThat(service.getAllHealthReports())
                .extracting(HealthReport::getPluginId).containsExactly(P);

        service.createAlert(P, AlertSeverity.INFO, "title", "message");
        assertThat(service.getPluginAlerts(P)).hasSize(1);

        try (LogCapture capture = LogCapture.attach(DefaultPluginMonitoringService.class)) {
            service.unregisterPlugin(P);
            assertThat(capture.formattedMessages())
                    .anyMatch(m -> m.contains("unregistered from monitoring: " + P));
        }
        assertThat(service.getHealthStatus(P)).isEqualTo(HealthStatus.UNKNOWN);
        assertThat(service.getPluginAlerts(P)).isEmpty();
        assertThat(service.getAllHealthReports()).isEmpty();
    }

    // ------------------------------------------------------------------
    // Error / warning recording
    // ------------------------------------------------------------------

    @Test
    @DisplayName("an alert is raised exactly every tenth error")
    void errorCadence() {
        service.registerPlugin(P);
        for (int i = 0; i < 9; i++) {
            service.recordError(P, new RuntimeException("e" + i));
        }
        assertThat(alertTitles(P)).noneMatch(t -> t.contains("Multiple Errors"));

        service.recordError(P, new RuntimeException("e9"));
        assertThat(service.getErrorCount(P)).isEqualTo(10);
        assertThat(alertTitles(P)).contains("Multiple Errors Detected");
        assertThat(alertMessages(P)).anyMatch(m -> m.contains("encountered 10 errors"));
    }

    @Test
    @DisplayName("an alert is raised exactly every twentieth warning")
    void warningCadence() {
        service.registerPlugin(P);
        for (int i = 0; i < 19; i++) {
            service.recordWarning(P, "w" + i);
        }
        assertThat(alertTitles(P)).noneMatch(t -> t.contains("Multiple Warnings"));

        service.recordWarning(P, "w19");
        assertThat(service.getWarningCount(P)).isEqualTo(20);
        assertThat(alertTitles(P)).contains("Multiple Warnings");
        assertThat(alertMessages(P)).anyMatch(m -> m.contains("generated 20 warnings"));
    }

    @Test
    @DisplayName("records and status flags for unknown plugins are ignored")
    void unknownPluginRecordsIgnored() {
        service.recordError("ghost", new RuntimeException("boom"));
        service.recordWarning("ghost", "warning");
        service.markPluginOffline("ghost");
        service.markPluginOnline("ghost");
        assertThat(service.getErrorCount("ghost")).isZero();
        assertThat(service.getWarningCount("ghost")).isZero();
        assertThat(service.getHealthStatus("ghost")).isEqualTo(HealthStatus.UNKNOWN);
    }

    // ------------------------------------------------------------------
    // Offline handling
    // ------------------------------------------------------------------

    @Test
    @DisplayName("offline status is sticky until the plugin is marked online")
    void offlineIsSticky() throws Exception {
        service.registerPlugin(P);

        try (LogCapture capture = LogCapture.attach(DefaultPluginMonitoringService.class)) {
            service.markPluginOffline(P);
            assertThat(capture.formattedMessages())
                    .anyMatch(m -> m.contains("Plugin marked offline: " + P));
        }
        assertThat(service.getHealthStatus(P)).isEqualTo(HealthStatus.OFFLINE);

        // a periodic update must not overwrite the offline state
        updateAll();
        assertThat(service.getHealthStatus(P)).isEqualTo(HealthStatus.OFFLINE);

        try (LogCapture capture = LogCapture.attach(DefaultPluginMonitoringService.class)) {
            service.markPluginOnline(P);
            assertThat(capture.formattedMessages())
                    .anyMatch(m -> m.contains("Plugin marked online: " + P));
        }
        updateAll();
        assertThat(service.getHealthStatus(P)).isEqualTo(HealthStatus.HEALTHY);
    }

    // ------------------------------------------------------------------
    // Global health status
    // ------------------------------------------------------------------

    @Test
    @DisplayName("global status follows the priority order")
    void globalStatusPriority() throws Exception {
        assertThat(service.getGlobalHealthStatus()).isEqualTo(HealthStatus.UNKNOWN);

        service.registerPlugin("a");
        service.registerPlugin("b");
        assertThat(service.getGlobalHealthStatus()).isEqualTo(HealthStatus.HEALTHY);

        setStatus("b", HealthStatus.OFFLINE);
        assertThat(service.getGlobalHealthStatus()).isEqualTo(HealthStatus.OFFLINE);

        setStatus("a", HealthStatus.DEGRADED);
        assertThat(service.getGlobalHealthStatus()).isEqualTo(HealthStatus.DEGRADED);

        setStatus("a", HealthStatus.CRITICAL);
        assertThat(service.getGlobalHealthStatus()).isEqualTo(HealthStatus.CRITICAL);
    }

    // ------------------------------------------------------------------
    // Health thresholds
    // ------------------------------------------------------------------

    @Test
    @DisplayName("health thresholds use strict comparisons")
    void healthThresholds() throws Exception {
        service.registerPlugin(P);
        Object monitor = monitor(P);

        errors(monitor, 50);
        invokeMonitor(monitor, "updateHealthStatus");
        assertThat(service.getHealthStatus(P)).isEqualTo(HealthStatus.DEGRADED);

        errors(monitor, 51);
        invokeMonitor(monitor, "updateHealthStatus");
        assertThat(service.getHealthStatus(P)).isEqualTo(HealthStatus.CRITICAL);

        errors(monitor, 20);
        warnings(monitor, 0);
        invokeMonitor(monitor, "updateHealthStatus");
        assertThat(service.getHealthStatus(P)).isEqualTo(HealthStatus.HEALTHY);

        errors(monitor, 21);
        invokeMonitor(monitor, "updateHealthStatus");
        assertThat(service.getHealthStatus(P)).isEqualTo(HealthStatus.DEGRADED);

        errors(monitor, 0);
        warnings(monitor, 100);
        invokeMonitor(monitor, "updateHealthStatus");
        assertThat(service.getHealthStatus(P)).isEqualTo(HealthStatus.HEALTHY);

        warnings(monitor, 101);
        invokeMonitor(monitor, "updateHealthStatus");
        assertThat(service.getHealthStatus(P)).isEqualTo(HealthStatus.DEGRADED);

        warnings(monitor, 0);
        setMonitorField(monitor, "cpuUsage", 80.0);
        setMonitorField(monitor, "memoryUsage", 0L);
        invokeMonitor(monitor, "updateHealthStatus");
        assertThat(service.getHealthStatus(P)).isEqualTo(HealthStatus.HEALTHY);

        setMonitorField(monitor, "cpuUsage", 80.5);
        invokeMonitor(monitor, "updateHealthStatus");
        assertThat(service.getHealthStatus(P)).isEqualTo(HealthStatus.DEGRADED);

        setMonitorField(monitor, "cpuUsage", 0.0);
        setMonitorField(monitor, "memoryUsage", 500L * 1024 * 1024);
        invokeMonitor(monitor, "updateHealthStatus");
        assertThat(service.getHealthStatus(P)).isEqualTo(HealthStatus.HEALTHY);

        setMonitorField(monitor, "memoryUsage", 500L * 1024 * 1024 + 1);
        invokeMonitor(monitor, "updateHealthStatus");
        assertThat(service.getHealthStatus(P)).isEqualTo(HealthStatus.DEGRADED);
    }

    // ------------------------------------------------------------------
    // Periodic update transitions
    // ------------------------------------------------------------------

    @Test
    @DisplayName("status transitions raise alerts and notify listeners")
    void healthTransitions() throws Exception {
        service.registerPlugin(P);
        RecordingListener listener = new RecordingListener();
        service.registerHealthMonitorListener(listener);

        for (int i = 0; i < 21; i++) {
            service.recordError(P, new RuntimeException("e" + i));
        }
        updateAll();
        assertThat(service.getHealthStatus(P)).isEqualTo(HealthStatus.DEGRADED);
        assertThat(alertTitles(P)).contains("Health Degraded");
        assertThat(listener.healthReports).hasSize(1);

        errors(monitor(P), 51);
        updateAll();
        assertThat(service.getHealthStatus(P)).isEqualTo(HealthStatus.CRITICAL);
        assertThat(alertTitles(P)).contains("Health Critical");
        assertThat(listener.healthReports).hasSize(2);

        errors(monitor(P), 0);
        updateAll();
        assertThat(service.getHealthStatus(P)).isEqualTo(HealthStatus.HEALTHY);
        assertThat(alertTitles(P)).contains("Health Restored");
        assertThat(listener.healthReports).hasSize(3);

        // no change, no notification
        updateAll();
        assertThat(listener.healthReports).hasSize(3);

        service.unregisterHealthMonitorListener(listener);
        errors(monitor(P), 51);
        updateAll();
        assertThat(listener.healthReports).hasSize(3);
    }

    @Test
    @DisplayName("registering and unregistering listeners is logged")
    void listenerRegistrationLogs() {
        RecordingListener listener = new RecordingListener();
        try (LogCapture capture = LogCapture.attach(DefaultPluginMonitoringService.class)) {
            service.registerHealthMonitorListener(listener);
            assertThat(capture.formattedMessages())
                    .anyMatch(m -> m.contains("Health monitor listener registered"));
        }
        try (LogCapture capture = LogCapture.attach(DefaultPluginMonitoringService.class)) {
            service.unregisterHealthMonitorListener(listener);
            assertThat(capture.formattedMessages())
                    .anyMatch(m -> m.contains("Health monitor listener unregistered"));
        }
    }

    // ------------------------------------------------------------------
    // Metric estimation
    // ------------------------------------------------------------------

    @Test
    @DisplayName("cpu and memory estimates derive deterministically from threads")
    void metricEstimation() throws Exception {
        service.registerPlugin(P);
        Object monitor = monitor(P);

        trackThreads(monitor, 3);
        invokeMonitor(monitor, "updateMetrics");
        assertThat(service.getThreadCount(P)).isEqualTo(3);
        assertThat(service.getCpuUsage(P)).isEqualTo(15.0);
        assertThat(service.getMemoryUsage(P)).isEqualTo(3L * 1024 * 1024);

        trackThreads(monitor, 17);
        invokeMonitor(monitor, "updateMetrics");
        assertThat(service.getThreadCount(P)).isEqualTo(20);
        assertThat(service.getCpuUsage(P)).isEqualTo(100.0);
        assertThat(service.getMemoryUsage(P)).isEqualTo(20L * 1024 * 1024);
    }

    // ------------------------------------------------------------------
    // Alerts
    // ------------------------------------------------------------------

    @Test
    @DisplayName("createAlert logs according to severity and fills every field")
    void createAlertPerSeverity() {
        service.registerPlugin(P);
        try (LogCapture capture = LogCapture.attach(DefaultPluginMonitoringService.class)) {
            Alert critical = service.createAlert(P, AlertSeverity.CRITICAL, "T1", "M1");
            Alert error = service.createAlert(P, AlertSeverity.ERROR, "T2", "M2");
            Alert warning = service.createAlert(P, AlertSeverity.WARNING, "T3", "M3");
            Alert info = service.createAlert(P, AlertSeverity.INFO, "T4", "M4");

            assertThat(capture.events()).anyMatch(e -> e.getLevel() == Level.ERROR
                    && e.getFormattedMessage().startsWith("CRITICAL alert for plugin"));
            assertThat(capture.events()).anyMatch(e -> e.getLevel() == Level.ERROR
                    && e.getFormattedMessage().startsWith("Alert for plugin")
                    && e.getFormattedMessage().contains("T2"));
            assertThat(capture.events()).anyMatch(e -> e.getLevel() == Level.WARN
                    && e.getFormattedMessage().contains("T3"));
            assertThat(capture.events()).anyMatch(e -> e.getLevel() == Level.INFO
                    && e.getFormattedMessage().contains("T4"));

            assertThat(info.getPluginId()).isEqualTo(P);
            assertThat(info.getSeverity()).isEqualTo(AlertSeverity.INFO);
            assertThat(info.getTitle()).isEqualTo("T4");
            assertThat(info.getMessage()).isEqualTo("M4");
            assertThat(info.getTimestamp()).isEqualTo(NOW);
            assertThat(info.isResolved()).isFalse();
            assertThat(info.getAlertId()).startsWith(P + "_alert_");
            assertThat(info.getAlertId()).isNotEqualTo(critical.getAlertId());
            assertThat(info.getAlertId()).isNotEqualTo(error.getAlertId());
            assertThat(info.getAlertId()).isNotEqualTo(warning.getAlertId());
            assertThat(info.toString())
                    .contains("Alert{id=")
                    .contains("severity=INFO")
                    .contains("title='T4'")
                    .contains("resolved=false");
        }

        assertThat(service.getPluginAlerts(P)).hasSize(4);
        assertThat(service.getActiveAlerts()).hasSize(4);
        assertThat(service.getPluginAlerts("ghost")).isEmpty();
    }

    @Test
    @DisplayName("listeners observe alert creation and resolution")
    void alertLifecycleListeners() {
        service.registerPlugin(P);
        RecordingListener listener = new RecordingListener();
        service.registerHealthMonitorListener(listener);

        Alert alert = service.createAlert(P, AlertSeverity.WARNING, "T", "M");
        assertThat(listener.created).containsExactly(alert);

        service.resolveAlert(alert.getAlertId());
        assertThat(listener.resolved).hasSize(1);
        Alert resolved = listener.resolved.get(0);
        assertThat(resolved.isResolved()).isTrue();
        assertThat(resolved.getAlertId()).isEqualTo(alert.getAlertId());
        assertThat(resolved.getTimestamp()).isEqualTo(alert.getTimestamp());
        assertThat(resolved.getSeverity()).isEqualTo(AlertSeverity.WARNING);

        service.unregisterHealthMonitorListener(listener);
        service.createAlert(P, AlertSeverity.INFO, "X", "Y");
        assertThat(listener.created).hasSize(1);
    }

    @Test
    @DisplayName("resolveAlert removes only the matching alert")
    void resolveAlertTargetsById() {
        service.registerPlugin(P);
        Alert first = service.createAlert(P, AlertSeverity.INFO, "A", "1");
        Alert second = service.createAlert(P, AlertSeverity.INFO, "B", "2");

        try (LogCapture capture = LogCapture.attach(DefaultPluginMonitoringService.class)) {
            service.resolveAlert(second.getAlertId());
            assertThat(capture.formattedMessages())
                    .anyMatch(m -> m.contains("Alert resolved: " + second.getAlertId()));
        }

        assertThat(service.getPluginAlerts(P))
                .extracting(Alert::getAlertId)
                .containsExactly(first.getAlertId());

        List<Alert> history = service.getAlertHistory(10);
        assertThat(history).hasSize(1);
        assertThat(history.get(0).isResolved()).isTrue();
        assertThat(history.get(0).getTitle()).isEqualTo("B");

        // resolving an unknown alert is a silent no-op
        service.resolveAlert("no-such-alert");
        assertThat(service.getAlertHistory(10)).hasSize(1);
        assertThat(service.getPluginAlerts(P)).hasSize(1);
    }

    @Test
    @DisplayName("alert history returns the last N entries")
    void alertHistoryWindow() {
        service.registerPlugin(P);
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Alert alert = service.createAlert(P, AlertSeverity.INFO, "T" + i, "m");
            service.resolveAlert(alert.getAlertId());
            ids.add(alert.getAlertId());
        }

        assertThat(service.getAlertHistory(2))
                .extracting(Alert::getAlertId)
                .containsExactly(ids.get(1), ids.get(2));
        assertThat(service.getAlertHistory(10)).hasSize(3);
        assertThat(service.getAlertHistory(0)).isEmpty();

        try (LogCapture capture = LogCapture.attach(DefaultPluginMonitoringService.class)) {
            service.clearAlertHistory();
            assertThat(capture.formattedMessages())
                    .anyMatch(m -> m.contains("Alert history cleared"));
        }
        assertThat(service.getAlertHistory(10)).isEmpty();
    }

    @Test
    @DisplayName("cleanup removes alerts strictly older than seven days")
    void cleanupOldAlerts() throws Exception {
        long sevenDays = TimeUnit.DAYS.toMillis(7);
        long boundary = NOW - sevenDays;

        List<Alert> history = alertHistory();
        history.add(newHistoryAlert("old", boundary - 1));
        history.add(newHistoryAlert("boundary", boundary));
        history.add(newHistoryAlert("recent", NOW));

        try (LogCapture capture = LogCapture.attach(DefaultPluginMonitoringService.class)) {
            cleanup();
            assertThat(capture.formattedMessages())
                    .anyMatch(m -> m.contains("Cleaned up old alerts, history size: 2"));
        }

        assertThat(history)
                .extracting(Alert::getAlertId)
                .containsExactly("boundary", "recent");
    }

    // ------------------------------------------------------------------
    // Global statistics
    // ------------------------------------------------------------------

    @Test
    @DisplayName("global statistics aggregate every plugin")
    @SuppressWarnings("unchecked")
    void globalStatistics() {
        service.registerPlugin("a");
        service.registerPlugin("b");
        service.recordError("a", new RuntimeException("e1"));
        service.recordError("a", new RuntimeException("e2"));
        service.recordWarning("b", "w");
        service.createAlert("a", AlertSeverity.INFO, "t", "m");

        Map<String, Object> stats = service.getGlobalStatistics();
        assertThat(stats.keySet()).containsExactly(
                "systemCpuLoad", "systemMemoryUsage", "systemThreadCount", "totalPlugins",
                "totalPluginMemoryUsage", "averagePluginCpuUsage", "totalPluginErrors",
                "totalPluginWarnings", "totalActiveAlerts", "healthDistribution");
        assertThat(stats).contains(
                entry("totalPlugins", 2),
                entry("totalPluginMemoryUsage", 0L),
                entry("averagePluginCpuUsage", 0.0),
                entry("totalPluginErrors", 2),
                entry("totalPluginWarnings", 1),
                entry("totalActiveAlerts", 1));
        assertThat((Map<HealthStatus, Long>) stats.get("healthDistribution"))
                .containsEntry(HealthStatus.HEALTHY, 2L);
        assertThat((Integer) stats.get("systemThreadCount")).isPositive();
    }

    @Test
    @DisplayName("global statistics of an empty service default to zero")
    void globalStatisticsEmpty() {
        Map<String, Object> stats = service.getGlobalStatistics();
        assertThat(stats).contains(
                entry("totalPlugins", 0),
                entry("totalPluginMemoryUsage", 0L),
                entry("averagePluginCpuUsage", 0.0),
                entry("totalPluginErrors", 0),
                entry("totalPluginWarnings", 0),
                entry("totalActiveAlerts", 0));
    }

    // ------------------------------------------------------------------
    // Health report rendering and details
    // ------------------------------------------------------------------

    @Test
    @DisplayName("health report toString formats every component")
    void healthReportToString() throws Exception {
        service.registerPlugin(P);
        Object monitor = monitor(P);
        setMonitorField(monitor, "cpuUsage", 42.5);
        setMonitorField(monitor, "memoryUsage", 2L * 1024 * 1024);
        errors(monitor, 3);

        HealthReport report = service.getHealthReport(P);
        assertThat(report.getCpuUsage()).isEqualTo(42.5);
        assertThat(report.getErrorCount()).isEqualTo(3);
        assertThat(report.toString())
                .contains("plugin='" + P + "'")
                .contains("status=HEALTHY")
                .contains("cpu=42.5%")
                .contains("memory=2MB")
                .contains("errors=3");
    }

    @Test
    @DisplayName("fresh monitor details use a one minute floor for rates")
    void detailsFreshMonitor() {
        service.registerPlugin(P);
        service.recordError(P, new RuntimeException("e"));
        service.recordWarning(P, "w");

        Map<String, Object> details = service.getHealthReport(P).getDetails();
        assertThat(details).contains(entry("errorRate", 1.0), entry("warningRate", 1.0));
        assertThat(details.get("startTime")).isInstanceOf(Date.class);
    }

    @Test
    @DisplayName("detail rates are divided by whole uptime minutes")
    void detailsMinuteBuckets() throws Exception {
        service.registerPlugin(P);
        Object monitor = monitor(P);
        // three minutes in the past, safely away from rounding edges
        setMonitorField(monitor, "startTime", System.currentTimeMillis() - 185_000L);
        service.recordError(P, new RuntimeException("e"));

        Map<String, Object> details = service.getHealthReport(P).getDetails();
        assertThat((double) details.get("errorRate")).isCloseTo(1.0 / 3.0, within(0.01));
        assertThat((double) details.get("warningRate")).isCloseTo(0.0, within(0.01));
    }
}
