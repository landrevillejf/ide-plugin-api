package com.protonmail.landrevillejf.ide.plugin.events;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileSavedEventTest {

    @TempDir
    Path tempDir;

    @Test
    void testConstructorAndGetters() {
        String source = "test-plugin";
        String filePath = "/path/to/document.txt";

        FileSavedEvent event = new FileSavedEvent(source, filePath);

        assertEquals(source, event.getSource());
        assertEquals(filePath, event.getFilePath());
        assertNotNull(event.getFile());
        assertEquals("txt", event.getFileType());
        // File might not exist, so size could be 0
        assertEquals(event.getFile().length(), event.getFileSize());
    }

    @Test
    void testGetFilePath() {
        String filePath = "/home/user/file.java";
        FileSavedEvent event = new FileSavedEvent("plugin", filePath);

        assertEquals(filePath, event.getFilePath());
    }

    @Test
    void testGetFile() {
        String filePath = "/tmp/testfile.txt";
        FileSavedEvent event = new FileSavedEvent("plugin", filePath);

        File file = event.getFile();
        assertNotNull(file);
        assertEquals(filePath, file.getPath());
    }

    @Test
    void testGetFileType_WithExtension() {
        // Test various file extensions
        FileSavedEvent event1 = new FileSavedEvent("plugin", "document.txt");
        assertEquals("txt", event1.getFileType());

        FileSavedEvent event2 = new FileSavedEvent("plugin", "code.java");
        assertEquals("java", event2.getFileType());

        FileSavedEvent event3 = new FileSavedEvent("plugin", "script.py");
        assertEquals("py", event3.getFileType());

        FileSavedEvent event4 = new FileSavedEvent("plugin", "archive.tar.gz");
        assertEquals("gz", event4.getFileType());

        FileSavedEvent event5 = new FileSavedEvent("plugin", "file.CsS");
        assertEquals("CsS", event5.getFileType()); // Case preservation
    }

    @Test
    void testGetFileType_NoExtension() {
        // Test files without extension
        FileSavedEvent event1 = new FileSavedEvent("plugin", "README");
        assertEquals("", event1.getFileType());

        FileSavedEvent event2 = new FileSavedEvent("plugin", "file.");
        assertEquals("", event2.getFileType()); // Dot at end, no extension

        FileSavedEvent event3 = new FileSavedEvent("plugin", ".hidden");
        assertEquals("hidden", event3.getFileType()); // Hidden file, no extension
    }

    @Test
    void testGetFileType_WithPath() {
        // Test with full paths
        FileSavedEvent event1 = new FileSavedEvent("plugin", "/home/user/document.pdf");
        assertEquals("pdf", event1.getFileType());

        FileSavedEvent event2 = new FileSavedEvent("plugin", "C:\\Projects\\file.docx");
        assertEquals("docx", event2.getFileType());

        FileSavedEvent event3 = new FileSavedEvent("plugin", "../relative/path/image.png");
        assertEquals("png", event3.getFileType());
    }

    @Test
    void testGetFileSize_WithExistingFile(@TempDir Path tempDir) throws IOException {
        // Create a temporary file with known content
        Path testFile = tempDir.resolve("test.txt");
        String content = "Hello, World!";
        Files.writeString(testFile, content);

        long expectedSize = content.getBytes().length;

        FileSavedEvent event = new FileSavedEvent("plugin", testFile.toString());

        assertEquals(expectedSize, event.getFileSize());
        assertEquals(testFile.toFile().length(), event.getFileSize());
    }

    @Test
    void testGetFileSize_WithEmptyFile(@TempDir Path tempDir) throws IOException {
        // Create an empty file
        Path emptyFile = tempDir.resolve("empty.txt");
        Files.createFile(emptyFile);

        FileSavedEvent event = new FileSavedEvent("plugin", emptyFile.toString());

        assertEquals(0L, event.getFileSize());
    }

    @Test
    void testGetFileSize_WithNonExistentFile() {
        String nonExistentPath = "/path/to/nonexistent/file_" + System.currentTimeMillis() + ".txt";
        FileSavedEvent event = new FileSavedEvent("plugin", nonExistentPath);

        // For a non-existent file, length() returns 0
        assertEquals(0L, event.getFileSize());
    }

    @Test
    void testGetFileSize_WithLargeFile(@TempDir Path tempDir) throws IOException {
        // Create a file with 1MB of data
        Path largeFile = tempDir.resolve("large.bin");
        byte[] data = new byte[1024 * 1024]; // 1MB
        // Fill with some pattern
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i % 256);
        }
        Files.write(largeFile, data);

        FileSavedEvent event = new FileSavedEvent("plugin", largeFile.toString());

        assertEquals(1024L * 1024L, event.getFileSize());
    }

    @Test
    void testGetFile_ReturnsSameInstance() {
        String filePath = "/tmp/samefile.txt";
        FileSavedEvent event = new FileSavedEvent("plugin", filePath);

        File file1 = event.getFile();
        File file2 = event.getFile();

        // Should return the same File instance (not a new one each time)
        assertSame(file1, file2);
    }

    @Test
    void testConstructorWithDifferentPathFormats() {
        // Unix path
        FileSavedEvent unixEvent = new FileSavedEvent("plugin", "/home/user/file.txt");
        assertEquals("/home/user/file.txt", unixEvent.getFilePath());
        assertEquals("txt", unixEvent.getFileType());

        // Windows path
        FileSavedEvent windowsEvent = new FileSavedEvent("plugin", "C:\\Users\\user\\file.java");
        assertEquals("C:\\Users\\user\\file.java", windowsEvent.getFilePath());
        assertEquals("java", windowsEvent.getFileType());

        // Relative path
        FileSavedEvent relativeEvent = new FileSavedEvent("plugin", "src/main/resources/config.yml");
        assertEquals("src/main/resources/config.yml", relativeEvent.getFilePath());
        assertEquals("yml", relativeEvent.getFileType());
    }

    @Test
    void testAllGettersTogether(@TempDir Path tempDir) throws IOException {
        // Create a test file
        Path testFile = tempDir.resolve("test.json");
        String jsonContent = "{\"key\": \"value\"}";
        Files.writeString(testFile, jsonContent);
        String filePath = testFile.toString();

        String source = "test-plugin";
        FileSavedEvent event = new FileSavedEvent(source, filePath);

        assertAll("All getters should return correct values",
                () -> assertEquals(source, event.getSource()),
                () -> assertEquals(filePath, event.getFilePath()),
                () -> assertNotNull(event.getFile()),
                () -> assertEquals("json", event.getFileType()),
                () -> assertEquals(jsonContent.getBytes().length, event.getFileSize())
        );
    }

    @Test
    void testInheritanceFromBaseEvent() {
        String source = "test-plugin";
        String filePath = "/test/file.txt";

        FileSavedEvent event = new FileSavedEvent(source, filePath);

        // Verify BaseEvent functionality
        assertEquals(source, event.getSource());
        assertNotNull(event.getTimestamp());
        assertTrue(event.getTimestamp() instanceof java.time.LocalDateTime);
    }

    @Test
    void testGetFileTypeEdgeCases() {
        // Very long extension
        String longExt = "a".repeat(100);
        FileSavedEvent event1 = new FileSavedEvent("plugin", "file." + longExt);
        assertEquals(longExt, event1.getFileType());

        // Multiple dots in filename
        FileSavedEvent event2 = new FileSavedEvent("plugin", "file.name.with.dots.txt");
        assertEquals("txt", event2.getFileType());

        // Extension with numbers
        FileSavedEvent event3 = new FileSavedEvent("plugin", "archive.7z");
        assertEquals("7z", event3.getFileType());

        // Extension with special characters
        FileSavedEvent event4 = new FileSavedEvent("plugin", "file.tar.bz2");
        assertEquals("bz2", event4.getFileType());
    }
}