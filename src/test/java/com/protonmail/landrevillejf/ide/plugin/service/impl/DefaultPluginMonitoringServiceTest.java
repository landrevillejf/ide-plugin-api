package com.protonmail.landrevillejf.ide.plugin.service.impl;

import com.protonmail.landrevillejf.ide.plugin.service.PluginMonitoringService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class DefaultPluginMonitoringServiceTest {

    private DefaultPluginMonitoringService monitoringService;
    private static final String TEST_PLUGIN = "test-plugin";
    private static final String TEST_PLUGIN_2 = "test-plugin-2";

    @BeforeEach
    void setUp() {
        monitoringService = new DefaultPluginMonitoringService();
        monitoringService.registerPlugin(TEST_PLUGIN);
        monitoringService.registerPlugin(TEST_PLUGIN_2);
    }

    @Test
    void getHealthReport() {
        PluginMonitoringService.HealthReport report = monitoringService.getHealthReport(TEST_PLUGIN);

        assertNotNull(report);
        assertEquals(TEST_PLUGIN, report.getPluginId());
        assertNotNull(report.getStatus());
        assertTrue(report.getCpuUsage() >= 0);
        assertTrue(report.getMemoryUsage() >= 0);
    }

    @Test
    void getHealthReportForUnknownPlugin() {
        PluginMonitoringService.HealthReport report = monitoringService.getHealthReport("unknown-plugin");

        assertNotNull(report);
        assertEquals("unknown-plugin", report.getPluginId());
        assertEquals(PluginMonitoringService.HealthStatus.UNKNOWN, report.getStatus());
    }

    @Test
    void getHealthStatus() {
        PluginMonitoringService.HealthStatus status = monitoringService.getHealthStatus(TEST_PLUGIN);

        assertNotNull(status);
    }

    @Test
    void getGlobalHealthStatus() {
        PluginMonitoringService.HealthStatus status = monitoringService.getGlobalHealthStatus();

        assertNotNull(status);
    }

    @Test
    void getCpuUsage() {
        double cpuUsage = monitoringService.getCpuUsage(TEST_PLUGIN);

        assertTrue(cpuUsage >= 0);
        assertTrue(cpuUsage <= 100);
    }

    @Test
    void getMemoryUsage() {
        long memoryUsage = monitoringService.getMemoryUsage(TEST_PLUGIN);

        assertTrue(memoryUsage >= 0);
    }

    @Test
    void getThreadCount() {
        int threadCount = monitoringService.getThreadCount(TEST_PLUGIN);

        assertTrue(threadCount >= 0);
    }

    @Test
    void getUptime() {
        long uptime = monitoringService.getUptime(TEST_PLUGIN);

        assertTrue(uptime >= 0);
    }

    @Test
    void getErrorCount() {
        assertEquals(0, monitoringService.getErrorCount(TEST_PLUGIN));

        monitoringService.recordError(TEST_PLUGIN, new RuntimeException("Test error"));
        monitoringService.recordError(TEST_PLUGIN, new RuntimeException("Another error"));

        assertEquals(2, monitoringService.getErrorCount(TEST_PLUGIN));
    }

    @Test
    void getWarningCount() {
        assertEquals(0, monitoringService.getWarningCount(TEST_PLUGIN));

        monitoringService.recordWarning(TEST_PLUGIN, "Test warning");
        monitoringService.recordWarning(TEST_PLUGIN, "Another warning");

        assertEquals(2, monitoringService.getWarningCount(TEST_PLUGIN));
    }

    @Test
    void createAlert() {
        PluginMonitoringService.Alert alert = monitoringService.createAlert(
                TEST_PLUGIN,
                PluginMonitoringService.AlertSeverity.ERROR,
                "Test Alert",
                "This is a test alert message"
        );

        assertNotNull(alert);
        assertNotNull(alert.getAlertId());
        assertEquals(TEST_PLUGIN, alert.getPluginId());
        assertEquals(PluginMonitoringService.AlertSeverity.ERROR, alert.getSeverity());
        assertEquals("Test Alert", alert.getTitle());
        assertEquals("This is a test alert message", alert.getMessage());
        assertFalse(alert.isResolved());
    }

    @Test
    void getActiveAlerts() {
        monitoringService.createAlert(TEST_PLUGIN, PluginMonitoringService.AlertSeverity.WARNING,
                "Alert 1", "Message 1");
        monitoringService.createAlert(TEST_PLUGIN_2, PluginMonitoringService.AlertSeverity.ERROR,
                "Alert 2", "Message 2");

        List<PluginMonitoringService.Alert> activeAlerts = monitoringService.getActiveAlerts();

        assertEquals(2, activeAlerts.size());
    }

    @Test
    void getPluginAlerts() {
        monitoringService.createAlert(TEST_PLUGIN, PluginMonitoringService.AlertSeverity.WARNING,
                "Alert 1", "Message 1");
        monitoringService.createAlert(TEST_PLUGIN, PluginMonitoringService.AlertSeverity.ERROR,
                "Alert 2", "Message 2");
        monitoringService.createAlert(TEST_PLUGIN_2, PluginMonitoringService.AlertSeverity.INFO,
                "Alert 3", "Message 3");

        List<PluginMonitoringService.Alert> pluginAlerts = monitoringService.getPluginAlerts(TEST_PLUGIN);

        assertEquals(2, pluginAlerts.size());
    }

    @Test
    void resolveAlert() {
        PluginMonitoringService.Alert alert = monitoringService.createAlert(
                TEST_PLUGIN,
                PluginMonitoringService.AlertSeverity.WARNING,
                "Test Alert",
                "Message"
        );

        assertFalse(monitoringService.getPluginAlerts(TEST_PLUGIN).isEmpty());

        monitoringService.resolveAlert(alert.getAlertId());

        assertTrue(monitoringService.getPluginAlerts(TEST_PLUGIN).isEmpty());
    }

    @Test
    void getAlertHistory() throws InterruptedException {
        PluginMonitoringService.Alert alert = monitoringService.createAlert(
                TEST_PLUGIN,
                PluginMonitoringService.AlertSeverity.WARNING,
                "Test Alert",
                "Message"
        );

        Thread.sleep(10);
        monitoringService.resolveAlert(alert.getAlertId());

        List<PluginMonitoringService.Alert> history = monitoringService.getAlertHistory(10);

        assertFalse(history.isEmpty());
        assertTrue(history.get(0).isResolved());
    }

    @Test
    void clearAlertHistory() {
        monitoringService.createAlert(TEST_PLUGIN, PluginMonitoringService.AlertSeverity.WARNING,
                "Alert 1", "Message 1");
        monitoringService.resolveAlert(monitoringService.getPluginAlerts(TEST_PLUGIN).get(0).getAlertId());

        assertFalse(monitoringService.getAlertHistory(10).isEmpty());

        monitoringService.clearAlertHistory();

        assertTrue(monitoringService.getAlertHistory(10).isEmpty());
    }

    @Test
    void getAllHealthReports() {
        List<PluginMonitoringService.HealthReport> reports = monitoringService.getAllHealthReports();

        assertEquals(2, reports.size());
        assertTrue(reports.stream().anyMatch(r -> r.getPluginId().equals(TEST_PLUGIN)));
        assertTrue(reports.stream().anyMatch(r -> r.getPluginId().equals(TEST_PLUGIN_2)));
    }

    @Test
    void getGlobalStatistics() {
        monitoringService.recordError(TEST_PLUGIN, new RuntimeException("Error"));
        monitoringService.recordWarning(TEST_PLUGIN, "Warning");

        Map<String, Object> stats = monitoringService.getGlobalStatistics();

        assertNotNull(stats);
        assertTrue(stats.containsKey("totalPlugins"));
        assertTrue(stats.containsKey("totalPluginErrors"));
        assertTrue(stats.containsKey("totalPluginWarnings"));
        assertTrue(stats.containsKey("totalActiveAlerts"));
        assertTrue(stats.containsKey("healthDistribution"));

        assertEquals(2, stats.get("totalPlugins"));
        assertTrue((Integer) stats.get("totalPluginErrors") >= 1);
        assertTrue((Integer) stats.get("totalPluginWarnings") >= 1);
    }

    @Test
    void registerHealthMonitorListener() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        AtomicBoolean alertCreated = new AtomicBoolean(false);
        AtomicBoolean alertResolved = new AtomicBoolean(false);

        PluginMonitoringService.HealthMonitorListener listener = new PluginMonitoringService.HealthMonitorListener() {
            @Override
            public void onAlertCreated(PluginMonitoringService.Alert alert) {
                alertCreated.set(true);
                latch.countDown();
            }

            @Override
            public void onAlertResolved(PluginMonitoringService.Alert alert) {
                alertResolved.set(true);
                latch.countDown();
            }

            @Override
            public void onHealthChanged(PluginMonitoringService.HealthReport report) {
                // Optional
            }
        };

        monitoringService.registerHealthMonitorListener(listener);

        PluginMonitoringService.Alert alert = monitoringService.createAlert(
                TEST_PLUGIN,
                PluginMonitoringService.AlertSeverity.WARNING,
                "Test Alert",
                "Message"
        );

        monitoringService.resolveAlert(alert.getAlertId());

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertTrue(alertCreated.get());
        assertTrue(alertResolved.get());

        monitoringService.unregisterHealthMonitorListener(listener);
    }

    @Test
    void registerPlugin() {
        monitoringService.registerPlugin("new-plugin");

        PluginMonitoringService.HealthReport report = monitoringService.getHealthReport("new-plugin");

        assertNotNull(report);
        assertEquals("new-plugin", report.getPluginId());
    }

    @Test
    void unregisterPlugin() {
        monitoringService.unregisterPlugin(TEST_PLUGIN);

        PluginMonitoringService.HealthReport report = monitoringService.getHealthReport(TEST_PLUGIN);

        // Should return empty report (not null)
        assertNotNull(report);
        assertEquals(TEST_PLUGIN, report.getPluginId());
        assertEquals(PluginMonitoringService.HealthStatus.UNKNOWN, report.getStatus());
    }

    @Test
    void recordError() {
        assertEquals(0, monitoringService.getErrorCount(TEST_PLUGIN));

        monitoringService.recordError(TEST_PLUGIN, new RuntimeException("Test error"));

        assertEquals(1, monitoringService.getErrorCount(TEST_PLUGIN));
    }

    @Test
    void recordWarning() {
        assertEquals(0, monitoringService.getWarningCount(TEST_PLUGIN));

        monitoringService.recordWarning(TEST_PLUGIN, "Test warning");

        assertEquals(1, monitoringService.getWarningCount(TEST_PLUGIN));
    }

    @Test
    void errorCountTriggersAlert() {
        // Record 10 errors should trigger an alert
        for (int i = 0; i < 10; i++) {
            monitoringService.recordError(TEST_PLUGIN, new RuntimeException("Error " + i));
        }

        // Allow time for the monitoring thread to process
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        List<PluginMonitoringService.Alert> alerts = monitoringService.getPluginAlerts(TEST_PLUGIN);

        assertFalse(alerts.isEmpty());
        assertTrue(alerts.stream().anyMatch(a -> a.getTitle().contains("Multiple Errors")));
    }

    @Test
    void warningCountTriggersAlert() {
        // Record 20 warnings should trigger an alert
        for (int i = 0; i < 20; i++) {
            monitoringService.recordWarning(TEST_PLUGIN, "Warning " + i);
        }

        // Allow time for the monitoring thread to process
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        List<PluginMonitoringService.Alert> alerts = monitoringService.getPluginAlerts(TEST_PLUGIN);

        assertFalse(alerts.isEmpty());
        assertTrue(alerts.stream().anyMatch(a -> a.getTitle().contains("Multiple Warnings")));
    }
}