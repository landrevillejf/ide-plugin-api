package com.protonmail.landrevillejf.ide.plugin.service.impl;

import com.protonmail.landrevillejf.ide.plugin.service.PluginNotificationService;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Default implementation of {@link PluginNotificationService}.
 * <p>
 * Provides a rich notification system with typed notifications, priority levels,
 * actionable notifications, listener support, and automatic UI display via Swing.
 * Includes periodic cleanup of old notifications.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 * @see PluginNotificationService
 */
@Slf4j
public class DefaultPluginNotificationService implements PluginNotificationService {

    private final Map<String, List<Notification>> activeNotifications = new ConcurrentHashMap<>();
    private final Map<String, List<Notification>> notificationHistory = new ConcurrentHashMap<>();
    private final Map<String, List<Consumer<Notification>>> listeners = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> statistics = new ConcurrentHashMap<>();
    private final AtomicLong notificationCounter = new AtomicLong(0);

    public DefaultPluginNotificationService() {
        // Cleanup old notifications every hour
        ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
        cleanupExecutor.scheduleAtFixedRate(this::cleanupOldNotifications, 1, 1, TimeUnit.HOURS);
        if (log.isInfoEnabled()) {
            log.info("DefaultPluginNotificationService initialized");
        }
    }

    @Override
    public void notify(String pluginId, String title, String message) {
        notify(pluginId, NotificationType.INFO, Priority.NORMAL, title, message);
    }

    @Override
    public String notify(String pluginId, NotificationType type, Priority priority, String title, String message) {
        return notifyWithMetadata(pluginId, type, priority, title, message, Collections.emptyMap());
    }

    @Override
    public String notifyWithMetadata(String pluginId, NotificationType type, Priority priority,
                                     String title, String message, Map<String, Object> metadata) {
        String notificationId = generateNotificationId(pluginId);
        NotificationImpl notification = new NotificationImpl(
                notificationId, pluginId, type, priority, title, message,
                System.currentTimeMillis(), Collections.emptyList(), metadata
        );

        // Store notification
        activeNotifications.computeIfAbsent(pluginId, k -> new CopyOnWriteArrayList<>()).add(notification);
        notificationHistory.computeIfAbsent(pluginId, k -> new CopyOnWriteArrayList<>()).add(notification);

        // Update statistics
        updateStatistics(pluginId, type);

        // Notify listeners
        notifyListeners(pluginId, notification);

        // Show in UI
        showInUI(notification);

        // Auto-dismiss based on priority
        scheduleAutoDismiss(notification);

        if (log.isDebugEnabled()) {
            log.debug("Notification sent: plugin={}, type={}, priority={}, title={}",
                    pluginId, type, priority, title);
        }

        return notificationId;
    }

    @Override
    public String notifyWithActions(String pluginId, NotificationType type, Priority priority,
                                    String title, String message, List<NotificationAction> actions) {
        String notificationId = generateNotificationId(pluginId);
        NotificationImpl notification = new NotificationImpl(
                notificationId, pluginId, type, priority, title, message,
                System.currentTimeMillis(), actions, Collections.emptyMap()
        );

        // Store notification
        activeNotifications.computeIfAbsent(pluginId, k -> new CopyOnWriteArrayList<>()).add(notification);
        notificationHistory.computeIfAbsent(pluginId, k -> new CopyOnWriteArrayList<>()).add(notification);

        // Update statistics
        updateStatistics(pluginId, type);

        // Notify listeners
        notifyListeners(pluginId, notification);

        // Show in UI with actions
        showInUIWithActions(notification);

        if (log.isDebugEnabled()) {
            log.debug("Notification with actions sent: plugin={}, type={}, priority={}, title={}",
                    pluginId, type, priority, title);
        }

        return notificationId;
    }

    @Override
    public void dismiss(String notificationId) {
        for (Map.Entry<String, List<Notification>> entry : activeNotifications.entrySet()) {
            Notification toRemove = null;
            for (Notification notification : entry.getValue()) {
                if (notification.getId().equals(notificationId)) {
                    toRemove = notification;
                    break;
                }
            }
            if (toRemove != null) {
                entry.getValue().remove(toRemove);
                if (log.isDebugEnabled()) {
                    log.debug("Notification dismissed: {}", notificationId);
                }
                break;
            }
        }
    }

    @Override
    public List<Notification> getActiveNotifications(String pluginId) {
        return activeNotifications.getOrDefault(pluginId, Collections.emptyList())
                .stream()
                .collect(Collectors.toList());
    }

    @Override
    public List<Notification> getRecentNotifications(String pluginId, int maxCount) {
        List<Notification> history = notificationHistory.getOrDefault(pluginId, Collections.emptyList());
        int start = Math.max(0, history.size() - maxCount);
        return history.subList(start, history.size())
                .stream()
                .collect(Collectors.toList());
    }

    @Override
    public void registerListener(String pluginId, Consumer<Notification> listener) {
        listeners.computeIfAbsent(pluginId, k -> new CopyOnWriteArrayList<>()).add(listener);
        if (log.isDebugEnabled()) {
            log.debug("Listener registered for plugin: {}", pluginId);
        }
    }

    @Override
    public void unregisterListener(String pluginId, Consumer<Notification> listener) {
        List<Consumer<Notification>> pluginListeners = listeners.get(pluginId);
        if (pluginListeners != null) {
            pluginListeners.remove(listener);
            if (log.isDebugEnabled()) {
                log.debug("Listener unregistered for plugin: {}", pluginId);
            }
        }
    }

    @Override
    public void clearNotifications(String pluginId) {
        List<Notification> active = activeNotifications.get(pluginId);
        if (active != null) {
            active.clear();
        }
        if (log.isDebugEnabled()) {
            log.debug("Notifications cleared for plugin: {}", pluginId);
        }
    }

    @Override
    public Map<String, Object> getStatistics(String pluginId) {
        return statistics.getOrDefault(pluginId, Collections.emptyMap());
    }

    private String generateNotificationId(String pluginId) {
        return pluginId + "_" + System.currentTimeMillis() + "_" + notificationCounter.incrementAndGet();
    }

    private void updateStatistics(String pluginId, NotificationType type) {
        Map<String, Object> stats = statistics.computeIfAbsent(pluginId, k -> new ConcurrentHashMap<>());
        @SuppressWarnings("unchecked")
        Map<String, AtomicLong> counts = (Map<String, AtomicLong>) stats.computeIfAbsent("counts",
                k -> new ConcurrentHashMap<>());

        AtomicLong counter = counts.computeIfAbsent(type.name(), k -> new AtomicLong());
        counter.incrementAndGet();

        stats.put("lastNotificationTime", System.currentTimeMillis());
        stats.put("totalNotifications",
                counts.values().stream().mapToLong(AtomicLong::get).sum());
    }

    private void notifyListeners(String pluginId, Notification notification) {
        List<Consumer<Notification>> pluginListeners = listeners.get(pluginId);
        if (pluginListeners != null) {
            for (Consumer<Notification> listener : pluginListeners) {
                try {
                    listener.accept(notification);
                } catch (Exception e) {
                    if (log.isErrorEnabled()) {
                        log.error("Error in notification listener for plugin {}", pluginId, e);
                    }
                }
            }
        }

        // Also notify global listeners (listeners registered for "all" plugin)
        List<Consumer<Notification>> globalListeners = listeners.get("all");
        if (globalListeners != null) {
            for (Consumer<Notification> listener : globalListeners) {
                try {
                    listener.accept(notification);
                } catch (Exception e) {
                    if (log.isErrorEnabled()) {
                        log.error("Error in global notification listener", e);
                    }
                }
            }
        }
    }

    private void showInUI(Notification notification) {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            String iconType = getIconType(notification.getType());
            int optionType = JOptionPane.INFORMATION_MESSAGE;

            switch (notification.getType()) {
                case INFO:
                    optionType = JOptionPane.INFORMATION_MESSAGE;
                    break;
                case SUCCESS:
                    optionType = JOptionPane.INFORMATION_MESSAGE;
                    break;
                case WARNING:
                    optionType = JOptionPane.WARNING_MESSAGE;
                    break;
                case ERROR:
                    optionType = JOptionPane.ERROR_MESSAGE;
                    break;
                case DEBUG:
                    optionType = JOptionPane.PLAIN_MESSAGE;
                    break;
                default:
                    optionType = JOptionPane.INFORMATION_MESSAGE;
            }

            JOptionPane.showMessageDialog(
                    null,
                    notification.getMessage(),
                    notification.getTitle(),
                    optionType
            );
        });
    }

    private void showInUIWithActions(Notification notification) {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        // For notifications with actions, show a custom dialog with buttons
        SwingUtilities.invokeLater(() -> {
            JDialog dialog = new JDialog();
            dialog.setTitle(notification.getTitle());
            dialog.setModal(false);
            dialog.setLayout(new BorderLayout());

            // Message panel
            JTextArea messageArea = new JTextArea(notification.getMessage());
            messageArea.setEditable(false);
            messageArea.setWrapStyleWord(true);
            messageArea.setLineWrap(true);
            messageArea.setOpaque(false);
            JScrollPane scrollPane = new JScrollPane(messageArea);
            scrollPane.setPreferredSize(new Dimension(400, 100));
            dialog.add(scrollPane, BorderLayout.CENTER);

            // Button panel
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            for (NotificationAction action : notification.getActions()) {
                JButton button = new JButton(action.getLabel());
                button.addActionListener(e -> {
                    try {
                        action.getCallback().run();
                    } catch (Exception ex) {
                        if (log.isErrorEnabled()) {
                            log.error("Error executing notification action: {}", action.getActionId(), ex);
                        }
                    } finally {
                        dialog.dispose();
                        dismiss(notification.getId());
                    }
                });
                buttonPanel.add(button);
            }

            // Close button
            JButton closeButton = new JButton("Close");
            closeButton.addActionListener(e -> {
                dialog.dispose();
                dismiss(notification.getId());
            });
            buttonPanel.add(closeButton);

            dialog.add(buttonPanel, BorderLayout.SOUTH);
            dialog.pack();
            dialog.setLocationRelativeTo(null);
            dialog.setVisible(true);
        });
    }

    public void scheduleAutoDismiss(Notification notification) {
        // Auto-dismiss based on priority
        long dismissDelay;
        switch (notification.getPriority()) {
            case LOW:
                dismissDelay = TimeUnit.SECONDS.toMillis(3);
                break;
            case NORMAL:
                dismissDelay = TimeUnit.SECONDS.toMillis(5);
                break;
            case HIGH:
                dismissDelay = TimeUnit.SECONDS.toMillis(10);
                break;
            default:
                // CRITICAL notifications stay the longest
                dismissDelay = TimeUnit.MINUTES.toMillis(1);
        }

        // For notifications with actions, don't auto-dismiss
        if (!notification.getActions().isEmpty()) {
            return;
        }

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() -> {
            dismiss(notification.getId());
            scheduler.shutdown();
        }, dismissDelay, TimeUnit.MILLISECONDS);
    }

    private void cleanupOldNotifications() {
        long oneDayAgo = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1);

        for (Map.Entry<String, List<Notification>> entry : notificationHistory.entrySet()) {
            List<Notification> history = entry.getValue();
            history.removeIf(notification -> notification.getTimestamp() < oneDayAgo);
        }

        if (log.isDebugEnabled()) {
            log.debug("Cleaned up old notifications");
        }
    }

    private String getIconType(NotificationType type) {
        switch (type) {
            case INFO: return "ℹ️";
            case SUCCESS: return "✅";
            case WARNING: return "⚠️";
            case ERROR: return "❌";
            case DEBUG: return "🐛";
            default: return "📢";
        }
    }

    /**
     * Implementation of the Notification interface
     */
    public static class NotificationImpl implements Notification {
        private final String id;
        private final String pluginId;
        private final NotificationType type;
        private final String title;
        private final String message;
        private final Priority priority;
        private final long timestamp;
        private final List<NotificationAction> actions;
        private final Map<String, Object> metadata;

        public NotificationImpl(String id, String pluginId, NotificationType type, Priority priority,
                                String title, String message, long timestamp,
                                List<NotificationAction> actions, Map<String, Object> metadata) {
            this.id = id;
            this.pluginId = pluginId;
            this.type = type;
            this.priority = priority;
            this.title = title;
            this.message = message;
            this.timestamp = timestamp;
            this.actions = actions != null ? actions : Collections.emptyList();
            this.metadata = metadata != null ? metadata : Collections.emptyMap();
        }

        @Override
        public String getId() { return id; }

        @Override
        public String getPluginId() { return pluginId; }

        @Override
        public NotificationType getType() { return type; }

        @Override
        public String getTitle() { return title; }

        @Override
        public String getMessage() { return message; }

        @Override
        public Priority getPriority() { return priority; }

        @Override
        public long getTimestamp() { return timestamp; }

        @Override
        public List<NotificationAction> getActions() { return actions; }

        @Override
        public Map<String, Object> getMetadata() { return metadata; }

        @Override
        public String toString() {
            return String.format("Notification{id='%s', pluginId='%s', type=%s, title='%s'}",
                    id, pluginId, type, title);
        }
    }

    /**
     * Implementation of NotificationAction
     */
    public static class NotificationActionImpl implements NotificationAction {
        private final String actionId;
        private final String label;
        private final Runnable callback;

        public NotificationActionImpl(String actionId, String label, Runnable callback) {
            this.actionId = actionId;
            this.label = label;
            this.callback = callback;
        }

        @Override
        public String getActionId() { return actionId; }

        @Override
        public String getLabel() { return label; }

        @Override
        public Runnable getCallback() { return callback; }
    }
}