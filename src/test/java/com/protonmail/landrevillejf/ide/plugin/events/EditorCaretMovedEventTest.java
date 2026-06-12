package com.protonmail.landrevillejf.ide.plugin.events;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class EditorCaretMovedEventTest {

    @Test
    void getEditorId() {
        String source = "testSource";
        String editorId = "editor123";
        String filePath = "/path/to/file.txt";
        int line = 42;
        int column = 15;

        EditorCaretMovedEvent event = new EditorCaretMovedEvent(source, editorId, filePath, line, column);

        assertEquals(editorId, event.getEditorId());
    }

    @Test
    void getFilePath() {
        String source = "testSource";
        String editorId = "editor123";
        String filePath = "/path/to/file.txt";
        int line = 42;
        int column = 15;

        EditorCaretMovedEvent event = new EditorCaretMovedEvent(source, editorId, filePath, line, column);

        assertEquals(filePath, event.getFilePath());
    }

    @Test
    void getLine() {
        String source = "testSource";
        String editorId = "editor123";
        String filePath = "/path/to/file.txt";
        int line = 42;
        int column = 15;

        EditorCaretMovedEvent event = new EditorCaretMovedEvent(source, editorId, filePath, line, column);

        assertEquals(line, event.getLine());
    }

    @Test
    void getColumn() {
        String source = "testSource";
        String editorId = "editor123";
        String filePath = "/path/to/file.txt";
        int line = 42;
        int column = 15;

        EditorCaretMovedEvent event = new EditorCaretMovedEvent(source, editorId, filePath, line, column);

        assertEquals(column, event.getColumn());
    }

    @Test
    void testConstructorAndGettersWithDifferentValues() {
        // Test with zero values
        EditorCaretMovedEvent event1 = new EditorCaretMovedEvent("source1", "editor1", "file1.java", 0, 0);
        assertEquals("editor1", event1.getEditorId());
        assertEquals("file1.java", event1.getFilePath());
        assertEquals(0, event1.getLine());
        assertEquals(0, event1.getColumn());

        // Test with negative values (if allowed)
        EditorCaretMovedEvent event2 = new EditorCaretMovedEvent("source2", "editor2", "file2.java", -1, -5);
        assertEquals(-1, event2.getLine());
        assertEquals(-5, event2.getColumn());

        // Test with large values
        EditorCaretMovedEvent event3 = new EditorCaretMovedEvent("source3", "editor3", "file3.java", Integer.MAX_VALUE, Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, event3.getLine());
        assertEquals(Integer.MAX_VALUE, event3.getColumn());
    }

    @Test
    void testNullValues() {
        // Test with null parameters (if allowed by the implementation)
        EditorCaretMovedEvent event = new EditorCaretMovedEvent(null, null, null, 10, 20);

        assertNull(event.getSource()); // Assuming getSource() exists in BaseEvent
        assertNull(event.getEditorId());
        assertNull(event.getFilePath());
        assertEquals(10, event.getLine());
        assertEquals(20, event.getColumn());
    }

    @Test
    void testEmptyStringValues() {
        EditorCaretMovedEvent event = new EditorCaretMovedEvent("", "", "", 5, 5);

        assertEquals("", event.getEditorId());
        assertEquals("", event.getFilePath());
    }

    @Test
    void testFilePathWithDifferentFormats() {
        // Unix path
        EditorCaretMovedEvent event1 = new EditorCaretMovedEvent("source", "editor", "/home/user/document.txt", 1, 1);
        assertEquals("/home/user/document.txt", event1.getFilePath());

        // Windows path
        EditorCaretMovedEvent event2 = new EditorCaretMovedEvent("source", "editor", "C:\\Projects\\file.java", 1, 1);
        assertEquals("C:\\Projects\\file.java", event2.getFilePath());

        // Relative path
        EditorCaretMovedEvent event3 = new EditorCaretMovedEvent("source", "editor", "src/main/java/App.java", 1, 1);
        assertEquals("src/main/java/App.java", event3.getFilePath());
    }

    @Test
    void testAllGettersTogether() {
        String source = "IDE";
        String editorId = "main-editor";
        String filePath = "/project/src/Main.java";
        int line = 100;
        int column = 25;

        EditorCaretMovedEvent event = new EditorCaretMovedEvent(source, editorId, filePath, line, column);

        assertAll("All getters should return correct values",
                () -> assertEquals(editorId, event.getEditorId()),
                () -> assertEquals(filePath, event.getFilePath()),
                () -> assertEquals(line, event.getLine()),
                () -> assertEquals(column, event.getColumn())
        );
    }
}