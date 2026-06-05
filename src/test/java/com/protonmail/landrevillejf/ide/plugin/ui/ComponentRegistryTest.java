package com.protonmail.landrevillejf.ide.plugin.ui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ComponentRegistry Tests")
class ComponentRegistryTest {

    private ComponentRegistry registry;
    private UIComponent testComponent;

    @BeforeEach
    void setUp() {
        registry = new ComponentRegistry();
        testComponent = new UIComponent(
                "test-component",
                UIComponent.ComponentType.IDE_TAB,
                "Test",
                new JPanel(),
                (String) null,  // iconPath
                100,            // order
                true            // removable
        );
    }

    @Test
    @DisplayName("Should register a component")
    void testRegisterComponent() {
        registry.registerComponent(testComponent, "test-plugin");

        assertTrue(registry.isRegistered("test-component"));
    }

    @Test
    @DisplayName("Should reject duplicate component IDs")
    void testRejectDuplicateId() {
        registry.registerComponent(testComponent, "test-plugin");

        assertThrows(IllegalArgumentException.class, () ->
                registry.registerComponent(testComponent, "test-plugin")
        );
    }

    @Test
    @DisplayName("Should retrieve component by ID")
    void testGetComponent() {
        registry.registerComponent(testComponent, "test-plugin");

        Optional<UIComponent> retrieved = registry.getComponent("test-component");

        assertTrue(retrieved.isPresent());
        assertEquals(testComponent.getComponentId(), retrieved.get().getComponentId());
    }

    @Test
    @DisplayName("Should return empty optional for non-existent component")
    void testGetNonExistentComponent() {
        Optional<UIComponent> retrieved = registry.getComponent("non-existent");

        assertFalse(retrieved.isPresent());
    }

    @Test
    @DisplayName("Should get components by type")
    void testGetComponentsByType() {
        registry.registerComponent(testComponent, "test-plugin");

        UIComponent component2 = new UIComponent(
                "another-tab",
                UIComponent.ComponentType.IDE_TAB,
                "Another",
                new JPanel(),
                (String) null,
                100,
                true
        );
        registry.registerComponent(component2, "test-plugin");

        List<UIComponent> tabs = registry.getComponentsByType(UIComponent.ComponentType.IDE_TAB);

        assertEquals(2, tabs.size());
    }

    @Test
    @DisplayName("Should return empty list for type with no components")
    void testGetComponentsByTypeEmpty() {
        List<UIComponent> panels = registry.getComponentsByType(UIComponent.ComponentType.BOTTOM_PANEL);

        assertTrue(panels.isEmpty());
    }

    @Test
    @DisplayName("Should unregister component")
    void testUnregisterComponent() {
        registry.registerComponent(testComponent, "test-plugin");
        assertTrue(registry.isRegistered("test-component"));

        boolean removed = registry.unregisterComponent("test-component", "test-plugin");

        assertTrue(removed);
        assertFalse(registry.isRegistered("test-component"));
    }

    @Test
    @DisplayName("Should return false when unregistering non-existent component")
    void testUnregisterNonExistentComponent() {
        boolean removed = registry.unregisterComponent("non-existent", "test-plugin");

        assertFalse(removed);
    }

    @Test
    @DisplayName("Should get all components")
    void testGetAllComponents() {
        registry.registerComponent(testComponent, "test-plugin");

        UIComponent component2 = new UIComponent(
                "panel-component",
                UIComponent.ComponentType.BOTTOM_PANEL,
                "Panel",
                new JPanel(),
                (String) null,
                100,
                true
        );
        registry.registerComponent(component2, "test-plugin");

        Collection<UIComponent> all = registry.getAllComponents();

        assertEquals(2, all.size());
    }

    @Test
    @DisplayName("Should count components by type")
    void testGetComponentCount() {
        registry.registerComponent(testComponent, "test-plugin");

        UIComponent component2 = new UIComponent(
                "another-tab",
                UIComponent.ComponentType.IDE_TAB,
                "Another",
                new JPanel(),
                (String) null,
                100,
                true
        );
        registry.registerComponent(component2, "test-plugin");

        int count = registry.getComponentCount(UIComponent.ComponentType.IDE_TAB);

        assertEquals(2, count);
    }

    @Test
    @DisplayName("Should get total component count")
    void testGetTotalComponentCount() {
        registry.registerComponent(testComponent, "test-plugin");

        UIComponent component2 = new UIComponent(
                "panel-component",
                UIComponent.ComponentType.BOTTOM_PANEL,
                "Panel",
                new JPanel(),
                (String) null,
                100,
                true
        );
        registry.registerComponent(component2, "test-plugin");

        int total = registry.getTotalComponentCount();

        assertEquals(2, total);
    }

    @Test
    @DisplayName("Should add and notify listeners")
    void testListenerNotification() {
        TestListener listener = new TestListener();

        registry.addListener(listener);
        registry.registerComponent(testComponent, "test-plugin");

        assertTrue(listener.wasNotified);
    }

    private static class TestListener implements ComponentRegistry.ComponentRegistryListener {
        public boolean wasNotified = false;

        @Override
        public void onComponentRegistered(UIComponent component, String pluginId) {
            wasNotified = true;
        }

        @Override
        public void onComponentUnregistered(UIComponent component, String pluginId) {
        }
    }

    @Test
    @DisplayName("Should sort components by order")
    void testComponentOrdering() {
        UIComponent comp1 = new UIComponent(
                "comp1", UIComponent.ComponentType.IDE_TAB, "Comp 1", new JPanel(), (String) null, 20, true
        );
        UIComponent comp2 = new UIComponent(
                "comp2", UIComponent.ComponentType.IDE_TAB, "Comp 2", new JPanel(), (String) null, 10, true
        );
        UIComponent comp3 = new UIComponent(
                "comp3", UIComponent.ComponentType.IDE_TAB, "Comp 3", new JPanel(), (String) null, 30, true
        );

        registry.registerComponent(comp1, "plugin");
        registry.registerComponent(comp2, "plugin");
        registry.registerComponent(comp3, "plugin");

        List<UIComponent> components = registry.getComponentsByType(UIComponent.ComponentType.IDE_TAB);

        assertEquals("comp2", components.get(0).getComponentId());
        assertEquals("comp1", components.get(1).getComponentId());
        assertEquals("comp3", components.get(2).getComponentId());
    }

    @Test
    @DisplayName("Should clear all components")
    void testClear() {
        registry.registerComponent(testComponent, "test-plugin");
        assertEquals(1, registry.getTotalComponentCount());

        registry.clear();

        assertEquals(0, registry.getTotalComponentCount());
    }

    @Test
    @DisplayName("Should reject null component")
    void testRejectNullComponent() {
        assertThrows(IllegalArgumentException.class, () ->
                registry.registerComponent(null, "test-plugin")
        );
    }

    @Test
    @DisplayName("Should reject null plugin ID")
    void testRejectNullPluginId() {
        assertThrows(IllegalArgumentException.class, () ->
                registry.registerComponent(testComponent, null)
        );
    }
}