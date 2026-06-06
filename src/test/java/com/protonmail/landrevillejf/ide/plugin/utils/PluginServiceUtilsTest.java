package com.protonmail.landrevillejf.ide.plugin.utils;

import com.protonmail.landrevillejf.ide.plugin.ExtendedPluginContext;
import com.protonmail.landrevillejf.ide.plugin.service.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PluginServiceUtilsTest {

    @Mock
    private PluginMetricsService metricsService;

    @Mock
    private PluginMonitoringService monitoringService;

    @Mock
    private PluginDataStore dataStore;

    @Mock
    private PluginCacheService cacheService;

    @Mock
    private PluginDependencyResolver dependencyResolver;

    @Mock
    private PluginPermissionService permissionService;

    @Mock
    private PluginLoggingService loggingService;

    @Mock
    private ExtendedPluginContext context;

    @Mock
    private PluginMonitoringService.HealthReport healthReport;

    @Mock
    private PluginMonitoringService.Alert alert;

    private String pluginId = "test.plugin.id";
    private String backupId = "backup-12345";

    // No stubbings in setUp to avoid unnecessary stubbings

    @Test
    void generatePerformanceReport_ShouldReturnReportWithMetrics() {
        // Given
        Map<String, Object> mockMetrics = Map.of(
                "requestCount", 1000L,
                "errorCount", 5L,
                "avgResponseTime", 150L
        );
        when(metricsService.getAllMetrics(pluginId)).thenReturn(mockMetrics);

        // When
        Map<String, Object> report = PluginServiceUtils.generatePerformanceReport(metricsService, pluginId);

        // Then
        assertNotNull(report);
        assertTrue(report.containsKey("metrics"));
        assertTrue(report.containsKey("timestamp"));
        assertEquals(mockMetrics, report.get("metrics"));
        assertTrue((Long) report.get("timestamp") > 0);
        verify(metricsService).getAllMetrics(pluginId);
    }

    @Test
    void generatePerformanceReport_ShouldHandleEmptyMetrics() {
        // Given
        when(metricsService.getAllMetrics(pluginId)).thenReturn(new HashMap<>());

        // When
        Map<String, Object> report = PluginServiceUtils.generatePerformanceReport(metricsService, pluginId);

        // Then
        assertNotNull(report);
        assertTrue(((Map<?, ?>) report.get("metrics")).isEmpty());
    }

    @Test
    void generateHealthSummary_ShouldReturnHealthData() {
        // Given
        when(monitoringService.getHealthReport(pluginId)).thenReturn(healthReport);
        when(healthReport.getStatus()).thenReturn(PluginMonitoringService.HealthStatus.HEALTHY);
        when(healthReport.getCpuUsage()).thenReturn(25.5);
        when(healthReport.getMemoryUsage()).thenReturn(1024L);
        when(healthReport.getThreadCount()).thenReturn(8);
        when(healthReport.getUptime()).thenReturn(3600000L);
        when(healthReport.getErrorCount()).thenReturn(2);
        when(healthReport.getWarningCount()).thenReturn(5);

        // When
        Map<String, Object> summary = PluginServiceUtils.generateHealthSummary(monitoringService, pluginId);

        // Then
        assertNotNull(summary);
        assertEquals(PluginMonitoringService.HealthStatus.HEALTHY, summary.get("status"));
        assertEquals(25.5, summary.get("cpuUsage"));
        assertEquals(1024L, summary.get("memoryUsage"));
        assertEquals(8, summary.get("threadCount"));
        assertEquals(3600000L, summary.get("uptime"));
        assertEquals(2, summary.get("errorCount"));
        assertEquals(5, summary.get("warningCount"));
        assertTrue((Long) summary.get("timestamp") > 0);
        verify(monitoringService).getHealthReport(pluginId);
    }

    @Test
    void backupPluginData_ShouldReturnBackupId() {
        // Given
        when(dataStore.backup(pluginId)).thenReturn(backupId);

        // When
        String result = PluginServiceUtils.backupPluginData(dataStore, pluginId);

        // Then
        assertEquals(backupId, result);
        verify(dataStore).backup(pluginId);
    }

    @Test
    void restorePluginData_ShouldReturnTrue_WhenRestoreSuccessful() {
        // Given
        when(dataStore.restore(pluginId, backupId)).thenReturn(true);

        // When
        boolean result = PluginServiceUtils.restorePluginData(dataStore, pluginId, backupId);

        // Then
        assertTrue(result);
        verify(dataStore).restore(pluginId, backupId);
    }

    @Test
    void restorePluginData_ShouldReturnFalse_WhenRestoreFails() {
        // Given
        when(dataStore.restore(pluginId, backupId)).thenReturn(false);

        // When
        boolean result = PluginServiceUtils.restorePluginData(dataStore, pluginId, backupId);

        // Then
        assertFalse(result);
        verify(dataStore).restore(pluginId, backupId);
    }

    @Test
    void getPluginAlerts_ShouldReturnListOfAlerts() {
        // Given
        List<PluginMonitoringService.Alert> alerts = Arrays.asList(alert, alert);
        when(monitoringService.getPluginAlerts(pluginId)).thenReturn(alerts);

        // When
        List<PluginMonitoringService.Alert> result = PluginServiceUtils.getPluginAlerts(monitoringService, pluginId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(monitoringService).getPluginAlerts(pluginId);
    }

    @Test
    void getPluginAlerts_ShouldReturnEmptyList_WhenNoAlerts() {
        // Given
        when(monitoringService.getPluginAlerts(pluginId)).thenReturn(List.of());

        // When
        List<PluginMonitoringService.Alert> result = PluginServiceUtils.getPluginAlerts(monitoringService, pluginId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void hasCriticalIssues_ShouldReturnTrue_WhenCriticalAlertExists() {
        // Given
        when(monitoringService.getPluginAlerts(pluginId)).thenReturn(Arrays.asList(alert));
        when(alert.getSeverity()).thenReturn(PluginMonitoringService.AlertSeverity.CRITICAL);

        // When
        boolean result = PluginServiceUtils.hasCriticalIssues(monitoringService, pluginId);

        // Then
        assertTrue(result);
        verify(monitoringService).getPluginAlerts(pluginId);
    }

    @Test
    void hasCriticalIssues_ShouldReturnFalse_WhenNoCriticalAlerts() {
        // Given
        when(monitoringService.getPluginAlerts(pluginId)).thenReturn(Arrays.asList(alert));
        when(alert.getSeverity()).thenReturn(PluginMonitoringService.AlertSeverity.WARNING);

        // When
        boolean result = PluginServiceUtils.hasCriticalIssues(monitoringService, pluginId);

        // Then
        assertFalse(result);
        verify(monitoringService).getPluginAlerts(pluginId);
    }

    @Test
    void hasCriticalIssues_ShouldReturnFalse_WhenNoAlerts() {
        // Given
        when(monitoringService.getPluginAlerts(pluginId)).thenReturn(List.of());

        // When
        boolean result = PluginServiceUtils.hasCriticalIssues(monitoringService, pluginId);

        // Then
        assertFalse(result);
    }

    @Test
    void clearPluginState_ShouldClearCacheAndMetrics() {
        // When
        PluginServiceUtils.clearPluginState(cacheService, metricsService, pluginId);

        // Then
        verify(cacheService).clear(pluginId);
        verify(metricsService).resetMetrics(pluginId);
    }

    @Test
    void validateAllDependencies_ShouldReturnValidationResult() {
        // Given
        Map<String, Object> validationResult = Map.of(
                "valid", true,
                "missingDependencies", List.of()
        );
        when(dependencyResolver.validateDependencies(pluginId)).thenReturn(validationResult);

        // When
        Map<String, Object> result = PluginServiceUtils.validateAllDependencies(dependencyResolver, pluginId);

        // Then
        assertNotNull(result);
        assertTrue((Boolean) result.get("valid"));
        verify(dependencyResolver).validateDependencies(pluginId);
    }

    @Test
    void hasRequiredPermissions_ShouldReturnTrue_WhenAllPermissionsPresent() {
        // Given
        String[] permissions = {"read", "write", "execute"};
        when(permissionService.hasAllPermissions(pluginId, permissions)).thenReturn(true);

        // When
        boolean result = PluginServiceUtils.hasRequiredPermissions(permissionService, pluginId, permissions);

        // Then
        assertTrue(result);
        verify(permissionService).hasAllPermissions(pluginId, permissions);
    }

    @Test
    void hasRequiredPermissions_ShouldReturnFalse_WhenPermissionsMissing() {
        // Given
        String[] permissions = {"read", "write", "admin"};
        when(permissionService.hasAllPermissions(pluginId, permissions)).thenReturn(false);

        // When
        boolean result = PluginServiceUtils.hasRequiredPermissions(permissionService, pluginId, permissions);

        // Then
        assertFalse(result);
        verify(permissionService).hasAllPermissions(pluginId, permissions);
    }

    @Test
    void hasRequiredPermissions_WithEmptyPermissions_ShouldReturnTrue() {
        // Given
        String[] permissions = {};
        when(permissionService.hasAllPermissions(pluginId, permissions)).thenReturn(true);

        // When
        boolean result = PluginServiceUtils.hasRequiredPermissions(permissionService, pluginId, permissions);

        // Then
        assertTrue(result);
    }

    @Test
    void exportPluginDiagnostics_ShouldReturnCompleteDiagnosticData() {
        // Given
        setupMockServicesForDiagnostics();

        // When
        Map<String, Object> diagnostics = PluginServiceUtils.exportPluginDiagnostics(context, pluginId);

        // Then
        assertNotNull(diagnostics);
        assertTrue(diagnostics.containsKey("health"));
        assertTrue(diagnostics.containsKey("performance"));
        assertTrue(diagnostics.containsKey("dataStoreStats"));
        assertTrue(diagnostics.containsKey("cacheStats"));
        assertTrue(diagnostics.containsKey("alerts"));
        assertTrue(diagnostics.containsKey("dependencies"));
        assertTrue(diagnostics.containsKey("recentLogs"));
        assertTrue(diagnostics.containsKey("timestamp"));
    }

    @Test
    void cleanupObsoleteData_ShouldClearCacheAndDataStore() {
        // When
        PluginServiceUtils.cleanupObsoleteData(cacheService, dataStore, pluginId);

        // Then
        verify(cacheService).clear(pluginId);
        verify(dataStore).clear(pluginId);
    }

    @Test
    void generatePerformanceReport_WithNullPluginId_ShouldPassNullToService() {
        // When/Then
        Assertions.assertDoesNotThrow(() -> PluginServiceUtils.generatePerformanceReport(metricsService, null));
    }

    private void setupMockServicesForDiagnostics() {
        when(context.getMonitoringService()).thenReturn(monitoringService);
        when(context.getMetricsService()).thenReturn(metricsService);
        when(context.getDataStore()).thenReturn(dataStore);
        when(context.getCacheService()).thenReturn(cacheService);
        when(context.getDependencyResolver()).thenReturn(dependencyResolver);
        when(context.getLoggingService()).thenReturn(loggingService);

        when(monitoringService.getHealthReport(pluginId)).thenReturn(healthReport);
        when(healthReport.getStatus()).thenReturn(PluginMonitoringService.HealthStatus.HEALTHY);
        when(healthReport.getCpuUsage()).thenReturn(0.0);
        when(healthReport.getMemoryUsage()).thenReturn(0L);
        when(healthReport.getThreadCount()).thenReturn(0);
        when(healthReport.getUptime()).thenReturn(0L);
        when(healthReport.getErrorCount()).thenReturn(0);
        when(healthReport.getWarningCount()).thenReturn(0);

        when(metricsService.getAllMetrics(pluginId)).thenReturn(new HashMap<>());
        when(dataStore.getStatistics(pluginId)).thenReturn(new HashMap<>());
        when(cacheService.getStatistics(pluginId)).thenReturn(new HashMap<>());
        when(monitoringService.getPluginAlerts(pluginId)).thenReturn(List.of());
        when(dependencyResolver.validateDependencies(pluginId)).thenReturn(new HashMap<>());
        when(loggingService.getRecentLogs(pluginId, 50)).thenReturn(List.of());
    }
}