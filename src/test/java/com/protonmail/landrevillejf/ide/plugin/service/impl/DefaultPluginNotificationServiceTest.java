package com.protonmail.landrevillejf.ide.plugin.service.impl;

import com.protonmail.landrevillejf.ide.plugin.service.PluginNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

class DefaultPluginNotificationServiceTest {

    private DefaultPluginNotificationService notificationService;
    private static final String TEST_PLUGIN = "test-plugin";
    private static final String TEST_PLUGIN_2 = "test-plugin-2";

    @BeforeEach
    void setUp() {
        notificationService = new DefaultPluginNotificationService();
    }

    @Test
    void testNotify() {
        notificationService.notify(TEST_PLUGIN, "Test Title", "Test Message");

        List<PluginNotificationService.Notification> recent =
                notificationService.getRecentNotifications(TEST_PLUGIN, 10);

        assertFalse(recent.isEmpty());
        assertEquals("Test Title", recent.get(0).getTitle());
        assertEquals("Test Message", recent.get(0).getMessage());
        assertEquals(PluginNotificationService.NotificationType.INFO, recent.get(0).getType());
    }

    @Test
    void testNotify1() {
        String notificationId = notificationService.notify(
                TEST_PLUGIN,
                PluginNotificationService.NotificationType.ERROR,
                PluginNotificationService.Priority.HIGH,
                "Error Title",
                "Error Message"
        );

        assertNotNull(notificationId);
        assertTrue(notificationId.contains(TEST_PLUGIN));

        List<PluginNotificationService.Notification> active =
                notificationService.getActiveNotifications(TEST_PLUGIN);
        assertFalse(active.isEmpty());
        assertEquals("Error Title", active.get(0).getTitle());
        assertEquals(PluginNotificationService.NotificationType.ERROR, active.get(0).getType());
    }

    @Test
    void notifyWithMetadata() {
        Map<String, Object> metadata = Map.of(
                "source", "test",
                "lineNumber", 42,
                "file", "Test.java"
        );

        String notificationId = notificationService.notifyWithMetadata(
                TEST_PLUGIN,
                PluginNotificationService.NotificationType.WARNING,
                PluginNotificationService.Priority.NORMAL,
                "Warning Title",
                "Warning Message",
                metadata
        );

        assertNotNull(notificationId);

        List<PluginNotificationService.Notification> notifications =
                notificationService.getRecentNotifications(TEST_PLUGIN, 10);

        assertFalse(notifications.isEmpty());
        PluginNotificationService.Notification notification = notifications.get(0);
        assertEquals(metadata.get("source"), notification.getMetadata().get("source"));
        assertEquals(metadata.get("lineNumber"), notification.getMetadata().get("lineNumber"));
        assertEquals(metadata.get("file"), notification.getMetadata().get("file"));
    }

    @Test
    void notifyWithActions() throws InterruptedException {
        AtomicBoolean actionExecuted = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);

        List<PluginNotificationService.NotificationAction> actions = List.of(
                new DefaultPluginNotificationService.NotificationActionImpl(
                        "action1", "Click Me", () -> {
                    actionExecuted.set(true);
                    latch.countDown();
                }
                )
        );

        String notificationId = notificationService.notifyWithActions(
                TEST_PLUGIN,
                PluginNotificationService.NotificationType.INFO,
                PluginNotificationService.Priority.NORMAL,
                "Action Title",
                "Action Message",
                actions
        );

        assertNotNull(notificationId);

        // Find the notification and simulate action execution
        List<PluginNotificationService.Notification> notifications =
                notificationService.getRecentNotifications(TEST_PLUGIN, 10);
        assertFalse(notifications.isEmpty());

        PluginNotificationService.Notification notification = notifications.get(0);
        assertFalse(notification.getActions().isEmpty());
        assertEquals("action1", notification.getActions().get(0).getActionId());
        assertEquals("Click Me", notification.getActions().get(0).getLabel());

        // Execute the action (simulating button click)
        notification.getActions().get(0).getCallback().run();

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertTrue(actionExecuted.get());
    }

    @Test
    void dismiss() {
        String notificationId = notificationService.notify(
                TEST_PLUGIN,
                PluginNotificationService.NotificationType.INFO,
                PluginNotificationService.Priority.LOW,
                "Dismiss Me",
                "This notification will be dismissed"
        );

        assertFalse(notificationService.getActiveNotifications(TEST_PLUGIN).isEmpty());

        notificationService.dismiss(notificationId);

        assertTrue(notificationService.getActiveNotifications(TEST_PLUGIN).isEmpty());
    }

    @Test
    void getActiveNotifications() {
        notificationService.notify(TEST_PLUGIN, "Active 1", "Message 1");
        notificationService.notify(TEST_PLUGIN, "Active 2", "Message 2");
        notificationService.notify(TEST_PLUGIN_2, "Active 3", "Message 3");

        List<PluginNotificationService.Notification> active =
                notificationService.getActiveNotifications(TEST_PLUGIN);

        assertEquals(2, active.size());
    }

    @Test
    void getRecentNotifications() {
        for (int i = 1; i <= 15; i++) {
            notificationService.notify(TEST_PLUGIN, "Notification " + i, "Message " + i);
        }

        List<PluginNotificationService.Notification> recent =
                notificationService.getRecentNotifications(TEST_PLUGIN, 10);

        assertEquals(10, recent.size());
        assertEquals("Notification 15", recent.get(9).getTitle());
    }

    @Test
    void registerListener() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean listenerCalled = new AtomicBoolean(false);

        notificationService.registerListener(TEST_PLUGIN, notification -> {
            listenerCalled.set(true);
            latch.countDown();
        });

        notificationService.notify(TEST_PLUGIN, "Listener Test", "This should trigger the listener");

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertTrue(listenerCalled.get());
    }

    @Test
    void unregisterListener() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean listenerCalled = new AtomicBoolean(false);

        java.util.function.Consumer<PluginNotificationService.Notification> listener =
                notification -> {
                    listenerCalled.set(true);
                    latch.countDown();
                };

        notificationService.registerListener(TEST_PLUGIN, listener);
        notificationService.unregisterListener(TEST_PLUGIN, listener);

        notificationService.notify(TEST_PLUGIN, "Unregister Test", "This should NOT trigger the listener");

        // Wait a bit to ensure notification is processed
        Thread.sleep(500);

        assertFalse(latch.await(1, TimeUnit.SECONDS));
        assertFalse(listenerCalled.get());
    }

    @Test
    void registerGlobalListener() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        AtomicBoolean plugin1Called = new AtomicBoolean(false);
        AtomicBoolean plugin2Called = new AtomicBoolean(false);

        notificationService.registerListener("all", notification -> {
            if (notification.getPluginId().equals(TEST_PLUGIN)) {
                plugin1Called.set(true);
                latch.countDown();
            } else if (notification.getPluginId().equals(TEST_PLUGIN_2)) {
                plugin2Called.set(true);
                latch.countDown();
            }
        });

        notificationService.notify(TEST_PLUGIN, "Global Test 1", "Message 1");
        notificationService.notify(TEST_PLUGIN_2, "Global Test 2", "Message 2");

        assertTrue(latch.await(5, TimeUnit.SECONDS));
        assertTrue(plugin1Called.get());
        assertTrue(plugin2Called.get());
    }

    @Test
    void clearNotifications() {
        notificationService.notify(TEST_PLUGIN, "Clear 1", "Message 1");
        notificationService.notify(TEST_PLUGIN, "Clear 2", "Message 2");

        assertFalse(notificationService.getActiveNotifications(TEST_PLUGIN).isEmpty());

        notificationService.clearNotifications(TEST_PLUGIN);

        assertTrue(notificationService.getActiveNotifications(TEST_PLUGIN).isEmpty());
        // History should still have the notifications
        List<PluginNotificationService.Notification> recent =
                notificationService.getRecentNotifications(TEST_PLUGIN, 10);
        assertFalse(recent.isEmpty());
    }

    @Test
    void getStatistics() {
        notificationService.notify(TEST_PLUGIN, "Stat 1", "Message 1");
        notificationService.notify(TEST_PLUGIN,
                PluginNotificationService.NotificationType.ERROR,
                PluginNotificationService.Priority.HIGH,
                "Stat 2", "Message 2");
        notificationService.notify(TEST_PLUGIN,
                PluginNotificationService.NotificationType.WARNING,
                PluginNotificationService.Priority.NORMAL,
                "Stat 3", "Message 3");

        Map<String, Object> stats = notificationService.getStatistics(TEST_PLUGIN);

        assertNotNull(stats);
        assertTrue(stats.containsKey("counts"));
        assertTrue(stats.containsKey("lastNotificationTime"));
        assertTrue(stats.containsKey("totalNotifications"));

        @SuppressWarnings("unchecked")
        Map<String, Object> counts = (Map<String, Object>) stats.get("counts");
        assertTrue(counts.containsKey("INFO"));
        assertTrue(counts.containsKey("ERROR"));
        assertTrue(counts.containsKey("WARNING"));

        // Convert to Number then to long
        Number total = (Number) stats.get("totalNotifications");
        assertEquals(3L, total.longValue());
    }

    @Test
    void multiplePluginsIsolation() {
        notificationService.notify(TEST_PLUGIN, "Plugin 1", "Message 1");
        notificationService.notify(TEST_PLUGIN_2, "Plugin 2", "Message 2");

        List<PluginNotificationService.Notification> plugin1History =
                notificationService.getRecentNotifications(TEST_PLUGIN, 10);
        List<PluginNotificationService.Notification> plugin2History =
                notificationService.getRecentNotifications(TEST_PLUGIN_2, 10);

        assertEquals(1, plugin1History.size());
        assertEquals(1, plugin2History.size());
        assertEquals("Plugin 1", plugin1History.get(0).getTitle());
        assertEquals("Plugin 2", plugin2History.get(0).getTitle());
    }

    @Test
    void autoDismissLowPriority() throws InterruptedException {
        notificationService.notify(
                TEST_PLUGIN,
                PluginNotificationService.NotificationType.INFO,
                PluginNotificationService.Priority.LOW,
                "Auto Dismiss",
                "This notification will auto-dismiss quickly"
        );

        assertFalse(notificationService.getActiveNotifications(TEST_PLUGIN).isEmpty());

        // Wait for auto-dismiss (3 seconds for LOW priority + some buffer)
        Thread.sleep(3500);

        assertTrue(notificationService.getActiveNotifications(TEST_PLUGIN).isEmpty());
    }

    @Test
    void notificationWithActionsDoesNotAutoDismiss() throws InterruptedException {
        List<PluginNotificationService.NotificationAction> actions = List.of(
                new DefaultPluginNotificationService.NotificationActionImpl(
                        "action1", "OK", () -> {}
                )
        );

        notificationService.notifyWithActions(
                TEST_PLUGIN,
                PluginNotificationService.NotificationType.INFO,
                PluginNotificationService.Priority.LOW,
                "Persistent Notification",
                "This notification has actions and should not auto-dismiss",
                actions
        );

        // Wait longer than normal auto-dismiss time
        Thread.sleep(4000);

        // Should still be active
        assertFalse(notificationService.getActiveNotifications(TEST_PLUGIN).isEmpty());
    }
}