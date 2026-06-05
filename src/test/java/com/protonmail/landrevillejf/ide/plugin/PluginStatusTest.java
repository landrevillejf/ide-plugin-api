package com.protonmail.landrevillejf.ide.plugin;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PluginStatusTest {

    @Test
    void activeAndInactiveFlagsRemainStable() {
        assertTrue(PluginStatus.ENABLED.isActive());
        assertFalse(PluginStatus.DISABLED.isActive());
        assertTrue(PluginStatus.DISABLED.isInactive());
        assertFalse(PluginStatus.LOADED.isInactive());
    }

    @Test
    void toStringReturnsReadableLabelsForLifecycleStates() {
        assertEquals("Enabled", PluginStatus.ENABLED.toString());
        assertEquals("Reloading", PluginStatus.RELOADING.toString());
        assertEquals("Shutting Down", PluginStatus.SHUTTING_DOWN.toString());
        assertEquals("Shutdown", PluginStatus.SHUTDOWN.toString());
    }

    @Test
    void canTransitionToRejectsNullTargets() {
        assertFalse(PluginStatus.ENABLED.canTransitionTo(null));
    }

    @Test
    void canTransitionToPreservesRepresentativeTransitions() {
        assertTrue(PluginStatus.LOADED.canTransitionTo(PluginStatus.ENABLED));
        assertTrue(PluginStatus.DISABLED.canTransitionTo(PluginStatus.ERROR));
        assertTrue(PluginStatus.ERROR.canTransitionTo(PluginStatus.DISABLED));
        assertFalse(PluginStatus.UNLOADED.canTransitionTo(PluginStatus.ENABLED));
        assertFalse(PluginStatus.SHUTDOWN.canTransitionTo(PluginStatus.ENABLED));
    }
}
