package com.protonmail.landrevillejf.ide.plugin.service.impl;

import com.protonmail.landrevillejf.ide.plugin.service.PluginMetricsService;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

@Slf4j
public class DefaultPluginMetricsService implements PluginMetricsService {

    private final Map<String, Map<String, CounterMetric>> counters = new ConcurrentHashMap<>();
    private final Map<String, Map<String, TimerMetric>> timers = new ConcurrentHashMap<>();
    private final Map<String, Map<String, HistogramMetric>> histograms = new ConcurrentHashMap<>();
    private final Map<String, Map<String, GaugeMetric>> gauges = new ConcurrentHashMap<>();

    @Override
    public void incrementCounter(String pluginId, String metricName) {
        incrementCounter(pluginId, metricName, 1);
    }

    @Override
    public void incrementCounter(String pluginId, String metricName, long amount) {
        CounterMetric counter = getOrCreateCounter(pluginId, metricName);
        counter.increment(amount);
        log.debug("Incremented counter {} for plugin {} by {}", metricName, pluginId, amount);
    }

    @Override
    public void decrementCounter(String pluginId, String metricName) {
        CounterMetric counter = getOrCreateCounter(pluginId, metricName);
        counter.decrement();
        log.debug("Decremented counter {} for plugin {}", metricName, pluginId);
    }

    @Override
    public void recordTimer(String pluginId, String metricName, long durationMillis) {
        TimerMetric timer = getOrCreateTimer(pluginId, metricName);
        timer.record(durationMillis);
        log.debug("Recorded timer {} for plugin {}: {}ms", metricName, pluginId, durationMillis);
    }

    @Override
    public TimerContext startTimer(String pluginId, String metricName) {
        return new DefaultTimerContext(pluginId, metricName, System.currentTimeMillis());
    }

    @Override
    public void recordHistogram(String pluginId, String metricName, long value) {
        HistogramMetric histogram = getOrCreateHistogram(pluginId, metricName);
        histogram.record(value);
        log.debug("Recorded histogram {} for plugin {}: {}", metricName, pluginId, value);
    }

    @Override
    public void setGauge(String pluginId, String metricName, long value) {
        GaugeMetric gauge = getOrCreateGauge(pluginId, metricName);
        gauge.set(value);
        log.debug("Set gauge {} for plugin {}: {}", metricName, pluginId, value);
    }

    @Override
    public long getCounterValue(String pluginId, String metricName) {
        Map<String, CounterMetric> pluginCounters = counters.get(pluginId);
        if (pluginCounters == null) {
            return 0;
        }
        CounterMetric counter = pluginCounters.get(metricName);
        return counter != null ? counter.getValue() : 0;
    }

    @Override
    public Map<String, Object> getAllMetrics(String pluginId) {
        Map<String, Object> allMetrics = new LinkedHashMap<>();

        // Add counters
        Map<String, CounterMetric> pluginCounters = counters.get(pluginId);
        if (pluginCounters != null) {
            Map<String, Long> counterValues = new LinkedHashMap<>();
            pluginCounters.forEach((name, metric) -> counterValues.put(name, metric.getValue()));
            allMetrics.put("counters", counterValues);
        }

        // Add timers
        Map<String, TimerMetric> pluginTimers = timers.get(pluginId);
        if (pluginTimers != null) {
            Map<String, Map<String, Object>> timerStats = new LinkedHashMap<>();
            pluginTimers.forEach((name, metric) -> timerStats.put(name, metric.getStatistics()));
            allMetrics.put("timers", timerStats);
        }

        // Add histograms
        Map<String, HistogramMetric> pluginHistograms = histograms.get(pluginId);
        if (pluginHistograms != null) {
            Map<String, Map<String, Object>> histogramStats = new LinkedHashMap<>();
            pluginHistograms.forEach((name, metric) -> histogramStats.put(name, metric.getStatistics()));
            allMetrics.put("histograms", histogramStats);
        }

        // Add gauges
        Map<String, GaugeMetric> pluginGauges = gauges.get(pluginId);
        if (pluginGauges != null) {
            Map<String, Long> gaugeValues = new LinkedHashMap<>();
            pluginGauges.forEach((name, metric) -> gaugeValues.put(name, metric.getValue()));
            allMetrics.put("gauges", gaugeValues);
        }

        return allMetrics;
    }

    @Override
    public Map<String, Object> getMetricsByType(String pluginId, MetricType type) {
        switch (type) {
            case COUNTER:
                Map<String, CounterMetric> pluginCounters = counters.get(pluginId);
                if (pluginCounters != null) {
                    return pluginCounters.entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getValue()));
                }
                break;
            case TIMER:
                Map<String, TimerMetric> pluginTimers = timers.get(pluginId);
                if (pluginTimers != null) {
                    return pluginTimers.entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getStatistics()));
                }
                break;
            case HISTOGRAM:
                Map<String, HistogramMetric> pluginHistograms = histograms.get(pluginId);
                if (pluginHistograms != null) {
                    return pluginHistograms.entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getStatistics()));
                }
                break;
            case GAUGE:
                Map<String, GaugeMetric> pluginGauges = gauges.get(pluginId);
                if (pluginGauges != null) {
                    return pluginGauges.entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().getValue()));
                }
                break;
        }
        return Collections.emptyMap();
    }

    @Override
    public void resetMetrics(String pluginId) {
        counters.remove(pluginId);
        timers.remove(pluginId);
        histograms.remove(pluginId);
        gauges.remove(pluginId);
        log.info("Reset all metrics for plugin {}", pluginId);
    }

    @Override
    public void resetMetric(String pluginId, String metricName) {
        if (counters.containsKey(pluginId)) {
            counters.get(pluginId).remove(metricName);
        }
        if (timers.containsKey(pluginId)) {
            timers.get(pluginId).remove(metricName);
        }
        if (histograms.containsKey(pluginId)) {
            histograms.get(pluginId).remove(metricName);
        }
        if (gauges.containsKey(pluginId)) {
            gauges.get(pluginId).remove(metricName);
        }
        log.debug("Reset metric {} for plugin {}", metricName, pluginId);
    }

    @Override
    public Map<String, Object> getMetricStatistics(String pluginId, String metricName) {
        // Check counters
        Map<String, CounterMetric> pluginCounters = counters.get(pluginId);
        if (pluginCounters != null && pluginCounters.containsKey(metricName)) {
            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("type", "COUNTER");
            stats.put("value", pluginCounters.get(metricName).getValue());
            return stats;
        }

        // Check timers
        Map<String, TimerMetric> pluginTimers = timers.get(pluginId);
        if (pluginTimers != null && pluginTimers.containsKey(metricName)) {
            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("type", "TIMER");
            stats.putAll(pluginTimers.get(metricName).getStatistics());
            return stats;
        }

        // Check histograms
        Map<String, HistogramMetric> pluginHistograms = histograms.get(pluginId);
        if (pluginHistograms != null && pluginHistograms.containsKey(metricName)) {
            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("type", "HISTOGRAM");
            stats.putAll(pluginHistograms.get(metricName).getStatistics());
            return stats;
        }

        // Check gauges
        Map<String, GaugeMetric> pluginGauges = gauges.get(pluginId);
        if (pluginGauges != null && pluginGauges.containsKey(metricName)) {
            Map<String, Object> stats = new LinkedHashMap<>();
            stats.put("type", "GAUGE");
            stats.put("value", pluginGauges.get(metricName).getValue());
            return stats;
        }

        return Collections.emptyMap();
    }

    @Override
    public Map<String, Object> exportMetrics(String pluginId) {
        Map<String, Object> export = new LinkedHashMap<>();
        export.put("pluginId", pluginId);
        export.put("timestamp", Instant.now().toString());
        export.put("metrics", getAllMetrics(pluginId));
        return export;
    }

    // Helper methods to get or create metrics
    private CounterMetric getOrCreateCounter(String pluginId, String metricName) {
        return counters.computeIfAbsent(pluginId, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(metricName, k -> new CounterMetric());
    }

    private TimerMetric getOrCreateTimer(String pluginId, String metricName) {
        return timers.computeIfAbsent(pluginId, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(metricName, k -> new TimerMetric());
    }

    private HistogramMetric getOrCreateHistogram(String pluginId, String metricName) {
        return histograms.computeIfAbsent(pluginId, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(metricName, k -> new HistogramMetric());
    }

    private GaugeMetric getOrCreateGauge(String pluginId, String metricName) {
        return gauges.computeIfAbsent(pluginId, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(metricName, k -> new GaugeMetric());
    }

    // Inner class for TimerContext implementation
    private class DefaultTimerContext implements TimerContext {
        private final String pluginId;
        private final String metricName;
        private final long startTime;
        private boolean stopped = false;

        public DefaultTimerContext(String pluginId, String metricName, long startTime) {
            this.pluginId = pluginId;
            this.metricName = metricName;
            this.startTime = startTime;
        }

        @Override
        public long getElapsedMillis() {
            return System.currentTimeMillis() - startTime;
        }

        @Override
        public long stop() {
            if (!stopped) {
                stopped = true;
                long elapsed = getElapsedMillis();
                recordTimer(pluginId, metricName, elapsed);
                return elapsed;
            }
            return getElapsedMillis();
        }

        @Override
        public void close() {
            stop();
        }
    }

    // Metric implementations
    private static class CounterMetric {
        private final LongAdder value = new LongAdder();

        void increment(long amount) {
            value.add(amount);
        }

        void decrement() {
            value.decrement();
        }

        long getValue() {
            return value.sum();
        }
    }

    private static class TimerMetric {
        private final LongAdder count = new LongAdder();
        private final LongAdder total = new LongAdder();
        private volatile long min = Long.MAX_VALUE;
        private volatile long max = 0;

        synchronized void record(long duration) {
            count.increment();
            total.add(duration);
            min = Math.min(min, duration);
            max = Math.max(max, duration);
        }

        Map<String, Object> getStatistics() {
            Map<String, Object> stats = new LinkedHashMap<>();
            long cnt = count.sum();
            if (cnt > 0) {
                stats.put("count", cnt);
                stats.put("total_ms", total.sum());
                stats.put("avg_ms", (double) total.sum() / cnt);
                stats.put("min_ms", min == Long.MAX_VALUE ? 0 : min);
                stats.put("max_ms", max);
            } else {
                stats.put("count", 0);
                stats.put("total_ms", 0);
                stats.put("avg_ms", 0);
                stats.put("min_ms", 0);
                stats.put("max_ms", 0);
            }
            return stats;
        }
    }

    private static class HistogramMetric {
        private final LongAdder count = new LongAdder();
        private final LongAdder total = new LongAdder();
        private volatile long min = Long.MAX_VALUE;
        private volatile long max = 0;
        private final Map<Integer, LongAdder> buckets = new ConcurrentHashMap<>();

        private static final int[] BUCKETS = {1, 5, 10, 25, 50, 100, 250, 500, 1000, 5000, 10000};

        synchronized void record(long value) {
            count.increment();
            total.add(value);
            min = Math.min(min, value);
            max = Math.max(max, value);

            // Record in appropriate bucket
            for (int bucket : BUCKETS) {
                if (value <= bucket) {
                    buckets.computeIfAbsent(bucket, k -> new LongAdder()).increment();
                    break;
                }
            }
        }

        Map<String, Object> getStatistics() {
            Map<String, Object> stats = new LinkedHashMap<>();
            long cnt = count.sum();
            if (cnt > 0) {
                stats.put("count", cnt);
                stats.put("sum", total.sum());
                stats.put("avg", (double) total.sum() / cnt);
                stats.put("min", min == Long.MAX_VALUE ? 0 : min);
                stats.put("max", max);

                // Add percentiles
                Map<String, Long> percentiles = new LinkedHashMap<>();
                long[] sortedValues = new long[(int) cnt]; // This is simplified, real implementation would store values
                percentiles.put("p50", 0L);
                percentiles.put("p90", 0L);
                percentiles.put("p95", 0L);
                percentiles.put("p99", 0L);
                stats.put("percentiles", percentiles);

                // Add bucket distribution
                Map<String, Long> distribution = new LinkedHashMap<>();
                for (Map.Entry<Integer, LongAdder> entry : buckets.entrySet()) {
                    distribution.put("≤" + entry.getKey(), entry.getValue().sum());
                }
                stats.put("distribution", distribution);
            }
            return stats;
        }
    }

    private static class GaugeMetric {
        private volatile long value = 0;

        void set(long value) {
            this.value = value;
        }

        long getValue() {
            return value;
        }
    }
}