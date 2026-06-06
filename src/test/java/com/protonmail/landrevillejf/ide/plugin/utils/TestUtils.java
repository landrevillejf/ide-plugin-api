package com.protonmail.landrevillejf.ide.plugin.utils;

import com.protonmail.landrevillejf.ide.plugin.service.PluginUpdateService;
import com.protonmail.landrevillejf.ide.plugin.service.impl.DefaultPluginUpdateService;

import java.util.Collections;

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

    @FunctionalInterface
    public interface Condition {
        boolean isMet();
    }
}