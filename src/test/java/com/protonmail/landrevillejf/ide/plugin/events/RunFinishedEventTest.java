package com.protonmail.landrevillejf.ide.plugin.events;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RunFinishedEventTest {

    private static final String SOURCE = "test-runner";
    private static final String PROJECT_PATH = "/Users/test/my-project";
    private static final String RUN_CONFIGURATION = "MainClass";
    private static final String OUTPUT = "Build successful\nProcess finished with exit code 0";

    private RunFinishedEvent successEvent;
    private RunFinishedEvent failureEvent;

    @BeforeEach
    void setUp() {
        successEvent = new RunFinishedEvent(
                SOURCE,
                PROJECT_PATH,
                RUN_CONFIGURATION,
                true,
                0,
                OUTPUT
        );

        failureEvent = new RunFinishedEvent(
                SOURCE,
                PROJECT_PATH,
                RUN_CONFIGURATION,
                false,
                1,
                "Error: compilation failed"
        );
    }

    // ==================== CONSTRUCTOR AND INHERITANCE TESTS ====================

    @Test
    void constructor_ShouldSetAllFieldsCorrectly() {
        assertNotNull(successEvent);
        assertNotNull(failureEvent);
    }

    @Test
    void constructor_ShouldInheritFromBaseEvent() {
        assertTrue(successEvent instanceof BaseEvent);
        assertEquals(SOURCE, successEvent.getSource());
        assertNotNull(successEvent.getTimestamp());
    }

    @Test
    void constructor_WithNullSource_ShouldStillCreateEvent() {
        RunFinishedEvent event = new RunFinishedEvent(
                null,
                PROJECT_PATH,
                RUN_CONFIGURATION,
                true,
                0,
                OUTPUT
        );

        assertNull(event.getSource());
        assertNotNull(event.getTimestamp());
    }

    @Test
    void constructor_WithNullProjectPath_ShouldAllowNull() {
        RunFinishedEvent event = new RunFinishedEvent(
                SOURCE,
                null,
                RUN_CONFIGURATION,
                true,
                0,
                OUTPUT
        );

        assertNull(event.getProjectPath());
    }

    @Test
    void constructor_WithNullRunConfiguration_ShouldAllowNull() {
        RunFinishedEvent event = new RunFinishedEvent(
                SOURCE,
                PROJECT_PATH,
                null,
                true,
                0,
                OUTPUT
        );

        assertNull(event.getRunConfiguration());
    }

    @Test
    void constructor_WithNullOutput_ShouldAllowNull() {
        RunFinishedEvent event = new RunFinishedEvent(
                SOURCE,
                PROJECT_PATH,
                RUN_CONFIGURATION,
                true,
                0,
                null
        );

        assertNull(event.getOutput());
    }

    // ==================== GET_PROJECT_PATH TESTS ====================

    @Test
    void getProjectPath_ShouldReturnCorrectPath() {
        assertEquals(PROJECT_PATH, successEvent.getProjectPath());
    }

    @Test
    void getProjectPath_WithEmptyPath_ShouldReturnEmptyString() {
        RunFinishedEvent event = new RunFinishedEvent(
                SOURCE,
                "",
                RUN_CONFIGURATION,
                true,
                0,
                OUTPUT
        );

        assertEquals("", event.getProjectPath());
    }

    // ==================== GET_RUN_CONFIGURATION TESTS ====================

    @Test
    void getRunConfiguration_ShouldReturnCorrectConfiguration() {
        assertEquals(RUN_CONFIGURATION, successEvent.getRunConfiguration());
    }

    @Test
    void getRunConfiguration_WithEmptyConfiguration_ShouldReturnEmptyString() {
        RunFinishedEvent event = new RunFinishedEvent(
                SOURCE,
                PROJECT_PATH,
                "",
                true,
                0,
                OUTPUT
        );

        assertEquals("", event.getRunConfiguration());
    }

    // ==================== IS_SUCCESS TESTS ====================

    @Test
    void isSuccess_ShouldReturnTrue_WhenRunSucceeded() {
        assertTrue(successEvent.isSuccess());
    }

    @Test
    void isSuccess_ShouldReturnFalse_WhenRunFailed() {
        assertFalse(failureEvent.isSuccess());
    }

    // ==================== GET_EXIT_CODE TESTS ====================

    @Test
    void getExitCode_ShouldReturnZero_ForSuccessfulRun() {
        assertEquals(0, successEvent.getExitCode());
    }

    @Test
    void getExitCode_ShouldReturnNonZero_ForFailedRun() {
        assertEquals(1, failureEvent.getExitCode());
    }

    @Test
    void getExitCode_ShouldHandleNegativeExitCode() {
        RunFinishedEvent event = new RunFinishedEvent(
                SOURCE,
                PROJECT_PATH,
                RUN_CONFIGURATION,
                false,
                -1,
                "Process terminated"
        );

        assertEquals(-1, event.getExitCode());
    }

    @Test
    void getExitCode_ShouldHandleLargeExitCode() {
        RunFinishedEvent event = new RunFinishedEvent(
                SOURCE,
                PROJECT_PATH,
                RUN_CONFIGURATION,
                false,
                255,
                "Process exited with code 255"
        );

        assertEquals(255, event.getExitCode());
    }

    // ==================== GET_OUTPUT TESTS ====================

    @Test
    void getOutput_ShouldReturnCorrectOutput() {
        assertEquals(OUTPUT, successEvent.getOutput());
    }

    @Test
    void getOutput_ShouldReturnErrorMessage_ForFailedRun() {
        assertEquals("Error: compilation failed", failureEvent.getOutput());
    }

    @Test
    void getOutput_ShouldHandleMultilineOutput() {
        String multilineOutput = "Line 1\nLine 2\nLine 3\n";
        RunFinishedEvent event = new RunFinishedEvent(
                SOURCE,
                PROJECT_PATH,
                RUN_CONFIGURATION,
                true,
                0,
                multilineOutput
        );

        assertEquals(multilineOutput, event.getOutput());
        assertTrue(event.getOutput().contains("Line 1"));
        assertTrue(event.getOutput().contains("Line 2"));
        assertTrue(event.getOutput().contains("Line 3"));
    }

    @Test
    void getOutput_ShouldHandleLongOutput() {
        StringBuilder longOutput = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longOutput.append("Log line ").append(i).append("\n");
        }
        String expected = longOutput.toString();

        RunFinishedEvent event = new RunFinishedEvent(
                SOURCE,
                PROJECT_PATH,
                RUN_CONFIGURATION,
                true,
                0,
                expected
        );

        assertEquals(expected, event.getOutput());
        assertEquals(1000, event.getOutput().split("\n").length);
    }

    // ==================== INTEGRATION TESTS ====================

    @Test
    void successEvent_ShouldHaveConsistentState() {
        assertTrue(successEvent.isSuccess());
        assertEquals(0, successEvent.getExitCode());
        assertEquals(PROJECT_PATH, successEvent.getProjectPath());
        assertEquals(RUN_CONFIGURATION, successEvent.getRunConfiguration());
        assertEquals(OUTPUT, successEvent.getOutput());
        assertEquals(SOURCE, successEvent.getSource());
    }

    @Test
    void failureEvent_ShouldHaveConsistentState() {
        assertFalse(failureEvent.isSuccess());
        assertEquals(1, failureEvent.getExitCode());
        assertEquals(PROJECT_PATH, failureEvent.getProjectPath());
        assertEquals(RUN_CONFIGURATION, failureEvent.getRunConfiguration());
        assertEquals("Error: compilation failed", failureEvent.getOutput());
        assertEquals(SOURCE, failureEvent.getSource());
    }

    @Test
    void timestamp_ShouldBeSetOnCreation() {
        java.time.LocalDateTime beforeCreation = java.time.LocalDateTime.now();
        RunFinishedEvent event = new RunFinishedEvent(
                SOURCE,
                PROJECT_PATH,
                RUN_CONFIGURATION,
                true,
                0,
                OUTPUT
        );
        java.time.LocalDateTime afterCreation = java.time.LocalDateTime.now();

        assertFalse(event.getTimestamp().isBefore(beforeCreation));
        assertFalse(event.getTimestamp().isAfter(afterCreation));
    }

    @Test
    void differentEvents_ShouldHaveDifferentTimestamps() throws InterruptedException {
        RunFinishedEvent event1 = new RunFinishedEvent(
                SOURCE,
                PROJECT_PATH,
                RUN_CONFIGURATION,
                true,
                0,
                OUTPUT
        );

        Thread.sleep(10);

        RunFinishedEvent event2 = new RunFinishedEvent(
                SOURCE,
                PROJECT_PATH,
                RUN_CONFIGURATION,
                true,
                0,
                OUTPUT
        );

        assertNotEquals(event1.getTimestamp(), event2.getTimestamp());
        assertTrue(event2.getTimestamp().isAfter(event1.getTimestamp()));
    }

    // ==================== EDGE CASE TESTS ====================

    @Test
    void constructor_WithAllNullValues_ShouldNotThrowException() {
        assertDoesNotThrow(() -> new RunFinishedEvent(
                null,
                null,
                null,
                false,
                0,
                null
        ));
    }

    @Test
    void constructor_WithEmptyStrings_ShouldNotThrowException() {
        assertDoesNotThrow(() -> new RunFinishedEvent(
                "",
                "",
                "",
                false,
                0,
                ""
        ));
    }

    @Test
    void getOutput_WithEmptyString_ShouldReturnEmptyString() {
        RunFinishedEvent event = new RunFinishedEvent(
                SOURCE,
                PROJECT_PATH,
                RUN_CONFIGURATION,
                true,
                0,
                ""
        );

        assertEquals("", event.getOutput());
    }

    @Test
    void getOutput_WithWhitespaceOnly_ShouldReturnWhitespace() {
        String whitespace = "   \n\t   ";
        RunFinishedEvent event = new RunFinishedEvent(
                SOURCE,
                PROJECT_PATH,
                RUN_CONFIGURATION,
                true,
                0,
                whitespace
        );

        assertEquals(whitespace, event.getOutput());
    }

    @Test
    void getProjectPath_WithWhitespaceOnly_ShouldReturnWhitespace() {
        String whitespace = "   /path with spaces   ";
        RunFinishedEvent event = new RunFinishedEvent(
                SOURCE,
                whitespace,
                RUN_CONFIGURATION,
                true,
                0,
                OUTPUT
        );

        assertEquals(whitespace, event.getProjectPath());
    }

    // ==================== DIFFERENT RUN CONFIGURATIONS TESTS ====================

    @Test
    void getRunConfiguration_ShouldSupportDifferentTypes() {
        String[] configurations = {
                "MainClass",
                "TestClass",
                "Gradle:build",
                "Maven:compile",
                "Custom Configuration with spaces"
        };

        for (String config : configurations) {
            RunFinishedEvent event = new RunFinishedEvent(
                    SOURCE,
                    PROJECT_PATH,
                    config,
                    true,
                    0,
                    OUTPUT
            );
            assertEquals(config, event.getRunConfiguration());
        }
    }

    // ==================== DIFFERENT PROJECT PATHS TESTS ====================

    @Test
    void getProjectPath_ShouldSupportDifferentPathFormats() {
        String[] paths = {
                "/absolute/path/to/project",
                "./relative/path",
                "C:\\Windows\\Path",
                "project-name",
                "/path/with/special_chars!@#$%"
        };

        for (String path : paths) {
            RunFinishedEvent event = new RunFinishedEvent(
                    SOURCE,
                    path,
                    RUN_CONFIGURATION,
                    true,
                    0,
                    OUTPUT
            );
            assertEquals(path, event.getProjectPath());
        }
    }
}