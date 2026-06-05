package com.protonmail.landrevillejf.ide.plugin.ui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UIComponent Tests")
class UIComponentTest {

    private JPanel testComponent;

    @BeforeEach
    void setUp() {
        testComponent = new JPanel();
    }

    @Test
    @DisplayName("Should create component with all parameters")
    void testCreateComponentWithAllParameters() {
        UIComponent component = new UIComponent(
                "test-component",
                UIComponent.ComponentType.IDE_TAB,
                "Test Component",
                testComponent,
                "icons/test.png",
                10,
                true
        );

        assertEquals("test-component", component.getComponentId());
        assertEquals(UIComponent.ComponentType.IDE_TAB, component.getType());
        assertEquals("Test Component", component.getTitle());
        assertEquals(testComponent, component.getComponent());
        assertEquals("icons/test.png", component.getIconPath());
        assertEquals(10, component.getOrder());
        assertTrue(component.isRemovable());
    }

    @Test
    @DisplayName("Should create component with basic parameters (String iconPath)")
    void testCreateComponentWithBasicParametersStringIcon() {
        UIComponent component = new UIComponent(
                "test-component",
                UIComponent.ComponentType.BOTTOM_PANEL,
                "Test Panel",
                testComponent,
                (String) null  // iconPath explicite
        );

        assertEquals("test-component", component.getComponentId());
        assertEquals(UIComponent.ComponentType.BOTTOM_PANEL, component.getType());
        assertEquals("Test Panel", component.getTitle());
        assertEquals(testComponent, component.getComponent());
        assertNull(component.getIconPath());
        assertEquals(Integer.MAX_VALUE, component.getOrder());
        assertTrue(component.isRemovable());
    }

    @Test
    @DisplayName("Should create component with basic parameters (Icon)")
    void testCreateComponentWithBasicParametersIcon() {
        Icon testIcon = UIManager.getIcon("OptionPane.informationIcon");
        UIComponent component = new UIComponent(
                "test-component",
                UIComponent.ComponentType.BOTTOM_PANEL,
                "Test Panel",
                testComponent,
                testIcon
        );

        assertEquals("test-component", component.getComponentId());
        assertEquals(UIComponent.ComponentType.BOTTOM_PANEL, component.getType());
        assertEquals("Test Panel", component.getTitle());
        assertEquals(testComponent, component.getComponent());
        assertEquals(testIcon, component.getIcon());
        assertNull(component.getIconPath());
        assertEquals(Integer.MAX_VALUE, component.getOrder());
        assertTrue(component.isRemovable());
    }

    @Test
    @DisplayName("Should create component with icon path and order")
    void testCreateComponentWithIconPathAndOrder() {
        UIComponent component = new UIComponent(
                "test-component",
                UIComponent.ComponentType.IDE_TAB,
                "Test Component",
                testComponent,
                "icons/test.png",
                5,
                false
        );

        assertEquals("test-component", component.getComponentId());
        assertEquals(UIComponent.ComponentType.IDE_TAB, component.getType());
        assertEquals("icons/test.png", component.getIconPath());
        assertEquals(5, component.getOrder());
        assertFalse(component.isRemovable());
    }

    @Test
    @DisplayName("Should create component with icon object and order")
    void testCreateComponentWithIconAndOrder() {
        Icon testIcon = UIManager.getIcon("OptionPane.informationIcon");
        UIComponent component = new UIComponent(
                "test-component",
                UIComponent.ComponentType.IDE_TAB,
                "Test Component",
                testComponent,
                testIcon,
                5,
                false
        );

        assertEquals("test-component", component.getComponentId());
        assertEquals(UIComponent.ComponentType.IDE_TAB, component.getType());
        assertEquals(testIcon, component.getIcon());
        assertEquals(5, component.getOrder());
        assertFalse(component.isRemovable());
    }

    @Test
    @DisplayName("Should reject null component ID")
    void testRejectNullComponentId() {
        assertThrows(IllegalArgumentException.class, () ->
                new UIComponent(
                        null,
                        UIComponent.ComponentType.IDE_TAB,
                        "Title",
                        testComponent,
                        (String) null
                )
        );
    }

    @Test
    @DisplayName("Should reject empty component ID")
    void testRejectEmptyComponentId() {
        assertThrows(IllegalArgumentException.class, () ->
                new UIComponent(
                        "   ",
                        UIComponent.ComponentType.IDE_TAB,
                        "Title",
                        testComponent,
                        (String) null
                )
        );
    }

    @Test
    @DisplayName("Should reject null type")
    void testRejectNullType() {
        assertThrows(IllegalArgumentException.class, () ->
                new UIComponent(
                        "test-component",
                        null,
                        "Title",
                        testComponent,
                        (String) null
                )
        );
    }

    @Test
    @DisplayName("Should reject null component")
    void testRejectNullComponent() {
        assertThrows(IllegalArgumentException.class, () ->
                new UIComponent(
                        "test-component",
                        UIComponent.ComponentType.IDE_TAB,
                        "Title",
                        null,
                        (String) null
                )
        );
    }

    @Test
    @DisplayName("Should accept null title")
    void testAcceptNullTitle() {
        UIComponent component = new UIComponent(
                "test-component",
                UIComponent.ComponentType.IDE_TAB,
                null,
                testComponent,
                (String) null
        );

        assertEquals("", component.getTitle());
    }

    @Test
    @DisplayName("Should generate proper toString")
    void testToString() {
        UIComponent component = new UIComponent(
                "test-component",
                UIComponent.ComponentType.IDE_TAB,
                "Test",
                testComponent,
                (String) null
        );

        String str = component.toString();
        assertNotNull(str);
        assertTrue(str.contains("test-component"));
        assertTrue(str.contains("IDE_TAB"));
    }

    @Test
    @DisplayName("Component types should have display names")
    void testComponentTypeDisplayNames() {
        for (UIComponent.ComponentType type : UIComponent.ComponentType.values()) {
            assertNotNull(type.getDisplayName());
            assertFalse(type.getDisplayName().isEmpty());
            assertNotNull(type.getDescription());
            assertFalse(type.getDescription().isEmpty());
        }
    }

    @Test
    @DisplayName("Should have all 8 component types")
    void testAllComponentTypesExist() {
        UIComponent.ComponentType[] types = UIComponent.ComponentType.values();
        assertEquals(8, types.length);
    }
}