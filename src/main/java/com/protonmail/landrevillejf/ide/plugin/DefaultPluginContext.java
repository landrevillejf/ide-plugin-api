package com.protonmail.landrevillejf.ide.plugin;

import com.protonmail.landrevillejf.ide.plugin.ui.ComponentRegistry;
import com.protonmail.landrevillejf.ide.plugin.ui.UIComponentAccessor;
import com.protonmail.landrevillejf.swingide.core.bus.EventBus;
import com.protonmail.landrevillejf.swingide.core.registry.ServiceRegistry;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.io.File;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Default implementation of the PluginContext interface.
 * <p>
 * This class provides a concrete implementation of the plugin context,
 * managing service registration, event bus access, and plugin data directory.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
@Slf4j
public class DefaultPluginContext implements PluginContext {
    private final ServiceRegistry serviceRegistry;
    private final Map<Class<?>, Object> localServices = new ConcurrentHashMap<>();
    private final PluginEventBus pluginEventBus;
    private final EventBus applicationEventBus;
    @Setter
    private PluginManager pluginManager;
    private final File pluginDataDirectory;
    private final String pluginId;
    private Plugin plugin;

    @Getter @Setter
    private static UIComponentAccessor uiComponentAccessor;

    /**
     * Shared component registry for all plugins in the IDE.
     * This is a class-level singleton initialized once.
     */
    private static final ComponentRegistry componentRegistry = new ComponentRegistry();

    /**
     * Creates a new default plugin context.
     *
     * @param serviceRegistry the application service registry
     * @param pluginEventBus the plugin event bus
     * @param applicationEventBus the application event bus
     * @param pluginManager the plugin manager
     * @param pluginDataDirectory the plugin data directory
     * @param pluginId the plugin identifier
     */
    public DefaultPluginContext(ServiceRegistry serviceRegistry,
                                PluginEventBus pluginEventBus,
                                EventBus applicationEventBus,
                                PluginManager pluginManager,
                                File pluginDataDirectory,
                                String pluginId) {
        this.serviceRegistry = serviceRegistry;
        this.pluginEventBus = pluginEventBus;
        this.applicationEventBus = applicationEventBus;
        this.pluginManager = pluginManager;
        this.pluginDataDirectory = pluginDataDirectory;
        this.pluginId = pluginId;
        this.plugin = null;

        if (pluginDataDirectory != null && !pluginDataDirectory.exists()) {
            boolean created = pluginDataDirectory.mkdirs();
            if (!created && log.isWarnEnabled()) {
                log.warn("Failed to create plugin data directory: {}", pluginDataDirectory);
            }
        }
    }

    /**
     * Creates a new default plugin context with a plugin instance.
     *
     * @param serviceRegistry the application service registry
     * @param pluginEventBus the plugin event bus
     * @param applicationEventBus the application event bus
     * @param pluginManager the plugin manager
     * @param pluginDataDirectory the plugin data directory
     * @param pluginId the plugin identifier
     * @param plugin the plugin instance
     */
    public DefaultPluginContext(ServiceRegistry serviceRegistry,
                                PluginEventBus pluginEventBus,
                                EventBus applicationEventBus,
                                PluginManager pluginManager,
                                File pluginDataDirectory,
                                String pluginId,
                                Plugin plugin) {
        this(serviceRegistry, pluginEventBus, applicationEventBus, pluginManager,
                pluginDataDirectory, pluginId);
        this.plugin = plugin;
        // Directory creation is handled by the delegated 6-arg constructor
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public PluginEventBus getEventBus() {
        return pluginEventBus;
    }

    /**
     * Returns a service by its class, checking local services first.
     *
     * @param <T> the service type
     * @param serviceClass the class of the service to retrieve
     * @return the service instance, or {@code null} if not found
     */
    @Override
    public <T> T getService(Class<T> serviceClass) {
        @SuppressWarnings("unchecked")
        T localService = (T) localServices.get(serviceClass);
        if (localService != null) {
            return localService;
        }
        return serviceRegistry.getService(serviceClass);
    }

    /**
     * Registers a service implementation in both local and global registries.
     *
     * @param <T> the service type
     * @param serviceClass the class of the service
     * @param instance the service instance to register
     * @throws NullPointerException if serviceClass or instance is null
     */
    @Override
    public <T> void registerService(Class<T> serviceClass, T instance) {
        Objects.requireNonNull(serviceClass, "Service class cannot be null");
        Objects.requireNonNull(instance, "Service instance cannot be null");

        serviceRegistry.register(serviceClass, instance);
        localServices.put(serviceClass, instance);
        if (log.isDebugEnabled()) {
            String pluginName = (plugin != null) ? plugin.getName() : pluginId;
            log.debug("Service registered: {} by plugin: {}", serviceClass.getSimpleName(), pluginName);
        }
    }

    /**
     * Unregisters a service from the local registry.
     *
     * @param <T> the service type
     * @param service the class of the service to unregister
     */
    @Override
    public <T> void unregisterService(Class<T> service) {
        if (service == null) {
            return;
        }

        localServices.remove(service);
        if (log.isDebugEnabled()) {
            String pluginName = (plugin != null) ? plugin.getName() : pluginId;
            log.debug("Service unregistered: {} for plugin: {}", service.getSimpleName(), pluginName);
        }
    }

    /**
     * Registers a service by its instance type in the local registry.
     *
     * @param service the service instance to register
     */
    @Override
    public void registerService(Object service) {
        if (service == null) {
            if (log.isWarnEnabled()) {
                String pluginName = (plugin != null) ? plugin.getName() : pluginId;
                log.warn("Attempted to register null service for plugin: {}", pluginName);
            }
            return;
        }

        Class<?> serviceClass = service.getClass();
        localServices.put(serviceClass, service);
        if (log.isDebugEnabled()) {
            String pluginName = (plugin != null) ? plugin.getName() : pluginId;
            log.debug("Service registered: {} for plugin: {}", serviceClass.getSimpleName(), pluginName);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public PluginManager getPluginManager() {
        return pluginManager;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public String getPluginDataPath() {
        return (pluginDataDirectory != null) ? pluginDataDirectory.getAbsolutePath() : "";
    }

    /**
     * Logs an info message with the plugin name prefix.
     *
     * @param message the message to log
     */
    @Override
    public void logInfo(String message) {
        if (log.isDebugEnabled()) {
            String pluginName = (plugin != null) ? plugin.getName() : pluginId;
            log.debug("[Plugin {}] {}", pluginName, message);
        }
    }

    /**
     * Logs a warning message with the plugin name prefix.
     *
     * @param message the message to log
     */
    @Override
    public void logWarning(String message) {
        if (log.isWarnEnabled()) {
            String pluginName = (plugin != null) ? plugin.getName() : pluginId;
            log.warn("[Plugin {}] {}", pluginName, message);
        }
    }

    /**
     * Logs an error message with the plugin name prefix.
     *
     * @param message the message to log
     * @param throwable the throwable to log
     */
    @Override
    public void logError(String message, Throwable throwable) {
        if (log.isErrorEnabled()) {
            String pluginName = (plugin != null) ? plugin.getName() : pluginId;
            log.error("[Plugin {}] {}", pluginName, message, throwable);
        }
    }

    /**
     * Shows a notification dialog to the user on the EDT.
     *
     * @param title the notification title
     * @param message the notification message
     */
    @Override
    public void showNotification(String title, String message) {
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(
                    null,
                    message,
                    title,
                    JOptionPane.INFORMATION_MESSAGE
            );
        });
    }

    /**
     * Logs a debug message with the plugin name prefix.
     *
     * @param message the message to log
     */
    @Override
    public void logDebug(String message) {
        if (log.isDebugEnabled()) {
            String pluginName = (plugin != null) ? plugin.getName() : pluginId;
            log.debug("[Plugin {}] {}", pluginName, message);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public ComponentRegistry getComponentRegistry() {
        return componentRegistry;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public String getPluginId() {
        return pluginId;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public Plugin getPlugin() {
        return plugin;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public File getPluginDataDirectory() {
        return pluginDataDirectory;
    }
}