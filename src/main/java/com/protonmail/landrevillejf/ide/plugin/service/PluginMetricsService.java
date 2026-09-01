package com.protonmail.landrevillejf.ide.plugin.service;

import java.util.Map;

/**
 * Metrics collection and monitoring service for plugins.
 * <p>
 * Provides counters, timers, histograms, and gauges for plugin performance monitoring.
 * Each plugin has its own isolated metric namespace identified by the plugin ID.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
public interface PluginMetricsService {

    /**
     * Metric types supported by the service.
     */
    enum MetricType {
        COUNTER,          // Simple counter
        TIMER,           // Time-based measurements
        HISTOGRAM,       // Distribution measurements
        GAUGE            // Point-in-time measurements
    }

    /**
     * Increments a counter metric.
     *
     * @param pluginId the plugin identifier
     * @param metricName the metric name
     */
    void incrementCounter(String pluginId, String metricName);

    /**
     * Increments a counter metric by a specific amount.
     *
     * @param pluginId the plugin identifier
     * @param metricName the metric name
     * @param amount the increment amount
     */
    void incrementCounter(String pluginId, String metricName, long amount);

    /**
     * Decrements a counter metric.
     *
     * @param pluginId the plugin identifier
     * @param metricName the metric name
     */
    void decrementCounter(String pluginId, String metricName);

    /**
     * Records a timer measurement.
     *
     * @param pluginId the plugin identifier
     * @param metricName the metric name
     * @param durationMillis the duration in milliseconds
     */
    void recordTimer(String pluginId, String metricName, long durationMillis);

    /**
     * Starts a timer context for measuring operations.
     *
     * @param pluginId the plugin identifier
     * @param metricName the metric name
     * @return a timer context that should be closed when done
     */
    TimerContext startTimer(String pluginId, String metricName);

    /**
     * Records a histogram value.
     *
     * @param pluginId the plugin identifier
     * @param metricName the metric name
     * @param value the value to record
     */
    void recordHistogram(String pluginId, String metricName, long value);

    /**
     * Sets a gauge value.
     *
     * @param pluginId the plugin identifier
     * @param metricName the metric name
     * @param value the gauge value
     */
    void setGauge(String pluginId, String metricName, long value);

    /**
     * Gets a counter value.
     *
     * @param pluginId the plugin identifier
     * @param metricName the metric name
     * @return the counter value
     */
    long getCounterValue(String pluginId, String metricName);

    /**
     * Gets all metrics for a plugin.
     *
     * @param pluginId the plugin identifier
     * @return a map of metric names to values
     */
    Map<String, Object> getAllMetrics(String pluginId);

    /**
     * Gets metrics filtered by type.
     *
     * @param pluginId the plugin identifier
     * @param type the metric type to filter
     * @return a map of metric names to values
     */
    Map<String, Object> getMetricsByType(String pluginId, MetricType type);

    /**
     * Resets all metrics for a plugin.
     *
     * @param pluginId the plugin identifier
     */
    void resetMetrics(String pluginId);

    /**
     * Resets a specific metric.
     *
     * @param pluginId the plugin identifier
     * @param metricName the metric name
     */
    void resetMetric(String pluginId, String metricName);

    /**
     * Gets statistical summary of a metric.
     *
     * @param pluginId the plugin identifier
     * @param metricName the metric name
     * @return a map containing statistics (mean, min, max, count, etc.)
     */
    Map<String, Object> getMetricStatistics(String pluginId, String metricName);

    /**
     * Exports metrics for a plugin to a map.
     *
     * @param pluginId the plugin identifier
     * @return a map containing all metrics and their values
     */
    Map<String, Object> exportMetrics(String pluginId);

    /**
     * Context for managing timer measurements.
     */
    interface TimerContext extends AutoCloseable {
        /**
         * Gets the elapsed time since timer started.
         *
         * @return the elapsed time in milliseconds
         */
        long getElapsedMillis();

        /**
         * Stops the timer and records the measurement.
         *
         * @return the total elapsed time in milliseconds
         */
        long stop();
    }
}

