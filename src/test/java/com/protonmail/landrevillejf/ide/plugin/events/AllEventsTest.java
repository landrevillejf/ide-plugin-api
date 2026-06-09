package com.protonmail.landrevillejf.ide.plugin.events;

import com.protonmail.landrevillejf.ide.plugin.PluginStatus;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class AllEventsTest {

    // ==================== BASE EVENT TESTS ====================

    @Test
    void testBaseEvent() {
        BaseEvent event = new BaseEvent("test-source") {};
        assertEquals("test-source", event.getSource());
        assertNotNull(event.getTimestamp());
        assertTrue(event.getTimestamp() instanceof LocalDateTime);
    }

    // ==================== APPLICATION EVENTS ====================

    @Test
    void testApplicationClosingEvent() {
        ApplicationClosingEvent event = new ApplicationClosingEvent("app");
        assertEquals("app", event.getSource());
        assertNotNull(event.getTimestamp());
    }

    @Test
    void testApplicationStartedEvent() {
        ApplicationStartedEvent event = new ApplicationStartedEvent("app", "1.0.0", 1234L);
        assertEquals("app", event.getSource());
        assertEquals("1.0.0", event.getVersion());
        assertEquals(1234L, event.getStartupTime());
    }

    @Test
    void testApplicationThemeChangedEvent() {
        ApplicationThemeChangedEvent event = new ApplicationThemeChangedEvent("app", "Dark", true);
        assertEquals("app", event.getSource());
        assertEquals("Dark", event.getThemeName());
        assertTrue(event.isDarkMode());
    }

    // ==================== BUILD EVENTS ====================

    @Test
    void testBuildStartedEvent() {
        BuildStartedEvent event = new BuildStartedEvent("builder", "/project", "MAVEN", "clean compile");
        assertEquals("builder", event.getSource());
        assertEquals("/project", event.getProjectPath());
        assertEquals("MAVEN", event.getBuildType());
        assertEquals("clean compile", event.getTarget());
    }

    @Test
    void testBuildFinishedEvent_Success() {
        BuildFinishedEvent event = new BuildFinishedEvent("builder", "/project", "MAVEN",
                true, "Build output", "No errors", 5000L);
        assertEquals("builder", event.getSource());
        assertEquals("/project", event.getProjectPath());
        assertEquals("MAVEN", event.getBuildType());
        assertTrue(event.isSuccess());
        assertEquals("Build output", event.getOutput());
        assertEquals("No errors", event.getErrors());
        assertEquals(5000L, event.getDuration());
    }

    @Test
    void testBuildFinishedEvent_Failure() {
        BuildFinishedEvent event = new BuildFinishedEvent("builder", "/project", "GRADLE",
                false, "Build failed", "Compilation error", 3000L);
        assertFalse(event.isSuccess());
        assertEquals("GRADLE", event.getBuildType());
    }

    // ==================== CODE COMPILED EVENT ====================

    @Test
    void testCodeCompiledEvent_SimpleConstructor() {
        CodeCompiledEvent event = new CodeCompiledEvent("compiler", "Main", "/path/Main.java");
        assertEquals("compiler", event.getSource());
        assertEquals("Main", event.getClassName());
        assertEquals("/path/Main.java", event.getFilePath());
        assertNotNull(event.getTimestamp());
    }

    @Test
    void testCodeCompiledEvent_WithSuccessAndTime() {
        CodeCompiledEvent event = new CodeCompiledEvent("compiler", "Main", "/path/Main.java", true, 100L);
        assertTrue(event.isSuccess());
        assertEquals(100L, event.getCompilationTime());
        assertEquals("SUCCESS", event.getStatus());
        assertFalse(event.hasErrors());
        assertFalse(event.hasWarnings());
    }

    @Test
    void testCodeCompiledEvent_Complex() {
        CodeCompiledEvent event = new CodeCompiledEvent();
        event.setSource("compiler");
        event.setClassName("Main");
        event.setFilePath("/path/Main.java");
        event.setLineCount(150);
        event.setErrorCount(2);
        event.setWarningCount(5);
        event.setSuccess(false);
        event.setCompilationTime(100L);
        event.setOutputDirectory("/build");
        event.setCompiler("javac");
        event.setErrorMessages(new String[]{"Error 1", "Error 2"});
        event.setWarningMessages(new String[]{"Warning 1"});

        assertEquals("compiler", event.getSource());
        assertEquals("Main", event.getClassName());
        assertEquals("/path/Main.java", event.getFilePath());
        assertEquals(150, event.getLineCount());
        assertEquals(2, event.getErrorCount());
        assertEquals(5, event.getWarningCount());
        assertFalse(event.isSuccess());
        assertEquals("FAILED", event.getStatus());
        assertTrue(event.hasErrors());
        assertTrue(event.hasWarnings());
        assertEquals(100L, event.getCompilationTime());
        assertEquals("/build", event.getOutputDirectory());
        assertEquals("javac", event.getCompiler());
        assertEquals("Main.java", event.getFileName());
        assertEquals("java", event.getFileExtension());
    }

    @Test
    void testCodeCompiledEvent_GetCompilationSpeed() {
        CodeCompiledEvent event = new CodeCompiledEvent();
        event.setLineCount(100);
        event.setCompilationTime(200L);
        double speed = event.getCompilationSpeed();
        assertEquals(500.0, speed); // 100 / 200 * 1000 = 500

        event.setCompilationTime(0);
        assertEquals(0, event.getCompilationSpeed());

        event.setLineCount(0);
        assertEquals(0, event.getCompilationSpeed());
    }

    @Test
    void testCodeCompiledEvent_GetFileName() {
        CodeCompiledEvent event = new CodeCompiledEvent();
        event.setFilePath("/path/to/Main.java");
        assertEquals("Main.java", event.getFileName());

        event.setFilePath("C:\\path\\to\\Main.java");
        assertEquals("Main.java", event.getFileName());

        event.setFilePath("Main.java");
        assertEquals("Main.java", event.getFileName());

        event.setFilePath(null);
        assertNull(event.getFileName());
    }

    // ==================== EDITOR EVENTS ====================

    @Test
    void testEditorCaretMovedEvent() {
        EditorCaretMovedEvent event = new EditorCaretMovedEvent("editor", "editor-123", "/file.txt", 10, 5);
        assertEquals("editor", event.getSource());
        assertEquals("editor-123", event.getEditorId());
        assertEquals("/file.txt", event.getFilePath());
        assertEquals(10, event.getLine());
        assertEquals(5, event.getColumn());
    }

    @Test
    void testEditorSelectionChangedEvent() {
        EditorSelectionChangedEvent event = new EditorSelectionChangedEvent("editor", "editor-123", "/file.txt",
                "selected", 10, 25);
        assertEquals("editor", event.getSource());
        assertEquals("editor-123", event.getEditorId());
        assertEquals("/file.txt", event.getFilePath());
        assertEquals("selected", event.getSelectedText());
        assertEquals(10, event.getSelectionStart());
        assertEquals(25, event.getSelectionEnd());
    }

    @Test
    void testEditorTextChangedEvent() {
        EditorTextChangedEvent event = new EditorTextChangedEvent("editor", "editor-123", "/file.txt",
                42, 10, 25, "new content");
        assertEquals("editor", event.getSource());
        assertEquals("editor-123", event.getEditorId());
        assertEquals("/file.txt", event.getFilePath());
        assertEquals(42, event.getCaretPosition());
        assertEquals(10, event.getSelectionStart());
        assertEquals(25, event.getSelectionEnd());
        assertEquals("new content", event.getText());
    }

    @Test
    void testActiveEditorChangedEvent() {
        EditorEvents.ActiveEditorChangedEvent event = new EditorEvents.ActiveEditorChangedEvent(
                new Object(), "/file.txt");
        assertNotNull(event.getEditorComponent());
        assertEquals("/file.txt", event.getFilePath());
        assertNotNull(event.getTimestamp());
    }

    @Test
    void testDocumentChangedEvent() {
        EditorEvents.DocumentChangedEvent event = new EditorEvents.DocumentChangedEvent(
                "content", "/file.txt", 42);
        assertEquals("content", event.getContent());
        assertEquals("/file.txt", event.getFilePath());
        assertEquals(42, event.getCaretPosition());
    }

    @Test
    void testBeforeFileSaveEvent() {
        EditorEvents.BeforeFileSaveEvent event = new EditorEvents.BeforeFileSaveEvent(
                "content", "/file.txt");
        assertEquals("content", event.getContent());
        assertEquals("/file.txt", event.getFilePath());
    }

    @Test
    void testEditorFileOpenedEvent() {
        EditorEvents.FileOpenedEvent event = new EditorEvents.FileOpenedEvent("/file.txt", "content");
        assertEquals("/file.txt", event.getFilePath());
        assertEquals("content", event.getContent());
    }

    // ==================== FILE EVENTS ====================

    @Test
    void testFileCreatedEvent() {
        FileCreatedEvent event = new FileCreatedEvent("file-plugin", "/path/newfile.txt");
        assertEquals("file-plugin", event.getSource());
        assertEquals("/path/newfile.txt", event.getFilePath());
        assertNotNull(event.getFile());
        assertEquals("txt", event.getFileType());
    }

    @Test
    void testFileDeletedEvent() {
        FileDeletedEvent event = new FileDeletedEvent("file-plugin", "/path/deleted.txt");
        assertEquals("file-plugin", event.getSource());
        assertEquals("/path/deleted.txt", event.getFilePath());
        assertEquals("deleted.txt", event.getFileName());
    }

    @Test
    void testFileOpenedEvent() {
        FileOpenedEvent event = new FileOpenedEvent("file-plugin", "/path/file.txt");
        assertEquals("file-plugin", event.getSource());
        assertEquals("/path/file.txt", event.getFilePath());
        assertNotNull(event.getFile());
        assertEquals("txt", event.getFileType());
    }

    @Test
    void testFileSavedEvent() {
        FileSavedEvent event = new FileSavedEvent("file-plugin", "/path/file.txt");
        assertEquals("file-plugin", event.getSource());
        assertEquals("/path/file.txt", event.getFilePath());
        assertNotNull(event.getFile());
        assertEquals("txt", event.getFileType());
        assertEquals(event.getFile().length(), event.getFileSize());
    }

    // ==================== MENU EVENTS ====================

    @Test
    void testMenuItemClickedEvent() {
        MenuItemClickedEvent event = new MenuItemClickedEvent("menu", "file-menu", "open", "Open File");
        assertEquals("menu", event.getSource());
        assertEquals("file-menu", event.getMenuId());
        assertEquals("open", event.getItemId());
        assertEquals("Open File", event.getItemText());
    }

    // ==================== PLUGIN EVENTS ====================

    @Test
    void testPluginDisabledEvent() {
        PluginDisabledEvent event = new PluginDisabledEvent("MyPlugin");
        assertEquals("MyPlugin", event.getPluginName());
    }

    @Test
    void testPluginEnabledEvent() {
        PluginEnabledEvent event = new PluginEnabledEvent("MyPlugin");
        assertEquals("MyPlugin", event.getPluginName());
    }

    @Test
    void testPluginLoadedEvent() {
        PluginLoadedEvent event = new PluginLoadedEvent("manager", "my-plugin", "My Plugin", "1.0.0");
        assertEquals("manager", event.getSource());
        assertEquals("my-plugin", event.getPluginId());
        assertEquals("My Plugin", event.getPluginName());
        assertEquals("1.0.0", event.getPluginVersion());
    }

    @Test
    void testPluginMenuAddedEvent() {
        PluginMenuAddedEvent event = new PluginMenuAddedEvent("MyPlugin", "com.test.PluginClass");
        assertEquals("MyPlugin", event.getPluginName());
        assertEquals("com.test.PluginClass", event.getPluginClass());
    }

    @Test
    void testPluginMenuRemovedEvent() {
        PluginMenuRemovedEvent event = new PluginMenuRemovedEvent("MyPlugin", "com.test.PluginClass");
        assertEquals("MyPlugin", event.getPluginName());
        assertEquals("com.test.PluginClass", event.getPluginClass());
    }

    @Test
    void testPluginStatusChangedEvent() {
        PluginStatusChangedEvent event = new PluginStatusChangedEvent("MyPlugin", PluginStatus.ENABLED);
        assertEquals("MyPlugin", event.getPluginName());
        assertEquals(PluginStatus.ENABLED, event.getStatus());
    }

    @Test
    void testPluginUnloadedEvent() {
        PluginUnloadedEvent event = new PluginUnloadedEvent("manager", "my-plugin", "My Plugin");
        assertEquals("manager", event.getSource());
        assertEquals("my-plugin", event.getPluginId());
        assertEquals("My Plugin", event.getPluginName());
    }

    // ==================== PROJECT EVENTS ====================

    @Test
    void testProjectClosedEvent() {
        ProjectClosedEvent event = new ProjectClosedEvent("project", "/path/project", "MyProject");
        assertEquals("project", event.getSource());
        assertEquals("/path/project", event.getProjectPath());
        assertEquals("MyProject", event.getProjectName());
    }

    @Test
    void testProjectCreatedEvent() {
        ProjectCreatedEvent event = new ProjectCreatedEvent("project", "/path/project", "MyProject", "JAVA");
        assertEquals("project", event.getSource());
        assertEquals("/path/project", event.getProjectPath());
        assertEquals("MyProject", event.getProjectName());
        assertEquals("JAVA", event.getProjectType());
    }

    @Test
    void testProjectOpenedEvent() {
        ProjectOpenedEvent event = new ProjectOpenedEvent("project", "/path/project", "MyProject");
        assertEquals("project", event.getSource());
        assertEquals("/path/project", event.getProjectPath());
        assertEquals("MyProject", event.getProjectName());
        assertNotNull(event.getProjectDirectory());
        assertEquals("/path/project", event.getProjectDirectory().getPath());
    }

    // ==================== RUN EVENTS ====================

    @Test
    void testRunStartedEvent() {
        RunStartedEvent event = new RunStartedEvent("runner", "/project", "Debug");
        assertEquals("runner", event.getSource());
        assertEquals("/project", event.getProjectPath());
        assertEquals("Debug", event.getRunConfiguration());
    }

    @Test
    void testRunFinishedEvent_Success() {
        RunFinishedEvent event = new RunFinishedEvent("runner", "/project", "Debug",
                true, 0, "Run output");
        assertEquals("runner", event.getSource());
        assertEquals("/project", event.getProjectPath());
        assertEquals("Debug", event.getRunConfiguration());
        assertTrue(event.isSuccess());
        assertEquals(0, event.getExitCode());
        assertEquals("Run output", event.getOutput());
    }

    @Test
    void testRunFinishedEvent_Failure() {
        RunFinishedEvent event = new RunFinishedEvent("runner", "/project", "Debug",
                false, 1, "Error output");
        assertFalse(event.isSuccess());
        assertEquals(1, event.getExitCode());
    }

    // ==================== TAB EVENTS ====================

    @Test
    void testSelectTabEvent() {
        SelectTabEvent event = new SelectTabEvent("tab-123", "my-plugin");
        assertEquals("tab-123", event.getComponentId());
        assertEquals("my-plugin", event.getPluginId());
    }

    @Test
    void testTabClosedEvent() {
        TabClosedEvent event = new TabClosedEvent("ui", "tab-123", "My Tab");
        assertEquals("ui", event.getSource());
        assertEquals("tab-123", event.getTabId());
        assertEquals("My Tab", event.getTabTitle());
    }

    @Test
    void testTabOpenedEvent() {
        TabOpenedEvent event = new TabOpenedEvent("ui", "tab-123", "My Tab", "/file.txt", "EDITOR");
        assertEquals("ui", event.getSource());
        assertEquals("tab-123", event.getTabId());
        assertEquals("My Tab", event.getTabTitle());
        assertEquals("/file.txt", event.getFilePath());
        assertEquals("EDITOR", event.getTabType());
    }

    @Test
    void testTabSelectedEvent() {
        TabSelectedEvent event = new TabSelectedEvent("ui", "tab-123", "My Tab");
        assertEquals("ui", event.getSource());
        assertEquals("tab-123", event.getTabId());
        assertEquals("My Tab", event.getTabTitle());
    }

    // ==================== UI COMPONENT EVENTS ====================

    @Test
    void testUIComponentAddedEvent() {
        // Assuming UIComponent exists - you'll need to create a mock or real instance
        // UIComponent component = new UIComponent(...);
        // UIComponentAddedEvent event = new UIComponentAddedEvent("ui", component, "my-plugin");
        // assertEquals("ui", event.getSource());
        // assertEquals(component, event.getComponent());
        // assertEquals("my-plugin", event.getPluginId());
        // assertFalse(event.isHandled());
        // event.setHandled(true);
        // assertTrue(event.isHandled());
    }

    @Test
    void testUIComponentRemovedEvent() {
        UIComponentRemovedEvent event = new UIComponentRemovedEvent("ui", "comp-123", "my-plugin", "TAB");
        assertEquals("ui", event.getSource());
        assertEquals("comp-123", event.getComponentId());
        assertEquals("my-plugin", event.getPluginId());
        assertEquals("TAB", event.getComponentType());
    }
}