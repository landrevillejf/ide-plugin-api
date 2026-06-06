package com.protonmail.landrevillejf.ide.plugin.events;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AllEventsTest {

    @Test
    void testBuildStartedEvent() {
        BuildStartedEvent event = new BuildStartedEvent("build-plugin", "/path/to/project", "MAVEN", "clean compile");
        assertEquals("build-plugin", event.getSource());
        assertEquals("/path/to/project", event.getProjectPath());
        assertEquals("MAVEN", event.getBuildType());
        assertEquals("clean compile", event.getTarget());
    }

    @Test
    void testEditorTextChangedEvent() {
        EditorTextChangedEvent event = new EditorTextChangedEvent(
                "editor-plugin", "editor1", "/path/to/file.txt", 15, 5, 10, "new content"
        );
        assertEquals("editor-plugin", event.getSource());
        assertEquals("editor1", event.getEditorId());
        assertEquals("/path/to/file.txt", event.getFilePath());
        assertEquals(15, event.getCaretPosition());
        assertEquals(5, event.getSelectionStart());
        assertEquals(10, event.getSelectionEnd());
        assertEquals("new content", event.getText());
    }

    @Test
    void testFileOpenedEvent() {
        FileOpenedEvent event = new FileOpenedEvent("file-plugin", "/path/to/file.txt");
        assertEquals("file-plugin", event.getSource());
        assertEquals("/path/to/file.txt", event.getFilePath());
    }

    @Test
    void testMenuItemClickedEvent() {
        MenuItemClickedEvent event = new MenuItemClickedEvent("menu-plugin", "file-menu", "open-item", "Open");
        assertEquals("menu-plugin", event.getSource());
        assertEquals("file-menu", event.getMenuId());
        assertEquals("open-item", event.getItemId());
        assertEquals("Open", event.getItemText());
    }

    @Test
    void testPluginLoadedEvent() {
        PluginLoadedEvent event = new PluginLoadedEvent("test-plugin", "my-plugin", "My Plugin", "1.0.0");
        assertEquals("test-plugin", event.getSource());
        assertEquals("my-plugin", event.getPluginId());
        assertEquals("My Plugin", event.getPluginName());
        assertEquals("1.0.0", event.getPluginVersion());
    }

    @Test
    void testPluginUnloadedEvent() {
        PluginUnloadedEvent event = new PluginUnloadedEvent("test-plugin", "my-plugin", "My Plugin");
        assertEquals("test-plugin", event.getSource());
        assertEquals("my-plugin", event.getPluginId());
        assertEquals("My Plugin", event.getPluginName());
    }

    @Test
    void testProjectClosedEvent() {
        ProjectClosedEvent event = new ProjectClosedEvent("project-plugin", "/path/to/project", "MyProject");
        assertEquals("project-plugin", event.getSource());
        assertEquals("/path/to/project", event.getProjectPath());
        assertEquals("MyProject", event.getProjectName());
    }

    @Test
    void testProjectCreatedEvent() {
        ProjectCreatedEvent event = new ProjectCreatedEvent("project-plugin", "/path/to/project", "MyProject", "JAVA");
        assertEquals("project-plugin", event.getSource());
        assertEquals("/path/to/project", event.getProjectPath());
        assertEquals("MyProject", event.getProjectName());
        assertEquals("JAVA", event.getProjectType());
    }

    @Test
    void testRunStartedEvent() {
        RunStartedEvent event = new RunStartedEvent("run-plugin", "/path/to/project", "Debug");
        assertEquals("run-plugin", event.getSource());
        assertEquals("/path/to/project", event.getProjectPath());
        assertEquals("Debug", event.getRunConfiguration());
    }

    @Test
    void testTabClosedEvent() {
        TabClosedEvent event = new TabClosedEvent("ui-plugin", "tab-123", "My Tab");
        assertEquals("ui-plugin", event.getSource());
        assertEquals("tab-123", event.getTabId());
        assertEquals("My Tab", event.getTabTitle());
    }

    @Test
    void testTabOpenedEvent() {
        TabOpenedEvent event = new TabOpenedEvent("ui-plugin", "tab-123", "My Tab", "/path/to/file.txt", "EDITOR");
        assertEquals("ui-plugin", event.getSource());
        assertEquals("tab-123", event.getTabId());
        assertEquals("My Tab", event.getTabTitle());
        assertEquals("/path/to/file.txt", event.getFilePath());
        assertEquals("EDITOR", event.getTabType());
    }

    @Test
    void testTabSelectedEvent() {
        TabSelectedEvent event = new TabSelectedEvent("ui-plugin", "tab-123", "My Tab");
        assertEquals("ui-plugin", event.getSource());
        assertEquals("tab-123", event.getTabId());
        assertEquals("My Tab", event.getTabTitle());
    }
}