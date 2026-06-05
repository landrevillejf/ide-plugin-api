package com.protonmail.landrevillejf.swingide.plugin.service;

import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for PluginMonitoringService interface
 */
@DisplayName("PluginMonitoringService Tests")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class PluginMonitoringServiceTests {

    private PluginMonitoringService monitoringService;
    private static final String PLUGIN_ID = "test-plugin";

    @BeforeEach
    void setUp() {
        monitoringService = new MockPluginMonitoringService();
    }

    @Test
    @DisplayName("should get health report")
    void test_get_health_report() {
        PluginMonitoringService.HealthReport report = monitoringService.getHealthReport(PLUGIN_ID);

        assertThat(report).isNotNull();
        assertThat(report.getStatus()).isNotNull();
    }

    @Test
    @DisplayName("should get health status")
    void test_get_health_status() {
        PluginMonitoringService.HealthStatus status = monitoringService.getHealthStatus(PLUGIN_ID);

        assertThat(status).isNotNull();
    }

    @Test
    @DisplayName("should get global health status")
    void test_get_global_health_status() {
        PluginMonitoringService.HealthStatus status = monitoringService.getGlobalHealthStatus();

        assertThat(status).isNotNull();
    }

    @Test
    @DisplayName("should get CPU usage")
    void test_get_cpu_usage() {
        double cpuUsage = monitoringService.getCpuUsage(PLUGIN_ID);

        assertThat(cpuUsage).isGreaterThanOrEqualTo(0).isLessThanOrEqualTo(100);
    }

    @Test
    @DisplayName("should create alert")
    void test_create_alert() {
        PluginMonitoringService.Alert alert = monitoringService.createAlert(PLUGIN_ID,
                PluginMonitoringService.AlertSeverity.WARNING,
                "Warning", "Test warning");

        assertThat(alert).isNotNull();
    }

    @Test
    @DisplayName("should get active alerts")
    void test_get_active_alerts() {
        List<PluginMonitoringService.Alert> alerts = monitoringService.getActiveAlerts();

        assertThat(alerts).isNotNull();
    }

    @Test
    @DisplayName("should get all health reports")
    void test_get_all_health_reports() {
        List<PluginMonitoringService.HealthReport> reports = monitoringService.getAllHealthReports();

        assertThat(reports).isNotNull();
    }

    // Mock implementation
    public static class MockPluginMonitoringService implements PluginMonitoringService {
        @Override
        public HealthReport getHealthReport(String pluginId) {
            return new MockHealthReport();
        }

        @Override
        public HealthStatus getHealthStatus(String pluginId) {
            return HealthStatus.HEALTHY;
        }

        @Override
        public HealthStatus getGlobalHealthStatus() {
            return HealthStatus.HEALTHY;
        }

        @Override
        public double getCpuUsage(String pluginId) {
            return 0.5;
        }

        @Override
        public long getMemoryUsage(String pluginId) {
            return 1024000;
        }

        @Override
        public int getThreadCount(String pluginId) {
            return 5;
        }

        @Override
        public long getUptime(String pluginId) {
            return 3600000;
        }

        @Override
        public int getErrorCount(String pluginId) {
            return 0;
        }

        @Override
        public int getWarningCount(String pluginId) {
            return 0;
        }

        @Override
        public Alert createAlert(String pluginId, AlertSeverity severity, String title, String message) {
            return new MockAlert();
        }

        @Override
        public List<Alert> getActiveAlerts() {
            return java.util.Collections.emptyList();
        }

        @Override
        public List<Alert> getPluginAlerts(String pluginId) {
            return java.util.Collections.emptyList();
        }

        @Override
        public void resolveAlert(String alertId) {}

        @Override
        public List<Alert> getAlertHistory(int maxCount) {
            return java.util.Collections.emptyList();
        }

        @Override
        public void clearAlertHistory() {}

        @Override
        public List<HealthReport> getAllHealthReports() {
            return java.util.Collections.emptyList();
        }

        @Override
        public Map<String, Object> getGlobalStatistics() {
            return new java.util.HashMap<>();
        }

        @Override
        public void registerHealthMonitorListener(HealthMonitorListener listener) {}

        @Override
        public void unregisterHealthMonitorListener(HealthMonitorListener listener) {}
    }

    static class MockHealthReport implements PluginMonitoringService.HealthReport {
        @Override
        public String getPluginId() { return "test"; }
        @Override
        public PluginMonitoringService.HealthStatus getStatus() { return PluginMonitoringService.HealthStatus.HEALTHY; }
        @Override
        public double getCpuUsage() { return 0.5; }
        @Override
        public long getMemoryUsage() { return 1024000; }
        @Override
        public int getThreadCount() { return 5; }
        @Override
        public long getUptime() { return 3600000; }
        @Override
        public int getErrorCount() { return 0; }
        @Override
        public int getWarningCount() { return 0; }
        @Override
        public Map<String, Object> getDetails() { return new java.util.HashMap<>(); }
    }

    static class MockAlert implements PluginMonitoringService.Alert {
        @Override
        public String getAlertId() { return "alert-1"; }
        @Override
        public String getPluginId() { return "test"; }
        @Override
        public PluginMonitoringService.AlertSeverity getSeverity() { return PluginMonitoringService.AlertSeverity.WARNING; }
        @Override
        public String getTitle() { return "Test Alert"; }
        @Override
        public String getMessage() { return "Test message"; }
        @Override
        public long getTimestamp() { return System.currentTimeMillis(); }
        @Override
        public boolean isResolved() { return false; }
    }
}

/**
 * Tests for PluginLifecycleListener interface
 */
@DisplayName("PluginLifecycleListener Tests")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class PluginLifecycleListenerTests {

    @Test
    @DisplayName("should handle lifecycle events")
    void test_lifecycle_events() {
        PluginLifecycleListener listener = new PluginLifecycleListener() {
            @Override
            public void onLoaded(String pluginId) {
                assertThat(pluginId).isNotNull();
            }
        };

        listener.onLoaded("test-plugin");
        listener.onEnabled("test-plugin");
        listener.onDisabled("test-plugin");
        listener.onUnloaded("test-plugin");
    }

    @Test
    @DisplayName("should handle error events")
    void test_error_events() {
        PluginLifecycleListener listener = new PluginLifecycleListener() {
            @Override
            public void onPluginError(String pluginId, Throwable error) {
                assertThat(error).isNotNull();
            }
        };

        Throwable error = new RuntimeException("Test error");
        listener.onPluginError("test-plugin", error);
    }

    @Test
    @DisplayName("should handle state change events")
    void test_state_change_events() {
        PluginLifecycleListener listener = new PluginLifecycleListener() {
            @Override
            public void onStateChanged(String pluginId, String oldState, String newState) {
                assertThat(oldState).isNotNull();
                assertThat(newState).isNotNull();
            }
        };

        listener.onStateChanged("test-plugin", "DISABLED", "ENABLED");
    }
}

