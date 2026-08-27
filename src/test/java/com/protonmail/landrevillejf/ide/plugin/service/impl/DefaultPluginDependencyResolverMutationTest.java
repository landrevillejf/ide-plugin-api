package com.protonmail.landrevillejf.ide.plugin.service.impl;

import com.protonmail.landrevillejf.ide.plugin.service.PluginDependencyResolver.DependencyLevel;
import com.protonmail.landrevillejf.ide.plugin.service.PluginDependencyResolver.PluginDependency;
import com.protonmail.landrevillejf.ide.plugin.utils.LogCapture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Mutation-killing tests for {@link DefaultPluginDependencyResolver}.
 */
@DisplayName("DefaultPluginDependencyResolver mutation tests")
class DefaultPluginDependencyResolverMutationTest {

    private DefaultPluginDependencyResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new DefaultPluginDependencyResolver();
    }

    @Nested
    @DisplayName("dependency registration")
    class RegistrationTests {

        @Test
        @DisplayName("addDependency builds identity and dependent links")
        void addDependency() {
            PluginDependency dep = resolver.addDependency(
                    "plugin-a", "plugin-b", ">=1.0.0", DependencyLevel.REQUIRED);
            assertThat(dep.getId()).isEqualTo("plugin-a->plugin-b");
            assertThat(dep.getPluginId()).isEqualTo("plugin-a");
            assertThat(dep.getProviderId()).isEqualTo("plugin-b");
            assertThat(dep.getRequiredVersion()).isEqualTo(">=1.0.0");
            assertThat(dep.getLevel()).isEqualTo(DependencyLevel.REQUIRED);
            assertThat(dep.isResolved()).isFalse();
            assertThat(resolver.getDependencies("plugin-a")).containsExactly(dep);
            assertThat(resolver.getDependents("plugin-b")).containsExactly("plugin-a");
        }

        @Test
        @DisplayName("dependencies are filtered by level")
        void levelFilters() {
            resolver.addDependency("p", "req", "1.0", DependencyLevel.REQUIRED);
            resolver.addDependency("p", "opt", "1.0", DependencyLevel.OPTIONAL);
            resolver.addDependency("p", "conf", "1.0", DependencyLevel.CONFLICTING);

            assertThat(resolver.getRequiredDependencies("p"))
                    .extracting(PluginDependency::getProviderId).containsExactly("req");
            assertThat(resolver.getOptionalDependencies("p"))
                    .extracting(PluginDependency::getProviderId).containsExactly("opt");
            assertThat(resolver.getConflictingDependencies("p"))
                    .extracting(PluginDependency::getProviderId).containsExactly("conf");
            assertThat(resolver.getDependencies("p")).hasSize(3);
            assertThat(resolver.getDependencies("unknown")).isEmpty();
        }

        @Test
        @DisplayName("removeDependency only removes the matching dependency")
        void removeDependencySelective() {
            resolver.addDependency("p", "keep", "1.0", DependencyLevel.REQUIRED);
            resolver.addDependency("p", "drop", "1.0", DependencyLevel.REQUIRED);

            assertThat(resolver.removeDependency("p", "drop")).isTrue();
            assertThat(resolver.getDependencies("p"))
                    .extracting(PluginDependency::getProviderId).containsExactly("keep");
            assertThat(resolver.getDependents("drop")).isEmpty();

            assertThat(resolver.removeDependency("p", "drop")).isFalse();
            assertThat(resolver.removeDependency("unknown", "keep")).isFalse();
        }
    }

    @Nested
    @DisplayName("version compatibility")
    class VersionCompatibilityTests {

        private int sequence;

        private boolean resolved(String actual, String required) {
            // Use a unique provider id per call so earlier addDependency
            // registrations cannot shadow the constraint under test.
            String depId = "dep-" + (++sequence);
            resolver.addDependency("p", depId, required, DependencyLevel.REQUIRED);
            resolver.registerAvailablePlugin(depId, actual);
            return resolver.isDependencyResolved("p", depId);
        }

        @Test
        @DisplayName("null and empty requirements match anything")
        void anyVersion() {
            assertThat(resolved("9.9.9", null)).isTrue();
            assertThat(resolved("0.0.1", "")).isTrue();
        }

        @Test
        @DisplayName(">= boundary is inclusive")
        void minimumBoundary() {
            assertThat(resolved("1.0.0", ">=1.0.0")).isTrue();
            assertThat(resolved("1.0.1", ">=1.0.0")).isTrue();
            assertThat(resolved("0.9.9", ">=1.0.0")).isFalse();
        }

        @Test
        @DisplayName("<= boundary is inclusive")
        void maximumBoundary() {
            assertThat(resolved("2.0.0", "<=2.0.0")).isTrue();
            assertThat(resolved("1.9.9", "<=2.0.0")).isTrue();
            assertThat(resolved("2.0.1", "<=2.0.0")).isFalse();
        }

        @Test
        @DisplayName("range is inclusive on both ends")
        void rangeBoundaries() {
            assertThat(resolved("1.0.0", "1.0.0-2.0.0")).isTrue();
            assertThat(resolved("2.0.0", "1.0.0-2.0.0")).isTrue();
            assertThat(resolved("1.5.0", "1.0.0-2.0.0")).isTrue();
            assertThat(resolved("0.9.0", "1.0.0-2.0.0")).isFalse();
            assertThat(resolved("2.0.1", "1.0.0-2.0.0")).isFalse();
        }

        @Test
        @DisplayName("malformed range falls back to exact match")
        void malformedRange() {
            assertThat(resolved("1.0.0", "1-2-3")).isFalse();
            assertThat(resolved("1-2-3", "1-2-3")).isTrue();
        }

        @Test
        @DisplayName("plus suffix means minimum version")
        void plusSuffix() {
            assertThat(resolved("3.0.0", "3.0.0+")).isTrue();
            assertThat(resolved("3.1.0", "3.0.0+")).isTrue();
            assertThat(resolved("2.9.0", "3.0.0+")).isFalse();
        }

        @Test
        @DisplayName("exact match is strict")
        void exactMatch() {
            assertThat(resolved("1.2.3", "1.2.3")).isTrue();
            assertThat(resolved("1.2.4", "1.2.3")).isFalse();
        }

        @Test
        @DisplayName("versions of different lengths compare with zero padding")
        void differentLengths() {
            assertThat(resolved("1.0", ">=1.0.0")).isTrue();
            assertThat(resolved("1.0.0", ">=1.0")).isTrue();
            assertThat(resolved("1.0.1", ">=1.0")).isTrue();
            assertThat(resolved("1.0.0.0.1", "<=1.0")).isFalse();
        }

        @Test
        @DisplayName("isDependencyResolved fails for unknown plugins and versions")
        void unresolvedCases() {
            resolver.addDependency("p", "dep", "1.0", DependencyLevel.REQUIRED);
            assertThat(resolver.isDependencyResolved("p", "dep")).isFalse();

            resolver.registerAvailablePlugin("dep", "1.0");
            assertThat(resolver.isDependencyResolved("p", "other")).isFalse();
        }

        @Test
        @DisplayName("available plugin without version metadata is unresolved")
        @SuppressWarnings("unchecked")
        void missingVersionMetadata() throws Exception {
            Field field = DefaultPluginDependencyResolver.class
                    .getDeclaredField("availablePlugins");
            field.setAccessible(true);
            Map<String, Map<String, String>> available =
                    (Map<String, Map<String, String>>) field.get(resolver);
            available.put("noversion", new HashMap<>());

            resolver.addDependency("p", "noversion", "1.0", DependencyLevel.REQUIRED);
            assertThat(resolver.isDependencyResolved("p", "noversion")).isFalse();
        }

        @Test
        @DisplayName("available plugin without dependency declaration is unresolved")
        void availableButNotDeclared() {
            resolver.registerAvailablePlugin("undeclared", "1.0");
            assertThat(resolver.isDependencyResolved("p", "undeclared")).isFalse();
        }

        @Test
        @DisplayName("unregisterAvailablePlugin removes availability")
        void unregister() {
            resolver.registerAvailablePlugin("dep", "1.0");
            resolver.addDependency("p", "dep", "1.0", DependencyLevel.REQUIRED);
            assertThat(resolver.isDependencyResolved("p", "dep")).isTrue();
            resolver.unregisterAvailablePlugin("dep");
            assertThat(resolver.isDependencyResolved("p", "dep")).isFalse();
        }
    }

    @Nested
    @DisplayName("resolution path and cycles")
    class ResolutionTests {

        @Test
        @DisplayName("linear chain resolves in dependency order")
        void linearChain() {
            resolver.addDependency("a", "b", "1.0", DependencyLevel.REQUIRED);
            resolver.addDependency("b", "c", "1.0", DependencyLevel.REQUIRED);
            resolver.registerAvailablePlugin("b", "1.0");
            resolver.registerAvailablePlugin("c", "1.0");

            assertThat(resolver.getResolutionPath("a")).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("unresolved dependencies are skipped in the path")
        void unresolvedSkipped() {
            resolver.addDependency("a", "missing", "1.0", DependencyLevel.REQUIRED);
            assertThat(resolver.getResolutionPath("a")).containsExactly("a");
        }

        @Test
        @DisplayName("circular dependency yields empty path and warning")
        void circularPath() {
            resolver.addDependency("a", "b", "1.0", DependencyLevel.REQUIRED);
            resolver.addDependency("b", "a", "1.0", DependencyLevel.REQUIRED);
            resolver.registerAvailablePlugin("a", "1.0");
            resolver.registerAvailablePlugin("b", "1.0");

            try (LogCapture capture = LogCapture.attach(DefaultPluginDependencyResolver.class)) {
                assertThat(resolver.getResolutionPath("a")).isEmpty();
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("Circular dependency detected"));
            }
        }

        @Test
        @DisplayName("cycle detection finds simple cycles")
        void simpleCycle() {
            resolver.addDependency("a", "b", "1.0", DependencyLevel.REQUIRED);
            resolver.addDependency("b", "a", "1.0", DependencyLevel.REQUIRED);

            List<List<String>> cycles = resolver.detectCircularDependencies("a");
            assertThat(cycles).hasSize(1);
            assertThat(cycles.get(0)).containsExactly("a", "b", "a");
        }

        @Test
        @DisplayName("diamond dependency is not a cycle")
        void diamondIsNotCycle() {
            resolver.addDependency("a", "b", "1.0", DependencyLevel.REQUIRED);
            resolver.addDependency("a", "c", "1.0", DependencyLevel.REQUIRED);
            resolver.addDependency("b", "d", "1.0", DependencyLevel.REQUIRED);
            resolver.addDependency("c", "d", "1.0", DependencyLevel.REQUIRED);

            assertThat(resolver.detectCircularDependencies("a")).isEmpty();
        }

        @Test
        @DisplayName("required resolution reflects availability")
        void requiredResolution() {
            resolver.addDependency("p", "dep", "1.0", DependencyLevel.REQUIRED);
            assertThat(resolver.areRequiredDependenciesResolved("p")).isFalse();
            resolver.registerAvailablePlugin("dep", "1.0");
            assertThat(resolver.areRequiredDependenciesResolved("p")).isTrue();
        }
    }

    @Nested
    @DisplayName("validation and graph")
    class ValidationTests {

        @Test
        @DisplayName("validateDependencies reports missing required dependency")
        void missingRequired() {
            resolver.addDependency("p", "missing", "1.0", DependencyLevel.REQUIRED);
            Map<String, Object> validation = resolver.validateDependencies("p");

            assertThat(validation.get("valid")).isEqualTo(false);
            @SuppressWarnings("unchecked")
            List<String> errors = (List<String>) validation.get("errors");
            assertThat(errors).containsExactly(
                    "Required dependency 'missing' version 1.0 is missing");
            assertThat(validation.get("requiredResolved")).isEqualTo(false);
            assertThat(validation.get("totalDependencies")).isEqualTo(1);
            assertThat(validation.get("resolvedDependencies")).isEqualTo(0);
        }

        @Test
        @DisplayName("validateDependencies counts resolved dependencies")
        void resolvedCounting() {
            resolver.addDependency("p", "one", "1.0", DependencyLevel.REQUIRED);
            resolver.addDependency("p", "two", "1.0", DependencyLevel.OPTIONAL);
            resolver.registerAvailablePlugin("one", "1.0");

            Map<String, Object> validation = resolver.validateDependencies("p");
            assertThat(validation.get("valid")).isEqualTo(true);
            assertThat(validation.get("resolvedDependencies")).isEqualTo(1);
            assertThat(validation.get("totalDependencies")).isEqualTo(2);
        }

        @Test
        @DisplayName("validateDependencies warns about present conflicting plugins")
        void conflictingPresent() {
            resolver.addDependency("p", "rival", "1.0", DependencyLevel.CONFLICTING);
            resolver.registerAvailablePlugin("rival", "1.0");

            Map<String, Object> validation = resolver.validateDependencies("p");
            assertThat(validation.get("valid")).isEqualTo(true);
            @SuppressWarnings("unchecked")
            List<String> warnings = (List<String>) validation.get("warnings");
            assertThat(warnings).containsExactly("Conflicting dependency 'rival' is present");
        }

        @Test
        @DisplayName("validateDependencies reports circular dependencies")
        void circularReported() {
            resolver.addDependency("a", "b", "1.0", DependencyLevel.REQUIRED);
            resolver.addDependency("b", "a", "1.0", DependencyLevel.REQUIRED);

            Map<String, Object> validation = resolver.validateDependencies("a");
            assertThat(validation.get("valid")).isEqualTo(false);
            @SuppressWarnings("unchecked")
            List<String> errors = (List<String>) validation.get("errors");
            assertThat(errors).anyMatch(e -> e.startsWith("Circular dependency detected"));
        }

        @Test
        @DisplayName("dependency graph contains nodes, edges and statistics")
        void dependencyGraph() {
            resolver.addDependency("a", "b", "1.0", DependencyLevel.REQUIRED);
            resolver.addDependency("b", "c", "1.0", DependencyLevel.REQUIRED);
            resolver.addDependency("a", "c", "1.0", DependencyLevel.REQUIRED);

            Map<String, Object> graph = resolver.getDependencyGraph("a");
            assertThat(graph.get("rootNode")).isEqualTo("a");

            @SuppressWarnings("unchecked")
            List<String> nodes = (List<String>) graph.get("nodes");
            assertThat(nodes).containsExactlyInAnyOrder("a", "b", "c");

            @SuppressWarnings("unchecked")
            Map<String, List<String>> edges = (Map<String, List<String>>) graph.get("edges");
            assertThat(edges.get("a")).containsExactlyInAnyOrder("b", "c");
            assertThat(edges.get("b")).containsExactly("c");
            assertThat(edges.get("c")).isEmpty();

            @SuppressWarnings("unchecked")
            Map<String, Object> stats = (Map<String, Object>) graph.get("statistics");
            assertThat(stats.get("totalNodes")).isEqualTo(3);
            assertThat(stats.get("totalEdges")).isEqualTo(3);
            assertThat(stats.get("maxDepth")).isEqualTo(2);
        }

        @Test
        @DisplayName("graph of isolated plugin has depth zero")
        void isolatedGraph() {
            Map<String, Object> graph = resolver.getDependencyGraph("lonely");
            @SuppressWarnings("unchecked")
            Map<String, Object> stats = (Map<String, Object>) graph.get("statistics");
            assertThat(stats.get("maxDepth")).isEqualTo(0);
            assertThat(stats.get("totalEdges")).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("PluginDependencyImpl internals")
    class DependencyImplTests {

        @Test
        @DisplayName("minimum and maximum version derivation")
        void versionDerivation() {
            assertThat(resolver.addDependency("p", "d", ">=1.2.3", DependencyLevel.REQUIRED)
                    .getMinimumVersion()).isEqualTo("1.2.3");
            assertThat(resolver.addDependency("p", "d", "<=4.5.6", DependencyLevel.REQUIRED)
                    .getMaximumVersion()).isEqualTo("4.5.6");
            assertThat(resolver.addDependency("p", "d", "1.0.0-2.0.0", DependencyLevel.REQUIRED)
                    .getMinimumVersion()).isEqualTo("1.0.0");
            assertThat(resolver.addDependency("p", "d", "1.0.0-2.0.0", DependencyLevel.REQUIRED)
                    .getMaximumVersion()).isEqualTo("2.0.0");
            assertThat(resolver.addDependency("p", "d", "3.0.0", DependencyLevel.REQUIRED)
                    .getMinimumVersion()).isEqualTo("3.0.0");
            assertThat(resolver.addDependency("p", "d", "3.0.0", DependencyLevel.REQUIRED)
                    .getMaximumVersion()).isEqualTo("3.0.0");
        }

        @Test
        @DisplayName("null requirement yields null minimum and maximum")
        void nullRequirement() {
            PluginDependency dep = resolver.addDependency("p", "d", null, DependencyLevel.REQUIRED);
            assertThat(dep.getMinimumVersion()).isNull();
            assertThat(dep.getMaximumVersion()).isNull();
        }

        @Test
        @DisplayName("copy preserves all fields including resolved state")
        void copyBehaviour() throws Exception {
            PluginDependency dep = resolver.addDependency(
                    "p", "d", ">=1.0", DependencyLevel.REQUIRED);

            Method setResolved = dep.getClass().getDeclaredMethod("setResolved", boolean.class);
            setResolved.setAccessible(true);
            setResolved.invoke(dep, true);
            assertThat(dep.isResolved()).isTrue();

            Method copy = dep.getClass().getDeclaredMethod("copy");
            copy.setAccessible(true);
            PluginDependency copied = (PluginDependency) copy.invoke(dep);

            assertThat(copied).isNotSameAs(dep);
            assertThat(copied.getId()).isEqualTo("p->d");
            assertThat(copied.getPluginId()).isEqualTo("p");
            assertThat(copied.getProviderId()).isEqualTo("d");
            assertThat(copied.getRequiredVersion()).isEqualTo(">=1.0");
            assertThat(copied.getLevel()).isEqualTo(DependencyLevel.REQUIRED);
            assertThat(copied.isResolved()).isTrue();
        }

        @Test
        @DisplayName("toString describes the dependency")
        void toStringFormat() {
            PluginDependency dep = resolver.addDependency(
                    "p", "d", "1.0", DependencyLevel.REQUIRED);
            assertThat(dep.toString()).isEqualTo("Dependency{p -> d 1.0 (REQUIRED)}");
        }
    }
}
