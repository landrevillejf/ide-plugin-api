package com.protonmail.landrevillejf.ide.plugin.service.impl;

import com.protonmail.landrevillejf.ide.plugin.service.PluginMetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DefaultPluginMetricsServiceTest {

    private DefaultPluginMetricsService metricsService;
    private static final String TEST_PLUGIN = "test-plugin";
    private static final String TEST_PLUGIN_2 = "test-plugin-2";
    private static final String METRIC_NAME = "test-metric";

    @BeforeEach
    void setUp() {
        metricsService = new DefaultPluginMetricsService();
    }

    @Test
    void incrementCounter() {
        metricsService.incrementCounter(TEST_PLUGIN, METRIC_NAME);

        long value = metricsService.getCounterValue(TEST_PLUGIN, METRIC_NAME);
        assertEquals(1, value);
    }

    @Test
    void testIncrementCounter() {
        metricsService.incrementCounter(TEST_PLUGIN, METRIC_NAME, 5);

        long value = metricsService.getCounterValue(TEST_PLUGIN, METRIC_NAME);
        assertEquals(5, value);
    }

    @Test
    void decrementCounter() {
        metricsService.incrementCounter(TEST_PLUGIN, METRIC_NAME, 10);
        metricsService.decrementCounter(TEST_PLUGIN, METRIC_NAME);

        long value = metricsService.getCounterValue(TEST_PLUGIN, METRIC_NAME);
        assertEquals(9, value);
    }

    @Test
    void recordTimer() {
        metricsService.recordTimer(TEST_PLUGIN, METRIC_NAME, 100);
        metricsService.recordTimer(TEST_PLUGIN, METRIC_NAME, 200);

        Map<String, Object> stats = metricsService.getMetricStatistics(TEST_PLUGIN, METRIC_NAME);

        assertEquals("TIMER", stats.get("type"));
        assertEquals(2L, stats.get("count"));
        assertEquals(300L, stats.get("total_ms"));
        assertEquals(150.0, (Double) stats.get("avg_ms"), 0.01);
        assertEquals(100L, stats.get("min_ms"));
        assertEquals(200L, stats.get("max_ms"));
    }

    @Test
    void startTimer() throws Exception {
        try (PluginMetricsService.TimerContext timer = metricsService.startTimer(TEST_PLUGIN, METRIC_NAME)) {
            Thread.sleep(50);
        }

        Map<String, Object> stats = metricsService.getMetricStatistics(TEST_PLUGIN, METRIC_NAME);

        assertEquals("TIMER", stats.get("type"));
        assertEquals(1L, stats.get("count"));
        assertTrue((Long) stats.get("min_ms") >= 45);
        assertTrue((Long) stats.get("max_ms") <= 100);
    }

    @Test
    void recordHistogram() {
        metricsService.recordHistogram(TEST_PLUGIN, METRIC_NAME, 5);
        metricsService.recordHistogram(TEST_PLUGIN, METRIC_NAME, 25);
        metricsService.recordHistogram(TEST_PLUGIN, METRIC_NAME, 100);

        Map<String, Object> stats = metricsService.getMetricStatistics(TEST_PLUGIN, METRIC_NAME);

        assertEquals("HISTOGRAM", stats.get("type"));
        assertEquals(3L, stats.get("count"));
        assertEquals(130L, stats.get("sum"));
        assertEquals(5L, stats.get("min"));
        assertEquals(100L, stats.get("max"));

        @SuppressWarnings("unchecked")
        Map<String, Long> distribution = (Map<String, Long>) stats.get("distribution");
        assertNotNull(distribution);
    }

    @Test
    void setGauge() {
        metricsService.setGauge(TEST_PLUGIN, METRIC_NAME, 42);

        Map<String, Object> stats = metricsService.getMetricStatistics(TEST_PLUGIN, METRIC_NAME);

        assertEquals("GAUGE", stats.get("type"));
        assertEquals(42L, stats.get("value"));
    }

    @Test
    void getCounterValue() {
        assertEquals(0, metricsService.getCounterValue(TEST_PLUGIN, METRIC_NAME));

        metricsService.incrementCounter(TEST_PLUGIN, METRIC_NAME);
        assertEquals(1, metricsService.getCounterValue(TEST_PLUGIN, METRIC_NAME));
    }

    @Test
    void getAllMetrics() {
        metricsService.incrementCounter(TEST_PLUGIN, "counter1");
        metricsService.incrementCounter(TEST_PLUGIN, "counter2", 5);
        metricsService.recordTimer(TEST_PLUGIN, "timer1", 100);
        metricsService.setGauge(TEST_PLUGIN, "gauge1", 99);

        Map<String, Object> allMetrics = metricsService.getAllMetrics(TEST_PLUGIN);

        assertTrue(allMetrics.containsKey("counters"));
        assertTrue(allMetrics.containsKey("timers"));
        assertTrue(allMetrics.containsKey("gauges"));

        @SuppressWarnings("unchecked")
        Map<String, Long> counters = (Map<String, Long>) allMetrics.get("counters");
        assertEquals(1L, counters.get("counter1"));
        assertEquals(5L, counters.get("counter2"));

        @SuppressWarnings("unchecked")
        Map<String, Long> gauges = (Map<String, Long>) allMetrics.get("gauges");
        assertEquals(99L, gauges.get("gauge1"));
    }

    @Test
    void getMetricsByType() {
        metricsService.incrementCounter(TEST_PLUGIN, "counter1");
        metricsService.incrementCounter(TEST_PLUGIN, "counter2");
        metricsService.recordTimer(TEST_PLUGIN, "timer1", 100);

        Map<String, Object> counters = metricsService.getMetricsByType(TEST_PLUGIN, PluginMetricsService.MetricType.COUNTER);
        Map<String, Object> timers = metricsService.getMetricsByType(TEST_PLUGIN, PluginMetricsService.MetricType.TIMER);

        assertEquals(2, counters.size());
        assertTrue(counters.containsKey("counter1"));
        assertTrue(counters.containsKey("counter2"));

        assertTrue(timers.containsKey("timer1"));
    }

    @Test
    void resetMetrics() {
        metricsService.incrementCounter(TEST_PLUGIN, METRIC_NAME);
        metricsService.recordTimer(TEST_PLUGIN, METRIC_NAME, 100);

        assertFalse(metricsService.getAllMetrics(TEST_PLUGIN).isEmpty());

        metricsService.resetMetrics(TEST_PLUGIN);

        assertTrue(metricsService.getAllMetrics(TEST_PLUGIN).isEmpty());
    }

    @Test
    void resetMetric() {
        metricsService.incrementCounter(TEST_PLUGIN, "counter1");
        metricsService.incrementCounter(TEST_PLUGIN, "counter2");

        metricsService.resetMetric(TEST_PLUGIN, "counter1");

        Map<String, Object> counters = metricsService.getMetricsByType(TEST_PLUGIN, PluginMetricsService.MetricType.COUNTER);
        assertEquals(1, counters.size());
        assertTrue(counters.containsKey("counter2"));
        assertFalse(counters.containsKey("counter1"));
    }

    @Test
    void getMetricStatistics() {
        metricsService.incrementCounter(TEST_PLUGIN, METRIC_NAME);

        Map<String, Object> stats = metricsService.getMetricStatistics(TEST_PLUGIN, METRIC_NAME);

        assertNotNull(stats);
        assertEquals("COUNTER", stats.get("type"));
        assertEquals(1L, stats.get("value"));
    }

    @Test
    void getMetricStatisticsForNonExistent() {
        Map<String, Object> stats = metricsService.getMetricStatistics(TEST_PLUGIN, "non-existent");

        assertTrue(stats.isEmpty());
    }

    @Test
    void exportMetrics() {
        metricsService.incrementCounter(TEST_PLUGIN, METRIC_NAME);
        metricsService.recordTimer(TEST_PLUGIN, "timer1", 100);

        Map<String, Object> export = metricsService.exportMetrics(TEST_PLUGIN);

        assertTrue(export.containsKey("pluginId"));
        assertTrue(export.containsKey("timestamp"));
        assertTrue(export.containsKey("metrics"));

        assertEquals(TEST_PLUGIN, export.get("pluginId"));

        @SuppressWarnings("unchecked")
        Map<String, Object> metrics = (Map<String, Object>) export.get("metrics");
        assertTrue(metrics.containsKey("counters"));
    }

    @Test
    void multiplePluginsIsolation() {
        metricsService.incrementCounter(TEST_PLUGIN, METRIC_NAME);
        metricsService.incrementCounter(TEST_PLUGIN_2, METRIC_NAME);

        assertEquals(1, metricsService.getCounterValue(TEST_PLUGIN, METRIC_NAME));
        assertEquals(1, metricsService.getCounterValue(TEST_PLUGIN_2, METRIC_NAME));

        metricsService.resetMetrics(TEST_PLUGIN);

        assertEquals(0, metricsService.getCounterValue(TEST_PLUGIN, METRIC_NAME));
        assertEquals(1, metricsService.getCounterValue(TEST_PLUGIN_2, METRIC_NAME));
    }

    @Test
    void multipleTimersSameName() {
        metricsService.recordTimer(TEST_PLUGIN, METRIC_NAME, 100);
        metricsService.recordTimer(TEST_PLUGIN, METRIC_NAME, 200);
        metricsService.recordTimer(TEST_PLUGIN, METRIC_NAME, 300);

        Map<String, Object> stats = metricsService.getMetricStatistics(TEST_PLUGIN, METRIC_NAME);

        assertEquals(3L, stats.get("count"));
        assertEquals(600L, stats.get("total_ms"));
        assertEquals(200.0, (Double) stats.get("avg_ms"), 0.01);
        assertEquals(100L, stats.get("min_ms"));
        assertEquals(300L, stats.get("max_ms"));
    }

    @Test
    void timerContextManualStop() {
        PluginMetricsService.TimerContext timer = metricsService.startTimer(TEST_PLUGIN, METRIC_NAME);

        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        long elapsed = timer.stop();

        assertTrue(elapsed >= 45);

        Map<String, Object> stats = metricsService.getMetricStatistics(TEST_PLUGIN, METRIC_NAME);
        assertEquals(1L, stats.get("count"));
    }

    @Test
    void histogramWithBuckets() {
        metricsService.recordHistogram(TEST_PLUGIN, METRIC_NAME, 1);
        metricsService.recordHistogram(TEST_PLUGIN, METRIC_NAME, 3);
        metricsService.recordHistogram(TEST_PLUGIN, METRIC_NAME, 7);
        metricsService.recordHistogram(TEST_PLUGIN, METRIC_NAME, 20);
        metricsService.recordHistogram(TEST_PLUGIN, METRIC_NAME, 100);

        Map<String, Object> stats = metricsService.getMetricStatistics(TEST_PLUGIN, METRIC_NAME);

        assertEquals(5L, stats.get("count"));
        @SuppressWarnings("unchecked")
        Map<String, Long> distribution = (Map<String, Long>) stats.get("distribution");
        assertNotNull(distribution);
    }
}