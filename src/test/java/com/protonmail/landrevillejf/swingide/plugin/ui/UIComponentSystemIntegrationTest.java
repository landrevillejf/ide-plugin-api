package com.protonmail.landrevillejf.swingide.plugin.ui;

import com.protonmail.landrevillejf.swingide.plugin.PluginContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.swing.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DisplayName("UIComponent System Integration Tests")
class UIComponentSystemIntegrationTest {

    @Mock
    private PluginContext mockContext;

    private ComponentRegistry registry;
    private UIComponentBuilder builder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        registry = new ComponentRegistry();

        when(mockContext.getComponentRegistry()).thenReturn(registry);
        builder = new UIComponentBuilder(mockContext, "integration-test-plugin");
    }

    @Test
    @DisplayName("Full plugin workflow: add, register, unregister components")
    void testFullPluginWorkflow() {
        // Step 1: Plugin creates components via builder
        JPanel toolPanel = new JPanel();
        JPanel outputPanel = new JPanel();

        builder
                .addTab("tools", "My Tools", toolPanel)
                .addBottomPanel("output", "Console Output", outputPanel);

        // Step 2: Plugin registers components
        int registered = builder.registerAll();

        assertEquals(2, registered);
        assertEquals(2, registry.getTotalComponentCount());
        assertTrue(registry.isRegistered("tools"));
        assertTrue(registry.isRegistered("output"));

        // Step 3: Query components
        List<UIComponent> tabs = registry.getComponentsByType(UIComponent.ComponentType.IDE_TAB);
        List<UIComponent> panels = registry.getComponentsByType(UIComponent.ComponentType.BOTTOM_PANEL);

        assertEquals(1, tabs.size());
        assertEquals(1, panels.size());

        // Step 4: Plugin disables and unregisters
        int unregistered = builder.unregisterAll();

        assertEquals(2, unregistered);
        assertEquals(0, registry.getTotalComponentCount());
    }

    @Test
    @DisplayName("Multiple plugins can register components simultaneously")
    void testMultiplePluginsWithComponents() {
        // Plugin 1
        UIComponentBuilder builder1 = new UIComponentBuilder(mockContext, "plugin1");
        builder1
                .addTab("plugin1-tab", "Plugin 1", new JPanel())
                .registerAll();

        // Plugin 2
        UIComponentBuilder builder2 = new UIComponentBuilder(mockContext, "plugin2");
        builder2
                .addTab("plugin2-tab", "Plugin 2", new JPanel())
                .addBottomPanel("plugin2-panel", "Plugin 2 Panel", new JPanel())
                .registerAll();

        // Verify all components registered
        assertEquals(3, registry.getTotalComponentCount());

        List<UIComponent> tabs = registry.getComponentsByType(UIComponent.ComponentType.IDE_TAB);
        assertEquals(2, tabs.size());
    }

    @Test
    @DisplayName("Component metadata is preserved through lifecycle")
    void testComponentMetadataPreservation() {
        UIComponent component = new UIComponent(
                "metadata-test",
                UIComponent.ComponentType.IDE_TAB,
                "Test Component",
                new JPanel(),
                "icons/test.png",
                5,
                false
        );

        registry.registerComponent(component, "test-plugin");

        UIComponent retrieved = registry.getComponent("metadata-test").get();

        assertEquals("metadata-test", retrieved.getComponentId());
        assertEquals(UIComponent.ComponentType.IDE_TAB, retrieved.getType());
        assertEquals("Test Component", retrieved.getTitle());
        assertEquals("icons/test.png", retrieved.getIconPath());
        assertEquals(5, retrieved.getOrder());
        assertFalse(retrieved.isRemovable());
    }

    @Test
    @DisplayName("Components are automatically ordered by registry")
    void testAutomaticComponentOrdering() {
        UIComponent comp3 = new UIComponent(
                "comp3", UIComponent.ComponentType.IDE_TAB, "C3", new JPanel(), (String) null, 30, true
        );
        UIComponent comp1 = new UIComponent(
                "comp1", UIComponent.ComponentType.IDE_TAB, "C1", new JPanel(), (String) null, 10, true
        );
        UIComponent comp2 = new UIComponent(
                "comp2", UIComponent.ComponentType.IDE_TAB, "C2", new JPanel(), (String) null, 20, true
        );

        registry.registerComponent(comp3, "plugin");
        registry.registerComponent(comp1, "plugin");
        registry.registerComponent(comp2, "plugin");

        List<UIComponent> components = registry.getComponentsByType(UIComponent.ComponentType.IDE_TAB);

        assertEquals("comp1", components.get(0).getComponentId());
        assertEquals("comp2", components.get(1).getComponentId());
        assertEquals("comp3", components.get(2).getComponentId());
    }

    @Test
    @DisplayName("Builder fluent API allows complex workflows")
    void testFluentAPIComplexWorkflow() {
        JPanel tool1 = new JPanel();
        JPanel tool2 = new JPanel();
        JPanel output = new JPanel();
        JPanel status = new JPanel();

        builder
                .addTab("advanced-tool-1", "Tool 1", tool1)
                .addTab("advanced-tool-2", "Tool 2", tool2)
                .addBottomPanel("advanced-output", "Output", output)
                .addStatusBarComponent("advanced-status", status);

        List<UIComponent> components = builder.getComponents();

        assertEquals(4, components.size());

        int registered = builder.registerAll();
        assertEquals(4, registered);
    }

    @Test
    @DisplayName("Registry listeners are notified on registration")
    void testRegistryListenerNotification() {
        RegisteredComponent registered = new RegisteredComponent();
        registry.addListener(registered);

        UIComponent component = new UIComponent(
                "listener-test", UIComponent.ComponentType.IDE_TAB, "Test", new JPanel(), (String) null, 100, true
        );

        registry.registerComponent(component, "test-plugin");

        assertTrue(registered.wasNotified);
        assertEquals("listener-test", registered.lastComponentId);
    }

    @Test
    @DisplayName("Registry listeners are notified on unregistration")
    void testRegistryListenerUnregistrationNotification() {
        UnregisteredComponent unregistered = new UnregisteredComponent();
        registry.addListener(unregistered);

        UIComponent component = new UIComponent(
                "unregister-test", UIComponent.ComponentType.BOTTOM_PANEL, "Test", new JPanel(), (String) null, 100, true
        );

        registry.registerComponent(component, "test-plugin");
        registry.unregisterComponent("unregister-test", "test-plugin");

        assertTrue(unregistered.wasNotified);
        assertEquals("unregister-test", unregistered.lastComponentId);
    }

    @Test
    @DisplayName("All component types can be created and registered")
    void testAllComponentTypes() {
        for (UIComponent.ComponentType type : UIComponent.ComponentType.values()) {
            String compId = "comp-" + type.name();
            UIComponent component = new UIComponent(
                    compId, type, type.getDisplayName(), new JPanel(), (String) null, 100, true
            );

            registry.registerComponent(component, "test-plugin");
            assertTrue(registry.isRegistered(compId));
        }

        assertEquals(8, registry.getTotalComponentCount());
    }

    @Test
    @DisplayName("Duplicate component ID rejection prevents conflicts")
    void testDuplicateComponentPrevention() {
        UIComponent comp1 = new UIComponent(
                "duplicate", UIComponent.ComponentType.IDE_TAB, "First", new JPanel(), (String) null, 100, true
        );
        UIComponent comp2 = new UIComponent(
                "duplicate", UIComponent.ComponentType.IDE_TAB, "Second", new JPanel(), (String) null, 100, true
        );

        registry.registerComponent(comp1, "plugin1");

        assertThrows(IllegalArgumentException.class, () ->
                registry.registerComponent(comp2, "plugin2")
        );
    }

    @Test
    @DisplayName("Clear operation removes all components")
    void testClearOperation() {
        builder
                .addTab("tab1", "Tab 1", new JPanel())
                .addBottomPanel("panel1", "Panel 1", new JPanel())
                .registerAll();

        assertEquals(2, registry.getTotalComponentCount());

        registry.clear();

        assertEquals(0, registry.getTotalComponentCount());
    }

    // Helper classes for listener testing
    private static class RegisteredComponent implements ComponentRegistry.ComponentRegistryListener {
        public boolean wasNotified = false;
        public String lastComponentId = null;

        @Override
        public void onComponentRegistered(UIComponent component, String pluginId) {
            wasNotified = true;
            lastComponentId = component.getComponentId();
        }

        @Override
        public void onComponentUnregistered(UIComponent component, String pluginId) {
        }
    }

    private static class UnregisteredComponent implements ComponentRegistry.ComponentRegistryListener {
        public boolean wasNotified = false;
        public String lastComponentId = null;

        @Override
        public void onComponentRegistered(UIComponent component, String pluginId) {
        }

        @Override
        public void onComponentUnregistered(UIComponent component, String pluginId) {
            wasNotified = true;
            lastComponentId = component.getComponentId();
        }
    }
}