package com.protonmail.landrevillejf.ide.plugin.service;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Advanced notification service for plugins with support for multiple channels and priorities.
 * <p>
 * Provides a rich notification system with typed notifications, priority levels,
 * actionable notifications, and listener support.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
public interface PluginNotificationService {

    /**
     * Notification types.
     */
    enum NotificationType {
        INFO,
        SUCCESS,
        WARNING,
        ERROR,
        DEBUG,
        CUSTOM
    }

    /**
     * Notification priority levels.
     */
    enum Priority {
        LOW(0),
        NORMAL(1),
        HIGH(2),
        CRITICAL(3);

        private final int level;

        Priority(int level) {
            this.level = level;
        }

        public int getLevel() {
            return level;
        }
    }

    /**
     * Represents a notification.
     */
    interface Notification {
        String getId();
        String getPluginId();
        NotificationType getType();
        String getTitle();
        String getMessage();
        Priority getPriority();
        long getTimestamp();
        List<NotificationAction> getActions();
        Map<String, Object> getMetadata();
    }

    /**
     * Represents an action that can be performed on a notification.
     */
    interface NotificationAction {
        String getActionId();
        String getLabel();
        Runnable getCallback();
    }

    /**
     * Sends a simple notification.
     *
     * @param pluginId the plugin identifier
     * @param title the notification title
     * @param message the notification message
     */
    void notify(String pluginId, String title, String message);

    /**
     * Sends a notification with type and priority.
     *
     * @param pluginId the plugin identifier
     * @param type the notification type
     * @param priority the notification priority
     * @param title the notification title
     * @param message the notification message
     * @return a unique notification ID
     */
    String notify(String pluginId, NotificationType type, Priority priority, String title, String message);

    /**
     * Sends a notification with custom metadata.
     *
     * @param pluginId the plugin identifier
     * @param type the notification type
     * @param priority the notification priority
     * @param title the notification title
     * @param message the notification message
     * @param metadata custom metadata
     * @return a unique notification ID
     */
    String notifyWithMetadata(String pluginId, NotificationType type, Priority priority, String title,
                             String message, Map<String, Object> metadata);

    /**
     * Sends a notification with actions.
     *
     * @param pluginId the plugin identifier
     * @param type the notification type
     * @param priority the notification priority
     * @param title the notification title
     * @param message the notification message
     * @param actions list of notification actions
     * @return a unique notification ID
     */
    String notifyWithActions(String pluginId, NotificationType type, Priority priority, String title,
                            String message, List<NotificationAction> actions);

    /**
     * Dismisses a notification.
     *
     * @param notificationId the notification identifier
     */
    void dismiss(String notificationId);

    /**
     * Gets all active notifications for a plugin.
     *
     * @param pluginId the plugin identifier
     * @return list of active notifications
     */
    List<Notification> getActiveNotifications(String pluginId);

    /**
     * Gets recent notifications for a plugin.
     *
     * @param pluginId the plugin identifier
     * @param maxCount the maximum number of notifications to retrieve
     * @return list of recent notifications
     */
    List<Notification> getRecentNotifications(String pluginId, int maxCount);

    /**
     * Registers a listener for notifications.
     *
     * @param pluginId the plugin identifier
     * @param listener the notification listener
     */
    void registerListener(String pluginId, Consumer<Notification> listener);

    /**
     * Unregisters a listener for notifications.
     *
     * @param pluginId the plugin identifier
     * @param listener the notification listener
     */
    void unregisterListener(String pluginId, Consumer<Notification> listener);

    /**
     * Clears all notifications for a plugin.
     *
     * @param pluginId the plugin identifier
     */
    void clearNotifications(String pluginId);

    /**
     * Gets notification statistics.
     *
     * @param pluginId the plugin identifier
     * @return a map containing notification statistics
     */
    Map<String, Object> getStatistics(String pluginId);
}

