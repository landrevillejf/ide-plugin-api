package com.protonmail.landrevillejf.ide.plugin.service.impl;

import com.protonmail.landrevillejf.ide.plugin.service.PluginHookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class DefaultPluginHookServiceTest {

    private DefaultPluginHookService hookService;
    private static final String TEST_PLUGIN = "test-plugin";
    private static final String TEST_PLUGIN_2 = "test-plugin-2";

    @BeforeEach
    void setUp() {
        hookService = new DefaultPluginHookService();
    }

    @Test
    void registerHook() {
        AtomicBoolean executed = new AtomicBoolean(false);

        String hookId = hookService.registerHook(TEST_PLUGIN, PluginHookService.HookType.PRE_ENABLE,
                context -> executed.set(true));

        assertNotNull(hookId);
        assertTrue(hookId.contains(TEST_PLUGIN));

        List<String> hooks = hookService.getPluginHooks(TEST_PLUGIN);
        assertEquals(1, hooks.size());
        assertEquals(hookId, hooks.get(0));
    }

    @Test
    void registerHookWithPriority() {
        AtomicInteger executionOrder = new AtomicInteger(0);
        AtomicInteger lowOrder = new AtomicInteger(0);
        AtomicInteger mediumOrder = new AtomicInteger(0);
        AtomicInteger highOrder = new AtomicInteger(0);

        // Low priority (10)
        hookService.registerHookWithPriority(TEST_PLUGIN, PluginHookService.HookType.PRE_ENABLE, 10,
                context -> lowOrder.set(executionOrder.incrementAndGet()));

        // Medium priority (50)
        hookService.registerHookWithPriority(TEST_PLUGIN, PluginHookService.HookType.PRE_ENABLE, 50,
                context -> mediumOrder.set(executionOrder.incrementAndGet()));

        // High priority (100)
        hookService.registerHookWithPriority(TEST_PLUGIN, PluginHookService.HookType.PRE_ENABLE, 100,
                context -> highOrder.set(executionOrder.incrementAndGet()));

        hookService.executeHooks(TEST_PLUGIN, PluginHookService.HookType.PRE_ENABLE, Map.of());

        // Higher priority should execute first (lower number = executed first)
        assertEquals(1, highOrder.get());
        assertEquals(2, mediumOrder.get());
        assertEquals(3, lowOrder.get());
    }

    @Test
    void unregisterHook() {
        String hookId = hookService.registerHook(TEST_PLUGIN, PluginHookService.HookType.PRE_ENABLE,
                context -> {});

        assertTrue(hookService.getPluginHooks(TEST_PLUGIN).contains(hookId));

        boolean unregistered = hookService.unregisterHook(hookId);

        assertTrue(unregistered);
        assertFalse(hookService.getPluginHooks(TEST_PLUGIN).contains(hookId));
    }

    @Test
    void unregisterNonExistentHook() {
        boolean unregistered = hookService.unregisterHook("non-existent-hook");

        assertFalse(unregistered);
    }

    @Test
    void unregisterHooksByType() {
        hookService.registerHook(TEST_PLUGIN, PluginHookService.HookType.PRE_ENABLE, context -> {});
        hookService.registerHook(TEST_PLUGIN, PluginHookService.HookType.POST_ENABLE, context -> {});
        hookService.registerHook(TEST_PLUGIN, PluginHookService.HookType.PRE_ENABLE, context -> {});

        int unregistered = hookService.unregisterHooksByType(TEST_PLUGIN, PluginHookService.HookType.PRE_ENABLE);

        assertEquals(2, unregistered);

        List<String> hooks = hookService.getPluginHooks(TEST_PLUGIN);
        assertEquals(1, hooks.size());
        assertTrue(hookService.getHooksByType(TEST_PLUGIN, PluginHookService.HookType.PRE_ENABLE).isEmpty());
    }

    @Test
    void executeHooks() {
        AtomicBoolean hook1Executed = new AtomicBoolean(false);
        AtomicBoolean hook2Executed = new AtomicBoolean(false);

        hookService.registerHook(TEST_PLUGIN, PluginHookService.HookType.PRE_ENABLE,
                context -> hook1Executed.set(true));
        hookService.registerHook(TEST_PLUGIN, PluginHookService.HookType.PRE_ENABLE,
                context -> hook2Executed.set(true));

        PluginHookService.HookContext context = hookService.executeHooks(
                TEST_PLUGIN, PluginHookService.HookType.PRE_ENABLE, Map.of("key", "value"));

        assertTrue(hook1Executed.get());
        assertTrue(hook2Executed.get());
        assertNotNull(context);
        assertEquals(TEST_PLUGIN, context.getPluginId());
        assertEquals(PluginHookService.HookType.PRE_ENABLE, context.getHookType());
        assertEquals("value", context.getHookData().get("key"));
    }

    @Test
    void executeHooksWithCancellation() {
        AtomicBoolean firstHookExecuted = new AtomicBoolean(false);
        AtomicBoolean secondHookExecuted = new AtomicBoolean(false);

        hookService.registerHook(TEST_PLUGIN, PluginHookService.HookType.PRE_ENABLE,
                context -> {
                    firstHookExecuted.set(true);
                    context.cancel();
                });

        hookService.registerHook(TEST_PLUGIN, PluginHookService.HookType.PRE_ENABLE,
                context -> secondHookExecuted.set(true));

        PluginHookService.HookContext context = hookService.executeHooks(
                TEST_PLUGIN, PluginHookService.HookType.PRE_ENABLE, Map.of());

        assertTrue(firstHookExecuted.get());
        assertFalse(secondHookExecuted.get()); // Should not execute because cancelled
        assertTrue(context.isCancelled());
    }

    @Test
    void executeHooksWithNoHooks() {
        PluginHookService.HookContext context = hookService.executeHooks(
                TEST_PLUGIN, PluginHookService.HookType.PRE_ENABLE, Map.of());

        assertNotNull(context);
        assertFalse(context.isCancelled());
    }

    @Test
    void executeHook() {
        AtomicBoolean executed = new AtomicBoolean(false);
        String hookId = hookService.registerHook(TEST_PLUGIN, PluginHookService.HookType.PRE_ENABLE,
                context -> {
                    executed.set(true);
                    context.setResult("success");
                });

        Object result = hookService.executeHook(hookId, Map.of("test", "data"));

        assertTrue(executed.get());
        assertEquals("success", result);
    }

    @Test
    void executeNonExistentHook() {
        Object result = hookService.executeHook("non-existent", Map.of());

        assertNull(result);
    }

    @Test
    void getPluginHooks() {
        String hook1 = hookService.registerHook(TEST_PLUGIN, PluginHookService.HookType.PRE_ENABLE, context -> {});
        String hook2 = hookService.registerHook(TEST_PLUGIN, PluginHookService.HookType.POST_ENABLE, context -> {});
        hookService.registerHook(TEST_PLUGIN_2, PluginHookService.HookType.PRE_ENABLE, context -> {});

        List<String> hooks = hookService.getPluginHooks(TEST_PLUGIN);

        assertEquals(2, hooks.size());
        assertTrue(hooks.contains(hook1));
        assertTrue(hooks.contains(hook2));
    }

    @Test
    void getHooksByType() {
        String preHook1 = hookService.registerHook(TEST_PLUGIN, PluginHookService.HookType.PRE_ENABLE, context -> {});
        String preHook2 = hookService.registerHook(TEST_PLUGIN, PluginHookService.HookType.PRE_ENABLE, context -> {});
        String postHook = hookService.registerHook(TEST_PLUGIN, PluginHookService.HookType.POST_ENABLE, context -> {});

        List<String> preHooks = hookService.getHooksByType(TEST_PLUGIN, PluginHookService.HookType.PRE_ENABLE);

        assertEquals(2, preHooks.size());
        assertTrue(preHooks.contains(preHook1));
        assertTrue(preHooks.contains(preHook2));
        assertFalse(preHooks.contains(postHook));
    }

    @Test
    void getHookExecutionHistory() throws InterruptedException {
        hookService.registerHook(TEST_PLUGIN, PluginHookService.HookType.PRE_ENABLE,
                context -> {});

        hookService.executeHooks(TEST_PLUGIN, PluginHookService.HookType.PRE_ENABLE, Map.of());

        Thread.sleep(10);

        hookService.executeHooks(TEST_PLUGIN, PluginHookService.HookType.PRE_ENABLE, Map.of());

        List<Map<String, Object>> history = hookService.getHookExecutionHistory(TEST_PLUGIN, 10);

        assertNotNull(history);
        assertEquals(2, history.size());

        Map<String, Object> firstRecord = history.get(0);
        assertTrue(firstRecord.containsKey("timestamp"));
        assertTrue(firstRecord.containsKey("hookType"));
        assertTrue(firstRecord.containsKey("hookId"));
        assertTrue(firstRecord.containsKey("executionTimeMs"));
    }

    @Test
    void getHookExecutionHistoryWithMaxEntries() {
        for (int i = 0; i < 15; i++) {
            String hookId = hookService.registerHook(TEST_PLUGIN, PluginHookService.HookType.PRE_ENABLE,
                    context -> {});
            hookService.executeHook(hookId, Map.of());
        }

        List<Map<String, Object>> history = hookService.getHookExecutionHistory(TEST_PLUGIN, 5);

        assertEquals(5, history.size());
    }

    @Test
    void getHookExecutionHistoryForUnknownPlugin() {
        List<Map<String, Object>> history = hookService.getHookExecutionHistory("unknown-plugin", 10);

        assertTrue(history.isEmpty());
    }

    @Test
    void clearHookExecutionHistory() {
        hookService.registerHook(TEST_PLUGIN, PluginHookService.HookType.PRE_ENABLE,
                context -> {});
        hookService.executeHooks(TEST_PLUGIN, PluginHookService.HookType.PRE_ENABLE, Map.of());

        assertFalse(hookService.getHookExecutionHistory(TEST_PLUGIN, 10).isEmpty());

        hookService.clearHookExecutionHistory(TEST_PLUGIN);

        assertTrue(hookService.getHookExecutionHistory(TEST_PLUGIN, 10).isEmpty());
    }

    @Test
    void executeHooksWithDataPassing() {
        hookService.registerHook(TEST_PLUGIN, PluginHookService.HookType.PRE_ENABLE,
                context -> {
                    context.getHookData().put("modified", "value");
                    context.setResult("processed");
                });

        Map<String, Object> inputData = Map.of("original", "data");
        PluginHookService.HookContext context = hookService.executeHooks(
                TEST_PLUGIN, PluginHookService.HookType.PRE_ENABLE, inputData);

        assertEquals("processed", context.getResult());
        assertEquals("data", context.getHookData().get("original"));
        assertEquals("value", context.getHookData().get("modified"));
    }

    @Test
    void registerMultipleHooksSamePlugin() {
        hookService.registerHook(TEST_PLUGIN, PluginHookService.HookType.PRE_ENABLE, context -> {});
        hookService.registerHook(TEST_PLUGIN, PluginHookService.HookType.PRE_ENABLE, context -> {});
        hookService.registerHook(TEST_PLUGIN, PluginHookService.HookType.PRE_ENABLE, context -> {});

        List<String> hooks = hookService.getPluginHooks(TEST_PLUGIN);

        assertEquals(3, hooks.size());
    }

    @Test
    void hookExecutionWithException() {
        String hookId = hookService.registerHook(TEST_PLUGIN, PluginHookService.HookType.PRE_ENABLE,
                context -> {
                    throw new RuntimeException("Hook failed");
                });

        // Should not throw exception
        Object result = hookService.executeHook(hookId, Map.of());

        assertNull(result);

        List<Map<String, Object>> history = hookService.getHookExecutionHistory(TEST_PLUGIN, 10);
        assertFalse(history.isEmpty());
        assertTrue(history.get(0).containsKey("error"));
    }
}