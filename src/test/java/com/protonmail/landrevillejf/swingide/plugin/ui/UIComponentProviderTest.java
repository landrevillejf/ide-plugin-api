package com.protonmail.landrevillejf.swingide.plugin.ui;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UIComponentProvider Tests")
class UIComponentProviderTest {

    @Test
    @DisplayName("Should allow custom component provider implementation")
    void testCustomProvider() {
        UIComponentProvider provider = new TestProvider();

        List<UIComponent> components = provider.provideComponents();

        assertNotNull(components);
        assertEquals(2, components.size());
    }

    @Test
    @DisplayName("Should support empty provider")
    void testEmptyProvider() {
        UIComponentProvider provider = new UIComponentProvider() {
            @Override
            public List<UIComponent> provideComponents() {
                return new ArrayList<>();
            }
        };

        List<UIComponent> components = provider.provideComponents();

        assertTrue(components.isEmpty());
    }

    @Test
    @DisplayName("Should call lifecycle callbacks")
    void testLifecycleCallbacks() {
        TestProvider provider = new TestProvider();

        provider.onComponentAdded("test-component");
        assertTrue(provider.wasComponentAdded);

        provider.onComponentRemoved("test-component");
        assertTrue(provider.wasComponentRemoved);
    }

    @Test
    @DisplayName("Should report lifecycle management")
    void testLifecycleManagement() {
        UIComponentProvider provider = new UIComponentProvider() {
            @Override
            public List<UIComponent> provideComponents() {
                return new ArrayList<>();
            }

            @Override
            public boolean managesComponentLifecycle() {
                return true;
            }
        };

        assertTrue(provider.managesComponentLifecycle());
    }

    @Test
    @DisplayName("Default lifecycle management should be false")
    void testDefaultLifecycleManagement() {
        UIComponentProvider provider = new UIComponentProvider() {
            @Override
            public List<UIComponent> provideComponents() {
                return new ArrayList<>();
            }
        };

        assertFalse(provider.managesComponentLifecycle());
    }

    // Test implementation
    private static class TestProvider implements UIComponentProvider {
        public boolean wasComponentAdded = false;
        public boolean wasComponentRemoved = false;

        @Override
        public List<UIComponent> provideComponents() {
            List<UIComponent> components = new ArrayList<>();

            components.add(new UIComponent(
                    "test1",
                    UIComponent.ComponentType.IDE_TAB,
                    "Test 1",
                    new JPanel(),
                    (String) null,  // iconPath
                    100,            // order
                    true            // removable
            ));

            components.add(new UIComponent(
                    "test2",
                    UIComponent.ComponentType.BOTTOM_PANEL,
                    "Test 2",
                    new JPanel(),
                    (String) null,  // iconPath
                    100,            // order
                    true            // removable
            ));

            return components;
        }

        @Override
        public void onComponentAdded(String componentId) {
            wasComponentAdded = true;
        }

        @Override
        public void onComponentRemoved(String componentId) {
            wasComponentRemoved = true;
        }
    }
}