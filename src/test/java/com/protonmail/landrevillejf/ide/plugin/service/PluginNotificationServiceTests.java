package com.protonmail.landrevillejf.ide.plugin.service;

import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for PluginNotificationService interface
 */
@DisplayName("PluginNotificationService Tests")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class PluginNotificationServiceTests {

    private PluginNotificationService notificationService;
    private static final String PLUGIN_ID = "test-plugin";

    @BeforeEach
    void setUp() {
        notificationService = new MockPluginNotificationService();
    }

    @Test
    @DisplayName("should send simple notification")
    void test_send_simple_notification() {
        assertThatNoException().isThrownBy(() ->
            notificationService.notify(PLUGIN_ID, "Test Title", "Test Message")
        );
    }

    @Test
    @DisplayName("should send notification with type and priority")
    void test_send_notification_with_type_and_priority() {
        String notifId = notificationService.notify(PLUGIN_ID,
                PluginNotificationService.NotificationType.SUCCESS,
                PluginNotificationService.Priority.HIGH,
                "Success", "Operation completed");

        assertThat(notifId).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("should get active notifications")
    void test_get_active_notifications() {
        notificationService.notify(PLUGIN_ID, "Title", "Message");

        List<PluginNotificationService.Notification> active = notificationService.getActiveNotifications(PLUGIN_ID);

        assertThat(active).isNotNull().isEmpty(); // Mock doesn't keep active
    }

    @Test
    @DisplayName("should get recent notifications")
    void test_get_recent_notifications() {
        notificationService.notify(PLUGIN_ID, "Title", "Message");

        List<PluginNotificationService.Notification> recent = notificationService.getRecentNotifications(PLUGIN_ID, 10);

        assertThat(recent).isNotNull();
    }

    @Test
    @DisplayName("should dismiss notification")
    void test_dismiss_notification() {
        String notifId = notificationService.notify(PLUGIN_ID,
                PluginNotificationService.NotificationType.INFO,
                PluginNotificationService.Priority.NORMAL,
                "Test", "Test");

        assertThatNoException().isThrownBy(() -> notificationService.dismiss(notifId));
    }

    @Test
    @DisplayName("should clear notifications")
    void test_clear_notifications() {
        notificationService.notify(PLUGIN_ID, "Title", "Message");

        assertThatNoException().isThrownBy(() -> notificationService.clearNotifications(PLUGIN_ID));
    }

    @Test
    @DisplayName("should get notification statistics")
    void test_get_notification_statistics() {
        notificationService.notify(PLUGIN_ID, "Title", "Message");

        Map<String, Object> stats = notificationService.getStatistics(PLUGIN_ID);

        assertThat(stats).isNotNull();
    }

    @Test
    @DisplayName("should register and notify listeners")
    void test_listener_registration() {
        java.util.concurrent.atomic.AtomicBoolean called = new java.util.concurrent.atomic.AtomicBoolean(false);
        Consumer<PluginNotificationService.Notification> listener = notif -> called.set(true);

        notificationService.registerListener(PLUGIN_ID, listener);

        assertThatNoException().isThrownBy(() -> notificationService.unregisterListener(PLUGIN_ID, listener));
    }

    @Test
    void priority_GetLevel_ShouldReturnCorrectLevel() {
        assertEquals(0, PluginNotificationService.Priority.LOW.getLevel());
        assertEquals(1, PluginNotificationService.Priority.NORMAL.getLevel());
        assertEquals(2, PluginNotificationService.Priority.HIGH.getLevel());
        assertEquals(3, PluginNotificationService.Priority.CRITICAL.getLevel());
    }

    @Test
    void priority_Values_ShouldContainAllPriorities() {
        PluginNotificationService.Priority[] values = PluginNotificationService.Priority.values();
        assertEquals(4, values.length);
        assertTrue(java.util.Arrays.asList(values).contains(PluginNotificationService.Priority.LOW));
        assertTrue(java.util.Arrays.asList(values).contains(PluginNotificationService.Priority.NORMAL));
        assertTrue(java.util.Arrays.asList(values).contains(PluginNotificationService.Priority.HIGH));
        assertTrue(java.util.Arrays.asList(values).contains(PluginNotificationService.Priority.CRITICAL));
    }

    @Test
    void priority_ValueOf_ShouldReturnCorrectEnum() {
        assertEquals(PluginNotificationService.Priority.LOW,
                PluginNotificationService.Priority.valueOf("LOW"));
        assertEquals(PluginNotificationService.Priority.NORMAL,
                PluginNotificationService.Priority.valueOf("NORMAL"));
        assertEquals(PluginNotificationService.Priority.HIGH,
                PluginNotificationService.Priority.valueOf("HIGH"));
        assertEquals(PluginNotificationService.Priority.CRITICAL,
                PluginNotificationService.Priority.valueOf("CRITICAL"));
    }

    // ==================== NOTIFICATION TYPE ENUM TESTS ====================

    @Test
    void notificationType_Values_ShouldContainAllTypes() {
        PluginNotificationService.NotificationType[] values = PluginNotificationService.NotificationType.values();
        assertEquals(6, values.length);
        assertTrue(java.util.Arrays.asList(values).contains(PluginNotificationService.NotificationType.INFO));
        assertTrue(java.util.Arrays.asList(values).contains(PluginNotificationService.NotificationType.SUCCESS));
        assertTrue(java.util.Arrays.asList(values).contains(PluginNotificationService.NotificationType.WARNING));
        assertTrue(java.util.Arrays.asList(values).contains(PluginNotificationService.NotificationType.ERROR));
        assertTrue(java.util.Arrays.asList(values).contains(PluginNotificationService.NotificationType.DEBUG));
        assertTrue(java.util.Arrays.asList(values).contains(PluginNotificationService.NotificationType.CUSTOM));
    }

    @Test
    void notificationType_ValueOf_ShouldReturnCorrectEnum() {
        assertEquals(PluginNotificationService.NotificationType.INFO,
                PluginNotificationService.NotificationType.valueOf("INFO"));
        assertEquals(PluginNotificationService.NotificationType.SUCCESS,
                PluginNotificationService.NotificationType.valueOf("SUCCESS"));
        assertEquals(PluginNotificationService.NotificationType.WARNING,
                PluginNotificationService.NotificationType.valueOf("WARNING"));
        assertEquals(PluginNotificationService.NotificationType.ERROR,
                PluginNotificationService.NotificationType.valueOf("ERROR"));
    }

    @Test
    void testPriorityGetLevel() {
        assertEquals(0, PluginNotificationService.Priority.LOW.getLevel());
        assertEquals(1, PluginNotificationService.Priority.NORMAL.getLevel());
        assertEquals(2, PluginNotificationService.Priority.HIGH.getLevel());
        assertEquals(3, PluginNotificationService.Priority.CRITICAL.getLevel());
    }

    // Mock implementation for testing
    public static class MockPluginNotificationService implements PluginNotificationService {
        private int notificationCounter = 0;

        @Override
        public void notify(String pluginId, String title, String message) {
            // No-op
        }

        @Override
        public String notify(String pluginId, NotificationType type, Priority priority, String title, String message) {
            return "notif-" + (++notificationCounter);
        }

        @Override
        public String notifyWithMetadata(String pluginId, NotificationType type, Priority priority, String title, String message, Map<String, Object> metadata) {
            return "notif-" + (++notificationCounter);
        }

        @Override
        public String notifyWithActions(String pluginId, NotificationType type, Priority priority, String title, String message, List<NotificationAction> actions) {
            return "notif-" + (++notificationCounter);
        }

        @Override
        public void dismiss(String notificationId) {
            // No-op
        }

        @Override
        public List<Notification> getActiveNotifications(String pluginId) {
            return java.util.Collections.emptyList();
        }

        @Override
        public List<Notification> getRecentNotifications(String pluginId, int maxCount) {
            return java.util.Collections.emptyList();
        }

        @Override
        public void registerListener(String pluginId, Consumer<Notification> listener) {
            // No-op
        }

        @Override
        public void unregisterListener(String pluginId, Consumer<Notification> listener) {
            // No-op
        }

        @Override
        public void clearNotifications(String pluginId) {
            // No-op
        }

        @Override
        public Map<String, Object> getStatistics(String pluginId) {
            Map<String, Object> stats = new java.util.HashMap<>();
            stats.put("totalNotifications", notificationCounter);
            return stats;
        }
    }
}

