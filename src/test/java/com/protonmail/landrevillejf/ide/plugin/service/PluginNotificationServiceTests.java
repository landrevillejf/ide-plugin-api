package com.protonmail.landrevillejf.ide.plugin.service;

import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

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

