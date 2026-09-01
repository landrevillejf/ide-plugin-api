package com.protonmail.landrevillejf.ide.plugin;

import com.protonmail.landrevillejf.ide.plugin.ui.ComponentRegistry;

import java.io.File;

/**
 * Context interface providing access to plugin services and resources.
 * <p>
 * This interface provides plugins with access to the host application's
 * services, event bus, and other resources needed for plugin operation.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
public interface PluginContext {
    /**
     * Returns the plugin event bus for publishing and subscribing to events.
     *
     * @return the plugin event bus
     */
    PluginEventBus getEventBus();

    /**
     * Returns a service by its class.
     *
     * @param <T> the service type
     * @param serviceClass the class of the service to retrieve
     * @return the service instance, or {@code null} if not found
     */
    <T> T getService(Class<T> serviceClass);

    /**
     * Registers a service implementation.
     *
     * @param <T> the service type
     * @param serviceClass the class of the service
     * @param instance the service instance to register
     */
    <T> void registerService(Class<T> serviceClass, T instance);

    /**
     * Unregisters a service.
     *
     * @param <T> the service type
     * @param serviceClass the class of the service to unregister
     */
    <T> void unregisterService(Class<T> serviceClass);

    /**
     * Registers a service by its instance type.
     *
     * @param service the service instance to register
     */
    void registerService(Object service);

    /**
     * Returns the plugin manager.
     *
     * @return the plugin manager
     */
    PluginManager getPluginManager();

    /**
     * Returns the path to the plugin's data directory.
     *
     * @return the plugin data directory path
     */
    String getPluginDataPath();

    /**
     * Logs an info message.
     *
     * @param message the message to log
     */
    void logInfo(String message);

    /**
     * Logs a warning message.
     *
     * @param message the message to log
     */
    void logWarning(String message);

    /**
     * Logs an error message with a throwable.
     *
     * @param message the message to log
     * @param throwable the throwable to log
     */
    void logError(String message, Throwable throwable);

    /**
     * Shows a notification to the user.
     *
     * @param title the notification title
     * @param message the notification message
     */
    void showNotification(String title, String message);

    /**
     * Logs a debug message.
     *
     * @param s the message to log
     */
    void logDebug(String s);

    /**
     * Gets the component registry for registering UI components.
     *
     * @return the component registry
     */
    ComponentRegistry getComponentRegistry();

    /**
     * Returns the plugin identifier.
     *
     * @return the plugin ID
     */
    String getPluginId();

    /**
     * Returns the plugin instance.
     *
     * @return the plugin instance
     */
    Plugin getPlugin();

    /**
     * Returns the plugin's data directory.
     *
     * @return the plugin data directory
     */
    File getPluginDataDirectory();
}