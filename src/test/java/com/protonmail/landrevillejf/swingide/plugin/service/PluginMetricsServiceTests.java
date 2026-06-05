package com.protonmail.landrevillejf.swingide.plugin.service;

import org.junit.jupiter.api.*;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * Tests for PluginMetricsService interface
 */
@DisplayName("PluginMetricsService Tests")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class PluginMetricsServiceTests {

    private PluginMetricsService metricsService;
    private static final String PLUGIN_ID = "test-plugin";

    @BeforeEach
    void setUp() {
        metricsService = new MockPluginMetricsService();
    }

    @Test
    @DisplayName("should increment counter")
    void test_increment_counter() {
        metricsService.incrementCounter(PLUGIN_ID, "requests");
        metricsService.incrementCounter(PLUGIN_ID, "requests");

        long value = metricsService.getCounterValue(PLUGIN_ID, "requests");

        assertThat(value).isEqualTo(2);
    }

    @Test
    @DisplayName("should increment counter by amount")
    void test_increment_counter_by_amount() {
        metricsService.incrementCounter(PLUGIN_ID, "requests", 5);

        long value = metricsService.getCounterValue(PLUGIN_ID, "requests");

        assertThat(value).isEqualTo(5);
    }

    @Test
    @DisplayName("should decrement counter")
    void test_decrement_counter() {
        metricsService.incrementCounter(PLUGIN_ID, "requests", 10);
        metricsService.decrementCounter(PLUGIN_ID, "requests");

        long value = metricsService.getCounterValue(PLUGIN_ID, "requests");

        assertThat(value).isEqualTo(9);
    }

    @Test
    @DisplayName("should record timer measurement")
    void test_record_timer() {
        assertThatNoException().isThrownBy(() ->
            metricsService.recordTimer(PLUGIN_ID, "response.time", 150)
        );
    }

    @Test
    @DisplayName("should start and stop timer context")
    void test_timer_context() {
        assertThatNoException().isThrownBy(() -> {
            try (PluginMetricsService.TimerContext timer = metricsService.startTimer(PLUGIN_ID, "operation")) {
                Thread.sleep(10);
                long elapsed = timer.getElapsedMillis();
                assertThat(elapsed).isGreaterThanOrEqualTo(0);
            }
        });
    }

    @Test
    @DisplayName("should record histogram value")
    void test_record_histogram() {
        assertThatNoException().isThrownBy(() -> {
            metricsService.recordHistogram(PLUGIN_ID, "response.size", 256);
            metricsService.recordHistogram(PLUGIN_ID, "response.size", 512);
        });
    }

    @Test
    @DisplayName("should set gauge value")
    void test_set_gauge() {
        metricsService.setGauge(PLUGIN_ID, "memory.usage", 1024);

        long value = metricsService.getCounterValue(PLUGIN_ID, "memory.usage");

        assertThat(value).isEqualTo(1024);
    }

    @Test
    @DisplayName("should get all metrics")
    void test_get_all_metrics() {
        metricsService.incrementCounter(PLUGIN_ID, "requests");

        Map<String, Object> metrics = metricsService.getAllMetrics(PLUGIN_ID);

        assertThat(metrics).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("should reset metrics")
    void test_reset_metrics() {
        metricsService.incrementCounter(PLUGIN_ID, "requests", 10);
        metricsService.resetMetrics(PLUGIN_ID);

        long value = metricsService.getCounterValue(PLUGIN_ID, "requests");

        assertThat(value).isZero();
    }

    @Test
    @DisplayName("should get metric statistics")
    void test_get_metric_statistics() {
        metricsService.recordTimer(PLUGIN_ID, "response.time", 100);
        metricsService.recordTimer(PLUGIN_ID, "response.time", 200);

        Map<String, Object> stats = metricsService.getMetricStatistics(PLUGIN_ID, "response.time");

        assertThat(stats).isNotNull();
    }

    // Mock implementation for testing
    public static class MockPluginMetricsService implements PluginMetricsService {
        private final Map<String, Map<String, Long>> counters = new java.util.HashMap<>();

        @Override
        public void incrementCounter(String pluginId, String metricName) {
            incrementCounter(pluginId, metricName, 1);
        }

        @Override
        public void incrementCounter(String pluginId, String metricName, long amount) {
            Map<String, Long> pluginMetrics = counters.computeIfAbsent(pluginId, k -> new java.util.HashMap<>());
            pluginMetrics.put(metricName, pluginMetrics.getOrDefault(metricName, 0L) + amount);
        }

        @Override
        public void decrementCounter(String pluginId, String metricName) {
            incrementCounter(pluginId, metricName, -1);
        }

        @Override
        public void recordTimer(String pluginId, String metricName, long durationMillis) {
            incrementCounter(pluginId, metricName + ".count", 1);
        }

        @Override
        public TimerContext startTimer(String pluginId, String metricName) {
            return new MockTimerContext();
        }

        @Override
        public void recordHistogram(String pluginId, String metricName, long value) {
            incrementCounter(pluginId, metricName + ".histogram", value);
        }

        @Override
        public void setGauge(String pluginId, String metricName, long value) {
            Map<String, Long> pluginMetrics = counters.computeIfAbsent(pluginId, k -> new java.util.HashMap<>());
            pluginMetrics.put(metricName, value);
        }

        @Override
        public long getCounterValue(String pluginId, String metricName) {
            return counters.getOrDefault(pluginId, new java.util.HashMap<>()).getOrDefault(metricName, 0L);
        }

        @Override
        public Map<String, Object> getAllMetrics(String pluginId) {
            Map<String, Object> result = new java.util.HashMap<>();
            result.putAll(counters.getOrDefault(pluginId, new java.util.HashMap<>()));
            return result;
        }

        @Override
        public Map<String, Object> getMetricsByType(String pluginId, MetricType type) {
            return new java.util.HashMap<>();
        }

        @Override
        public void resetMetrics(String pluginId) {
            counters.remove(pluginId);
        }

        @Override
        public void resetMetric(String pluginId, String metricName) {
            counters.getOrDefault(pluginId, new java.util.HashMap<>()).remove(metricName);
        }

        @Override
        public Map<String, Object> getMetricStatistics(String pluginId, String metricName) {
            Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("value", getCounterValue(pluginId, metricName));
            return stats;
        }

        @Override
        public Map<String, Object> exportMetrics(String pluginId) {
            return getAllMetrics(pluginId);
        }
    }

    static class MockTimerContext implements PluginMetricsService.TimerContext {
        private final long startTime = System.currentTimeMillis();

        @Override
        public long getElapsedMillis() {
            return System.currentTimeMillis() - startTime;
        }

        @Override
        public long stop() {
            return getElapsedMillis();
        }

        @Override
        public void close() {}
    }
}

