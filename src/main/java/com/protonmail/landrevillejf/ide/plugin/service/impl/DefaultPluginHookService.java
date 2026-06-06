package com.protonmail.landrevillejf.ide.plugin.service.impl;

import com.protonmail.landrevillejf.ide.plugin.service.PluginHookService;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
public final class DefaultPluginHookService implements PluginHookService {

    private final Map<String, RegisteredHook> hooks = new ConcurrentHashMap<>();
    private final Map<String, List<RegisteredHook>> hooksByPlugin = new ConcurrentHashMap<>();
    private final Map<String, List<HookExecutionRecord>> executionHistory = new ConcurrentHashMap<>();
    private final AtomicLong hookIdGenerator = new AtomicLong(0);

    public DefaultPluginHookService() {
        if (log.isInfoEnabled()) {
            log.info("DefaultPluginHookService initialized");
        }
    }

    @Override
    public String registerHook(String pluginId, HookType hookType, HookCallback callback) {
        return registerHookWithPriority(pluginId, hookType, 0, callback);
    }

    @Override
    public String registerHookWithPriority(String pluginId, HookType hookType, int priority, HookCallback callback) {
        String hookId = generateHookId(pluginId, hookType);
        RegisteredHook hook = new RegisteredHook(hookId, pluginId, hookType, priority, callback);

        hooks.put(hookId, hook);
        hooksByPlugin.computeIfAbsent(pluginId, k -> new CopyOnWriteArrayList<>()).add(hook);

        // Sort hooks by priority when adding
        List<RegisteredHook> pluginHooks = hooksByPlugin.get(pluginId);
        if (pluginHooks != null) {
            pluginHooks.sort((h1, h2) -> Integer.compare(h2.priority, h1.priority));
        }

        if (log.isDebugEnabled()) {
            log.debug("Hook registered: plugin={}, type={}, id={}, priority={}",
                    pluginId, hookType, hookId, priority);
        }

        return hookId;
    }

    @Override
    public boolean unregisterHook(String hookId) {
        RegisteredHook hook = hooks.remove(hookId);
        if (hook == null) {
            return false;
        }

        List<RegisteredHook> pluginHooks = hooksByPlugin.get(hook.pluginId);
        if (pluginHooks != null) {
            pluginHooks.remove(hook);
        }

        if (log.isDebugEnabled()) {
            log.debug("Hook unregistered: plugin={}, type={}, id={}",
                    hook.pluginId, hook.hookType, hookId);
        }

        return true;
    }

    @Override
    public int unregisterHooksByType(String pluginId, HookType hookType) {
        List<RegisteredHook> pluginHooks = hooksByPlugin.get(pluginId);
        if (pluginHooks == null || pluginHooks.isEmpty()) {
            return 0;
        }

        List<RegisteredHook> toRemove = pluginHooks.stream()
                .filter(hook -> hook.hookType == hookType)
                .collect(Collectors.toList());

        for (RegisteredHook hook : toRemove) {
            hooks.remove(hook.id);
            pluginHooks.remove(hook);
        }

        if (log.isDebugEnabled()) {
            log.debug("Unregistered {} hooks for plugin={}, type={}", toRemove.size(), pluginId, hookType);
        }
        return toRemove.size();
    }

    @Override
    public HookContext executeHooks(String pluginId, HookType hookType, Map<String, Object> hookData) {
        List<RegisteredHook> pluginHooks = hooksByPlugin.get(pluginId);
        if (pluginHooks == null || pluginHooks.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("No hooks to execute for plugin={}, type={}", pluginId, hookType);
            }
            return new HookContextImpl(pluginId, hookType, hookData, false);
        }

        // Filter hooks by type
        List<RegisteredHook> hooksToExecute = pluginHooks.stream()
                .filter(hook -> hook.hookType == hookType)
                .sorted((h1, h2) -> Integer.compare(h2.priority, h1.priority))
                .collect(Collectors.toList());

        if (hooksToExecute.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("No hooks of type {} for plugin={}", hookType, pluginId);
            }
            return new HookContextImpl(pluginId, hookType, hookData, false);
        }

        if (log.isDebugEnabled()) {
            log.debug("Executing {} hooks for plugin={}, type={}", hooksToExecute.size(), pluginId, hookType);
        }

        HookContextImpl context = new HookContextImpl(pluginId, hookType, hookData, true);

        for (RegisteredHook hook : hooksToExecute) {
            if (context.isCancelled()) {
                if (log.isDebugEnabled()) {
                    log.debug("Hook execution cancelled at plugin={}, type={}, hook={}",
                            pluginId, hookType, hook.id);
                }
                break;
            }

            try {
                long startTime = System.currentTimeMillis();
                hook.callback.execute(context);
                long executionTime = System.currentTimeMillis() - startTime;

                // Record execution
                recordExecution(pluginId, hookType, hook.id, executionTime, null);

                if (log.isDebugEnabled()) {
                    log.debug("Hook executed: plugin={}, type={}, id={}, time={}ms",
                            pluginId, hookType, hook.id, executionTime);
                }

            } catch (Exception e) {
                if (log.isErrorEnabled()) {
                    log.error("Hook execution failed: plugin={}, type={}, id={}",
                            pluginId, hookType, hook.id, e);
                }
                recordExecution(pluginId, hookType, hook.id, 0, e);
            }
        }

        return context;
    }

    @Override
    public Object executeHook(String hookId, Map<String, Object> hookData) {
        RegisteredHook hook = hooks.get(hookId);
        if (hook == null) {
            if (log.isWarnEnabled()) {
                log.warn("Hook not found: {}", hookId);
            }
            return null;
        }

        HookContextImpl context = new HookContextImpl(hook.pluginId, hook.hookType, hookData, false);

        try {
            long startTime = System.currentTimeMillis();
            hook.callback.execute(context);
            long executionTime = System.currentTimeMillis() - startTime;

            recordExecution(hook.pluginId, hook.hookType, hookId, executionTime, null);

            if (log.isDebugEnabled()) {
                log.debug("Single hook executed: plugin={}, type={}, id={}, time={}ms",
                        hook.pluginId, hook.hookType, hookId, executionTime);
            }

        } catch (Exception e) {
            if (log.isErrorEnabled()) {
                log.error("Single hook execution failed: plugin={}, type={}, id={}",
                        hook.pluginId, hook.hookType, hookId, e);
            }
            recordExecution(hook.pluginId, hook.hookType, hookId, 0, e);
        }

        return context.getResult();
    }

    @Override
    public List<String> getPluginHooks(String pluginId) {
        List<RegisteredHook> pluginHooks = hooksByPlugin.get(pluginId);
        if (pluginHooks == null) {
            return Collections.emptyList();
        }
        return pluginHooks.stream()
                .map(hook -> hook.id)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getHooksByType(String pluginId, HookType hookType) {
        List<RegisteredHook> pluginHooks = hooksByPlugin.get(pluginId);
        if (pluginHooks == null) {
            return Collections.emptyList();
        }
        return pluginHooks.stream()
                .filter(hook -> hook.hookType == hookType)
                .map(hook -> hook.id)
                .collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Object>> getHookExecutionHistory(String pluginId, int maxEntries) {
        List<HookExecutionRecord> records = executionHistory.get(pluginId);
        if (records == null || records.isEmpty()) {
            return Collections.emptyList();
        }

        int start = Math.max(0, records.size() - maxEntries);
        List<Map<String, Object>> result = new ArrayList<>();

        for (int i = start; i < records.size(); i++) {
            HookExecutionRecord record = records.get(i);
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("timestamp", record.timestamp);
            map.put("hookType", record.hookType.name());
            map.put("hookId", record.hookId);
            map.put("executionTimeMs", record.executionTimeMs);
            if (record.error != null) {
                map.put("error", record.error.getMessage());
            }
            result.add(map);
        }

        return result;
    }

    @Override
    public void clearHookExecutionHistory(String pluginId) {
        List<HookExecutionRecord> records = executionHistory.get(pluginId);
        if (records != null) {
            records.clear();
            if (log.isDebugEnabled()) {
                log.debug("Hook execution history cleared for plugin: {}", pluginId);
            }
        }
    }

    private String generateHookId(String pluginId, HookType hookType) {
        return pluginId + "_" + hookType.name() + "_" + hookIdGenerator.incrementAndGet();
    }

    private void recordExecution(String pluginId, HookType hookType, String hookId,
                                 long executionTimeMs, Throwable error) {
        List<HookExecutionRecord> records = executionHistory.computeIfAbsent(pluginId,
                k -> new CopyOnWriteArrayList<>());

        HookExecutionRecord record = new HookExecutionRecord(hookType, hookId, executionTimeMs, error);
        records.add(record);

        // Keep only last 1000 records per plugin
        if (records.size() > 1000) {
            records.remove(0);
        }
    }

    /**
     * Implementation of HookContext
     */
    private static final class HookContextImpl implements HookContext {
        private final String pluginId;
        private final HookType hookType;
        private final Map<String, Object> hookData;
        private final boolean allowCancellation;
        private Object result;
        private boolean cancelled;

        public HookContextImpl(String pluginId, HookType hookType, Map<String, Object> hookData,
                               boolean allowCancellation) {
            this.pluginId = pluginId;
            this.hookType = hookType;
            this.hookData = hookData != null ? new ConcurrentHashMap<>(hookData) : new ConcurrentHashMap<>();
            this.allowCancellation = allowCancellation;
            this.cancelled = false;
        }

        @Override
        public String getPluginId() { return pluginId; }

        @Override
        public HookType getHookType() { return hookType; }

        @Override
        public Map<String, Object> getHookData() { return hookData; }

        @Override
        public void setResult(Object result) { this.result = result; }

        @Override
        public Object getResult() { return result; }

        @Override
        public void cancel() {
            if (allowCancellation) {
                this.cancelled = true;
                if (log.isDebugEnabled()) {
                    log.debug("Hook execution cancelled for plugin={}, type={}", pluginId, hookType);
                }
            } else {
                if (log.isWarnEnabled()) {
                    log.warn("Attempted to cancel non-cancellable hook execution for plugin={}, type={}",
                            pluginId, hookType);
                }
            }
        }

        @Override
        public boolean isCancelled() { return cancelled; }
    }

    /**
     * Registered hook information
     */
    private static final class RegisteredHook {
        final String id;
        final String pluginId;
        final HookType hookType;
        final int priority;
        final HookCallback callback;

        RegisteredHook(String id, String pluginId, HookType hookType, int priority, HookCallback callback) {
            this.id = id;
            this.pluginId = pluginId;
            this.hookType = hookType;
            this.priority = priority;
            this.callback = callback;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            RegisteredHook that = (RegisteredHook) o;
            return Objects.equals(id, that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }

    /**
     * Hook execution record for history
     */
    private static final class HookExecutionRecord {
        final long timestamp;
        final HookType hookType;
        final String hookId;
        final long executionTimeMs;
        final Throwable error;

        HookExecutionRecord(HookType hookType, String hookId, long executionTimeMs, Throwable error) {
            this.timestamp = System.currentTimeMillis();
            this.hookType = hookType;
            this.hookId = hookId;
            this.executionTimeMs = executionTimeMs;
            this.error = error;
        }
    }
}