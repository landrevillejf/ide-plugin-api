package com.protonmail.landrevillejf.ide.plugin.service;

import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

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
    public static class MockPluginHookService implements PluginHookService {
        private int hookCounter = 0;

        @Override
        public String registerHook(String pluginId, HookType hookType, HookCallback callback) {
            return "hook-" + (++hookCounter);
        }

        @Override
        public String registerHookWithPriority(String pluginId, HookType hookType, int priority, HookCallback callback) {
            return "hook-" + (++hookCounter);
        }

        @Override
        public boolean unregisterHook(String hookId) { return true; }

        @Override
        public int unregisterHooksByType(String pluginId, HookType hookType) { return 0; }

        @Override
        public HookContext executeHooks(String pluginId, HookType hookType, Map<String, Object> hookData) {
            return new MockHookContext();
        }

        @Override
        public Object executeHook(String hookId, Map<String, Object> hookData) { return null; }

        @Override
        public List<String> getPluginHooks(String pluginId) { return java.util.Collections.emptyList(); }

        @Override
        public List<String> getHooksByType(String pluginId, HookType hookType) { return java.util.Collections.emptyList(); }

        @Override
        public List<Map<String, Object>> getHookExecutionHistory(String pluginId, int maxEntries) { return java.util.Collections.emptyList(); }

        @Override
        public void clearHookExecutionHistory(String pluginId) {}
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

