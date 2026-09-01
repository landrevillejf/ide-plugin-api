package com.protonmail.landrevillejf.ide.plugin.service;

import java.util.List;

/**
 * Plugin dependency resolution and management service.
 * <p>
 * Handles dependency registration, resolution, circular dependency detection,
 * and dependency graph computation for plugins.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
public interface PluginDependencyResolver {

    /**
     * Dependency requirement level.
     */
    enum DependencyLevel {
        /** Must be available for plugin to work */
        REQUIRED,
        /** Recommended but optional */
        OPTIONAL,
        /** Conflicts with plugin */
        CONFLICTING
    }

    /**
     * Represents a dependency.
     */
    interface PluginDependency {
        String getId();
        String getPluginId();  // Plugin qui a la dépendance
        String getProviderId(); // Plugin dont on dépend
        String getRequiredVersion();
        String getMinimumVersion();
        String getMaximumVersion();
        DependencyLevel getLevel();
        boolean isResolved();
    }

    /**
     * Registers a dependency for a plugin.
     *
     * @param pluginId the dependent plugin identifier
     * @param dependencyId the dependency identifier
     * @param requiredVersion the required version
     * @param level the dependency level
     * @return the registered dependency
     */
    PluginDependency addDependency(String pluginId, String dependencyId, String requiredVersion, DependencyLevel level);

    /**
     * Removes a dependency.
     *
     * @param pluginId the plugin identifier
     * @param dependencyId the dependency identifier
     * @return true if the dependency was removed
     */
    boolean removeDependency(String pluginId, String dependencyId);

    /**
     * Gets all dependencies of a plugin.
     *
     * @param pluginId the plugin identifier
     * @return list of dependencies
     */
    List<PluginDependency> getDependencies(String pluginId);

    /**
     * Gets required dependencies of a plugin.
     *
     * @param pluginId the plugin identifier
     * @return list of required dependencies
     */
    List<PluginDependency> getRequiredDependencies(String pluginId);

    /**
     * Gets optional dependencies of a plugin.
     *
     * @param pluginId the plugin identifier
     * @return list of optional dependencies
     */
    List<PluginDependency> getOptionalDependencies(String pluginId);

    /**
     * Gets conflicting dependencies of a plugin.
     *
     * @param pluginId the plugin identifier
     * @return list of conflicting dependencies
     */
    List<PluginDependency> getConflictingDependencies(String pluginId);

    /**
     * Checks if all required dependencies are resolved.
     *
     * @param pluginId the plugin identifier
     * @return true if all required dependencies are available
     */
    boolean areRequiredDependenciesResolved(String pluginId);

    /**
     * Checks if a specific dependency is resolved.
     *
     * @param pluginId the plugin identifier
     * @param dependencyId the dependency identifier
     * @return true if the dependency is available and compatible
     */
    boolean isDependencyResolved(String pluginId, String dependencyId);

    /**
     * Gets the resolution path from a plugin to its dependencies (topological order).
     *
     * @param pluginId the plugin identifier
     * @return list of plugin identifiers in dependency resolution order
     */
    List<String> getResolutionPath(String pluginId);

    /**
     * Detects circular dependencies.
     *
     * @param pluginId the plugin identifier
     * @return list of circular dependency chains, or empty if none
     */
    List<List<String>> detectCircularDependencies(String pluginId);

    /**
     * Gets all dependents of a plugin (plugins that depend on this one).
     *
     * @param pluginId the plugin identifier
     * @return list of dependent plugin identifiers
     */
    List<String> getDependents(String pluginId);

    /**
     * Validates dependency compatibility.
     *
     * @param pluginId the plugin identifier
     * @return a validation result with any compatibility issues
     */
    java.util.Map<String, Object> validateDependencies(String pluginId);

    /**
     * Gets dependency information as a graph.
     *
     * @param pluginId the plugin identifier
     * @return map representing the dependency graph
     */
    java.util.Map<String, Object> getDependencyGraph(String pluginId);
}

