package com.protonmail.landrevillejf.ide.plugin.service;

import java.util.List;
import java.util.Map;

/**
 * Asynchronous task execution service for plugins with thread pooling and task monitoring.
 * <p>
 * Supports named tasks, prioritized execution, scheduled and periodic tasks,
 * as well as task cancellation and per-plugin thread pool configuration.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
public interface PluginAsyncTaskExecutor {

    /**
     * Task priority levels.
     */
    enum TaskPriority {
        LOW,
        NORMAL,
        HIGH
    }

    /**
     * Task state.
     */
    enum TaskState {
        PENDING,
        RUNNING,
        COMPLETED,
        FAILED,
        CANCELLED
    }

    /**
     * Represents a submitted task.
     */
    interface PluginTask {
        String getId();
        String getPluginId();
        String getName();
        TaskState getState();
        TaskPriority getPriority();
        long getSubmitTime();
        long getStartTime();
        long getEndTime();
        Throwable getError();
        int getProgress();
        String getDescription();
    }

    /**
     * Executes a runnable task asynchronously.
     *
     * @param pluginId the plugin identifier
     * @param task the task to execute
     * @return a future-like interface for the task
     */
    PluginTask executeTask(String pluginId, Runnable task);

    /**
     * Executes a named task asynchronously.
     *
     * @param pluginId the plugin identifier
     * @param taskName the task name
     * @param task the task to execute
     * @return a task identifier
     */
    String executeNamedTask(String pluginId, String taskName, Runnable task);

    /**
     * Executes a task with priority.
     *
     * @param pluginId the plugin identifier
     * @param task the task to execute
     * @param priority the task priority
     * @return a task identifier
     */
    String executeTaskWithPriority(String pluginId, Runnable task, TaskPriority priority);

    /**
     * Executes a callable task asynchronously.
     *
     * @param pluginId the plugin identifier
     * @param task the callable task
     * @return a future for retrieving the result
     */
    <T> java.util.concurrent.Future<T> executeCallable(String pluginId, java.util.concurrent.Callable<T> task);

    /**
     * Schedules a task to run after a delay.
     *
     * @param pluginId the plugin identifier
     * @param task the task to execute
     * @param delayMillis the delay in milliseconds
     * @return a task identifier
     */
    String scheduleTask(String pluginId, Runnable task, long delayMillis);

    /**
     * Schedules a task to run periodically.
     *
     * @param pluginId the plugin identifier
     * @param task the task to execute
     * @param initialDelayMillis the initial delay in milliseconds
     * @param periodMillis the period between executions in milliseconds
     * @return a task identifier
     */
    String schedulePeriodicTask(String pluginId, Runnable task, long initialDelayMillis, long periodMillis);

    /**
     * Gets a task by id.
     *
     * @param taskId the task identifier
     * @return the task, or null if not found
     */
    PluginTask getTask(String taskId);

    /**
     * Gets all tasks for a plugin.
     *
     * @param pluginId the plugin identifier
     * @return list of tasks
     */
    List<PluginTask> getPluginTasks(String pluginId);

    /**
     * Gets active tasks for a plugin.
     *
     * @param pluginId the plugin identifier
     * @return list of active tasks
     */
    List<PluginTask> getActiveTasks(String pluginId);

    /**
     * Cancels a task.
     *
     * @param taskId the task identifier
     * @return true if the task was cancelled
     */
    boolean cancelTask(String taskId);

    /**
     * Cancels all tasks for a plugin.
     *
     * @param pluginId the plugin identifier
     * @return the number of cancelled tasks
     */
    int cancelAllTasks(String pluginId);

    /**
     * Sets the thread pool size for a plugin.
     *
     * @param pluginId the plugin identifier
     * @param poolSize the thread pool size
     */
    void setThreadPoolSize(String pluginId, int poolSize);

    /**
     * Gets the thread pool size for a plugin.
     *
     * @param pluginId the plugin identifier
     * @return the thread pool size
     */
    int getThreadPoolSize(String pluginId);

    /**
     * Gets executor statistics for a plugin.
     *
     * @param pluginId the plugin identifier
     * @return a map containing executor statistics
     */
    Map<String, Object> getStatistics(String pluginId);

    /**
     * Shuts down the executor for a plugin.
     *
     * @param pluginId the plugin identifier
     */
    void shutdown(String pluginId);
}

