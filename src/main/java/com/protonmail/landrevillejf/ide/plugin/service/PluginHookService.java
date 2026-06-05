package com.protonmail.landrevillejf.ide.plugin.service;

import java.util.List;
import java.util.Map;

/**
 * Plugin hook registry and execution service for lifecycle and event hooks.
 */
public interface PluginHookService {

    /**
     * Predefined hook types.
     */
    enum HookType {
        /** Executed before plugin initialization */
        PRE_INIT,
        /** Executed after plugin initialization */
        POST_INIT,
        /** Executed before plugin enable */
        PRE_ENABLE,
        /** Executed after plugin enable */
        POST_ENABLE,
        /** Executed before plugin disable */
        PRE_DISABLE,
        /** Executed after plugin disable */
        POST_DISABLE,
        /** Executed before plugin shutdown */
        PRE_SHUTDOWN,
        /** Executed after plugin shutdown */
        POST_SHUTDOWN,
        /** Executed on configuration change */
        CONFIG_CHANGED,
        /** Executed before configuration update */
        PRE_CONFIG_UPDATE,
        /** Executed after configuration update */
        POST_CONFIG_UPDATE,
        /** Custom hook */
        CUSTOM
    }

    /**
     * Represents a hook execution context.
     */
    interface HookContext {
        String getPluginId();
        HookType getHookType();
        Map<String, Object> getHookData();
        void setResult(Object result);
        Object getResult();
        void cancel();
        boolean isCancelled();
    }

    /**
     * Registers a hook for a plugin.
     *
     * @param pluginId the plugin identifier
     * @param hookType the hook type
     * @param callback the hook callback
     * @return a hook ID for later reference
     */
    String registerHook(String pluginId, HookType hookType, HookCallback callback);

    /**
     * Registers a hook with priority.
     *
     * @param pluginId the plugin identifier
     * @param hookType the hook type
     * @param priority the execution priority (higher = earlier)
     * @param callback the hook callback
     * @return a hook ID for later reference
     */
    String registerHookWithPriority(String pluginId, HookType hookType, int priority, HookCallback callback);

    /**
     * Unregisters a hook.
     *
     * @param hookId the hook identifier
     * @return true if the hook was unregistered
     */
    boolean unregisterHook(String hookId);

    /**
     * Unregisters all hooks of a type for a plugin.
     *
     * @param pluginId the plugin identifier
     * @param hookType the hook type
     * @return the number of hooks unregistered
     */
    int unregisterHooksByType(String pluginId, HookType hookType);

    /**
     * Executes all hooks of a type for a plugin.
     *
     * @param pluginId the plugin identifier
     * @param hookType the hook type
     * @param hookData optional hook data
     * @return execution context with results
     */
    HookContext executeHooks(String pluginId, HookType hookType, Map<String, Object> hookData);

    /**
     * Executes a specific hook.
     *
     * @param hookId the hook identifier
     * @param hookData optional hook data
     * @return the hook execution result
     */
    Object executeHook(String hookId, Map<String, Object> hookData);

    /**
     * Gets all hooks for a plugin.
     *
     * @param pluginId the plugin identifier
     * @return list of hook IDs
     */
    List<String> getPluginHooks(String pluginId);

    /**
     * Gets all hooks of a specific type for a plugin.
     *
     * @param pluginId the plugin identifier
     * @param hookType the hook type
     * @return list of hook IDs
     */
    List<String> getHooksByType(String pluginId, HookType hookType);

    /**
     * Gets hook execution history for a plugin.
     *
     * @param pluginId the plugin identifier
     * @param maxEntries the maximum number of history entries
     * @return list of hook execution records
     */
    List<Map<String, Object>> getHookExecutionHistory(String pluginId, int maxEntries);

    /**
     * Clears hook execution history for a plugin.
     *
     * @param pluginId the plugin identifier
     */
    void clearHookExecutionHistory(String pluginId);

    /**
     * Hook execution callback interface.
     */
    interface HookCallback {
        /**
         * Executes the hook.
         *
         * @param context the hook execution context
         */
        void execute(HookContext context);
    }
}

