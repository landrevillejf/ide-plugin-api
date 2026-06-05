package com.protonmail.landrevillejf.swingide.plugin.service.impl;

import com.protonmail.landrevillejf.swingide.plugin.service.PluginAsyncTaskExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Slf4j
public class DefaultPluginAsyncTaskExecutor implements PluginAsyncTaskExecutor {

    private final Map<String, PluginExecutor> pluginExecutors = new ConcurrentHashMap<>();
    private final Map<String, PluginTask> tasks = new ConcurrentHashMap<>();
    private final AtomicLong taskIdGenerator = new AtomicLong(0);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public DefaultPluginAsyncTaskExecutor() {
        // Start periodic cleanup of old tasks
        scheduler.scheduleAtFixedRate(this::cleanupOldTasks, 1, 1, TimeUnit.HOURS);
        log.info("DefaultPluginAsyncTaskExecutor initialized");
    }

    @Override
    public PluginTask executeTask(String pluginId, Runnable task) {
        return executeNamedTaskInternal(pluginId, "Unnamed Task", task, TaskPriority.NORMAL);
    }

    @Override
    public String executeNamedTask(String pluginId, String taskName, Runnable task) {
        PluginTask pluginTask = executeNamedTaskInternal(pluginId, taskName, task, TaskPriority.NORMAL);
        return pluginTask.getId();
    }

    @Override
    public String executeTaskWithPriority(String pluginId, Runnable task, TaskPriority priority) {
        PluginTask pluginTask = executeNamedTaskInternal(pluginId, "Unnamed Task", task, priority);
        return pluginTask.getId();
    }

    @Override
    public <T> Future<T> executeCallable(String pluginId, Callable<T> task) {
        PluginExecutor executor = getOrCreateExecutor(pluginId);
        CompletableFuture<T> future = new CompletableFuture<>();

        executor.submit(() -> {
            try {
                T result = task.call();
                future.complete(result);
            } catch (Exception e) {
                future.completeExceptionally(e);
                log.error("Callable task failed for plugin {}", pluginId, e);
            }
        });

        return future;
    }

    @Override
    public String scheduleTask(String pluginId, Runnable task, long delayMillis) {
        String taskId = generateTaskId(pluginId);
        PluginExecutor executor = getOrCreateExecutor(pluginId);

        ScheduledFuture<?> scheduledFuture = executor.schedule(() -> {
            executeTaskWithId(taskId, pluginId, task);
        }, delayMillis, TimeUnit.MILLISECONDS);

        // Store scheduled task info
        ScheduledPluginTask scheduledTask = new ScheduledPluginTask(
                taskId, pluginId, "Scheduled Task", TaskPriority.NORMAL, scheduledFuture
        );
        tasks.put(taskId, scheduledTask);

        log.debug("Task scheduled: plugin={}, taskId={}, delay={}ms", pluginId, taskId, delayMillis);
        return taskId;
    }

    @Override
    public String schedulePeriodicTask(String pluginId, Runnable task, long initialDelayMillis, long periodMillis) {
        String taskId = generateTaskId(pluginId);
        PluginExecutor executor = getOrCreateExecutor(pluginId);

        ScheduledFuture<?> scheduledFuture = executor.scheduleAtFixedRate(() -> {
            executeTaskWithId(taskId, pluginId, task);
        }, initialDelayMillis, periodMillis, TimeUnit.MILLISECONDS);

        // Store periodic task info
        PeriodicPluginTask periodicTask = new PeriodicPluginTask(
                taskId, pluginId, "Periodic Task", TaskPriority.NORMAL, scheduledFuture, periodMillis
        );
        tasks.put(taskId, periodicTask);

        log.debug("Periodic task scheduled: plugin={}, taskId={}, period={}ms", pluginId, taskId, periodMillis);
        return taskId;
    }

    @Override
    public PluginTask getTask(String taskId) {
        return tasks.get(taskId);
    }

    @Override
    public List<PluginTask> getPluginTasks(String pluginId) {
        return tasks.values().stream()
                .filter(task -> task.getPluginId().equals(pluginId))
                .collect(Collectors.toList());
    }

    @Override
    public List<PluginTask> getActiveTasks(String pluginId) {
        return tasks.values().stream()
                .filter(task -> task.getPluginId().equals(pluginId))
                .filter(task -> task.getState() == TaskState.PENDING || task.getState() == TaskState.RUNNING)
                .collect(Collectors.toList());
    }

    @Override
    public boolean cancelTask(String taskId) {
        PluginTask task = tasks.get(taskId);
        if (task == null) {
            return false;
        }

        // Try to cancel if it's a scheduled task
        if (task instanceof ScheduledPluginTask) {
            ((ScheduledPluginTask) task).cancel();
        } else if (task instanceof PeriodicPluginTask) {
            ((PeriodicPluginTask) task).cancel();
        }

        // Update task state
        if (task instanceof AbstractPluginTask) {
            ((AbstractPluginTask) task).setState(TaskState.CANCELLED);
        }

        log.debug("Task cancelled: taskId={}", taskId);
        return true;
    }

    @Override
    public int cancelAllTasks(String pluginId) {
        List<PluginTask> pluginTasks = getPluginTasks(pluginId);
        int cancelled = 0;

        for (PluginTask task : pluginTasks) {
            if (cancelTask(task.getId())) {
                cancelled++;
            }
        }

        log.debug("Cancelled {} tasks for plugin {}", cancelled, pluginId);
        return cancelled;
    }

    @Override
    public void setThreadPoolSize(String pluginId, int poolSize) {
        PluginExecutor executor = pluginExecutors.get(pluginId);
        if (executor != null) {
            executor.setThreadPoolSize(poolSize);
            log.debug("Thread pool size set for plugin {}: {}", pluginId, poolSize);
        }
    }

    @Override
    public int getThreadPoolSize(String pluginId) {
        PluginExecutor executor = pluginExecutors.get(pluginId);
        return executor != null ? executor.getThreadPoolSize() : 0;
    }

    @Override
    public Map<String, Object> getStatistics(String pluginId) {
        PluginExecutor executor = pluginExecutors.get(pluginId);
        if (executor == null) {
            return Collections.emptyMap();
        }

        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("activeCount", executor.getActiveCount());
        stats.put("queueSize", executor.getQueueSize());
        stats.put("completedTasks", executor.getCompletedTasks());
        stats.put("threadPoolSize", executor.getThreadPoolSize());
        stats.put("totalTasks", executor.getTotalTasks());

        return stats;
    }

    @Override
    public void shutdown(String pluginId) {
        PluginExecutor executor = pluginExecutors.remove(pluginId);
        if (executor != null) {
            executor.shutdown();
            log.debug("Executor shutdown for plugin {}", pluginId);
        }
    }

    private PluginTask executeNamedTaskInternal(String pluginId, String taskName, Runnable task, TaskPriority priority) {
        String taskId = generateTaskId(pluginId);
        PluginExecutor executor = getOrCreateExecutor(pluginId);

        PluginTaskImpl pluginTask = new PluginTaskImpl(taskId, pluginId, taskName, priority);
        tasks.put(taskId, pluginTask);

        executor.submit(() -> {
            executeTaskWithId(taskId, pluginId, task);
        }, priority);

        return pluginTask;
    }

    private void executeTaskWithId(String taskId, String pluginId, Runnable task) {
        PluginTask pluginTask = tasks.get(taskId);
        if (pluginTask == null) {
            log.warn("Task not found: {}", taskId);
            task.run(); // Still run the task even if not tracked
            return;
        }

        AbstractPluginTask abstractTask = (AbstractPluginTask) pluginTask;
        abstractTask.start();

        try {
            task.run();
            abstractTask.complete();
            log.debug("Task completed: plugin={}, taskId={}", pluginId, taskId);
        } catch (Exception e) {
            abstractTask.fail(e);
            log.error("Task failed: plugin={}, taskId={}", pluginId, taskId, e);
        }
    }

    private String generateTaskId(String pluginId) {
        return pluginId + "_" + System.currentTimeMillis() + "_" + taskIdGenerator.incrementAndGet();
    }

    private PluginExecutor getOrCreateExecutor(String pluginId) {
        return pluginExecutors.computeIfAbsent(pluginId, k -> new PluginExecutor(pluginId));
    }

    private void cleanupOldTasks() {
        long oneDayAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1);

        tasks.entrySet().removeIf(entry -> {
            PluginTask task = entry.getValue();
            long endTime = task.getEndTime();
            return endTime > 0 && endTime < oneDayAgo &&
                    (task.getState() == TaskState.COMPLETED ||
                            task.getState() == TaskState.FAILED ||
                            task.getState() == TaskState.CANCELLED);
        });

        log.debug("Cleaned up old tasks, remaining tasks: {}", tasks.size());
    }

    /**
     * Plugin-specific executor with priority support
     */
    private static class PluginExecutor {
        private final String pluginId;
        private final BlockingQueue<PriorityTask> taskQueue;  // Changé: PriorityQueue -> BlockingQueue
        private final AtomicInteger activeCount = new AtomicInteger(0);
        private final AtomicLong completedTasks = new AtomicLong(0);
        private final AtomicLong totalTasks = new AtomicLong(0);
        private volatile int threadPoolSize;
        private ExecutorService executor;
        private final ReentrantLock lock = new ReentrantLock();
        private volatile boolean running = true;

        public PluginExecutor(String pluginId) {
            this.pluginId = pluginId;
            this.threadPoolSize = Math.max(2, Runtime.getRuntime().availableProcessors());
            this.taskQueue = new PriorityBlockingQueue<>(11,
                    (a, b) -> b.priority.ordinal() - a.priority.ordinal());
            initializeExecutor();
            startWorker();
        }

        private void initializeExecutor() {
            executor = Executors.newFixedThreadPool(threadPoolSize, r -> {
                Thread t = new Thread(r, "Plugin-" + pluginId + "-Task");
                t.setDaemon(true);
                return t;
            });
        }

        private void startWorker() {
            Thread worker = new Thread(() -> {
                while (running && !Thread.currentThread().isInterrupted()) {
                    try {
                        // take() bloque jusqu'à ce qu'une tâche soit disponible
                        PriorityTask task = taskQueue.take();
                        if (task != null) {
                            activeCount.incrementAndGet();
                            executor.submit(() -> {
                                try {
                                    task.task.run();
                                    completedTasks.incrementAndGet();
                                } finally {
                                    activeCount.decrementAndGet();
                                }
                            });
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        log.error("Error processing task for plugin: {}", pluginId, e);
                    }
                }
            }, "Plugin-" + pluginId + "-Worker");
            worker.setDaemon(true);
            worker.start();
        }

        public void submit(Runnable task, TaskPriority priority) {
            taskQueue.offer(new PriorityTask(task, priority));
            totalTasks.incrementAndGet();
        }

        public void submit(Runnable task) {
            submit(task, TaskPriority.NORMAL);
        }

        public ScheduledFuture<?> schedule(Runnable task, long delay, TimeUnit unit) {
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            return scheduler.schedule(() -> {
                submit(task);
                scheduler.shutdown();
            }, delay, unit);
        }

        public ScheduledFuture<?> scheduleAtFixedRate(Runnable task, long initialDelay, long period, TimeUnit unit) {
            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            return scheduler.scheduleAtFixedRate(() -> {
                submit(task);
            }, initialDelay, period, unit);
        }

        public void setThreadPoolSize(int size) {
            lock.lock();
            try {
                this.threadPoolSize = size;
                executor.shutdownNow();
                initializeExecutor();
            } finally {
                lock.unlock();
            }
        }

        public int getThreadPoolSize() { return threadPoolSize; }
        public int getActiveCount() { return activeCount.get(); }
        public int getQueueSize() { return taskQueue.size(); }
        public long getCompletedTasks() { return completedTasks.get(); }
        public long getTotalTasks() { return totalTasks.get(); }

        public void shutdown() {
            running = false;
            executor.shutdownNow();
        }

        private static class PriorityTask {
            final Runnable task;
            final TaskPriority priority;
            PriorityTask(Runnable task, TaskPriority priority) {
                this.task = task;
                this.priority = priority;
            }
        }
    }

    /**
     * Base class for plugin tasks
     */
    private abstract static class AbstractPluginTask implements PluginTask {
        protected final String id;
        protected final String pluginId;
        protected final String name;
        protected final TaskPriority priority;
        protected final long submitTime;
        protected volatile long startTime = 0;
        protected volatile long endTime = 0;
        protected volatile TaskState state = TaskState.PENDING;
        protected volatile Throwable error = null;
        protected volatile int progress = 0;

        public AbstractPluginTask(String id, String pluginId, String name, TaskPriority priority) {
            this.id = id;
            this.pluginId = pluginId;
            this.name = name;
            this.priority = priority;
            this.submitTime = System.currentTimeMillis();
        }

        @Override
        public String getId() { return id; }
        @Override
        public String getPluginId() { return pluginId; }
        @Override
        public String getName() { return name; }
        @Override
        public TaskState getState() { return state; }
        @Override
        public TaskPriority getPriority() { return priority; }
        @Override
        public long getSubmitTime() { return submitTime; }
        @Override
        public long getStartTime() { return startTime; }
        @Override
        public long getEndTime() { return endTime; }
        @Override
        public Throwable getError() { return error; }
        @Override
        public int getProgress() { return progress; }
        @Override
        public String getDescription() { return name; }

        public void setState(TaskState state) { this.state = state; }
        public void setProgress(int progress) { this.progress = progress; }

        public void start() {
            this.startTime = System.currentTimeMillis();
            this.state = TaskState.RUNNING;
        }

        public void complete() {
            this.endTime = System.currentTimeMillis();
            this.state = TaskState.COMPLETED;
            this.progress = 100;
        }

        public void fail(Throwable error) {
            this.endTime = System.currentTimeMillis();
            this.state = TaskState.FAILED;
            this.error = error;
        }
    }

    /**
     * Implementation for normal tasks
     */
    private static class PluginTaskImpl extends AbstractPluginTask {
        public PluginTaskImpl(String id, String pluginId, String name, TaskPriority priority) {
            super(id, pluginId, name, priority);
        }
    }

    /**
     * Implementation for scheduled tasks
     */
    private static class ScheduledPluginTask extends AbstractPluginTask {
        private final ScheduledFuture<?> future;

        public ScheduledPluginTask(String id, String pluginId, String name, TaskPriority priority, ScheduledFuture<?> future) {
            super(id, pluginId, name, priority);
            this.future = future;
        }

        public void cancel() {
            future.cancel(true);
            state = TaskState.CANCELLED;
        }
    }

    /**
     * Implementation for periodic tasks
     */
    private static class PeriodicPluginTask extends AbstractPluginTask {
        private final ScheduledFuture<?> future;
        private final long periodMillis;
        private long lastExecutionTime = 0;
        private long executionCount = 0;

        public PeriodicPluginTask(String id, String pluginId, String name, TaskPriority priority,
                                  ScheduledFuture<?> future, long periodMillis) {
            super(id, pluginId, name, priority);
            this.future = future;
            this.periodMillis = periodMillis;
            this.state = TaskState.RUNNING;
        }

        public void cancel() {
            future.cancel(true);
            state = TaskState.CANCELLED;
        }

        public long getPeriodMillis() { return periodMillis; }
        public long getExecutionCount() { return executionCount; }
        public long getLastExecutionTime() { return lastExecutionTime; }
    }
}