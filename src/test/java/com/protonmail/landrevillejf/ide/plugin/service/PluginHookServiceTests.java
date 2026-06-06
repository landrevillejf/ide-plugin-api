package com.protonmail.landrevillejf.ide.plugin.service;

import org.junit.jupiter.api.*;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for PluginHookService interface
 */
@DisplayName("PluginHookService Tests")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class PluginHookServiceTests {

    private PluginHookService hookService;
    private static final String PLUGIN_ID = "test-plugin";

    @BeforeEach
    void setUp() {
        hookService = new MockPluginHookService();
    }

    @Test
    @DisplayName("should register hook")
    void test_register_hook() {
        String hookId = hookService.registerHook(PLUGIN_ID, PluginHookService.HookType.POST_INIT, (ctx) -> {});

        assertThat(hookId).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("should register hook with priority")
    void test_register_hook_with_priority() {
        String hookId = hookService.registerHookWithPriority(PLUGIN_ID,
                PluginHookService.HookType.POST_INIT,
                10,
                (ctx) -> {});

        assertThat(hookId).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("should unregister hook")
    void test_unregister_hook() {
        String hookId = hookService.registerHook(PLUGIN_ID, PluginHookService.HookType.POST_INIT, (ctx) -> {});

        boolean unregistered = hookService.unregisterHook(hookId);

        assertThat(unregistered).isTrue();
    }

    @Test
    @DisplayName("should execute hooks")
    void test_execute_hooks() {
        hookService.registerHook(PLUGIN_ID, PluginHookService.HookType.POST_INIT, (ctx) -> {});

        PluginHookService.HookContext context = hookService.executeHooks(PLUGIN_ID,
                PluginHookService.HookType.POST_INIT,
                new java.util.HashMap<>());

        assertThat(context).isNotNull();
    }

    // Mock implementation
    // Dans PluginHookServiceTests.java
    public static class MockPluginHookService implements PluginHookService {
        private final Map<String, HookCallback> hooks = new HashMap<>();
        private int counter = 0;

        @Override
        public String registerHook(String pluginId, HookType hookType, HookCallback callback) {
            String hookId = "hook-" + (++counter);
            hooks.put(hookId, callback);
            return hookId;
        }

        @Override
        public String registerHookWithPriority(String pluginId, HookType hookType, int priority, HookCallback callback) {
            return registerHook(pluginId, hookType, callback);
        }

        @Override
        public boolean unregisterHook(String hookId) {
            return hooks.remove(hookId) != null;
        }

        @Override
        public int unregisterHooksByType(String pluginId, HookType hookType) {
            int count = 0;
            Iterator<Map.Entry<String, HookCallback>> iterator = hooks.entrySet().iterator();
            while (iterator.hasNext()) {
                iterator.next();
                count++;
                iterator.remove();
            }
            return count;
        }

        @Override
        public HookContext executeHooks(String pluginId, HookType hookType, Map<String, Object> hookData) {
            // ⭐ CRITIQUE : Exécuter tous les callbacks
            for (HookCallback callback : hooks.values()) {
                try {
                    callback.execute(new SimpleHookContext(pluginId, hookType, hookData));
                } catch (Exception e) {
                    // Ignorer les erreurs pour le test
                }
            }

            return new SimpleHookContext(pluginId, hookType, hookData);
        }

        @Override
        public Object executeHook(String hookId, Map<String, Object> hookData) {
            HookCallback callback = hooks.get(hookId);
            if (callback != null) {
                try {
                    callback.execute(new SimpleHookContext("test-plugin", HookType.POST_INIT, hookData));
                } catch (Exception e) {
                    // Ignorer
                }
            }
            return null;
        }

        @Override
        public List<String> getPluginHooks(String pluginId) {
            return new ArrayList<>(hooks.keySet());
        }

        @Override
        public List<String> getHooksByType(String pluginId, HookType hookType) {
            return new ArrayList<>(hooks.keySet());
        }

        @Override
        public List<Map<String, Object>> getHookExecutionHistory(String pluginId, int maxEntries) {
            return new ArrayList<>();
        }

        @Override
        public void clearHookExecutionHistory(String pluginId) {
        }

        // Implémentation simple de HookContext
        private static class SimpleHookContext implements HookContext {
            private final String pluginId;
            private final HookType hookType;
            private final Map<String, Object> hookData;
            private Object result;
            private boolean cancelled;

            SimpleHookContext(String pluginId, HookType hookType, Map<String, Object> hookData) {
                this.pluginId = pluginId;
                this.hookType = hookType;
                this.hookData = hookData != null ? new HashMap<>(hookData) : new HashMap<>();
            }

            @Override public String getPluginId() { return pluginId; }
            @Override public HookType getHookType() { return hookType; }
            @Override public Map<String, Object> getHookData() { return hookData; }
            @Override public void setResult(Object result) { this.result = result; }
            @Override public Object getResult() { return result; }
            @Override public void cancel() { this.cancelled = true; }
            @Override public boolean isCancelled() { return cancelled; }
        }
    }

    static class MockHookContext implements PluginHookService.HookContext {
        @Override
        public String getPluginId() { return "test"; }
        @Override
        public PluginHookService.HookType getHookType() { return PluginHookService.HookType.CUSTOM; }
        @Override
        public Map<String, Object> getHookData() { return new java.util.HashMap<>(); }
        @Override
        public void setResult(Object result) {}
        @Override
        public Object getResult() { return null; }
        @Override
        public void cancel() {}
        @Override
        public boolean isCancelled() { return false; }
    }
}

