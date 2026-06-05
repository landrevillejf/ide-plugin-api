package com.protonmail.landrevillejf.ide.plugin.service.impl;

import com.protonmail.landrevillejf.ide.plugin.service.PluginDependencyResolver;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Slf4j
public class DefaultPluginDependencyResolver implements PluginDependencyResolver {

    private final Map<String, List<PluginDependency>> dependencies = new ConcurrentHashMap<>();
    private final Map<String, Map<String, String>> availablePlugins = new ConcurrentHashMap<>();
    private final Map<String, List<String>> dependents = new ConcurrentHashMap<>();

    public DefaultPluginDependencyResolver() {
        log.info("DefaultPluginDependencyResolver initialized");
    }

    @Override
    public PluginDependency addDependency(String pluginId, String dependencyId,
                                          String requiredVersion, DependencyLevel level) {
        PluginDependencyImpl dependency = new PluginDependencyImpl(
                generateDependencyId(pluginId, dependencyId),
                pluginId,
                dependencyId,
                requiredVersion,
                level
        );

        dependencies.computeIfAbsent(pluginId, k -> new CopyOnWriteArrayList<>()).add(dependency);

        // Update dependents map
        dependents.computeIfAbsent(dependencyId, k -> new CopyOnWriteArrayList<>()).add(pluginId);

        log.debug("Dependency added: plugin={} depends on {} ({})",
                pluginId, dependencyId, level);

        return dependency;
    }

    @Override
    public boolean removeDependency(String pluginId, String dependencyId) {
        List<PluginDependency> pluginDeps = dependencies.get(pluginId);
        if (pluginDeps == null) {
            return false;
        }

        boolean removed = pluginDeps.removeIf(dep -> dep.getProviderId().equals(dependencyId));

        if (removed) {
            // Update dependents map
            List<String> pluginDependents = dependents.get(dependencyId);
            if (pluginDependents != null) {
                pluginDependents.remove(pluginId);
            }
            log.debug("Dependency removed: plugin={} no longer depends on {}", pluginId, dependencyId);
        }

        return removed;
    }

    @Override
    public List<PluginDependency> getDependencies(String pluginId) {
        return new ArrayList<>(dependencies.getOrDefault(pluginId, Collections.emptyList()));
    }

    @Override
    public List<PluginDependency> getRequiredDependencies(String pluginId) {
        return dependencies.getOrDefault(pluginId, Collections.emptyList())
                .stream()
                .filter(dep -> dep.getLevel() == DependencyLevel.REQUIRED)
                .collect(Collectors.toList());
    }

    @Override
    public List<PluginDependency> getOptionalDependencies(String pluginId) {
        return dependencies.getOrDefault(pluginId, Collections.emptyList())
                .stream()
                .filter(dep -> dep.getLevel() == DependencyLevel.OPTIONAL)
                .collect(Collectors.toList());
    }

    @Override
    public List<PluginDependency> getConflictingDependencies(String pluginId) {
        return dependencies.getOrDefault(pluginId, Collections.emptyList())
                .stream()
                .filter(dep -> dep.getLevel() == DependencyLevel.CONFLICTING)
                .collect(Collectors.toList());
    }

    @Override
    public boolean areRequiredDependenciesResolved(String pluginId) {
        List<PluginDependency> required = getRequiredDependencies(pluginId);

        for (PluginDependency dep : required) {
            if (!isDependencyResolved(pluginId, dep.getProviderId())) {
                log.debug("Required dependency not resolved: plugin={} depends on {}",
                        pluginId, dep.getProviderId());
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean isDependencyResolved(String pluginId, String dependencyId) {
        // Check if dependency is available
        Map<String, String> available = availablePlugins.get(dependencyId);
        if (available == null) {
            return false;
        }

        String version = available.get("version");
        if (version == null) {
            return false;
        }

        // Find the dependency definition
        PluginDependency dep = findDependency(pluginId, dependencyId);
        if (dep == null) {
            return false;
        }

        // Check version compatibility
        return isVersionCompatible(version, dep.getRequiredVersion());
    }

    @Override
    public List<String> getResolutionPath(String pluginId) {
        Set<String> visited = new HashSet<>();
        List<String> path = new ArrayList<>();

        try {
            buildResolutionPath(pluginId, visited, path);
        } catch (CircularDependencyException e) {
            log.warn("Circular dependency detected for plugin {}: {}", pluginId, e.getMessage());
            return Collections.emptyList();
        }

        return path;
    }

    @Override
    public List<List<String>> detectCircularDependencies(String pluginId) {
        List<List<String>> cycles = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();
        List<String> path = new ArrayList<>();

        detectCycles(pluginId, visited, recursionStack, path, cycles);

        return cycles;
    }

    @Override
    public List<String> getDependents(String pluginId) {
        return dependents.getOrDefault(pluginId, Collections.emptyList())
                .stream()
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> validateDependencies(String pluginId) {
        Map<String, Object> validation = new LinkedHashMap<>();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Check required dependencies
        List<PluginDependency> required = getRequiredDependencies(pluginId);
        for (PluginDependency dep : required) {
            if (!isDependencyResolved(pluginId, dep.getProviderId())) {
                errors.add(String.format("Required dependency '%s' version %s is missing",
                        dep.getProviderId(), dep.getRequiredVersion()));
            }
        }

        // Check conflicting dependencies
        List<PluginDependency> conflicting = getConflictingDependencies(pluginId);
        for (PluginDependency dep : conflicting) {
            if (isDependencyResolved(pluginId, dep.getProviderId())) {
                warnings.add(String.format("Conflicting dependency '%s' is present", dep.getProviderId()));
            }
        }

        // Check circular dependencies
        List<List<String>> cycles = detectCircularDependencies(pluginId);
        if (!cycles.isEmpty()) {
            for (List<String> cycle : cycles) {
                errors.add("Circular dependency detected: " + String.join(" -> ", cycle));
            }
        }

        validation.put("valid", errors.isEmpty());
        validation.put("errors", errors);
        validation.put("warnings", warnings);
        validation.put("requiredResolved", areRequiredDependenciesResolved(pluginId));
        validation.put("totalDependencies", getDependencies(pluginId).size());
        validation.put("resolvedDependencies", countResolvedDependencies(pluginId));

        return validation;
    }

    @Override
    public Map<String, Object> getDependencyGraph(String pluginId) {
        Map<String, Object> graph = new LinkedHashMap<>();
        Map<String, List<String>> adjacencyList = new LinkedHashMap<>();
        Set<String> allNodes = new HashSet<>();

        buildDependencyGraph(pluginId, adjacencyList, allNodes);

        graph.put("nodes", new ArrayList<>(allNodes));
        graph.put("edges", adjacencyList);
        graph.put("rootNode", pluginId);

        // Add statistics
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalNodes", allNodes.size());
        stats.put("totalEdges", adjacencyList.values().stream().mapToInt(List::size).sum());
        stats.put("maxDepth", calculateMaxDepth(pluginId, adjacencyList));
        graph.put("statistics", stats);

        return graph;
    }

    /**
     * Registers an available plugin for dependency resolution.
     * This would typically be called by the plugin manager when plugins are loaded.
     */
    public void registerAvailablePlugin(String pluginId, String version) {
        Map<String, String> info = new ConcurrentHashMap<>();
        info.put("version", version);
        info.put("status", "available");
        availablePlugins.put(pluginId, info);
        log.debug("Plugin registered as available: {} version {}", pluginId, version);
    }

    /**
     * Unregisters a plugin from availability.
     */
    public void unregisterAvailablePlugin(String pluginId) {
        availablePlugins.remove(pluginId);
        log.debug("Plugin unregistered from available: {}", pluginId);
    }

    private String generateDependencyId(String pluginId, String dependencyId) {
        return pluginId + "->" + dependencyId;
    }

    private PluginDependency findDependency(String pluginId, String dependencyId) {
        List<PluginDependency> deps = dependencies.get(pluginId);
        if (deps == null) {
            return null;
        }

        return deps.stream()
                .filter(dep -> dep.getProviderId().equals(dependencyId))
                .findFirst()
                .orElse(null);
    }

    private boolean isVersionCompatible(String actualVersion, String requiredVersion) {
        if (requiredVersion == null || requiredVersion.isEmpty()) {
            return true;
        }

        // Simple version comparison (supports exact, minimum, range)
        if (requiredVersion.startsWith(">=")) {
            String minVersion = requiredVersion.substring(2);
            return compareVersions(actualVersion, minVersion) >= 0;
        } else if (requiredVersion.startsWith("<=")) {
            String maxVersion = requiredVersion.substring(2);
            return compareVersions(actualVersion, maxVersion) <= 0;
        } else if (requiredVersion.contains("-")) {
            String[] parts = requiredVersion.split("-");
            if (parts.length == 2) {
                return compareVersions(actualVersion, parts[0]) >= 0 &&
                        compareVersions(actualVersion, parts[1]) <= 0;
            }
        } else if (requiredVersion.endsWith("+")) {
            String minVersion = requiredVersion.substring(0, requiredVersion.length() - 1);
            return compareVersions(actualVersion, minVersion) >= 0;
        }

        // Exact match
        return actualVersion.equals(requiredVersion);
    }

    private int compareVersions(String v1, String v2) {
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");

        int maxLength = Math.max(parts1.length, parts2.length);

        for (int i = 0; i < maxLength; i++) {
            int num1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int num2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;

            if (num1 != num2) {
                return num1 - num2;
            }
        }

        return 0;
    }

    private void buildResolutionPath(String pluginId, Set<String> visited, List<String> path)
            throws CircularDependencyException {
        if (visited.contains(pluginId)) {
            throw new CircularDependencyException("Circular dependency detected: " + pluginId);
        }

        visited.add(pluginId);
        path.add(pluginId);

        List<PluginDependency> deps = getRequiredDependencies(pluginId);
        for (PluginDependency dep : deps) {
            if (!isDependencyResolved(pluginId, dep.getProviderId())) {
                continue;
            }
            buildResolutionPath(dep.getProviderId(), visited, path);
        }
    }

    private void detectCycles(String pluginId, Set<String> visited, Set<String> recursionStack,
                              List<String> path, List<List<String>> cycles) {
        if (recursionStack.contains(pluginId)) {
            // Found a cycle
            int startIndex = path.indexOf(pluginId);
            if (startIndex >= 0) {
                List<String> cycle = new ArrayList<>(path.subList(startIndex, path.size()));
                cycle.add(pluginId);
                cycles.add(cycle);
            }
            return;
        }

        if (visited.contains(pluginId)) {
            return;
        }

        visited.add(pluginId);
        recursionStack.add(pluginId);
        path.add(pluginId);

        List<PluginDependency> deps = getDependencies(pluginId);
        for (PluginDependency dep : deps) {
            detectCycles(dep.getProviderId(), visited, recursionStack, path, cycles);
        }

        recursionStack.remove(pluginId);
        path.remove(path.size() - 1);
    }

    private void buildDependencyGraph(String pluginId, Map<String, List<String>> adjacencyList,
                                      Set<String> allNodes) {
        if (allNodes.contains(pluginId)) {
            return;
        }

        allNodes.add(pluginId);
        List<String> neighbors = new ArrayList<>();

        List<PluginDependency> deps = getDependencies(pluginId);
        for (PluginDependency dep : deps) {
            neighbors.add(dep.getProviderId());
            buildDependencyGraph(dep.getProviderId(), adjacencyList, allNodes);
        }

        adjacencyList.put(pluginId, neighbors);
    }

    private int calculateMaxDepth(String pluginId, Map<String, List<String>> adjacencyList) {
        int maxDepth = 0;

        for (String neighbor : adjacencyList.getOrDefault(pluginId, Collections.emptyList())) {
            int depth = 1 + calculateMaxDepth(neighbor, adjacencyList);
            maxDepth = Math.max(maxDepth, depth);
        }

        return maxDepth;
    }

    private int countResolvedDependencies(String pluginId) {
        int resolved = 0;
        List<PluginDependency> deps = getDependencies(pluginId);

        for (PluginDependency dep : deps) {
            if (isDependencyResolved(pluginId, dep.getProviderId())) {
                resolved++;
            }
        }

        return resolved;
    }

    /**
     * Implementation of PluginDependency
     */
    private static class PluginDependencyImpl implements PluginDependency {
        private final String id;
        private final String pluginId;
        private final String providerId;
        private final String requiredVersion;
        private final DependencyLevel level;
        private boolean resolved;

        public PluginDependencyImpl(String id, String pluginId, String providerId,
                                    String requiredVersion, DependencyLevel level) {
            this.id = id;
            this.pluginId = pluginId;
            this.providerId = providerId;
            this.requiredVersion = requiredVersion;
            this.level = level;
            this.resolved = false;
        }

        @Override
        public String getId() { return id; }

        @Override
        public String getProviderId() { return providerId; }

        @Override
        public String getRequiredVersion() { return requiredVersion; }

        @Override
        public String getMinimumVersion() {
            if (requiredVersion == null) return null;
            if (requiredVersion.startsWith(">=")) return requiredVersion.substring(2);
            if (requiredVersion.contains("-")) return requiredVersion.split("-")[0];
            return requiredVersion;
        }

        @Override
        public String getMaximumVersion() {
            if (requiredVersion == null) return null;
            if (requiredVersion.startsWith("<=")) return requiredVersion.substring(2);
            if (requiredVersion.contains("-")) return requiredVersion.split("-")[1];
            return requiredVersion;
        }

        @Override
        public DependencyLevel getLevel() { return level; }

        @Override
        public boolean isResolved() { return resolved; }

        public void setResolved(boolean resolved) { this.resolved = resolved; }

        public PluginDependency copy() {
            PluginDependencyImpl copy = new PluginDependencyImpl(id, pluginId, providerId,
                    requiredVersion, level);
            copy.setResolved(resolved);
            return copy;
        }

        @Override
        public String toString() {
            return String.format("Dependency{%s -> %s %s (%s)}",
                    pluginId, providerId, requiredVersion, level);
        }
    }

    /**
     * Exception for circular dependency detection
     */
    private static class CircularDependencyException extends Exception {
        public CircularDependencyException(String message) {
            super(message);
        }
    }
}