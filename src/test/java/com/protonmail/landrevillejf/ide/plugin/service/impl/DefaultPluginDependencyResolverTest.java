package com.protonmail.landrevillejf.ide.plugin.service.impl;

import com.protonmail.landrevillejf.ide.plugin.service.PluginDependencyResolver;
import com.protonmail.landrevillejf.ide.plugin.utils.TestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DefaultPluginDependencyResolverTest {

    private DefaultPluginDependencyResolver resolver;
    private static final String PLUGIN_A = "plugin-a";
    private static final String PLUGIN_B = "plugin-b";
    private static final String PLUGIN_C = "plugin-c";
    private static final String PLUGIN_D = "plugin-d";

    @BeforeEach
    void setUp() {
        resolver = new DefaultPluginDependencyResolver();
    }

    @Test
    void addDependency() {
        PluginDependencyResolver.PluginDependency dep = resolver.addDependency(
                PLUGIN_A, PLUGIN_B, "1.0.0", PluginDependencyResolver.DependencyLevel.REQUIRED
        );

        assertNotNull(dep);
        // Correction : getProviderId() retourne le plugin fournisseur (PLUGIN_B)
        assertEquals(PLUGIN_B, dep.getProviderId());
        assertEquals("1.0.0", dep.getRequiredVersion());
        assertEquals(PluginDependencyResolver.DependencyLevel.REQUIRED, dep.getLevel());

        List<PluginDependencyResolver.PluginDependency> deps = resolver.getDependencies(PLUGIN_A);
        assertEquals(1, deps.size());
        assertEquals(PLUGIN_B, deps.getFirst().getProviderId());
    }

    @Test
    void testDependencyResolve() {
        PluginDependencyResolver.PluginDependency dep = resolver.addDependency(
                PLUGIN_A, PLUGIN_B, "1.0.0", PluginDependencyResolver.DependencyLevel.REQUIRED
        );

        assertNotNull(dep);
        assertEquals(PLUGIN_A, dep.getPluginId());      // Plugin qui a la dépendance
        assertEquals(PLUGIN_B, dep.getProviderId());   // Plugin dont on dépend
        assertEquals("1.0.0", dep.getRequiredVersion());
        assertEquals(PluginDependencyResolver.DependencyLevel.REQUIRED, dep.getLevel());

        List<PluginDependencyResolver.PluginDependency> deps = resolver.getDependencies(PLUGIN_A);
        assertEquals(1, deps.size());
    }

    @Test
    void removeDependency() {
        resolver.addDependency(PLUGIN_A, PLUGIN_B, "1.0.0", PluginDependencyResolver.DependencyLevel.REQUIRED);

        boolean removed = resolver.removeDependency(PLUGIN_A, PLUGIN_B);

        assertTrue(removed);
        assertTrue(resolver.getDependencies(PLUGIN_A).isEmpty());
    }

    @Test
    void removeNonExistentDependency() {
        boolean removed = resolver.removeDependency(PLUGIN_A, "non-existent");

        assertFalse(removed);
    }

    @Test
    void getDependencies() {
        resolver.addDependency(PLUGIN_A, PLUGIN_B, "1.0.0", PluginDependencyResolver.DependencyLevel.REQUIRED);
        resolver.addDependency(PLUGIN_A, PLUGIN_C, "2.0.0", PluginDependencyResolver.DependencyLevel.OPTIONAL);

        List<PluginDependencyResolver.PluginDependency> deps = resolver.getDependencies(PLUGIN_A);

        assertEquals(2, deps.size());
    }

    @Test
    void getRequiredDependencies() {
        resolver.addDependency(PLUGIN_A, PLUGIN_B, "1.0.0", PluginDependencyResolver.DependencyLevel.REQUIRED);
        resolver.addDependency(PLUGIN_A, PLUGIN_C, "2.0.0", PluginDependencyResolver.DependencyLevel.OPTIONAL);

        List<PluginDependencyResolver.PluginDependency> required = resolver.getRequiredDependencies(PLUGIN_A);

        assertEquals(1, required.size());
        assertEquals(PLUGIN_B, required.getFirst().getProviderId());
    }

    @Test
    void getOptionalDependencies() {
        resolver.addDependency(PLUGIN_A, PLUGIN_B, "1.0.0", PluginDependencyResolver.DependencyLevel.REQUIRED);
        resolver.addDependency(PLUGIN_A, PLUGIN_C, "2.0.0", PluginDependencyResolver.DependencyLevel.OPTIONAL);
        resolver.addDependency(PLUGIN_A, PLUGIN_D, "3.0.0", PluginDependencyResolver.DependencyLevel.OPTIONAL);

        List<PluginDependencyResolver.PluginDependency> optional = resolver.getOptionalDependencies(PLUGIN_A);

        assertEquals(2, optional.size());
    }

    @Test
    void getConflictingDependencies() {
        resolver.addDependency(PLUGIN_A, PLUGIN_B, "1.0.0", PluginDependencyResolver.DependencyLevel.CONFLICTING);
        resolver.addDependency(PLUGIN_A, PLUGIN_C, "2.0.0", PluginDependencyResolver.DependencyLevel.REQUIRED);

        List<PluginDependencyResolver.PluginDependency> conflicting = resolver.getConflictingDependencies(PLUGIN_A);

        assertEquals(1, conflicting.size());
        assertEquals(PLUGIN_B, conflicting.getFirst().getProviderId());
    }

    @Test
    void areRequiredDependenciesResolved() {
        // Register available plugins
        resolver.registerAvailablePlugin(PLUGIN_B, "1.0.0");
        resolver.addDependency(PLUGIN_A, PLUGIN_B, "1.0.0", PluginDependencyResolver.DependencyLevel.REQUIRED);

        boolean resolved = resolver.areRequiredDependenciesResolved(PLUGIN_A);

        assertTrue(resolved);
    }

    @Test
    void areRequiredDependenciesResolvedWhenMissing() {
        resolver.addDependency(PLUGIN_A, PLUGIN_B, "1.0.0", PluginDependencyResolver.DependencyLevel.REQUIRED);

        boolean resolved = resolver.areRequiredDependenciesResolved(PLUGIN_A);

        assertFalse(resolved);
    }

    @Test
    void isDependencyResolved() {
        resolver.registerAvailablePlugin(PLUGIN_B, "1.0.0");
        resolver.addDependency(PLUGIN_A, PLUGIN_B, "1.0.0", PluginDependencyResolver.DependencyLevel.REQUIRED);

        boolean resolved = resolver.isDependencyResolved(PLUGIN_A, PLUGIN_B);

        assertTrue(resolved);
    }

    @Test
    void isDependencyResolvedWithVersionMismatch() {
        resolver.registerAvailablePlugin(PLUGIN_B, "2.0.0");
        resolver.addDependency(PLUGIN_A, PLUGIN_B, "1.0.0", PluginDependencyResolver.DependencyLevel.REQUIRED);

        boolean resolved = resolver.isDependencyResolved(PLUGIN_A, PLUGIN_B);

        assertFalse(resolved);
    }

    @Test
    void getResolutionPath() {
        resolver.registerAvailablePlugin(PLUGIN_B, "1.0.0");
        resolver.registerAvailablePlugin(PLUGIN_C, "1.0.0");
        resolver.addDependency(PLUGIN_A, PLUGIN_B, "1.0.0", PluginDependencyResolver.DependencyLevel.REQUIRED);
        resolver.addDependency(PLUGIN_B, PLUGIN_C, "1.0.0", PluginDependencyResolver.DependencyLevel.REQUIRED);

        List<String> path = resolver.getResolutionPath(PLUGIN_A);

        assertEquals(3, path.size());
        assertEquals(PLUGIN_A, path.get(0));
        assertEquals(PLUGIN_B, path.get(1));
        assertEquals(PLUGIN_C, path.get(2));
    }

    @Test
    void detectCircularDependencies() {
        resolver.addDependency(PLUGIN_A, PLUGIN_B, "1.0.0", PluginDependencyResolver.DependencyLevel.REQUIRED);
        resolver.addDependency(PLUGIN_B, PLUGIN_A, "1.0.0", PluginDependencyResolver.DependencyLevel.REQUIRED);

        List<List<String>> cycles = resolver.detectCircularDependencies(PLUGIN_A);

        assertFalse(cycles.isEmpty());
    }

    @Test
    void getDependents() {
        resolver.addDependency(PLUGIN_A, PLUGIN_B, "1.0.0", PluginDependencyResolver.DependencyLevel.REQUIRED);
        resolver.addDependency(PLUGIN_C, PLUGIN_B, "1.0.0", PluginDependencyResolver.DependencyLevel.REQUIRED);

        List<String> dependents = resolver.getDependents(PLUGIN_B);

        assertEquals(2, dependents.size());
        assertTrue(dependents.contains(PLUGIN_A));
        assertTrue(dependents.contains(PLUGIN_C));
    }

    @Test
    void validateDependencies() {
        resolver.registerAvailablePlugin(PLUGIN_B, "1.0.0");
        resolver.addDependency(PLUGIN_A, PLUGIN_B, "1.0.0", PluginDependencyResolver.DependencyLevel.REQUIRED);
        resolver.addDependency(PLUGIN_A, PLUGIN_C, "1.0.0", PluginDependencyResolver.DependencyLevel.OPTIONAL);

        Map<String, Object> validation = resolver.validateDependencies(PLUGIN_A);

        assertTrue((Boolean) validation.get("valid"));
        assertTrue((Boolean) validation.get("requiredResolved"));
        assertEquals(2, validation.get("totalDependencies"));
    }

    @Test
    void validateDependenciesWithMissingRequired() {
        resolver.addDependency(PLUGIN_A, PLUGIN_B, "1.0.0", PluginDependencyResolver.DependencyLevel.REQUIRED);

        Map<String, Object> validation = resolver.validateDependencies(PLUGIN_A);

        assertFalse((Boolean) validation.get("valid"));
        assertFalse((Boolean) validation.get("requiredResolved"));

        @SuppressWarnings("unchecked")
        List<String> errors = (List<String>) validation.get("errors");
        assertFalse(errors.isEmpty());
    }

    @Test
    void getDependencyGraph() {
        resolver.addDependency(PLUGIN_A, PLUGIN_B, "1.0.0", PluginDependencyResolver.DependencyLevel.REQUIRED);
        resolver.addDependency(PLUGIN_B, PLUGIN_C, "1.0.0", PluginDependencyResolver.DependencyLevel.REQUIRED);

        Map<String, Object> graph = resolver.getDependencyGraph(PLUGIN_A);

        assertNotNull(graph.get("nodes"));
        assertNotNull(graph.get("edges"));
        assertNotNull(graph.get("statistics"));

        @SuppressWarnings("unchecked")
        Map<String, Object> stats = (Map<String, Object>) graph.get("statistics");
        assertEquals(3, stats.get("totalNodes"));
    }

    @Test
    void registerAvailablePlugin() {
        resolver.registerAvailablePlugin(PLUGIN_B, "1.0.0");
        resolver.addDependency(PLUGIN_A, PLUGIN_B, "1.0.0", PluginDependencyResolver.DependencyLevel.REQUIRED);

        boolean resolved = resolver.isDependencyResolved(PLUGIN_A, PLUGIN_B);

        assertTrue(resolved);
    }

    @Test
    void unregisterAvailablePlugin() {
        resolver.registerAvailablePlugin(PLUGIN_B, "1.0.0");
        resolver.addDependency(PLUGIN_A, PLUGIN_B, "1.0.0", PluginDependencyResolver.DependencyLevel.REQUIRED);

        assertTrue(resolver.isDependencyResolved(PLUGIN_A, PLUGIN_B));

        resolver.unregisterAvailablePlugin(PLUGIN_B);

        assertFalse(resolver.isDependencyResolved(PLUGIN_A, PLUGIN_B));
    }

    @Test
    void testVersionComparisonWithRange() {
        resolver.registerAvailablePlugin(PLUGIN_B, "1.5.0");
        resolver.addDependency(PLUGIN_A, PLUGIN_B, "1.0.0-2.0.0", PluginDependencyResolver.DependencyLevel.REQUIRED);

        assertTrue(resolver.isDependencyResolved(PLUGIN_A, PLUGIN_B));
    }

    @Test
    void testVersionComparisonWithMinimum() {
        resolver.registerAvailablePlugin(PLUGIN_B, "2.0.0");
        resolver.addDependency(PLUGIN_A, PLUGIN_B, ">=1.0.0", PluginDependencyResolver.DependencyLevel.REQUIRED);

        assertTrue(resolver.isDependencyResolved(PLUGIN_A, PLUGIN_B));
    }

    @Test
    void testVersionComparisonWithMaximum() {
        resolver.registerAvailablePlugin(PLUGIN_B, "1.5.0");
        resolver.addDependency(PLUGIN_A, PLUGIN_B, "<=2.0.0", PluginDependencyResolver.DependencyLevel.REQUIRED);

        assertTrue(resolver.isDependencyResolved(PLUGIN_A, PLUGIN_B));
    }

    @Test
    void testVersionComparisonWithWildcard() {
        resolver.registerAvailablePlugin(PLUGIN_B, "1.5.0");
        resolver.addDependency(PLUGIN_A, PLUGIN_B, "1.+", PluginDependencyResolver.DependencyLevel.REQUIRED);

        assertTrue(resolver.isDependencyResolved(PLUGIN_A, PLUGIN_B));
    }

    @Nested
    @DisplayName("Coverage completion tests")
    class CoverageCompletionTests {

        @Test
        @DisplayName("Should skip all logging when the resolver logger is disabled")
        void shouldCoverLogGuardFalseBranches() throws Exception {
            TestUtils.withLoggingOffThrowing(DefaultPluginDependencyResolver.class, () -> {
                DefaultPluginDependencyResolver silentResolver = new DefaultPluginDependencyResolver();

                silentResolver.addDependency(PLUGIN_A, PLUGIN_B, "1.0.0",
                        PluginDependencyResolver.DependencyLevel.REQUIRED);
                assertTrue(silentResolver.removeDependency(PLUGIN_A, PLUGIN_B));

                silentResolver.addDependency(PLUGIN_A, PLUGIN_B, "1.0.0",
                        PluginDependencyResolver.DependencyLevel.REQUIRED);
                assertFalse(silentResolver.areRequiredDependenciesResolved(PLUGIN_A));

                silentResolver.registerAvailablePlugin(PLUGIN_A, "1.0.0");
                silentResolver.registerAvailablePlugin(PLUGIN_B, "1.0.0");
                silentResolver.addDependency(PLUGIN_B, PLUGIN_A, "1.0.0",
                        PluginDependencyResolver.DependencyLevel.REQUIRED);
                assertTrue(silentResolver.getResolutionPath(PLUGIN_A).isEmpty());

                // Conflicting dependency that is NOT resolved exercises the
                // false branch of the conflicting-dependency check
                silentResolver.addDependency(PLUGIN_A, PLUGIN_C, "1.0.0",
                        PluginDependencyResolver.DependencyLevel.CONFLICTING);
                Map<String, Object> validation = silentResolver.validateDependencies(PLUGIN_A);
                @SuppressWarnings("unchecked")
                List<String> warnings = (List<String>) validation.get("warnings");
                assertTrue(warnings.isEmpty());

                silentResolver.unregisterAvailablePlugin(PLUGIN_B);
            });
        }
    }
}