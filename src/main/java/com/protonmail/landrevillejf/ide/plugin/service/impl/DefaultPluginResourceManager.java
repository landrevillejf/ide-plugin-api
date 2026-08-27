package com.protonmail.landrevillejf.ide.plugin.service.impl;

import com.protonmail.landrevillejf.ide.plugin.service.PluginResourceManager;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Slf4j
public class DefaultPluginResourceManager implements PluginResourceManager {

    private final Map<String, Resource> resources = new ConcurrentHashMap<>();
    private final Map<String, List<String>> resourcesByProvider = new ConcurrentHashMap<>();
    private final Map<String, List<String>> resourcesByType = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> accessGrants = new ConcurrentHashMap<>();
    private final List<AuditEntry> auditLog = new CopyOnWriteArrayList<>();
    private final AtomicLong auditIdGenerator = new AtomicLong(0);

    public DefaultPluginResourceManager() {
        log.info("DefaultPluginResourceManager initialized");
    }

    @Override
    public boolean registerResource(String pluginId, String resourceId, String name, Object resource) {
        return registerResourceWithMetadata(pluginId, resourceId, name, "", "generic", resource, Collections.emptyMap());
    }

    @Override
    public boolean registerResourceWithMetadata(String pluginId, String resourceId, String name,
                                                String description, String resourceType,
                                                Object resource, Map<String, Object> metadata) {
        String fullResourceId = pluginId + ":" + resourceId;

        if (resources.containsKey(fullResourceId)) {
            log.warn("Resource already registered: plugin={}, resourceId={}", pluginId, resourceId);
            return false;
        }

        ResourceImpl newResource = new ResourceImpl(
                fullResourceId, pluginId, name, description, resourceType,
                resource, metadata, System.currentTimeMillis()
        );

        resources.put(fullResourceId, newResource);

        // Index by provider
        resourcesByProvider.computeIfAbsent(pluginId, k -> new CopyOnWriteArrayList<>())
                .add(fullResourceId);

        // Index by type
        resourcesByType.computeIfAbsent(resourceType, k -> new CopyOnWriteArrayList<>())
                .add(fullResourceId);

        log.debug("Resource registered: plugin={}, resourceId={}, type={}",
                pluginId, resourceId, resourceType);

        return true;
    }

    @Override
    public boolean unregisterResource(String pluginId, String resourceId) {
        String fullResourceId = pluginId + ":" + resourceId;
        Resource removed = resources.remove(fullResourceId);

        if (removed == null) {
            return false;
        }

        // Remove from provider index
        List<String> providerResources = resourcesByProvider.get(pluginId);
        if (providerResources != null) {
            providerResources.remove(fullResourceId);
        }

        // Remove from type index
        List<String> typeResources = resourcesByType.get(removed.getResourceType());
        if (typeResources != null) {
            typeResources.remove(fullResourceId);
        }

        // Remove access grants
        accessGrants.values().forEach(grantSet -> grantSet.remove(fullResourceId));

        log.debug("Resource unregistered: plugin={}, resourceId={}", pluginId, resourceId);
        return true;
    }

    @Override
    public Resource getResource(String resourceId) {
        return resources.get(resourceId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getResourceValue(String resourceId, Class<T> valueClass) {
        Resource resource = resources.get(resourceId);
        if (resource == null) {
            return null;
        }

        Object value = resource.getValue();
        if (value != null && valueClass.isAssignableFrom(value.getClass())) {
            return (T) value;
        }

        return null;
    }

    @Override
    public List<Resource> getPluginResources(String pluginId) {
        List<String> resourceIds = resourcesByProvider.get(pluginId);
        if (resourceIds == null || resourceIds.isEmpty()) {
            return Collections.emptyList();
        }

        return resourceIds.stream()
                .map(resources::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<Resource> getAllResources() {
        return new ArrayList<>(resources.values());
    }

    @Override
    public List<Resource> getResourcesByType(String resourceType) {
        List<String> resourceIds = resourcesByType.get(resourceType);
        if (resourceIds == null || resourceIds.isEmpty()) {
            return Collections.emptyList();
        }

        return resourceIds.stream()
                .map(resources::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public boolean grantResourceAccess(String pluginId, String resourceId) {
        Resource resource = resources.get(resourceId);
        if (resource == null) {
            log.warn("Cannot grant access - resource not found: {}", resourceId);
            return false;
        }

        Set<String> grants = accessGrants.computeIfAbsent(pluginId, k -> ConcurrentHashMap.newKeySet());
        boolean granted = grants.add(resourceId);

        if (granted) {
            addAuditEntry(pluginId, resourceId, "GRANT_ACCESS", true);
            log.debug("Resource access granted: plugin={}, resource={}", pluginId, resourceId);
        }

        return granted;
    }

    @Override
    public boolean revokeResourceAccess(String pluginId, String resourceId) {
        Set<String> grants = accessGrants.get(pluginId);
        if (grants == null) {
            return false;
        }

        boolean revoked = grants.remove(resourceId);

        if (revoked) {
            addAuditEntry(pluginId, resourceId, "REVOKE_ACCESS", true);
            log.debug("Resource access revoked: plugin={}, resource={}", pluginId, resourceId);
        }

        return revoked;
    }

    @Override
    public boolean hasResourceAccess(String pluginId, String resourceId) {
        // Provider always has access
        if (resourceId.startsWith(pluginId + ":")) {
            return true;
        }

        Set<String> grants = accessGrants.get(pluginId);
        return grants != null && grants.contains(resourceId);
    }

    @Override
    public List<Resource> getAccessibleResources(String pluginId) {
        List<Resource> accessible = new ArrayList<>();

        // Add provider's own resources
        accessible.addAll(getPluginResources(pluginId));

        // Add granted resources
        Set<String> grants = accessGrants.get(pluginId);
        if (grants != null) {
            for (String resourceId : grants) {
                Resource resource = resources.get(resourceId);
                if (resource != null) {
                    accessible.add(resource);
                }
            }
        }

        return accessible;
    }

    @Override
    public boolean updateResource(String pluginId, String resourceId, Object newValue) {
        String fullResourceId = pluginId + ":" + resourceId;
        Resource existing = resources.get(fullResourceId);

        if (existing == null) {
            log.warn("Cannot update resource - not found: plugin={}, resourceId={}", pluginId, resourceId);
            return false;
        }

        // The lookup key is pluginId + ":" + resourceId, so a found resource
        // is always owned by pluginId; no separate provider check is needed.
        ResourceImpl updated = new ResourceImpl(
                existing.getId(), existing.getProviderId(), existing.getName(),
                existing.getDescription(), existing.getResourceType(), newValue,
                existing.getMetadata(), existing.getCreatedTime()
        );

        resources.put(fullResourceId, updated);
        addAuditEntry(pluginId, fullResourceId, "UPDATE_RESOURCE", true);

        log.debug("Resource updated: plugin={}, resourceId={}", pluginId, resourceId);
        return true;
    }

    @Override
    public List<Map<String, Object>> getAccessAuditLog(String resourceId) {
        return auditLog.stream()
                .filter(entry -> entry.resourceId.equals(resourceId))
                .map(entry -> {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put("timestamp", entry.timestamp);
                    map.put("pluginId", entry.pluginId);
                    map.put("resourceId", entry.resourceId);
                    map.put("action", entry.action);
                    map.put("success", entry.success);
                    return map;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalResources", resources.size());
        stats.put("totalProviders", resourcesByProvider.size());
        stats.put("totalResourceTypes", resourcesByType.size());
        stats.put("totalAccessGrants", accessGrants.values().stream().mapToInt(Set::size).sum());
        stats.put("auditLogSize", auditLog.size());

        // Resources by type statistics
        Map<String, Integer> byType = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : resourcesByType.entrySet()) {
            byType.put(entry.getKey(), entry.getValue().size());
        }
        stats.put("resourcesByType", byType);

        // Resources by provider statistics
        Map<String, Integer> byProvider = new LinkedHashMap<>();
        for (Map.Entry<String, List<String>> entry : resourcesByProvider.entrySet()) {
            byProvider.put(entry.getKey(), entry.getValue().size());
        }
        stats.put("resourcesByProvider", byProvider);

        return stats;
    }

    private void addAuditEntry(String pluginId, String resourceId, String action, boolean success) {
        AuditEntry entry = new AuditEntry(
                auditIdGenerator.incrementAndGet(),
                System.currentTimeMillis(),
                pluginId,
                resourceId,
                action,
                success
        );
        auditLog.add(entry);

        // Keep only last 1000 audit entries
        if (auditLog.size() > 1000) {
            auditLog.remove(0);
        }
    }

    /**
     * Implementation of Resource interface
     */
    private static class ResourceImpl implements Resource {
        private final String id;
        private final String providerId;
        private final String name;
        private final String description;
        private final String resourceType;
        private final Object value;
        private final Map<String, Object> metadata;
        private final long createdTime;

        public ResourceImpl(String id, String providerId, String name, String description,
                            String resourceType, Object value, Map<String, Object> metadata, long createdTime) {
            this.id = id;
            this.providerId = providerId;
            this.name = name;
            this.description = description;
            this.resourceType = resourceType;
            this.value = value;
            this.metadata = metadata != null ? new ConcurrentHashMap<>(metadata) : new ConcurrentHashMap<>();
            this.createdTime = createdTime;
        }

        @Override
        public String getId() { return id; }

        @Override
        public String getProviderId() { return providerId; }

        @Override
        public String getName() { return name; }

        @Override
        public String getDescription() { return description; }

        @Override
        public String getResourceType() { return resourceType; }

        @Override
        public Object getValue() { return value; }

        @Override
        public Map<String, Object> getMetadata() { return metadata; }

        @Override
        public long getCreatedTime() { return createdTime; }

        @Override
        public String toString() {
            return String.format("Resource{id='%s', provider='%s', type='%s', name='%s'}",
                    id, providerId, resourceType, name);
        }
    }

    /**
     * Audit entry for access logging
     */
    private static class AuditEntry {
        final long id;
        final long timestamp;
        final String pluginId;
        final String resourceId;
        final String action;
        final boolean success;

        AuditEntry(long id, long timestamp, String pluginId, String resourceId, String action, boolean success) {
            this.id = id;
            this.timestamp = timestamp;
            this.pluginId = pluginId;
            this.resourceId = resourceId;
            this.action = action;
            this.success = success;
        }
    }
}