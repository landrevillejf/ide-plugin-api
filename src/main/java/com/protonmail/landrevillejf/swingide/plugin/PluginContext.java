package com.protonmail.landrevillejf.swingide.plugin;

import com.protonmail.landrevillejf.swingide.plugin.ui.ComponentRegistry;

public interface PluginContext {
    PluginEventBus getEventBus();
    <T> T getService(Class<T> serviceClass);
    <T> void registerService(Class<T> serviceClass, T instance);
    <T> void unregisterService(Class<T> serviceClass);
    void registerService(Object service);
    PluginManager getPluginManager();
    String getPluginDataPath();
    void logInfo(String message);
    void logWarning(String message);
    void logError(String message, Throwable throwable);
    void showNotification(String title, String message);
    void logDebug(String s);

    /**
     * Gets the component registry for registering UI components.
     *
     * @return the component registry
     */
    ComponentRegistry getComponentRegistry();
}