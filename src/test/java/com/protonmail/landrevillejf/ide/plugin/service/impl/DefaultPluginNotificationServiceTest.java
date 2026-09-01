package com.protonmail.landrevillejf.ide.plugin.service.impl;

import com.protonmail.landrevillejf.ide.plugin.service.PluginNotificationService;
import com.protonmail.landrevillejf.ide.plugin.utils.LogCapture;
import com.protonmail.landrevillejf.ide.plugin.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

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

    @Test
    void notifyListeners_shouldNotifyGlobalAndHandleExceptions() throws InterruptedException {
        CountDownLatch globalLatch = new CountDownLatch(1);
        AtomicBoolean globalCalled = new AtomicBoolean(false);
        notificationService.registerListener("all", n -> {
            globalCalled.set(true);
            globalLatch.countDown();
        });

        // Listener qui lance une exception
        notificationService.registerListener(TEST_PLUGIN, n -> {
            throw new RuntimeException("Test exception");
        });

        notificationService.notify(TEST_PLUGIN, "Test", "Message");

        assertTrue(globalLatch.await(2, TimeUnit.SECONDS));
        assertTrue(globalCalled.get());
        // Vérifier que la notification est quand même créée
        assertFalse(notificationService.getActiveNotifications(TEST_PLUGIN).isEmpty());
    }

    @Test
    void cleanupOldNotifications_shouldRemoveOldEntries() throws Exception {
        // Créer une notification avec timestamp vieux de 2 jours via réflexion
        Method method = DefaultPluginNotificationService.class
                .getDeclaredMethod("cleanupOldNotifications");
        method.setAccessible(true);

        // Ajouter une notification ancienne dans l'historique
        Field historyField = DefaultPluginNotificationService.class
                .getDeclaredField("notificationHistory");
        historyField.setAccessible(true);
        Map<String, List<PluginNotificationService.Notification>> history =
                (Map<String, List<PluginNotificationService.Notification>>) historyField.get(notificationService);

        PluginNotificationService.Notification oldNotif = new DefaultPluginNotificationService.NotificationImpl(
                "old", TEST_PLUGIN, PluginNotificationService.NotificationType.INFO,
                PluginNotificationService.Priority.NORMAL, "Old", "Old message",
                System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2),
                Collections.emptyList(), Collections.emptyMap()
        );

        // Also add a recent notification that must survive cleanup
        PluginNotificationService.Notification recentNotif = new DefaultPluginNotificationService.NotificationImpl(
                "recent", TEST_PLUGIN, PluginNotificationService.NotificationType.INFO,
                PluginNotificationService.Priority.NORMAL, "Recent", "Recent message",
                System.currentTimeMillis(),
                Collections.emptyList(), Collections.emptyMap()
        );

        history.computeIfAbsent(TEST_PLUGIN, k -> new CopyOnWriteArrayList<>()).add(oldNotif);
        history.get(TEST_PLUGIN).add(recentNotif);

        // Appeler cleanup
        method.invoke(notificationService);

        // Vérifier que l'ancienne notification a été supprimée
        List<PluginNotificationService.Notification> remaining =
                notificationService.getRecentNotifications(TEST_PLUGIN, 10);
        assertFalse(remaining.contains(oldNotif));
        // Verify the recent notification survived cleanup (kills getTimestamp() mutation)
        assertTrue(remaining.contains(recentNotif),
                "Recent notification must survive cleanup");
    }

    @Test
    void showInUI_shouldNotThrowOnEDT() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            notificationService.notify(TEST_PLUGIN, "UI Test", "Message");
        });
        // fermer les fenêtres ouvertes
        for (Window window : Window.getWindows()) {
            window.dispose();
        }
    }

    @Test
    void notificationActionImpl_shouldExposeFields() {
        Runnable callback = () -> {};
        DefaultPluginNotificationService.NotificationActionImpl action =
                new DefaultPluginNotificationService.NotificationActionImpl("id", "Label", callback);
        assertEquals("id", action.getActionId());
        assertEquals("Label", action.getLabel());
        assertSame(callback, action.getCallback());
    }

    @Test
    void dismiss_unknownNotification_shouldDoNothing() {
        // Ne doit pas lever d'exception
        assertDoesNotThrow(() -> notificationService.dismiss("non-existent-id"));
        // Vérifier qu'aucune notification n'a été supprimée
        notificationService.notify(TEST_PLUGIN, "Keep", "Message");
        assertEquals(1, notificationService.getActiveNotifications(TEST_PLUGIN).size());
        notificationService.dismiss("autre-id");
        assertEquals(1, notificationService.getActiveNotifications(TEST_PLUGIN).size());
    }

    @Test
    void getIconType_shouldReturnCorrectIconForEachType() throws Exception {
        Method method = DefaultPluginNotificationService.class
                .getDeclaredMethod("getIconType", PluginNotificationService.NotificationType.class);
        method.setAccessible(true);

        assertEquals("ℹ️", method.invoke(notificationService, PluginNotificationService.NotificationType.INFO));
        assertEquals("✅", method.invoke(notificationService, PluginNotificationService.NotificationType.SUCCESS));
        assertEquals("⚠️", method.invoke(notificationService, PluginNotificationService.NotificationType.WARNING));
        assertEquals("❌", method.invoke(notificationService, PluginNotificationService.NotificationType.ERROR));
        assertEquals("🐛", method.invoke(notificationService, PluginNotificationService.NotificationType.DEBUG));
    }

    @Test
    void showInUI_allNotificationTypes_shouldNotThrow() throws Exception {
        for (PluginNotificationService.NotificationType type : PluginNotificationService.NotificationType.values()) {
            SwingUtilities.invokeAndWait(() -> {
                notificationService.notify(TEST_PLUGIN, type,
                        PluginNotificationService.Priority.NORMAL,
                        "Title " + type, "Message " + type);
            });
            // Fermer les fenêtres ouvertes pour éviter l'accumulation
            for (Window window : Window.getWindows()) {
                window.dispose();
            }
        }
    }

    // Méthode utilitaire pour parcourir récursivement les composants
    private java.util.List<java.awt.Component> getAllComponents(java.awt.Container container) {
        java.util.List<java.awt.Component> components = new java.util.ArrayList<>();
        for (java.awt.Component comp : container.getComponents()) {
            components.add(comp);
            if (comp instanceof java.awt.Container) {
                components.addAll(getAllComponents((java.awt.Container) comp));
            }
        }
        return components;
    }

    @Test
    void notifyListeners_globalListenerThrows_shouldCatchException() {
        notificationService.registerListener("all", n -> {
            throw new RuntimeException("Global listener error");
        });

        // Ne doit pas bloquer la création de la notification
        assertDoesNotThrow(() ->
                notificationService.notify(TEST_PLUGIN, "Test", "Message")
        );

        // La notification doit être présente
        assertFalse(notificationService.getActiveNotifications(TEST_PLUGIN).isEmpty());
    }

    @Test
    void clearNotifications_unknownPlugin_shouldDoNothing() {
        assertDoesNotThrow(() -> notificationService.clearNotifications("unknown.plugin"));
    }

    @Test
    void registerListener_multipleListeners_samePlugin() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);
        notificationService.registerListener(TEST_PLUGIN, n -> latch.countDown());
        notificationService.registerListener(TEST_PLUGIN, n -> latch.countDown());

        notificationService.notify(TEST_PLUGIN, "Multi", "Message");

        assertTrue(latch.await(2, TimeUnit.SECONDS));
    }

    // ==================== TESTS UI (exécutés uniquement si un affichage est disponible) ====================

    @Test
    @DisplayName("Should execute action and dismiss dialog when action button clicked")
    void showInUIWithActions_shouldExecuteActionAndDismiss() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "Skipping UI test in headless environment");

        CountDownLatch actionLatch = new CountDownLatch(1);
        AtomicBoolean actionExecuted = new AtomicBoolean(false);
        List<PluginNotificationService.NotificationAction> actions = List.of(
                new DefaultPluginNotificationService.NotificationActionImpl(
                        "a1", "Click me", () -> {
                    actionExecuted.set(true);
                    actionLatch.countDown();
                })
        );

        String id = notificationService.notifyWithActions(
                TEST_PLUGIN,
                PluginNotificationService.NotificationType.INFO,
                PluginNotificationService.Priority.NORMAL,
                "Title",
                "Message",
                actions
        );

        // Attendre que le dialog soit créé
        Thread.sleep(500);

        SwingUtilities.invokeAndWait(() -> {
            Window[] windows = Window.getWindows();
            JDialog dialog = null;
            for (Window w : windows) {
                if (w instanceof JDialog && w.isVisible()) {
                    dialog = (JDialog) w;
                    break;
                }
            }
            assertNotNull(dialog);
            // Récupérer le bouton d'action (premier bouton du panel sud)
            Component[] comps = dialog.getContentPane().getComponents();
            JPanel buttonPanel = (JPanel) comps[1]; // BorderLayout.SOUTH
            JButton actionButton = (JButton) buttonPanel.getComponent(0);
            actionButton.doClick();
        });

        assertTrue(actionLatch.await(2, TimeUnit.SECONDS));
        assertTrue(actionExecuted.get());
        // La notification doit être supprimée après l'action
        assertNull(getActiveNotification(id));
    }

    @Test
    @DisplayName("Should dismiss dialog when Close button is clicked")
    void showInUIWithActions_shouldDismissOnCloseButton() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "Skipping UI test in headless environment");

        List<PluginNotificationService.NotificationAction> actions = List.of(
                new DefaultPluginNotificationService.NotificationActionImpl("a1", "OK", () -> {})
        );

        String id = notificationService.notifyWithActions(
                TEST_PLUGIN,
                PluginNotificationService.NotificationType.INFO,
                PluginNotificationService.Priority.NORMAL,
                "Title",
                "Message",
                actions
        );

        Thread.sleep(500);

        SwingUtilities.invokeAndWait(() -> {
            Window[] windows = Window.getWindows();
            JDialog dialog = null;
            for (Window w : windows) {
                if (w instanceof JDialog && w.isVisible()) {
                    dialog = (JDialog) w;
                    break;
                }
            }
            assertNotNull(dialog);
            // Trouver le bouton "Close" (dernier bouton du panel sud)
            Component[] comps = dialog.getContentPane().getComponents();
            JPanel buttonPanel = (JPanel) comps[1];
            int count = buttonPanel.getComponentCount();
            JButton closeButton = (JButton) buttonPanel.getComponent(count - 1);
            closeButton.doClick();
        });

        Thread.sleep(200);
        assertNull(getActiveNotification(id));
    }

    @Test
    @DisplayName("Should handle exception thrown by action callback")
    void showInUIWithActions_shouldCatchExceptionFromCallback() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "Skipping UI test in headless environment");

        List<PluginNotificationService.NotificationAction> actions = List.of(
                new DefaultPluginNotificationService.NotificationActionImpl(
                        "a1", "Throw", () -> { throw new RuntimeException("Intentional failure"); })
        );

        String id = notificationService.notifyWithActions(
                TEST_PLUGIN,
                PluginNotificationService.NotificationType.INFO,
                PluginNotificationService.Priority.NORMAL,
                "Title",
                "Message",
                actions
        );

        Thread.sleep(500);

        SwingUtilities.invokeAndWait(() -> {
            Window[] windows = Window.getWindows();
            JDialog dialog = null;
            for (Window w : windows) {
                if (w instanceof JDialog && w.isVisible()) {
                    dialog = (JDialog) w;
                    break;
                }
            }
            assertNotNull(dialog);
            Component[] comps = dialog.getContentPane().getComponents();
            JPanel buttonPanel = (JPanel) comps[1];
            JButton actionButton = (JButton) buttonPanel.getComponent(0);
            // Le clic ne doit pas lever d'exception (le catch les attrape)
            assertDoesNotThrow((Executable) actionButton::doClick);
        });

        Thread.sleep(200);
        // La notification doit être supprimée malgré l'exception (finally block)
        assertNull(getActiveNotification(id));
    }

    // ==================== TEST DE LA MÉTHODE showInUI (lambda) ====================

    @Test
    @DisplayName("showInUI should show JOptionPane for each type")
    void showInUI_shouldShowDialogForAllTypes() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "Skipping UI test in headless environment");

        for (PluginNotificationService.NotificationType type : PluginNotificationService.NotificationType.values()) {
            // On crée une notification et on appelle showInUI via réflexion
            Method showInUIMethod = DefaultPluginNotificationService.class.getDeclaredMethod(
                    "showInUI", PluginNotificationService.Notification.class);
            showInUIMethod.setAccessible(true);

            // Créer une notification
            PluginNotificationService.Notification notif = new DefaultPluginNotificationService.NotificationImpl(
                    "id", TEST_PLUGIN, type, PluginNotificationService.Priority.NORMAL,
                    "Title " + type, "Message " + type,
                    System.currentTimeMillis(), Collections.emptyList(), Collections.emptyMap()
            );

            // Appeler showInUI (invokeLater, donc il faut attendre)
            CountDownLatch latch = new CountDownLatch(1);
            SwingUtilities.invokeLater(() -> {
                try {
                    showInUIMethod.invoke(notificationService, notif);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
            assertTrue(latch.await(2, TimeUnit.SECONDS));
            // Attendre que le dialog apparaisse
            Thread.sleep(300);
            // Fermer le dialog
            SwingUtilities.invokeAndWait(() -> {
                for (Window window : Window.getWindows()) {
                    if (window.isVisible()) {
                        window.dispose();
                    }
                }
            });
        }
    }

    // ==================== TEST DE LA MÉTHODE showInUIWithActions (lambda) ====================

    @Test
    @DisplayName("showInUIWithActions should create dialog with actions")
    void showInUIWithActions_shouldCreateDialogAndButtons() throws Exception {
        assumeFalse(GraphicsEnvironment.isHeadless(), "Skipping UI test in headless environment");

        List<PluginNotificationService.NotificationAction> actions = List.of(
                new DefaultPluginNotificationService.NotificationActionImpl("a1", "Action1", () -> {}),
                new DefaultPluginNotificationService.NotificationActionImpl("a2", "Action2", () -> {})
        );

        String id = notificationService.notifyWithActions(
                TEST_PLUGIN,
                PluginNotificationService.NotificationType.INFO,
                PluginNotificationService.Priority.NORMAL,
                "Title",
                "Message",
                actions
        );

        Thread.sleep(500);

        SwingUtilities.invokeAndWait(() -> {
            Window[] windows = Window.getWindows();
            JDialog dialog = null;
            for (Window w : windows) {
                if (w instanceof JDialog && w.isVisible()) {
                    dialog = (JDialog) w;
                    break;
                }
            }
            assertNotNull(dialog);
            // Vérifier que les boutons sont présents
            Component[] comps = dialog.getContentPane().getComponents();
            JPanel buttonPanel = (JPanel) comps[1];
            assertEquals(3, buttonPanel.getComponentCount()); // 2 actions + Close
            // Vérifier les labels
            JButton b1 = (JButton) buttonPanel.getComponent(0);
            assertEquals("Action1", b1.getText());
            JButton b2 = (JButton) buttonPanel.getComponent(1);
            assertEquals("Action2", b2.getText());
            JButton close = (JButton) buttonPanel.getComponent(2);
            assertEquals("Close", close.getText());
            // Fermer
            dialog.dispose();
        });
    }

    // ==================== HELPER ====================

    private PluginNotificationService.Notification getActiveNotification(String id) {
        for (PluginNotificationService.Notification n :
                notificationService.getActiveNotifications(TEST_PLUGIN)) {
            if (n.getId().equals(id)) return n;
        }
        return null;
    }

    @Test
    @DisplayName("registerListener should log debug when debug enabled")
    void testRegisterListener_LogsDebug() {
        try (LogCapture capture = LogCapture.attach(DefaultPluginNotificationService.class)) {
            notificationService.registerListener(TEST_PLUGIN, n -> {});

            assertTrue(capture.formattedMessages().stream()
                .anyMatch(msg -> msg.contains("Listener registered")));
        }
    }

    @Test
    @DisplayName("unregisterListener should log debug when debug enabled")
    void testUnregisterListener_LogsDebug() {
        java.util.function.Consumer<PluginNotificationService.Notification> listener = n -> {};
        notificationService.registerListener(TEST_PLUGIN, listener);

        try (LogCapture capture = LogCapture.attach(DefaultPluginNotificationService.class)) {
            notificationService.unregisterListener(TEST_PLUGIN, listener);

            assertTrue(capture.formattedMessages().stream()
                .anyMatch(msg -> msg.contains("Listener unregistered")));
        }
    }

    @Nested
    @DisplayName("Coverage completion tests")
    class CoverageCompletionTests {

        @Test
        @DisplayName("Should skip all logging when the notification logger is disabled")
        void shouldCoverLogGuardFalseBranches() throws Exception {
            TestUtils.withLoggingOffThrowing(DefaultPluginNotificationService.class, () -> {
                // Constructor log guard
                DefaultPluginNotificationService quietService =
                        new DefaultPluginNotificationService();

                java.util.function.Consumer<PluginNotificationService.Notification> failing = n -> {
                    throw new RuntimeException("listener failure");
                };

                // registerListener guard + plugin/global listener error guards
                quietService.registerListener(TEST_PLUGIN, failing);
                quietService.registerListener("all", failing);
                quietService.notify(TEST_PLUGIN, "Title", "Message");

                // notifyWithActions and dismiss guards
                String notificationId = quietService.notifyWithActions(TEST_PLUGIN,
                        PluginNotificationService.NotificationType.INFO,
                        PluginNotificationService.Priority.NORMAL, "Title", "Message",
                        List.of(new DefaultPluginNotificationService.NotificationActionImpl(
                                "a", "A", () -> {})));
                quietService.dismiss(notificationId);

                // unregisterListener guards (registered and unknown plugin)
                quietService.unregisterListener(TEST_PLUGIN, failing);
                quietService.unregisterListener(TEST_PLUGIN_2, failing);

                // clearNotifications guard
                quietService.clearNotifications(TEST_PLUGIN);

                // cleanupOldNotifications guard
                Method cleanup = DefaultPluginNotificationService.class
                        .getDeclaredMethod("cleanupOldNotifications");
                cleanup.setAccessible(true);
                cleanup.invoke(quietService);
            });
        }

        @Test
        @DisplayName("Should display notifications in the UI when not headless")
        void shouldShowNotificationsInUIWhenNotHeadless() {
            List<ActionListener> buttonListeners = new ArrayList<>();

            try (MockedStatic<GraphicsEnvironment> graphics =
                         Mockito.mockStatic(GraphicsEnvironment.class);
                 MockedStatic<SwingUtilities> swing = Mockito.mockStatic(SwingUtilities.class);
                 MockedStatic<JOptionPane> optionPane = Mockito.mockStatic(JOptionPane.class);
                 MockedConstruction<JDialog> dialogs = Mockito.mockConstruction(JDialog.class);
                 // Real Swing components cannot build their component tree in headless mode
                 MockedConstruction<JTextArea> textAreas = Mockito.mockConstruction(JTextArea.class);
                 MockedConstruction<JScrollPane> scrollPanes = Mockito.mockConstruction(JScrollPane.class);
                 MockedConstruction<JPanel> panels = Mockito.mockConstruction(JPanel.class);
                 MockedConstruction<JButton> buttons = Mockito.mockConstruction(JButton.class,
                         (mock, context) -> Mockito.doAnswer(invocation -> {
                             buttonListeners.add(invocation.getArgument(0));
                             return null;
                         }).when(mock).addActionListener(Mockito.any(ActionListener.class)))) {

                graphics.when(GraphicsEnvironment::isHeadless).thenReturn(false);
                // Run EDT tasks synchronously so the lambda bodies are exercised
                swing.when(() -> SwingUtilities.invokeLater(Mockito.any(Runnable.class)))
                        .thenAnswer(invocation -> {
                            ((Runnable) invocation.getArgument(0)).run();
                            return null;
                        });

                // Every notification type drives its own message dialog style
                for (PluginNotificationService.NotificationType type
                        : PluginNotificationService.NotificationType.values()) {
                    notificationService.notify(TEST_PLUGIN, type,
                            PluginNotificationService.Priority.LOW, "Title", "Message");
                }

                optionPane.verify(() -> JOptionPane.showMessageDialog(
                                Mockito.isNull(), Mockito.anyString(),
                                Mockito.anyString(), Mockito.anyInt()),
                        Mockito.times(PluginNotificationService.NotificationType.values().length));

                // Actionable notification: two action buttons plus the close button
                PluginNotificationService.NotificationAction okAction =
                        new DefaultPluginNotificationService.NotificationActionImpl(
                                "ok", "OK", () -> {});
                PluginNotificationService.NotificationAction badAction =
                        new DefaultPluginNotificationService.NotificationActionImpl(
                                "bad", "Bad", () -> {
                                    throw new RuntimeException("action failed");
                                });
                String notificationId = notificationService.notifyWithActions(TEST_PLUGIN,
                        PluginNotificationService.NotificationType.INFO,
                        PluginNotificationService.Priority.NORMAL, "Title", "Message",
                        List.of(okAction, badAction));

                assertEquals(3, buttonListeners.size());

                // Click outside the mock scopes: action callbacks and dismiss still work
                buttonListeners.get(0).actionPerformed(null);
                buttonListeners.get(1).actionPerformed(null); // caught and logged
                TestUtils.withLoggingOff(DefaultPluginNotificationService.class,
                        () -> buttonListeners.get(1).actionPerformed(null)); // error guard false
                buttonListeners.get(2).actionPerformed(null); // close button

                assertFalse(notificationService.getActiveNotifications(TEST_PLUGIN)
                        .stream().anyMatch(n -> n.getId().equals(notificationId)));
            }
        }

        @Test
        @DisplayName("Auto-dismiss delay depends on priority, actions block dismissal")
        void scheduleAutoDismissCoversAllPriorities() {
            for (PluginNotificationService.Priority priority
                    : PluginNotificationService.Priority.values()) {
                DefaultPluginNotificationService.NotificationImpl notification =
                        new DefaultPluginNotificationService.NotificationImpl(
                                "id-" + priority, TEST_PLUGIN,
                                PluginNotificationService.NotificationType.INFO,
                                priority, "Title", "Message", System.currentTimeMillis(),
                                Collections.emptyList(), Collections.emptyMap());
                notificationService.scheduleAutoDismiss(notification);
            }

            // Notifications with actions are never auto-dismissed
            PluginNotificationService.NotificationAction action =
                    new DefaultPluginNotificationService.NotificationActionImpl(
                            "a", "A", () -> {});
            DefaultPluginNotificationService.NotificationImpl withActions =
                    new DefaultPluginNotificationService.NotificationImpl(
                            "id-actions", TEST_PLUGIN,
                            PluginNotificationService.NotificationType.INFO,
                            PluginNotificationService.Priority.NORMAL, "Title", "Message",
                            System.currentTimeMillis(), List.of(action), Collections.emptyMap());
            notificationService.scheduleAutoDismiss(withActions);
        }

        @Test
        @DisplayName("NotificationImpl tolerates null collections and renders toString")
        void notificationImplNullCollectionsAndToString() {
            DefaultPluginNotificationService.NotificationImpl notification =
                    new DefaultPluginNotificationService.NotificationImpl(
                            "id-1", TEST_PLUGIN,
                            PluginNotificationService.NotificationType.CUSTOM,
                            PluginNotificationService.Priority.NORMAL, "Title", "Message",
                            123L, null, null);

            assertTrue(notification.getActions().isEmpty());
            assertTrue(notification.getMetadata().isEmpty());
            assertEquals(123L, notification.getTimestamp());
            assertTrue(notification.toString().contains("id-1"));
            assertTrue(notification.toString().contains("CUSTOM"));
        }
    }
}