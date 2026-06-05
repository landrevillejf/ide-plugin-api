package com.protonmail.landrevillejf.ide.plugin.ui;

import com.protonmail.landrevillejf.ide.plugin.PluginContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.swing.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@DisplayName("UIComponentBuilder Tests")
class UIComponentBuilderTest {

    @Mock
    private PluginContext mockContext;

    private ComponentRegistry registry;
    private UIComponentBuilder builder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        registry = new ComponentRegistry();

        when(mockContext.getComponentRegistry()).thenReturn(registry);
        builder = new UIComponentBuilder(mockContext, "test-plugin");
    }

    @Test
    @DisplayName("Should add IDE tab component")
    void testAddTab() {
        JPanel panel = new JPanel();
        builder.addTab("my-tab", "My Tab", panel);

        List<UIComponent> components = builder.getComponents();

        assertEquals(1, components.size());
        assertEquals("my-tab", components.get(0).getComponentId());
        assertEquals(UIComponent.ComponentType.IDE_TAB, components.get(0).getType());
        assertEquals("My Tab", components.get(0).getTitle());
    }

    @Test
    @DisplayName("Should add bottom panel component")
    void testAddBottomPanel() {
        JPanel panel = new JPanel();
        builder.addBottomPanel("my-panel", "My Panel", panel);

        List<UIComponent> components = builder.getComponents();

        assertEquals(1, components.size());
        assertEquals("my-panel", components.get(0).getComponentId());
        assertEquals(UIComponent.ComponentType.BOTTOM_PANEL, components.get(0).getType());
    }

    @Test
    @DisplayName("Should add left sidebar component")
    void testAddLeftSidebar() {
        JPanel panel = new JPanel();
        builder.addLeftSidebar("sidebar", "Sidebar", panel);

        List<UIComponent> components = builder.getComponents();

        assertEquals(1, components.size());
        assertEquals(UIComponent.ComponentType.LEFT_SIDEBAR, components.get(0).getType());
    }

    @Test
    @DisplayName("Should add right sidebar component")
    void testAddRightSidebar() {
        JPanel panel = new JPanel();
        builder.addRightSidebar("sidebar", "Sidebar", panel);

        List<UIComponent> components = builder.getComponents();

        assertEquals(1, components.size());
        assertEquals(UIComponent.ComponentType.RIGHT_SIDEBAR, components.get(0).getType());
    }

    @Test
    @DisplayName("Should add dockable panel component")
    void testAddDockablePanel() {
        JPanel panel = new JPanel();
        builder.addDockablePanel("dock", "Dockable", panel);

        List<UIComponent> components = builder.getComponents();

        assertEquals(1, components.size());
        assertEquals(UIComponent.ComponentType.DOCKABLE_PANEL, components.get(0).getType());
    }

    @Test
    @DisplayName("Should add status bar component")
    void testAddStatusBarComponent() {
        JPanel panel = new JPanel();
        builder.addStatusBarComponent("status", panel);

        List<UIComponent> components = builder.getComponents();

        assertEquals(1, components.size());
        assertEquals(UIComponent.ComponentType.STATUS_BAR_COMPONENT, components.get(0).getType());
    }

    @Test
    @DisplayName("Should support fluent API chaining")
    void testFluentChaining() {
        JPanel panel1 = new JPanel();
        JPanel panel2 = new JPanel();
        JPanel panel3 = new JPanel();

        builder
                .addTab("tab1", "Tab 1", panel1)
                .addBottomPanel("panel1", "Panel 1", panel2)
                .addStatusBarComponent("status1", panel3);

        List<UIComponent> components = builder.getComponents();

        assertEquals(3, components.size());
    }

    @Test
    @DisplayName("Should register single component")
    void testRegisterSingleComponent() {
        UIComponent component = new UIComponent(
                "test-comp",
                UIComponent.ComponentType.IDE_TAB,
                "Test",
                new JPanel(),
                (String) null,  // iconPath
                100,            // order
                true            // removable
        );

        boolean registered = builder.register(component);

        assertTrue(registered);
        assertTrue(registry.isRegistered("test-comp"));
    }

    @Test
    @DisplayName("Should register all components")
    void testRegisterAll() {
        JPanel panel1 = new JPanel();
        JPanel panel2 = new JPanel();

        builder
                .addTab("tab1", "Tab 1", panel1)
                .addBottomPanel("panel1", "Panel 1", panel2);

        int registered = builder.registerAll();

        assertEquals(2, registered);
        assertTrue(registry.isRegistered("tab1"));
        assertTrue(registry.isRegistered("panel1"));
    }

    @Test
    @DisplayName("Should unregister all components")
    void testUnregisterAll() {
        JPanel panel1 = new JPanel();
        JPanel panel2 = new JPanel();

        builder
                .addTab("tab1", "Tab 1", panel1)
                .addBottomPanel("panel1", "Panel 1", panel2)
                .registerAll();

        int unregistered = builder.unregisterAll();

        assertEquals(2, unregistered);
        assertFalse(registry.isRegistered("tab1"));
        assertFalse(registry.isRegistered("panel1"));
    }

    @Test
    @DisplayName("Should clear builder components")
    void testClear() {
        builder.addTab("tab1", "Tab 1", new JPanel());
        builder.addBottomPanel("panel1", "Panel 1", new JPanel());

        assertEquals(2, builder.getComponents().size());

        builder.clear();

        assertEquals(0, builder.getComponents().size());
    }

    @Test
    @DisplayName("Should reject null context")
    void testRejectNullContext() {
        assertThrows(IllegalArgumentException.class, () ->
                new UIComponentBuilder(null, "test-plugin")
        );
    }

    @Test
    @DisplayName("Should reject null plugin ID")
    void testRejectNullPluginId() {
        assertThrows(IllegalArgumentException.class, () ->
                new UIComponentBuilder(mockContext, null)
        );
    }

    @Test
    @DisplayName("Should handle component with icon path")
    void testComponentWithIcon() {
        JPanel panel = new JPanel();
        builder.addTab("tab", "Tab", panel, "icons/test.png");

        List<UIComponent> components = builder.getComponents();

        assertEquals("icons/test.png", components.get(0).getIconPath());
    }

    @Test
    @DisplayName("Should handle component ordering")
    void testComponentOrdering() {
        builder.addComponent("comp1", UIComponent.ComponentType.IDE_TAB, "Comp 1",
                new JPanel(), (String) null, 20, true);
        builder.addComponent("comp2", UIComponent.ComponentType.IDE_TAB, "Comp 2",
                new JPanel(), (String) null, 10, true);

        builder.registerAll();

        List<UIComponent> components = registry.getComponentsByType(UIComponent.ComponentType.IDE_TAB);

        assertEquals("comp2", components.get(0).getComponentId());
        assertEquals("comp1", components.get(1).getComponentId());
    }

    @Test
    @DisplayName("Should handle removable and fixed components")
    void testRemovableComponents() {
        builder.addComponent("removable", UIComponent.ComponentType.IDE_TAB, "Removable",
                new JPanel(), (String) null, 0, true);
        builder.addComponent("fixed", UIComponent.ComponentType.IDE_TAB, "Fixed",
                new JPanel(), (String) null, 0, false);

        List<UIComponent> components = builder.getComponents();

        assertTrue(components.get(0).isRemovable());
        assertFalse(components.get(1).isRemovable());
    }
}