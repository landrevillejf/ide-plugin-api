package com.protonmail.landrevillejf.ide.plugin.service.impl;

import com.protonmail.landrevillejf.ide.plugin.service.PluginHookService.HookContext;
import com.protonmail.landrevillejf.ide.plugin.service.PluginHookService.HookType;
import com.protonmail.landrevillejf.ide.plugin.utils.LogCapture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Mutation-killing tests for {@link DefaultPluginHookService}.
 */
@DisplayName("DefaultPluginHookService mutation tests")
class DefaultPluginHookServiceMutationTest {

    private static final String P = "hook-plugin";

    private DefaultPluginHookService service;

    @BeforeEach
    void setUp() {
        service = new DefaultPluginHookService();
    }

    @Test
    @DisplayName("constructor logs initialization")
    void constructorLogs() {
        try (LogCapture capture = LogCapture.attach(DefaultPluginHookService.class)) {
            new DefaultPluginHookService();
            assertThat(capture.formattedMessages())
                    .anyMatch(m -> m.contains("DefaultPluginHookService initialized"));
        }
    }

    @Nested
    @DisplayName("registration")
    class RegistrationTests {

        @Test
        @DisplayName("hook ids follow the documented format")
        void hookIdFormat() {
            String id = service.registerHook(P, HookType.PRE_INIT, ctx -> { });
            assertThat(id).startsWith("hook-plugin_PRE_INIT_");
        }

        @Test
        @DisplayName("plugin hooks are kept sorted by priority")
        void sortedByPriority() {
            String low = service.registerHookWithPriority(P, HookType.PRE_INIT, 1, ctx -> { });
            String high = service.registerHookWithPriority(P, HookType.PRE_INIT, 10, ctx -> { });
            String mid = service.registerHookWithPriority(P, HookType.PRE_INIT, 5, ctx -> { });

            assertThat(service.getPluginHooks(P)).containsExactly(high, mid, low);
        }

        @Test
        @DisplayName("registration logs the hook details")
        void registrationLogs() {
            try (LogCapture capture = LogCapture.attach(DefaultPluginHookService.class)) {
                service.registerHookWithPriority(P, HookType.POST_INIT, 3, ctx -> { });
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("Hook registered") && m.contains("priority=3"));
            }
        }

        @Test
        @DisplayName("unregisterHook removes a single hook")
        void unregisterHook() {
            String id = service.registerHook(P, HookType.PRE_INIT, ctx -> { });

            try (LogCapture capture = LogCapture.attach(DefaultPluginHookService.class)) {
                assertThat(service.unregisterHook(id)).isTrue();
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("Hook unregistered"));
            }
            assertThat(service.unregisterHook(id)).isFalse();
            assertThat(service.getPluginHooks(P)).isEmpty();
        }

        @Test
        @DisplayName("unregisterHooksByType removes only matching hooks")
        void unregisterByType() {
            service.registerHook(P, HookType.PRE_INIT, ctx -> { });
            service.registerHook(P, HookType.PRE_INIT, ctx -> { });
            service.registerHook(P, HookType.POST_INIT, ctx -> { });

            try (LogCapture capture = LogCapture.attach(DefaultPluginHookService.class)) {
                assertThat(service.unregisterHooksByType(P, HookType.PRE_INIT)).isEqualTo(2);
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("Unregistered 2 hooks"));
            }
            assertThat(service.getPluginHooks(P)).hasSize(1);
            assertThat(service.unregisterHooksByType(P, HookType.PRE_INIT)).isZero();
            assertThat(service.unregisterHooksByType("ghost", HookType.PRE_INIT)).isZero();
        }
    }

    @Nested
    @DisplayName("execution")
    class ExecutionTests {

        @Test
        @DisplayName("executeHooks without registered hooks returns a fresh context")
        void noHooks() {
            try (LogCapture capture = LogCapture.attach(DefaultPluginHookService.class)) {
                HookContext ctx = service.executeHooks("ghost", HookType.PRE_INIT, Map.of());
                assertThat(ctx).isNotNull();
                assertThat(ctx.getPluginId()).isEqualTo("ghost");
                assertThat(ctx.getHookType()).isEqualTo(HookType.PRE_INIT);
                assertThat(ctx.isCancelled()).isFalse();
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("No hooks to execute"));
            }
        }

        @Test
        @DisplayName("executeHooks without matching type returns a fresh context")
        void noMatchingType() {
            service.registerHook(P, HookType.POST_INIT, ctx -> { });

            try (LogCapture capture = LogCapture.attach(DefaultPluginHookService.class)) {
                HookContext ctx = service.executeHooks(P, HookType.PRE_SHUTDOWN, Map.of());
                assertThat(ctx).isNotNull();
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("No hooks of type"));
            }
        }

        @Test
        @DisplayName("hooks run by priority order and only for the requested type")
        void orderAndTypeFilter() {
            List<String> calls = new ArrayList<>();
            service.registerHookWithPriority(P, HookType.PRE_INIT, 1,
                    ctx -> calls.add("low"));
            service.registerHookWithPriority(P, HookType.PRE_INIT, 10,
                    ctx -> calls.add("high"));
            service.registerHook(P, HookType.POST_INIT, ctx -> calls.add("wrong-type"));

            try (LogCapture capture = LogCapture.attach(DefaultPluginHookService.class)) {
                HookContext ctx = service.executeHooks(P, HookType.PRE_INIT, Map.of("k", "v"));
                assertThat(ctx.getHookData()).containsEntry("k", "v");
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("Executing 2 hooks"));
            }
            assertThat(calls).containsExactly("high", "low");
        }

        @Test
        @DisplayName("null hook data yields an empty data map")
        void nullHookData() {
            service.registerHook(P, HookType.PRE_INIT,
                    ctx -> assertThat(ctx.getHookData()).isEmpty());
            HookContext ctx = service.executeHooks(P, HookType.PRE_INIT, null);
            assertThat(ctx).isNotNull();
        }

        @Test
        @DisplayName("cancellation stops subsequent hooks")
        void cancellation() {
            List<String> calls = new ArrayList<>();
            service.registerHookWithPriority(P, HookType.PRE_INIT, 10, ctx -> {
                calls.add("first");
                ctx.cancel();
            });
            service.registerHookWithPriority(P, HookType.PRE_INIT, 1,
                    ctx -> calls.add("second"));

            try (LogCapture capture = LogCapture.attach(DefaultPluginHookService.class)) {
                HookContext ctx = service.executeHooks(P, HookType.PRE_INIT, Map.of());
                assertThat(ctx.isCancelled()).isTrue();
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("Hook execution cancelled for plugin"))
                        .anyMatch(m -> m.contains("Hook execution cancelled at plugin"));
            }
            assertThat(calls).containsExactly("first");
        }

        @Test
        @DisplayName("cancel on a non-cancellable context warns")
        void cancelNonCancellable() {
            try (LogCapture capture = LogCapture.attach(DefaultPluginHookService.class)) {
                HookContext ctx = service.executeHooks("ghost", HookType.PRE_INIT, Map.of());
                ctx.cancel();
                assertThat(ctx.isCancelled()).isFalse();
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("non-cancellable"));
            }
        }

        @Test
        @DisplayName("hook exceptions are recorded but do not break execution")
        void hookException() {
            List<String> calls = new ArrayList<>();
            service.registerHookWithPriority(P, HookType.PRE_INIT, 10, ctx -> {
                throw new IllegalStateException("boom");
            });
            service.registerHookWithPriority(P, HookType.PRE_INIT, 1,
                    ctx -> calls.add("after"));

            try (LogCapture capture = LogCapture.attach(DefaultPluginHookService.class)) {
                service.executeHooks(P, HookType.PRE_INIT, Map.of());
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("Hook execution failed"));
            }
            assertThat(calls).containsExactly("after");

            List<Map<String, Object>> history = service.getHookExecutionHistory(P, 10);
            assertThat(history).hasSize(2);
            assertThat(history.get(0)).containsEntry("error", "boom");
            assertThat(history.get(1)).doesNotContainKey("error");
        }

        @Test
        @DisplayName("executeHook returns the callback result")
        void singleHookResult() {
            String id = service.registerHook(P, HookType.PRE_INIT,
                    ctx -> ctx.setResult("done"));

            try (LogCapture capture = LogCapture.attach(DefaultPluginHookService.class)) {
                assertThat(service.executeHook(id, Map.of())).isEqualTo("done");
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("Single hook executed"));
            }
        }

        @Test
        @DisplayName("executeHook with unknown id warns and returns null")
        void singleHookUnknown() {
            try (LogCapture capture = LogCapture.attach(DefaultPluginHookService.class)) {
                assertThat(service.executeHook("nope", Map.of())).isNull();
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("Hook not found"));
            }
        }

        @Test
        @DisplayName("executeHook records exceptions")
        void singleHookException() {
            String id = service.registerHook(P, HookType.PRE_INIT, ctx -> {
                throw new IllegalStateException("single-boom");
            });

            try (LogCapture capture = LogCapture.attach(DefaultPluginHookService.class)) {
                assertThat(service.executeHook(id, Map.of())).isNull();
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("Single hook execution failed"));
            }

            List<Map<String, Object>> history = service.getHookExecutionHistory(P, 10);
            assertThat(history).hasSize(1);
            assertThat(history.get(0)).containsEntry("error", "single-boom");
        }

        @Test
        @DisplayName("execution time is a small non-negative duration")
        void executionTimeSanity() {
            String id = service.registerHook(P, HookType.PRE_INIT, ctx -> { });
            service.executeHook(id, Map.of());
            service.executeHooks(P, HookType.PRE_INIT, Map.of());

            List<Map<String, Object>> history = service.getHookExecutionHistory(P, 10);
            assertThat(history).hasSize(2);
            for (Map<String, Object> record : history) {
                long time = (Long) record.get("executionTimeMs");
                assertThat(time).isBetween(0L, 5_000L);
            }
        }
    }

    @Nested
    @DisplayName("history")
    class HistoryTests {

        @Test
        @DisplayName("history keeps only the last 1000 records, oldest first")
        void historyCap() {
            AtomicInteger counter = new AtomicInteger();
            String id = service.registerHook(P, HookType.PRE_INIT,
                    ctx -> { throw new IllegalStateException("err-" + counter.getAndIncrement()); });

            for (int i = 0; i < 1001; i++) {
                service.executeHook(id, Map.of());
            }

            List<Map<String, Object>> history = service.getHookExecutionHistory(P, 2000);
            assertThat(history).hasSize(1000);
            assertThat(history.get(0)).containsEntry("error", "err-1");
            assertThat(history.get(999)).containsEntry("error", "err-1000");
        }

        @Test
        @DisplayName("maxEntries limits the returned window")
        void maxEntries() {
            String id = service.registerHook(P, HookType.PRE_INIT,
                    ctx -> ctx.setResult("ok"));
            service.executeHook(id, Map.of());
            service.executeHook(id, Map.of());
            service.executeHook(id, Map.of());

            List<Map<String, Object>> window = service.getHookExecutionHistory(P, 2);
            assertThat(window).hasSize(2);
            assertThat(window.get(0)).containsKeys("timestamp", "hookType", "hookId", "executionTimeMs");
            assertThat(window.get(0)).containsEntry("hookType", "PRE_INIT");
            assertThat(window.get(0)).containsEntry("hookId", id);
            assertThat(service.getHookExecutionHistory(P, 0)).isEmpty();
        }

        @Test
        @DisplayName("history of unknown plugin is empty")
        void unknownHistory() {
            assertThat(service.getHookExecutionHistory("ghost", 5)).isEmpty();
        }

        @Test
        @DisplayName("clearHookExecutionHistory empties the records")
        void clearHistory() {
            String id = service.registerHook(P, HookType.PRE_INIT, ctx -> { });
            service.executeHook(id, Map.of());

            try (LogCapture capture = LogCapture.attach(DefaultPluginHookService.class)) {
                service.clearHookExecutionHistory(P);
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("history cleared"));
            }
            assertThat(service.getHookExecutionHistory(P, 5)).isEmpty();

            // clearing an unknown plugin is a silent no-op
            service.clearHookExecutionHistory("ghost");
        }
    }

    @Nested
    @DisplayName("queries")
    class QueryTests {

        @Test
        @DisplayName("getPluginHooks of unknown plugin is empty")
        void unknownPluginHooks() {
            assertThat(service.getPluginHooks("ghost")).isEmpty();
        }

        @Test
        @DisplayName("getHooksByType filters by type")
        void hooksByType() {
            String init = service.registerHook(P, HookType.PRE_INIT, ctx -> { });
            service.registerHook(P, HookType.POST_INIT, ctx -> { });

            assertThat(service.getHooksByType(P, HookType.PRE_INIT)).containsExactly(init);
            assertThat(service.getHooksByType(P, HookType.PRE_SHUTDOWN)).isEmpty();
            assertThat(service.getHooksByType("ghost", HookType.PRE_INIT)).isEmpty();
        }
    }
}
