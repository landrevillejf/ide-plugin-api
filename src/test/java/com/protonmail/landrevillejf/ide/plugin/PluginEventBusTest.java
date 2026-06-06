package com.protonmail.landrevillejf.ide.plugin;

import com.protonmail.landrevillejf.ide.plugin.events.Event;
import com.protonmail.landrevillejf.ide.plugin.events.EventListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class PluginEventBusTest {

    private PluginEventBus eventBus;

    // Test event implementations
    private static class TestEvent implements Event {
        private final String data;
        TestEvent(String data) { this.data = data; }
        String getData() { return data; }
        @Override public String getSource() { return "test"; }
    }

    private static class AnotherEvent implements Event {
        private final int value;
        AnotherEvent(int value) { this.value = value; }
        int getValue() { return value; }
        @Override public String getSource() { return "another"; }
    }

    private static class DifferentEvent implements Event {
        @Override public String getSource() { return "different"; }
    }

    @BeforeEach
    void setUp() {
        eventBus = new PluginEventBus();
    }

    @Test
    void testSubscribe_ShouldAddListener() {
        // Given
        AtomicInteger callCount = new AtomicInteger(0);
        EventListener<TestEvent> listener = event -> callCount.incrementAndGet();

        // When
        eventBus.subscribe(TestEvent.class, listener);

        // Then
        assertTrue(eventBus.hasSubscribers(TestEvent.class));

        eventBus.publish(new TestEvent("test"));
        assertEquals(1, callCount.get());
    }

    @Test
    void testSubscribe_MultipleListeners_ForSameEvent() {
        // Given
        AtomicInteger counter1 = new AtomicInteger(0);
        AtomicInteger counter2 = new AtomicInteger(0);
        EventListener<TestEvent> listener1 = event -> counter1.incrementAndGet();
        EventListener<TestEvent> listener2 = event -> counter2.incrementAndGet();

        // When
        eventBus.subscribe(TestEvent.class, listener1);
        eventBus.subscribe(TestEvent.class, listener2);

        // Then
        assertTrue(eventBus.hasSubscribers(TestEvent.class));

        eventBus.publish(new TestEvent("test"));

        assertEquals(1, counter1.get());
        assertEquals(1, counter2.get());
    }

    @Test
    void testSubscribe_MultipleListeners_ForDifferentEvents() {
        // Given
        AtomicInteger testCount = new AtomicInteger(0);
        AtomicInteger anotherCount = new AtomicInteger(0);
        EventListener<TestEvent> testListener = event -> testCount.incrementAndGet();
        EventListener<AnotherEvent> anotherListener = event -> anotherCount.incrementAndGet();

        // When
        eventBus.subscribe(TestEvent.class, testListener);
        eventBus.subscribe(AnotherEvent.class, anotherListener);

        // Then
        assertTrue(eventBus.hasSubscribers(TestEvent.class));
        assertTrue(eventBus.hasSubscribers(AnotherEvent.class));

        eventBus.publish(new TestEvent("test"));
        eventBus.publish(new AnotherEvent(42));

        assertEquals(1, testCount.get());
        assertEquals(1, anotherCount.get());
    }

    @Test
    void testSubscribe_SameListenerMultipleTimes() {
        // Given
        AtomicInteger count = new AtomicInteger(0);
        EventListener<TestEvent> listener = event -> count.incrementAndGet();

        // When
        eventBus.subscribe(TestEvent.class, listener);
        eventBus.subscribe(TestEvent.class, listener);

        // Then
        eventBus.publish(new TestEvent("test"));
        assertEquals(2, count.get());
    }

    @Test
    void testUnsubscribe_ShouldRemoveListener() {
        // Given
        AtomicInteger count = new AtomicInteger(0);
        EventListener<TestEvent> listener = event -> count.incrementAndGet();
        eventBus.subscribe(TestEvent.class, listener);

        eventBus.publish(new TestEvent("test"));
        assertEquals(1, count.get());

        // When
        eventBus.unsubscribe(TestEvent.class, listener);

        // Then
        assertFalse(eventBus.hasSubscribers(TestEvent.class));

        eventBus.publish(new TestEvent("test"));
        assertEquals(1, count.get());
    }

    @Test
    void testUnsubscribe_RemovesOnlySpecifiedListener() {
        // Given
        AtomicInteger counter1 = new AtomicInteger(0);
        AtomicInteger counter2 = new AtomicInteger(0);
        EventListener<TestEvent> listener1 = event -> counter1.incrementAndGet();
        EventListener<TestEvent> listener2 = event -> counter2.incrementAndGet();

        eventBus.subscribe(TestEvent.class, listener1);
        eventBus.subscribe(TestEvent.class, listener2);

        // When
        eventBus.unsubscribe(TestEvent.class, listener1);

        // Then
        eventBus.publish(new TestEvent("test"));

        assertEquals(0, counter1.get());
        assertEquals(1, counter2.get());
    }

    @Test
    void testUnsubscribe_WhenNoSubscribers_ShouldDoNothing() {
        // Given
        EventListener<TestEvent> listener = event -> {};

        // When/Then - should not throw exception
        assertDoesNotThrow(() -> eventBus.unsubscribe(TestEvent.class, listener));
    }

    @Test
    void testUnsubscribe_WhenEventTypeNotFound_ShouldDoNothing() {
        // Given
        EventListener<TestEvent> listener = event -> {};
        eventBus.subscribe(DifferentEvent.class, event -> {});

        // When/Then
        assertDoesNotThrow(() -> eventBus.unsubscribe(TestEvent.class, listener));
    }

    @Test
    void testPublish_ShouldNotifyAllSubscribers() {
        // Given
        List<String> receivedData = new ArrayList<>();
        EventListener<TestEvent> listener = event -> receivedData.add(event.getData());
        eventBus.subscribe(TestEvent.class, listener);

        // When
        eventBus.publish(new TestEvent("Hello"));
        eventBus.publish(new TestEvent("World"));

        // Then
        assertEquals(2, receivedData.size());
        assertEquals("Hello", receivedData.get(0));
        assertEquals("World", receivedData.get(1));
    }

    @Test
    void testPublish_WithNoSubscribers_ShouldDoNothing() {
        // When/Then - should not throw exception
        assertDoesNotThrow(() -> eventBus.publish(new TestEvent("test")));
    }

    @Test
    void testPublish_WithDifferentEventTypes() {
        // Given
        List<String> receivedEvents = new ArrayList<>();
        EventListener<TestEvent> testListener = event -> receivedEvents.add("TestEvent");
        EventListener<AnotherEvent> anotherListener = event -> receivedEvents.add("AnotherEvent");

        eventBus.subscribe(TestEvent.class, testListener);
        eventBus.subscribe(AnotherEvent.class, anotherListener);

        // When
        eventBus.publish(new TestEvent("test"));
        eventBus.publish(new AnotherEvent(42));

        // Then
        assertEquals(2, receivedEvents.size());
        assertEquals("TestEvent", receivedEvents.get(0));
        assertEquals("AnotherEvent", receivedEvents.get(1));
    }

    @Test
    void testPublish_PreservesEventOrder() {
        // Given
        List<Integer> order = new ArrayList<>();
        EventListener<TestEvent> listener1 = event -> order.add(1);
        EventListener<TestEvent> listener2 = event -> order.add(2);

        eventBus.subscribe(TestEvent.class, listener1);
        eventBus.subscribe(TestEvent.class, listener2);

        // When
        eventBus.publish(new TestEvent("test"));

        // Then - listeners are called in the order they were added
        assertEquals(2, order.size());
        assertEquals(1, order.get(0));
        assertEquals(2, order.get(1));
    }

    @Test
    void testHasSubscribers_ReturnsTrue_WhenListenersExist() {
        // Given
        assertFalse(eventBus.hasSubscribers(TestEvent.class));

        // When
        eventBus.subscribe(TestEvent.class, event -> {});

        // Then
        assertTrue(eventBus.hasSubscribers(TestEvent.class));
    }

    @Test
    void testHasSubscribers_ReturnsFalse_WhenNoListeners() {
        // Then
        assertFalse(eventBus.hasSubscribers(TestEvent.class));
    }

    @Test
    void testHasSubscribers_ReturnsFalse_AfterUnsubscribe() {
        // Given
        EventListener<TestEvent> listener = event -> {};
        eventBus.subscribe(TestEvent.class, listener);
        assertTrue(eventBus.hasSubscribers(TestEvent.class));

        // When
        eventBus.unsubscribe(TestEvent.class, listener);

        // Then
        assertFalse(eventBus.hasSubscribers(TestEvent.class));
    }

    @Test
    void testHasSubscribers_ForDifferentEventType() {
        // Given
        eventBus.subscribe(TestEvent.class, event -> {});

        // Then
        assertTrue(eventBus.hasSubscribers(TestEvent.class));
        assertFalse(eventBus.hasSubscribers(AnotherEvent.class));
    }

    @Test
    void testClear_RemovesAllListeners() {
        // Given
        eventBus.subscribe(TestEvent.class, event -> {});
        eventBus.subscribe(AnotherEvent.class, event -> {});
        eventBus.subscribe(DifferentEvent.class, event -> {});

        assertTrue(eventBus.hasSubscribers(TestEvent.class));
        assertTrue(eventBus.hasSubscribers(AnotherEvent.class));
        assertTrue(eventBus.hasSubscribers(DifferentEvent.class));

        // When
        eventBus.clear();

        // Then
        assertFalse(eventBus.hasSubscribers(TestEvent.class));
        assertFalse(eventBus.hasSubscribers(AnotherEvent.class));
        assertFalse(eventBus.hasSubscribers(DifferentEvent.class));
    }

    @Test
    void testClear_WithEmptyEventBus() {
        // When/Then
        assertDoesNotThrow(() -> eventBus.clear());
        assertFalse(eventBus.hasSubscribers(TestEvent.class));
    }

    @Test
    void testClear_WithEventType_RemovesOnlyThatType() {
        // Given
        eventBus.subscribe(TestEvent.class, event -> {});
        eventBus.subscribe(AnotherEvent.class, event -> {});

        assertTrue(eventBus.hasSubscribers(TestEvent.class));
        assertTrue(eventBus.hasSubscribers(AnotherEvent.class));

        // When
        eventBus.clear(TestEvent.class);

        // Then
        assertFalse(eventBus.hasSubscribers(TestEvent.class));
        assertTrue(eventBus.hasSubscribers(AnotherEvent.class));
    }

    @Test
    void testClear_WithEventType_WhenNotExists() {
        // Given
        eventBus.subscribe(TestEvent.class, event -> {});

        // When
        eventBus.clear(AnotherEvent.class);

        // Then
        assertTrue(eventBus.hasSubscribers(TestEvent.class));
    }

    @Test
    void testConcurrentPublish() throws InterruptedException {
        // Given
        AtomicInteger receivedCount = new AtomicInteger(0);
        EventListener<TestEvent> listener = event -> receivedCount.incrementAndGet();
        eventBus.subscribe(TestEvent.class, listener);

        int threadCount = 10;
        int eventsPerThread = 100;
        Thread[] threads = new Thread[threadCount];
        CountDownLatch latch = new CountDownLatch(threadCount);

        // When
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < eventsPerThread; j++) {
                    eventBus.publish(new TestEvent("test"));
                }
                latch.countDown();
            });
            threads[i].start();
        }

        // Then
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertEquals(threadCount * eventsPerThread, receivedCount.get());
    }

    @Test
    void testConcurrentSubscribeAndUnsubscribe() throws InterruptedException {
        // Given
        int threadCount = 10;
        int operationsPerThread = 100;
        Thread[] threads = new Thread[threadCount];
        CountDownLatch latch = new CountDownLatch(threadCount);

        // When
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    EventListener<TestEvent> listener = event -> {};
                    eventBus.subscribe(TestEvent.class, listener);
                    eventBus.unsubscribe(TestEvent.class, listener);
                }
                latch.countDown();
            });
            threads[i].start();
        }

        // Then
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertFalse(eventBus.hasSubscribers(TestEvent.class));
    }

    @Test
    void testSubscribeAndUnsubscribe_WithSameListenerMultipleTimes() {
        // Given
        AtomicInteger count = new AtomicInteger(0);
        EventListener<TestEvent> listener = event -> count.incrementAndGet();

        // When - subscribe twice
        eventBus.subscribe(TestEvent.class, listener);
        eventBus.subscribe(TestEvent.class, listener);

        // Then - should be called twice
        eventBus.publish(new TestEvent("test"));
        assertEquals(2, count.get());

        // When - unsubscribe once
        eventBus.unsubscribe(TestEvent.class, listener);

        // Then - should be called once
        eventBus.publish(new TestEvent("test"));
        assertEquals(3, count.get());

        // When - unsubscribe again
        eventBus.unsubscribe(TestEvent.class, listener);

        // Then - should not be called
        eventBus.publish(new TestEvent("test"));
        assertEquals(3, count.get());
    }
}