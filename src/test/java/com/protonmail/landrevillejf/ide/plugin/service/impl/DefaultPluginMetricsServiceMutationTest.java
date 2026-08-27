package com.protonmail.landrevillejf.ide.plugin.service.impl;

import com.protonmail.landrevillejf.ide.plugin.service.PluginMetricsService.MetricType;
import com.protonmail.landrevillejf.ide.plugin.service.PluginMetricsService.TimerContext;
import com.protonmail.landrevillejf.ide.plugin.utils.LogCapture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

/**
 * Mutation-killing tests for {@link DefaultPluginMetricsService}.
 */
@DisplayName("DefaultPluginMetricsService mutation tests")
class DefaultPluginMetricsServiceMutationTest {

    private static final String P = "metrics-plugin";

    private DefaultPluginMetricsService service;

    @BeforeEach
    void setUp() {
        service = new DefaultPluginMetricsService();
    }

    @SuppressWarnings("unchecked")
    private Object innerMetric(String mapFieldName, String metricName) throws Exception {
        Field field = DefaultPluginMetricsService.class.getDeclaredField(mapFieldName);
        field.setAccessible(true);
        Map<String, Map<String, Object>> outer =
                (Map<String, Map<String, Object>>) field.get(service);
        return outer.get(P).get(metricName);
    }

    private static void resetAdders(Object metric) throws Exception {
        for (String name : new String[]{"count", "total"}) {
            Field field = metric.getClass().getDeclaredField(name);
            field.setAccessible(true);
            ((LongAdder) field.get(metric)).reset();
        }
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Nested
    @DisplayName("counters and gauges")
    class CounterTests {

        @Test
        @DisplayName("counters increment by one, by amount and decrement")
        void counterArithmetic() {
            try (LogCapture capture = LogCapture.attach(DefaultPluginMetricsService.class)) {
                service.incrementCounter(P, "c");
                service.incrementCounter(P, "c");
                service.incrementCounter(P, "c", 5);
                service.decrementCounter(P, "c");
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("Incremented counter c") && m.contains("by 5"))
                        .anyMatch(m -> m.contains("Decremented counter c"));
            }
            assertThat(service.getCounterValue(P, "c")).isEqualTo(6);
        }

        @Test
        @DisplayName("unknown plugins and metrics read as zero")
        void unknownCounter() {
            service.incrementCounter(P, "c");
            assertThat(service.getCounterValue("ghost", "c")).isZero();
            assertThat(service.getCounterValue(P, "other")).isZero();
        }

        @Test
        @DisplayName("gauges overwrite their value")
        void gauges() {
            try (LogCapture capture = LogCapture.attach(DefaultPluginMetricsService.class)) {
                service.setGauge(P, "g", 42);
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("Set gauge g"));
            }
            service.setGauge(P, "g", 7);
            assertThat(service.getMetricStatistics(P, "g"))
                    .contains(entry("type", "GAUGE"), entry("value", 7L));
        }
    }

    @Nested
    @DisplayName("timers")
    class TimerTests {

        @Test
        @DisplayName("timer statistics are exact")
        void timerStatistics() {
            try (LogCapture capture = LogCapture.attach(DefaultPluginMetricsService.class)) {
                service.recordTimer(P, "t", 10);
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("Recorded timer t"));
            }
            service.recordTimer(P, "t", 30);
            service.recordTimer(P, "t", 20);

            Map<String, Object> stats = service.getMetricStatistics(P, "t");
            assertThat(stats).contains(
                    entry("type", "TIMER"),
                    entry("count", 3L),
                    entry("total_ms", 60L),
                    entry("avg_ms", 20.0),
                    entry("min_ms", 10L),
                    entry("max_ms", 30L));
        }

        @Test
        @DisplayName("a timer without recordings reports zeroes")
        void timerZeroState() throws Exception {
            service.recordTimer(P, "t", 10);
            resetAdders(innerMetric("timers", "t"));

            Map<String, Object> stats = service.getMetricStatistics(P, "t");
            assertThat(stats).contains(
                    entry("count", 0L),
                    entry("total_ms", 0L),
                    entry("avg_ms", 0.0),
                    entry("min_ms", 0L),
                    entry("max_ms", 0L));
        }

        @Test
        @DisplayName("startTimer measures elapsed time and records exactly once")
        void timerContext() {
            TimerContext context = service.startTimer(P, "ctx");
            sleep(5);

            assertThat(context.getElapsedMillis()).isBetween(0L, 5_000L);
            long elapsed = context.stop();
            assertThat(elapsed).isBetween(0L, 5_000L);

            // stopping twice must not record twice
            long again = context.stop();
            assertThat(again).isBetween(0L, 5_000L);
            assertThat(service.getMetricStatistics(P, "ctx")).containsEntry("count", 1L);
        }

        @Test
        @DisplayName("closing a timer context records it")
        void timerContextClose() throws Exception {
            try (TimerContext context = service.startTimer(P, "closable")) {
                sleep(2);
            }
            assertThat(service.getMetricStatistics(P, "closable")).containsEntry("count", 1L);
        }
    }

    @Nested
    @DisplayName("histograms")
    class HistogramTests {

        @Test
        @DisplayName("histogram statistics include buckets at exact boundaries")
        void histogramStatistics() {
            try (LogCapture capture = LogCapture.attach(DefaultPluginMetricsService.class)) {
                service.recordHistogram(P, "h", 5);
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("Recorded histogram h"));
            }
            service.recordHistogram(P, "h", 10);
            service.recordHistogram(P, "h", 250);
            service.recordHistogram(P, "h", 20_000);

            Map<String, Object> stats = service.getMetricStatistics(P, "h");
            assertThat(stats).contains(
                    entry("type", "HISTOGRAM"),
                    entry("count", 4L),
                    entry("sum", 20_265L),
                    entry("avg", 20_265.0 / 4),
                    entry("min", 5L),
                    entry("max", 20_000L));

            @SuppressWarnings("unchecked")
            Map<String, Long> distribution = (Map<String, Long>) stats.get("distribution");
            assertThat(distribution)
                    .containsEntry("≤5", 1L)
                    .containsEntry("≤10", 1L)
                    .containsEntry("≤250", 1L)
                    .hasSize(3);

            @SuppressWarnings("unchecked")
            Map<String, Long> percentiles = (Map<String, Long>) stats.get("percentiles");
            assertThat(percentiles).containsOnly(
                    entry("p50", 0L), entry("p90", 0L),
                    entry("p95", 0L), entry("p99", 0L));
        }

        @Test
        @DisplayName("a histogram without recordings reports empty statistics")
        void histogramZeroState() throws Exception {
            service.recordHistogram(P, "h", 5);
            resetAdders(innerMetric("histograms", "h"));

            assertThat(service.getMetricStatistics(P, "h"))
                    .containsOnly(entry("type", "HISTOGRAM"));
        }
    }

    @Nested
    @DisplayName("aggregation and lifecycle")
    class AggregationTests {

        @Test
        @DisplayName("getAllMetrics exposes every section")
        @SuppressWarnings("unchecked")
        void allMetrics() {
            service.incrementCounter(P, "c", 3);
            service.recordTimer(P, "t", 15);
            service.recordHistogram(P, "h", 8);
            service.setGauge(P, "g", 9);

            Map<String, Object> all = service.getAllMetrics(P);
            assertThat(all.keySet())
                    .containsExactly("counters", "timers", "histograms", "gauges");
            assertThat((Map<String, Object>) all.get("counters")).containsEntry("c", 3L);
            assertThat((Map<String, Object>) all.get("gauges")).containsEntry("g", 9L);
            assertThat((Map<String, Object>) all.get("timers")).containsKey("t");
            assertThat((Map<String, Object>) all.get("histograms")).containsKey("h");
        }

        @Test
        @DisplayName("getAllMetrics of an unknown plugin is empty")
        void allMetricsUnknown() {
            assertThat(service.getAllMetrics("ghost")).isEmpty();
        }

        @Test
        @DisplayName("getMetricsByType returns per-type views")
        void metricsByType() {
            service.incrementCounter(P, "c", 2);
            service.recordTimer(P, "t", 5);
            service.recordHistogram(P, "h", 5);
            service.setGauge(P, "g", 5);

            assertThat(service.getMetricsByType(P, MetricType.COUNTER)).containsEntry("c", 2L);
            assertThat(service.getMetricsByType(P, MetricType.TIMER)).containsKey("t");
            assertThat(service.getMetricsByType(P, MetricType.HISTOGRAM)).containsKey("h");
            assertThat(service.getMetricsByType(P, MetricType.GAUGE)).containsEntry("g", 5L);

            for (MetricType type : MetricType.values()) {
                assertThat(service.getMetricsByType("ghost", type)).isEmpty();
            }
            assertThat(service.getMetricsByType(P, MetricType.TIMER)).isNotEmpty();
        }

        @Test
        @DisplayName("getMetricStatistics of an unknown metric is empty")
        void unknownStatistics() {
            assertThat(service.getMetricStatistics("ghost", "x")).isEmpty();
            service.incrementCounter(P, "c");
            assertThat(service.getMetricStatistics(P, "x")).isEmpty();
        }

        @Test
        @DisplayName("exportMetrics wraps the metrics with identity and timestamp")
        @SuppressWarnings("unchecked")
        void export() {
            service.incrementCounter(P, "c");
            Map<String, Object> export = service.exportMetrics(P);
            assertThat(export).containsKey("timestamp");
            assertThat(export).containsEntry("pluginId", P);
            assertThat((Map<String, Object>) export.get("metrics")).containsKey("counters");
        }

        @Test
        @DisplayName("resetMetrics wipes every metric family")
        void resetAll() {
            service.incrementCounter(P, "c");
            service.recordTimer(P, "t", 1);
            service.recordHistogram(P, "h", 1);
            service.setGauge(P, "g", 1);

            try (LogCapture capture = LogCapture.attach(DefaultPluginMetricsService.class)) {
                service.resetMetrics(P);
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("Reset all metrics"));
            }
            assertThat(service.getAllMetrics(P)).isEmpty();
        }

        @Test
        @DisplayName("resetMetric removes the metric from every family")
        void resetSingle() {
            service.incrementCounter(P, "shared");
            service.recordTimer(P, "shared", 1);
            service.recordHistogram(P, "shared", 1);
            service.setGauge(P, "shared", 1);
            service.incrementCounter(P, "keep");

            try (LogCapture capture = LogCapture.attach(DefaultPluginMetricsService.class)) {
                service.resetMetric(P, "shared");
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("Reset metric shared"));
            }

            for (MetricType type : MetricType.values()) {
                assertThat(service.getMetricsByType(P, type)).doesNotContainKey("shared");
            }
            assertThat(service.getCounterValue(P, "keep")).isEqualTo(1);

            // resetting on an unknown plugin is a silent no-op
            service.resetMetric("ghost", "shared");
        }
    }
}
