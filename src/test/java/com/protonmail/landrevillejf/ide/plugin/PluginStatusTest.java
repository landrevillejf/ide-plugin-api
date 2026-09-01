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

    @Test
    void canTransitionTo_CoversAllStateSpecificBranches() {
        assertTrue(PluginStatus.UNLOADED.canTransitionTo(PluginStatus.LOADED));
        assertTrue(PluginStatus.LOADED.canTransitionTo(PluginStatus.INITIALIZED));
        assertTrue(PluginStatus.LOADED.canTransitionTo(PluginStatus.ENABLING));
        assertTrue(PluginStatus.INITIALIZED.canTransitionTo(PluginStatus.DISABLED));
        assertTrue(PluginStatus.DISABLED.canTransitionTo(PluginStatus.SHUTTING_DOWN));
        assertTrue(PluginStatus.ENABLED.canTransitionTo(PluginStatus.DISABLING));
        assertTrue(PluginStatus.ENABLING.canTransitionTo(PluginStatus.ERROR));
        assertTrue(PluginStatus.DISABLING.canTransitionTo(PluginStatus.ERROR));
        assertTrue(PluginStatus.SHUTTING_DOWN.canTransitionTo(PluginStatus.SHUTDOWN));
        assertTrue(PluginStatus.RELOADING.canTransitionTo(PluginStatus.LOADED));
        assertTrue(PluginStatus.ENABLED.canTransitionTo(PluginStatus.ENABLED));
        assertFalse(PluginStatus.ERROR.canTransitionTo(PluginStatus.ENABLED));
        assertFalse(PluginStatus.RELOADING.canTransitionTo(PluginStatus.SHUTDOWN));
        assertFalse(PluginStatus.SHUTDOWN.canTransitionTo(PluginStatus.ERROR));
        // Cover false-branches of OR expressions in ENABLING, DISABLING, SHUTTING_DOWN cases
        assertFalse(PluginStatus.ENABLING.canTransitionTo(PluginStatus.LOADED));
        assertFalse(PluginStatus.DISABLING.canTransitionTo(PluginStatus.LOADED));
        assertFalse(PluginStatus.SHUTTING_DOWN.canTransitionTo(PluginStatus.LOADED));
        // SHUTDOWN -> false for any state
        assertFalse(PluginStatus.SHUTDOWN.canTransitionTo(PluginStatus.LOADED));
    }
}
