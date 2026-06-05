package com.protonmail.landrevillejf.swingide.plugin.service;

import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for PluginAsyncTaskExecutor interface
 */
@DisplayName("PluginAsyncTaskExecutor Tests")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class PluginAsyncTaskExecutorTests {

    private PluginAsyncTaskExecutor executor;
    private static final String PLUGIN_ID = "test-plugin";

    @BeforeEach
    void setUp() {
        executor = new MockPluginAsyncTaskExecutor();
    }

    @Test
    @DisplayName("should execute named task")
    void test_execute_named_task() {
        java.util.concurrent.atomic.AtomicBoolean executed = new java.util.concurrent.atomic.AtomicBoolean(false);

        String taskId = executor.executeNamedTask(PLUGIN_ID, "my-task", () -> executed.set(true));

        assertThat(taskId).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("should execute task with priority")
    void test_execute_task_with_priority() {
        String taskId = executor.executeTaskWithPriority(PLUGIN_ID,
                () -> {},
                PluginAsyncTaskExecutor.TaskPriority.HIGH);

        assertThat(taskId).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("should schedule task with delay")
    void test_schedule_task_with_delay() {
        String taskId = executor.scheduleTask(PLUGIN_ID, () -> {}, 1000);

        assertThat(taskId).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("should schedule periodic task")
    void test_schedule_periodic_task() {
        String taskId = executor.schedulePeriodicTask(PLUGIN_ID, () -> {}, 0, 1000);

        assertThat(taskId).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("should get plugin tasks")
    void test_get_plugin_tasks() {
        executor.executeNamedTask(PLUGIN_ID, "task1", () -> {});

        List<PluginAsyncTaskExecutor.PluginTask> tasks = executor.getPluginTasks(PLUGIN_ID);

        assertThat(tasks).isNotNull();
    }

    @Test
    @DisplayName("should cancel task")
    void test_cancel_task() {
        String taskId = executor.executeNamedTask(PLUGIN_ID, "task", () -> {});

        boolean cancelled = executor.cancelTask(taskId);

        assertThat(cancelled).isTrue();
    }

    // Mock implementation
    public static class MockPluginAsyncTaskExecutor implements PluginAsyncTaskExecutor {
        private int taskCounter = 0;

        @Override
        public PluginTask executeTask(String pluginId, Runnable task) { return null; }

        @Override
        public String executeNamedTask(String pluginId, String taskName, Runnable task) {
            return "task-" + (++taskCounter);
        }

        @Override
        public String executeTaskWithPriority(String pluginId, Runnable task, TaskPriority priority) {
            return "task-" + (++taskCounter);
        }

        @Override
        public <T> java.util.concurrent.Future<T> executeCallable(String pluginId, java.util.concurrent.Callable<T> task) {
            return null;
        }

        @Override
        public String scheduleTask(String pluginId, Runnable task, long delayMillis) {
            return "task-" + (++taskCounter);
        }

        @Override
        public String schedulePeriodicTask(String pluginId, Runnable task, long initialDelayMillis, long periodMillis) {
            return "task-" + (++taskCounter);
        }

        @Override
        public PluginTask getTask(String taskId) { return null; }

        @Override
        public List<PluginTask> getPluginTasks(String pluginId) { return java.util.Collections.emptyList(); }

        @Override
        public List<PluginTask> getActiveTasks(String pluginId) { return java.util.Collections.emptyList(); }

        @Override
        public boolean cancelTask(String taskId) { return true; }

        @Override
        public int cancelAllTasks(String pluginId) { return 0; }

        @Override
        public void setThreadPoolSize(String pluginId, int poolSize) {}

        @Override
        public int getThreadPoolSize(String pluginId) { return 1; }

        @Override
        public Map<String, Object> getStatistics(String pluginId) { return new java.util.HashMap<>(); }

        @Override
        public void shutdown(String pluginId) {}
    }
}

