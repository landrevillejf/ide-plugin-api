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

    @FunctionalInterface
    public interface Condition {
        boolean isMet();
    }
}