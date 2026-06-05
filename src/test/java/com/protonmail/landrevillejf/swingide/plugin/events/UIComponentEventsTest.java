package com.protonmail.landrevillejf.swingide.plugin.events;

import com.protonmail.landrevillejf.swingide.plugin.ui.UIComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UIComponentAddedEvent Tests")
class UIComponentAddedEventTest {

    private UIComponent testComponent;

    @BeforeEach
    void setUp() {
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
    @DisplayName("Should create event with all parameters")
    void testCreateEvent() {
        UIComponentAddedEvent event = new UIComponentAddedEvent(
                "test-plugin",
                testComponent,
                "test-plugin-id"
        );

        assertEquals("test-plugin", event.getSource());
        assertEquals(testComponent, event.getComponent());
        assertEquals("test-plugin-id", event.getPluginId());
        assertFalse(event.isHandled());
    }

    @Test
    @DisplayName("Should get timestamp")
    void testGetTimestamp() {
        UIComponentAddedEvent event = new UIComponentAddedEvent(
                "source", testComponent, "plugin"
        );

        LocalDateTime timestamp = event.getTimestamp();
        assertNotNull(timestamp);
    }

    @Test
    @DisplayName("Should mark event as handled")
    void testSetHandled() {
        UIComponentAddedEvent event = new UIComponentAddedEvent(
                "source", testComponent, "plugin"
        );

        assertFalse(event.isHandled());
        event.setHandled(true);
        assertTrue(event.isHandled());
    }

    @Test
    @DisplayName("Should get component")
    void testGetComponent() {
        UIComponentAddedEvent event = new UIComponentAddedEvent(
                "source", testComponent, "plugin"
        );

        assertEquals(testComponent.getComponentId(), event.getComponent().getComponentId());
    }

    @Test
    @DisplayName("Should get plugin ID")
    void testGetPluginId() {
        UIComponentAddedEvent event = new UIComponentAddedEvent(
                "source", testComponent, "my-plugin-id"
        );

        assertEquals("my-plugin-id", event.getPluginId());
    }
}

@DisplayName("UIComponentRemovedEvent Tests")
class UIComponentRemovedEventTest {

    @Test
    @DisplayName("Should create removal event with all parameters")
    void testCreateRemovalEvent() {
        UIComponentRemovedEvent event = new UIComponentRemovedEvent(
                "ide-source",
                "test-component",
                "test-plugin-id",
                "IDE_TAB"
        );

        assertEquals("ide-source", event.getSource());
        assertEquals("test-component", event.getComponentId());
        assertEquals("test-plugin-id", event.getPluginId());
        assertEquals("IDE_TAB", event.getComponentType());
    }

    @Test
    @DisplayName("Should get timestamp")
    void testGetTimestamp() {
        UIComponentRemovedEvent event = new UIComponentRemovedEvent(
                "source", "comp-id", "plugin-id", "BOTTOM_PANEL"
        );

        LocalDateTime timestamp = event.getTimestamp();
        assertNotNull(timestamp);
    }

    @Test
    @DisplayName("Should get component ID")
    void testGetComponentId() {
        UIComponentRemovedEvent event = new UIComponentRemovedEvent(
                "source", "my-component-id", "plugin", "IDE_TAB"
        );

        assertEquals("my-component-id", event.getComponentId());
    }

    @Test
    @DisplayName("Should get plugin ID")
    void testGetPluginId() {
        UIComponentRemovedEvent event = new UIComponentRemovedEvent(
                "source", "comp", "my-plugin-id", "BOTTOM_PANEL"
        );

        assertEquals("my-plugin-id", event.getPluginId());
    }

    @Test
    @DisplayName("Should get component type")
    void testGetComponentType() {
        UIComponentRemovedEvent event = new UIComponentRemovedEvent(
                "source", "comp", "plugin", "DOCKABLE_PANEL"
        );

        assertEquals("DOCKABLE_PANEL", event.getComponentType());
    }
}