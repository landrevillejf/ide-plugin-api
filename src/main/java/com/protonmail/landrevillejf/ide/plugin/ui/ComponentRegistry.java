package com.protonmail.landrevillejf.ide.plugin.ui;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Registry for managing UI components contributed by plugins.
 *
 * <p>
 * This service maintains a registry of all UI components added by plugins,
 * allowing the IDE to query and manage them.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
public class ComponentRegistry {
    private final Map<String, ComponentEntry> componentsByID = new ConcurrentHashMap<>();
    private final Map<UIComponent.ComponentType, List<ComponentEntry>> componentsByType = new ConcurrentHashMap<>();
    private final List<ComponentRegistryListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * Internal record to store component with its plugin ID.
     */
    private static class ComponentEntry {
        final UIComponent component;
        final String pluginId;

        ComponentEntry(UIComponent component, String pluginId) {
            this.component = component;
            this.pluginId = pluginId;
        }

        UIComponent getComponent() {
            return component;
        }

        String getPluginId() {
            return pluginId;
        }
    }

    /**
     * Registers a component provided by a plugin.
     *
     * @param component the component to register
     * @param pluginId the ID of the plugin providing this component
     * @throws IllegalArgumentException if a component with the same ID already exists
     */
    public void registerComponent(UIComponent component, String pluginId) {
        if (component == null) {
            throw new IllegalArgumentException("component cannot be null");
        }
        if (pluginId == null || pluginId.trim().isEmpty()) {
            throw new IllegalArgumentException("pluginId cannot be null or empty");
        }

        String componentId = component.getComponentId();

        if (componentsByID.containsKey(componentId)) {
            throw new IllegalArgumentException(
                    "Component with ID '" + componentId + "' is already registered"
            );
        }

        ComponentEntry entry = new ComponentEntry(component, pluginId);
        componentsByID.put(componentId, entry);
        componentsByType.computeIfAbsent(component.getType(), k -> new CopyOnWriteArrayList<>())
                .add(entry);

        // Sort by order
        componentsByType.get(component.getType()).sort(
                Comparator.comparingInt(e -> e.getComponent().getOrder())
        );

        // Notify listeners
        listeners.forEach(l -> l.onComponentRegistered(component, pluginId));
    }

    /**
     * Unregisters a component.
     *
     * @param componentId the ID of the component to unregister
     * @param pluginId the ID of the plugin that provided this component
     * @return {@code true} if the component was unregistered, {@code false} if not found
     */
    public boolean unregisterComponent(String componentId, String pluginId) {
        ComponentEntry entry = componentsByID.get(componentId);

        if (entry == null) {
            return false;
        }

        // Verify that the plugin unregistering is the same that registered
        if (!entry.getPluginId().equals(pluginId)) {
            return false;
        }

        componentsByID.remove(componentId);
        // The type list always exists: registerComponent populates both maps together
        List<ComponentEntry> typeList = componentsByType.get(entry.getComponent().getType());
        typeList.remove(entry);
        if (typeList.isEmpty()) {
            componentsByType.remove(entry.getComponent().getType());
        }

        // Notify listeners
        listeners.forEach(l -> l.onComponentUnregistered(entry.getComponent(), pluginId));
        return true;
    }

    /**
     * Gets a component by its ID.
     *
     * @param componentId the component ID
     * @return the component, or empty if not found
     */
    public Optional<UIComponent> getComponent(String componentId) {
        ComponentEntry entry = componentsByID.get(componentId);
        return entry != null ? Optional.of(entry.getComponent()) : Optional.empty();
    }

    /**
     * Gets all components of a specific type.
     *
     * @param type the component type
     * @return an unmodifiable list of components of that type, sorted by order
     */
    public List<UIComponent> getComponentsByType(UIComponent.ComponentType type) {
        List<ComponentEntry> entries = componentsByType.getOrDefault(type, new ArrayList<>());
        return Collections.unmodifiableList(
                entries.stream().map(ComponentEntry::getComponent).toList()
        );
    }

    /**
     * Gets all registered components.
     *
     * @return an unmodifiable collection of all components
     */
    public Collection<UIComponent> getAllComponents() {
        return Collections.unmodifiableCollection(
                componentsByID.values().stream().map(ComponentEntry::getComponent).toList()
        );
    }

    /**
     * Checks if a component is registered.
     *
     * @param componentId the component ID
     * @return {@code true} if registered, {@code false} otherwise
     */
    public boolean isRegistered(String componentId) {
        return componentsByID.containsKey(componentId);
    }

    /**
     * Gets the count of components of a specific type.
     *
     * @param type the component type
     * @return the count
     */
    public int getComponentCount(UIComponent.ComponentType type) {
        return componentsByType.getOrDefault(type, new ArrayList<>()).size();
    }

    /**
     * Gets the total count of all registered components.
     *
     * @return the total count
     */
    public int getTotalComponentCount() {
        return componentsByID.size();
    }

    /**
     * Registers a listener for component registry events.
     *
     * @param listener the listener to add
     */
    public void addListener(ComponentRegistryListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    /**
     * Unregisters a listener.
     *
     * @param listener the listener to remove
     */
    public void removeListener(ComponentRegistryListener listener) {
        listeners.remove(listener);
    }

    /**
     * Clears all registered components and listeners.
     * This should typically only be called during shutdown.
     */
    public void clear() {
        componentsByID.clear();
        componentsByType.clear();
        listeners.clear();
    }

    /**
     * Listener interface for component registry events.
     */
    public interface ComponentRegistryListener {
        /**
         * Called when a component is registered.
         *
         * @param component the registered component
         * @param pluginId the ID of the plugin that registered it
         */
        void onComponentRegistered(UIComponent component, String pluginId);

        /**
         * Called when a component is unregistered.
         *
         * @param component the unregistered component
         * @param pluginId the ID of the plugin that provided it
         */
        void onComponentUnregistered(UIComponent component, String pluginId);
    }
}