package com.protonmail.landrevillejf.swingide.plugin.service;

import java.util.List;
import java.util.Map;

/**
 * Plugin resource management service for sharing and requesting resources between plugins.
 */
public interface PluginResourceManager {

    /**
     * Represents a shareable resource.
     */
    interface Resource {
        String getId();
        String getProviderId();
        String getName();
        String getDescription();
        String getResourceType();
        Object getValue();
        Map<String, Object> getMetadata();
        long getCreatedTime();
    }

    /**
     * Registers a resource provided by a plugin.
     *
     * @param pluginId the providing plugin identifier
     * @param resourceId the resource identifier
     * @param name the resource name
     * @param resource the resource object
     * @return true if registration was successful
     */
    boolean registerResource(String pluginId, String resourceId, String name, Object resource);

    /**
     * Registers a resource with metadata.
     *
     * @param pluginId the providing plugin identifier
     * @param resourceId the resource identifier
     * @param name the resource name
     * @param description the resource description
     * @param resourceType the resource type
     * @param resource the resource object
     * @param metadata optional metadata
     * @return true if registration was successful
     */
    boolean registerResourceWithMetadata(String pluginId, String resourceId, String name,
                                        String description, String resourceType,
                                        Object resource, Map<String, Object> metadata);

    /**
     * Unregisters a resource.
     *
     * @param pluginId the providing plugin identifier
     * @param resourceId the resource identifier
     * @return true if unregistration was successful
     */
    boolean unregisterResource(String pluginId, String resourceId);

    /**
     * Gets a resource by id.
     *
     * @param resourceId the resource identifier
     * @return the resource, or null if not found
     */
    Resource getResource(String resourceId);

    /**
     * Gets a resource value by id with type casting.
     *
     * @param resourceId the resource identifier
     * @param valueClass the expected value class
     * @return the resource value, or null if not found
     */
    <T> T getResourceValue(String resourceId, Class<T> valueClass);

    /**
     * Gets all resources provided by a plugin.
     *
     * @param pluginId the provider plugin identifier
     * @return list of resources
     */
    List<Resource> getPluginResources(String pluginId);

    /**
     * Gets all available resources.
     *
     * @return list of all resources
     */
    List<Resource> getAllResources();

    /**
     * Gets resources filtered by type.
     *
     * @param resourceType the resource type
     * @return list of resources of the specified type
     */
    List<Resource> getResourcesByType(String resourceType);

    /**
     * Grants access to a resource from one plugin to another.
     *
     * @param pluginId the requesting plugin identifier
     * @param resourceId the resource identifier
     * @return true if access was granted
     */
    boolean grantResourceAccess(String pluginId, String resourceId);

    /**
     * Revokes resource access.
     *
     * @param pluginId the plugin identifier
     * @param resourceId the resource identifier
     * @return true if access was revoked
     */
    boolean revokeResourceAccess(String pluginId, String resourceId);

    /**
     * Checks if a plugin has access to a resource.
     *
     * @param pluginId the plugin identifier
     * @param resourceId the resource identifier
     * @return true if the plugin has access
     */
    boolean hasResourceAccess(String pluginId, String resourceId);

    /**
     * Gets all accessible resources for a plugin.
     *
     * @param pluginId the plugin identifier
     * @return list of accessible resources
     */
    List<Resource> getAccessibleResources(String pluginId);

    /**
     * Updates a resource value.
     *
     * @param pluginId the providing plugin identifier
     * @param resourceId the resource identifier
     * @param newValue the new resource value
     * @return true if update was successful
     */
    boolean updateResource(String pluginId, String resourceId, Object newValue);

    /**
     * Gets resource access audit log.
     *
     * @param resourceId the resource identifier
     * @return list of access audit records
     */
    List<Map<String, Object>> getAccessAuditLog(String resourceId);

    /**
     * Gets resource manager statistics.
     *
     * @return a map containing resource statistics
     */
    Map<String, Object> getStatistics();
}

