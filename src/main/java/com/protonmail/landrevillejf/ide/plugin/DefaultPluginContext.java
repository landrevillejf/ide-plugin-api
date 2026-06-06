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
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class DefaultPluginContext implements PluginContext {
    private final ServiceRegistry serviceRegistry;
    private final ConcurrentHashMap<Class<?>, Object> localServices = new ConcurrentHashMap<>();
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
            pluginDataDirectory.mkdirs();
        }
    }

    public DefaultPluginContext(ServiceRegistry serviceRegistry,
                                PluginEventBus pluginEventBus,
                                EventBus applicationEventBus,
                                PluginManager pluginManager,
                                File pluginDataDirectory,
                                String pluginId,
                                Plugin plugin) {
        this(serviceRegistry, pluginEventBus, applicationEventBus, pluginManager,
                pluginDataDirectory, pluginId);
        this.plugin = plugin;  // Si vous avez un champ plugin dans la classe

        if (pluginDataDirectory != null && !pluginDataDirectory.exists()) {
            pluginDataDirectory.mkdirs();
        }
    }

    @Override
    public PluginEventBus getEventBus() {
        return pluginEventBus;
    }

    @Override
    public <T> T getService(Class<T> serviceClass) {
        T localService = (T) localServices.get(serviceClass);
        if (localService != null) {
            return localService;
        }
        return serviceRegistry.getService(serviceClass);
    }

    @Override
    public <T> void registerService(Class<T> serviceClass, T instance) {
        Objects.requireNonNull(serviceClass, "Service class cannot be null");
        Objects.requireNonNull(instance, "Service instance cannot be null");

        serviceRegistry.register(serviceClass, instance);
        localServices.put(serviceClass, instance);
        log.debug("Service registered: {} by plugin: {}",
                serviceClass.getSimpleName(), plugin != null ? plugin.getName() : pluginId);
    }

    @Override
    public <T> void unregisterService(Class<T> service) {
        if (service == null) {
            return;
        }

        localServices.remove(service);;
        log.debug("Service unregistered: {} for plugin: {}",
                service.getSimpleName(), plugin != null ? plugin.getName() : pluginId);
    }

    @Override
    public void registerService(Object service) {
        if (service == null) {
            log.warn("Attempted to register null service for plugin: {}",
                    plugin != null ? plugin.getName() : pluginId);
            return;
        }

        Class<?> serviceClass = service.getClass();
        localServices.put(serviceClass, service);
        log.debug("Service registered: {} for plugin: {}",
                serviceClass.getSimpleName(), plugin != null ? plugin.getName() : pluginId);
    }

    @Override
    public PluginManager getPluginManager() {
        return pluginManager;
    }

    @Override
    public String getPluginDataPath() {
        return pluginDataDirectory != null ? pluginDataDirectory.getAbsolutePath() : "";
    }

    @Override
    public void logInfo(String message) {
        log.debug("[Plugin {}] {}", plugin != null ? plugin.getName() : pluginId, message);
    }

    @Override
    public void logWarning(String message) {
        log.warn("[Plugin {}] {}", plugin != null ? plugin.getName() : pluginId, message);
    }

    @Override
    public void logError(String message, Throwable throwable) {
        log.error("[Plugin {}] {}", plugin != null ? plugin.getName() : pluginId, message, throwable);
    }

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

    @Override
    public void logDebug(String message) {
        log.debug("[Plugin {}] {}", plugin != null ? plugin.getName() : pluginId, message);
    }

    @Override
    public ComponentRegistry getComponentRegistry() {
        return componentRegistry;
    }
}