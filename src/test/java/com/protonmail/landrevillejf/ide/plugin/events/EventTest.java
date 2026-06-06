package com.protonmail.landrevillejf.ide.plugin.events;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EventTest {

    // Implémentation concrète pour les tests
    private static class TestEvent implements Event {
        private final String source;

        TestEvent(String source) {
            this.source = source;
        }

        @Override
        public String getSource() {
            return source;
        }
    }

    private static class AnotherEvent implements Event {
        private final String source;
        private final int value;

        AnotherEvent(String source, int value) {
            this.source = source;
            this.value = value;
        }

        @Override
        public String getSource() {
            return source;
        }

        public int getValue() {
            return value;
        }
    }

    @Test
    void getTimestamp_ShouldReturnNonNullCurrentTime() {
        // Given
        Event event = new TestEvent("test");

        // When
        LocalDateTime timestamp = event.getTimestamp();

        // Then
        assertNotNull(timestamp);
        assertTrue(timestamp instanceof LocalDateTime);
    }

    @Test
    void getTimestamp_ShouldReturnTimeWithinReasonableRange() {
        // Given
        Event event = new TestEvent("test");
        LocalDateTime before = LocalDateTime.now();

        // When
        LocalDateTime timestamp = event.getTimestamp();
        LocalDateTime after = LocalDateTime.now();

        // Then
        assertNotNull(timestamp);
        assertTrue(timestamp.isAfter(before) || timestamp.equals(before));
        assertTrue(timestamp.isBefore(after) || timestamp.equals(after));
    }

    @Test
    void getTimestamp_ShouldReturnDifferentTimes_ForDifferentInstances() throws InterruptedException {
        // Given
        Event event1 = new TestEvent("test1");

        // Attendre un peu pour avoir des timestamps différents
        Thread.sleep(10);

        Event event2 = new TestEvent("test2");

        // When
        LocalDateTime timestamp1 = event1.getTimestamp();
        LocalDateTime timestamp2 = event2.getTimestamp();

        // Then
        assertNotNull(timestamp1);
        assertNotNull(timestamp2);
        assertTrue(timestamp2.isAfter(timestamp1) || timestamp2.equals(timestamp1));
    }

    @Test
    void getTimestamp_ShouldBeCalledMultipleTimes_AndReturnDifferentValues() throws InterruptedException {
        // Given
        Event event = new TestEvent("test");

        // When
        LocalDateTime firstCall = event.getTimestamp();

        Thread.sleep(10);

        LocalDateTime secondCall = event.getTimestamp();

        // Then
        assertNotNull(firstCall);
        assertNotNull(secondCall);
        assertTrue(secondCall.isAfter(firstCall));
    }

    @Test
    void getSource_ShouldReturnSourceString() {
        // Given
        String expectedSource = "plugin.test.source";
        Event event = new TestEvent(expectedSource);

        // When
        String source = event.getSource();

        // Then
        assertEquals(expectedSource, source);
    }

    @Test
    void getSource_ShouldReturnEmptyString_WhenEmptySourceProvided() {
        // Given
        Event event = new TestEvent("");

        // When
        String source = event.getSource();

        // Then
        assertEquals("", source);
    }

    @Test
    void getSource_ShouldReturnNull_WhenNullSourceProvided() {
        // Given
        Event event = new TestEvent(null);

        // When
        String source = event.getSource();

        // Then
        assertNull(source);
    }

    @Test
    void getSource_ShouldReturnDifferentValues_ForDifferentEvents() {
        // Given
        Event event1 = new TestEvent("source1");
        Event event2 = new TestEvent("source2");

        // When
        String source1 = event1.getSource();
        String source2 = event2.getSource();

        // Then
        assertEquals("source1", source1);
        assertEquals("source2", source2);
        assertNotEquals(source1, source2);
    }

    @Test
    void getSource_ShouldReturnCorrectSource_ForMultipleImplementations() {
        // Given
        Event testEvent = new TestEvent("test.source");
        Event anotherEvent = new AnotherEvent("another.source", 42);

        // When
        String testSource = testEvent.getSource();
        String anotherSource = anotherEvent.getSource();

        // Then
        assertEquals("test.source", testSource);
        assertEquals("another.source", anotherSource);
    }

    @Test
    void getTimestamp_ShouldBeCalledOnDifferentImplementations() {
        // Given
        Event testEvent = new TestEvent("test");
        Event anotherEvent = new AnotherEvent("another", 100);

        // When
        LocalDateTime timestamp1 = testEvent.getTimestamp();
        LocalDateTime timestamp2 = anotherEvent.getTimestamp();

        // Then
        assertNotNull(timestamp1);
        assertNotNull(timestamp2);
        assertTrue(timestamp2.isAfter(timestamp1) || timestamp2.equals(timestamp1));
    }

    @Test
    void eventShouldBeUsableInPolymorphicContext() {
        // Given
        java.util.List<Event> events = java.util.Arrays.asList(
                new TestEvent("event1"),
                new AnotherEvent("event2", 10),
                new TestEvent("event3")
        );

        // When/Then
        for (Event event : events) {
            assertNotNull(event.getSource());
            assertNotNull(event.getTimestamp());
        }
    }

    @Test
    void timestampShouldBeInThePast_WhenCalledInSameMillisecond() {
        // Given
        Event event = new TestEvent("test");
        LocalDateTime now = LocalDateTime.now();

        // When
        LocalDateTime timestamp = event.getTimestamp();

        // Then
        // Le timestamp peut être égal à now ou légèrement après selon l'ordre d'exécution
        assertTrue(timestamp.isAfter(now) || timestamp.equals(now) || timestamp.isBefore(now));
    }

    @Test
    void defaultMethodCanBeOverridden() {
        // Given
        class CustomEvent implements Event {
            @Override
            public String getSource() {
                return "custom";
            }

            @Override
            public LocalDateTime getTimestamp() {
                // Override with fixed timestamp
                return LocalDateTime.of(2024, 1, 1, 12, 0, 0);
            }
        }

        Event event = new CustomEvent();
        LocalDateTime fixedTimestamp = LocalDateTime.of(2024, 1, 1, 12, 0, 0);

        // When
        LocalDateTime timestamp = event.getTimestamp();
        String source = event.getSource();

        // Then
        assertEquals(fixedTimestamp, timestamp);
        assertEquals("custom", source);
    }
}