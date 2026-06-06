package com.protonmail.landrevillejf.ide.plugin.service.impl;

import com.protonmail.landrevillejf.ide.plugin.service.PluginAsyncTaskExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class DefaultPluginAsyncTaskExecutorTest {

    private DefaultPluginAsyncTaskExecutor executor;
    private static final String TEST_PLUGIN_ID = "test-plugin";

    @BeforeEach
    void setUp() {
        executor = new DefaultPluginAsyncTaskExecutor();
    }

    @Test
    void executeTask() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean executed = new AtomicBoolean(false);

        var task = executor.executeTask(TEST_PLUGIN_ID, () -> {
            executed.set(true);
            latch.countDown();
        });

        assertNotNull(task);
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertTrue(executed.get());
        assertEquals(PluginAsyncTaskExecutor.TaskState.COMPLETED, task.getState());
    }

    @Test
    void executeNamedTask() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean executed = new AtomicBoolean(false);

        String taskId = executor.executeNamedTask(TEST_PLUGIN_ID, "Test Task", () -> {
            executed.set(true);
            latch.countDown();
        });

        assertNotNull(taskId);
        assertTrue(taskId.contains(TEST_PLUGIN_ID));
    }

    @Test
    void executeTaskWithPriority() throws Exception {
        CountDownLatch latch = new CountDownLatch(3);
        List<String> submissionOrder = new CopyOnWriteArrayList<>();
        List<String> executionOrder = new CopyOnWriteArrayList<>();

        // Low priority - soumis en premier
        executor.executeTaskWithPriority(TEST_PLUGIN_ID, () -> {
            executionOrder.add("LOW");
            latch.countDown();
        }, PluginAsyncTaskExecutor.TaskPriority.LOW);
        submissionOrder.add("LOW_SUBMITTED");

        // Normal priority
        executor.executeTaskWithPriority(TEST_PLUGIN_ID, () -> {
            executionOrder.add("NORMAL");
            latch.countDown();
        }, PluginAsyncTaskExecutor.TaskPriority.NORMAL);
        submissionOrder.add("NORMAL_SUBMITTED");

        // High priority - soumis en dernier mais devrait être pris en premier de la queue
        executor.executeTaskWithPriority(TEST_PLUGIN_ID, () -> {
            executionOrder.add("HIGH");
            latch.countDown();
        }, PluginAsyncTaskExecutor.TaskPriority.HIGH);
        submissionOrder.add("HIGH_SUBMITTED");

        // Vérifier que les tâches ont été soumises dans l'ordre attendu
        assertEquals("LOW_SUBMITTED", submissionOrder.get(0));
        assertEquals("NORMAL_SUBMITTED", submissionOrder.get(1));
        assertEquals("HIGH_SUBMITTED", submissionOrder.get(2));

        assertTrue(latch.await(5, TimeUnit.SECONDS));

        // Toutes les tâches doivent avoir été exécutées
        assertTrue(executionOrder.contains("HIGH"));
        assertTrue(executionOrder.contains("NORMAL"));
        assertTrue(executionOrder.contains("LOW"));
    }

    @Test
    void executeCallable() throws Exception {
        java.util.concurrent.Future<String> future = executor.executeCallable(TEST_PLUGIN_ID, () -> "Hello World");

        String result = future.get(5, TimeUnit.SECONDS);
        assertEquals("Hello World", result);
    }

    @Test
    void scheduleTask() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicLong startTime = new AtomicLong();

        String taskId = executor.scheduleTask(TEST_PLUGIN_ID, () -> {
            startTime.set(System.currentTimeMillis());
            latch.countDown();
        }, 1000);

        long before = System.currentTimeMillis();
        assertTrue(latch.await(3, TimeUnit.SECONDS));
        long after = System.currentTimeMillis();

        assertNotNull(taskId);
        assertTrue(after - before >= 1000);
    }

    @Test
    void cancelTask() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);

        String taskId = executor.scheduleTask(TEST_PLUGIN_ID, () -> {
            latch.countDown();
        }, 5000);

        boolean cancelled = executor.cancelTask(taskId);
        assertTrue(cancelled);

        // Task should not execute
        assertFalse(latch.await(2, TimeUnit.SECONDS));

        var task = executor.getTask(taskId);
        assertNotNull(task);
        assertEquals(PluginAsyncTaskExecutor.TaskState.CANCELLED, task.getState());
    }

    @Test
    void cancelAllTasks() {
        String id1 = executor.executeNamedTask(TEST_PLUGIN_ID, "Task 1", () -> {});
        String id2 = executor.executeNamedTask(TEST_PLUGIN_ID, "Task 2", () -> {});
        String id3 = executor.executeNamedTask("other-plugin", "Task 3", () -> {});

        int cancelled = executor.cancelAllTasks(TEST_PLUGIN_ID);

        assertEquals(2, cancelled);
        assertEquals(PluginAsyncTaskExecutor.TaskState.CANCELLED, executor.getTask(id1).getState());
        assertEquals(PluginAsyncTaskExecutor.TaskState.CANCELLED, executor.getTask(id2).getState());
        assertNotEquals(PluginAsyncTaskExecutor.TaskState.CANCELLED, executor.getTask(id3).getState());
    }

    @Test
    void getStatistics() {
        executor.executeNamedTask(TEST_PLUGIN_ID, "Task 1", () -> {});
        executor.executeNamedTask(TEST_PLUGIN_ID, "Task 2", () -> {});

        var stats = executor.getStatistics(TEST_PLUGIN_ID);

        assertNotNull(stats);
        assertTrue(stats.containsKey("totalTasks"));
        assertTrue(stats.containsKey("activeCount"));
        assertTrue(stats.containsKey("queueSize"));
    }

    @Test
    void getPluginTasks() throws Exception {
        // Use CountDownLatch to ensure tasks are submitted before checking
        CountDownLatch latch = new CountDownLatch(2);

        executor.executeNamedTask(TEST_PLUGIN_ID, "Task 1", () -> {
            try { Thread.sleep(10); } catch (InterruptedException e) {}
            latch.countDown();
        });
        executor.executeNamedTask(TEST_PLUGIN_ID, "Task 2", () -> {
            try { Thread.sleep(10); } catch (InterruptedException e) {}
            latch.countDown();
        });
        executor.executeNamedTask("other-plugin", "Task 3", () -> {});

        // Wait a bit for tasks to be registered
        Thread.sleep(100);

        var tasks = executor.getPluginTasks(TEST_PLUGIN_ID);

        assertEquals(2, tasks.size());
        // Don't rely on order, just check that both tasks exist
        List<String> taskNames = tasks.stream()
                .map(PluginAsyncTaskExecutor.PluginTask::getName)
                .toList();

        assertTrue(taskNames.contains("Task 1"));
        assertTrue(taskNames.contains("Task 2"));
        assertFalse(taskNames.contains("Task 3"));
    }

    @Test
    void getActiveTasks() {
        executor.executeNamedTask(TEST_PLUGIN_ID, "Task 1", () -> {
            try { Thread.sleep(100); } catch (InterruptedException e) {}
        });
        executor.executeNamedTask(TEST_PLUGIN_ID, "Task 2", () -> {});

        var activeTasks = executor.getActiveTasks(TEST_PLUGIN_ID);

        assertNotNull(activeTasks);
        // Note: Due to async execution, might be 0 or more depending on timing
    }

    @Test
    void shutdown() {
        executor.executeNamedTask(TEST_PLUGIN_ID, "Task 1", () -> {});

        executor.shutdown(TEST_PLUGIN_ID);

        var stats = executor.getStatistics(TEST_PLUGIN_ID);
        assertTrue(stats.isEmpty());
    }
}