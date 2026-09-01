package com.protonmail.landrevillejf.ide.plugin.utils;

import com.protonmail.landrevillejf.ide.plugin.DefaultPluginManager;
import com.protonmail.landrevillejf.ide.plugin.Plugin;
import com.protonmail.landrevillejf.ide.plugin.service.PluginUpdateService;
import com.protonmail.landrevillejf.ide.plugin.service.impl.DefaultPluginUpdateService;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Map;

import static org.mockito.Mockito.when;

public class TestUtils {

    public static PluginUpdateService.PluginVersion createMockVersion(String version) {
        return new DefaultPluginUpdateService.PluginVersionImpl(
                version,
                "Test version " + version,
                "2024-01-01",
                Collections.singletonList("Test changelog"),
                Collections.singletonList("Test feature"),
                Collections.singletonList("Test fix"),
                Collections.emptyMap()
        );
    }

    public static void waitForCondition(TestUtils.Condition condition, long timeoutMs) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        while (!condition.isMet() && (System.currentTimeMillis() - startTime) < timeoutMs) {
            Thread.sleep(100);
        }
        if (!condition.isMet()) {
            throw new AssertionError("Condition not met within timeout");
        }
    }

    @SuppressWarnings("unchecked")
    public static void addMockPlugin(DefaultPluginManager manager, String pluginName, Plugin mockPlugin) throws Exception {
        Field pluginsField = DefaultPluginManager.class.getDeclaredField("plugins");
        pluginsField.setAccessible(true);
        Map<String, Plugin> plugins = (Map<String, Plugin>) pluginsField.get(manager);
        plugins.put(pluginName, mockPlugin);

        when(mockPlugin.getName()).thenReturn(pluginName);
        when(mockPlugin.getVersion()).thenReturn("1.0.0");
    }

    /**
     * Runs the given action with the logger of the given class switched to
     * {@code Level.OFF}, then restores the original level. Used to cover the
     * false branch of {@code log.isXxxEnabled()} guards.
     *
     * @param clazz  the class whose logger must be silenced
     * @param action the action to run while logging is off
     */
    public static void withLoggingOff(Class<?> clazz, Runnable action) {
        ch.qos.logback.classic.Logger logger =
                (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(clazz);
        ch.qos.logback.classic.Level originalLevel = logger.getLevel();
        logger.setLevel(ch.qos.logback.classic.Level.OFF);
        try {
            action.run();
        } finally {
            logger.setLevel(originalLevel);
        }
    }

    /**
     * Same as {@link #withLoggingOff(Class, Runnable)} but allows the action
     * to throw checked exceptions, which are propagated to the caller.
     *
     * @param clazz  the class whose logger must be silenced
     * @param action the action to run while logging is off
     * @throws Exception if the action throws
     */
    public static void withLoggingOffThrowing(Class<?> clazz, ThrowingAction action) throws Exception {
        ch.qos.logback.classic.Logger logger =
                (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(clazz);
        ch.qos.logback.classic.Level originalLevel = logger.getLevel();
        logger.setLevel(ch.qos.logback.classic.Level.OFF);
        try {
            action.run();
        } finally {
            logger.setLevel(originalLevel);
        }
    }

    @FunctionalInterface
    public interface ThrowingAction {
        void run() throws Exception;
    }

    @FunctionalInterface
    public interface Condition {
        boolean isMet();
    }
}