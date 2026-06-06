package com.protonmail.landrevillejf.ide.plugin.service.impl;

import com.protonmail.landrevillejf.ide.plugin.service.PluginResourceManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DefaultPluginResourceManagerTest {

    private DefaultPluginResourceManager resourceManager;
    private static final String TEST_PLUGIN = "test-plugin";
    private static final String TEST_PLUGIN_2 = "test-plugin-2";
    private static final String RESOURCE_ID = "test-resource";
    private static final String FULL_RESOURCE_ID = TEST_PLUGIN + ":" + RESOURCE_ID;

    @BeforeEach
    void setUp() {
        resourceManager = new DefaultPluginResourceManager();
    }

    @Test
    void registerResource() {
        boolean registered = resourceManager.registerResource(
                TEST_PLUGIN, RESOURCE_ID, "Test Resource", "Test Value"
        );

        assertTrue(registered);

        PluginResourceManager.Resource resource = resourceManager.getResource(FULL_RESOURCE_ID);
        assertNotNull(resource);
        assertEquals("Test Resource", resource.getName());
        assertEquals("Test Value", resource.getValue());
        assertEquals("generic", resource.getResourceType());
    }

    @Test
    void registerDuplicateResource() {
        resourceManager.registerResource(TEST_PLUGIN, RESOURCE_ID, "First", "Value 1");
        boolean registered = resourceManager.registerResource(TEST_PLUGIN, RESOURCE_ID, "Second", "Value 2");

        assertFalse(registered);
    }

    @Test
    void registerResourceWithMetadata() {
        Map<String, Object> metadata = Map.of("version", "1.0", "author", "Test Author");

        boolean registered = resourceManager.registerResourceWithMetadata(
                TEST_PLUGIN, RESOURCE_ID, "Test Resource", "Test Description",
                "service", "Test Value", metadata
        );

        assertTrue(registered);

        PluginResourceManager.Resource resource = resourceManager.getResource(FULL_RESOURCE_ID);
        assertNotNull(resource);
        assertEquals("Test Description", resource.getDescription());
        assertEquals("service", resource.getResourceType());
        assertEquals("1.0", resource.getMetadata().get("version"));
        assertEquals("Test Author", resource.getMetadata().get("author"));
    }

    @Test
    void unregisterResource() {
        resourceManager.registerResource(TEST_PLUGIN, RESOURCE_ID, "Test", "Value");
        assertNotNull(resourceManager.getResource(FULL_RESOURCE_ID));

        boolean unregistered = resourceManager.unregisterResource(TEST_PLUGIN, RESOURCE_ID);

        assertTrue(unregistered);
        assertNull(resourceManager.getResource(FULL_RESOURCE_ID));
    }

    @Test
    void unregisterNonExistentResource() {
        boolean unregistered = resourceManager.unregisterResource(TEST_PLUGIN, "non-existent");

        assertFalse(unregistered);
    }

    @Test
    void getResource() {
        resourceManager.registerResource(TEST_PLUGIN, RESOURCE_ID, "Test Resource", "Test Value");

        PluginResourceManager.Resource resource = resourceManager.getResource(FULL_RESOURCE_ID);

        assertNotNull(resource);
        assertEquals(RESOURCE_ID, resource.getId().substring(resource.getId().indexOf(":") + 1));
    }

    @Test
    void getResourceValue() {
        Map<String, String> complexValue = Map.of("key1", "value1", "key2", "value2");
        resourceManager.registerResource(TEST_PLUGIN, RESOURCE_ID, "Complex", complexValue);

        @SuppressWarnings("unchecked")
        Map<String, String> retrieved = resourceManager.getResourceValue(FULL_RESOURCE_ID, Map.class);

        assertNotNull(retrieved);
        assertEquals("value1", retrieved.get("key1"));
        assertEquals("value2", retrieved.get("key2"));
    }

    @Test
    void getResourceValueWithWrongType() {
        resourceManager.registerResource(TEST_PLUGIN, RESOURCE_ID, "Test", "String Value");

        Integer retrieved = resourceManager.getResourceValue(FULL_RESOURCE_ID, Integer.class);

        assertNull(retrieved);
    }

    @Test
    void getPluginResources() {
        resourceManager.registerResource(TEST_PLUGIN, "res1", "Resource 1", "Value 1");
        resourceManager.registerResource(TEST_PLUGIN, "res2", "Resource 2", "Value 2");
        resourceManager.registerResource(TEST_PLUGIN_2, "res3", "Resource 3", "Value 3");

        List<PluginResourceManager.Resource> pluginResources =
                resourceManager.getPluginResources(TEST_PLUGIN);

        assertEquals(2, pluginResources.size());
    }

    @Test
    void getAllResources() {
        resourceManager.registerResource(TEST_PLUGIN, "res1", "Resource 1", "Value 1");
        resourceManager.registerResource(TEST_PLUGIN_2, "res2", "Resource 2", "Value 2");

        List<PluginResourceManager.Resource> allResources = resourceManager.getAllResources();

        assertEquals(2, allResources.size());
    }

    @Test
    void getResourcesByType() {
        resourceManager.registerResourceWithMetadata(
                TEST_PLUGIN, "service1", "Service 1", "", "service", "Value 1", Map.of()
        );
        resourceManager.registerResourceWithMetadata(
                TEST_PLUGIN, "service2", "Service 2", "", "service", "Value 2", Map.of()
        );
        resourceManager.registerResourceWithMetadata(
                TEST_PLUGIN, "config1", "Config 1", "", "config", "Value 3", Map.of()
        );

        List<PluginResourceManager.Resource> serviceResources =
                resourceManager.getResourcesByType("service");

        assertEquals(2, serviceResources.size());
    }

    @Test
    void grantResourceAccess() {
        resourceManager.registerResource(TEST_PLUGIN, RESOURCE_ID, "Test", "Value");

        boolean granted = resourceManager.grantResourceAccess(TEST_PLUGIN_2, FULL_RESOURCE_ID);

        assertTrue(granted);
        assertTrue(resourceManager.hasResourceAccess(TEST_PLUGIN_2, FULL_RESOURCE_ID));
    }

    @Test
    void grantAccessToNonExistentResource() {
        boolean granted = resourceManager.grantResourceAccess(TEST_PLUGIN_2, "non-existent");

        assertFalse(granted);
    }

    @Test
    void revokeResourceAccess() {
        resourceManager.registerResource(TEST_PLUGIN, RESOURCE_ID, "Test", "Value");
        resourceManager.grantResourceAccess(TEST_PLUGIN_2, FULL_RESOURCE_ID);

        assertTrue(resourceManager.hasResourceAccess(TEST_PLUGIN_2, FULL_RESOURCE_ID));

        boolean revoked = resourceManager.revokeResourceAccess(TEST_PLUGIN_2, FULL_RESOURCE_ID);

        assertTrue(revoked);
        assertFalse(resourceManager.hasResourceAccess(TEST_PLUGIN_2, FULL_RESOURCE_ID));
    }

    @Test
    void hasResourceAccess() {
        resourceManager.registerResource(TEST_PLUGIN, RESOURCE_ID, "Test", "Value");

        // Provider always has access
        assertTrue(resourceManager.hasResourceAccess(TEST_PLUGIN, FULL_RESOURCE_ID));

        // Other plugin does not have access
        assertFalse(resourceManager.hasResourceAccess(TEST_PLUGIN_2, FULL_RESOURCE_ID));

        // Grant access
        resourceManager.grantResourceAccess(TEST_PLUGIN_2, FULL_RESOURCE_ID);
        assertTrue(resourceManager.hasResourceAccess(TEST_PLUGIN_2, FULL_RESOURCE_ID));
    }

    @Test
    void getAccessibleResources() {
        resourceManager.registerResource(TEST_PLUGIN, "own1", "Own 1", "Value 1");
        resourceManager.registerResource(TEST_PLUGIN, "own2", "Own 2", "Value 2");

        resourceManager.registerResource(TEST_PLUGIN_2, "other", "Other", "Value 3");
        resourceManager.grantResourceAccess(TEST_PLUGIN, TEST_PLUGIN_2 + ":other");

        List<PluginResourceManager.Resource> accessible =
                resourceManager.getAccessibleResources(TEST_PLUGIN);

        assertEquals(3, accessible.size());
    }

    @Test
    void updateResource() {
        resourceManager.registerResource(TEST_PLUGIN, RESOURCE_ID, "Original", "Original Value");

        boolean updated = resourceManager.updateResource(TEST_PLUGIN, RESOURCE_ID, "Updated Value");

        assertTrue(updated);

        PluginResourceManager.Resource resource = resourceManager.getResource(FULL_RESOURCE_ID);
        assertEquals("Updated Value", resource.getValue());
    }

    @Test
    void updateResourceByNonProvider() {
        resourceManager.registerResource(TEST_PLUGIN, RESOURCE_ID, "Original", "Original Value");

        boolean updated = resourceManager.updateResource(TEST_PLUGIN_2, RESOURCE_ID, "Hacked Value");

        assertFalse(updated);

        PluginResourceManager.Resource resource = resourceManager.getResource(FULL_RESOURCE_ID);
        assertEquals("Original Value", resource.getValue());
    }

    @Test
    void updateNonExistentResource() {
        boolean updated = resourceManager.updateResource(TEST_PLUGIN, "non-existent", "Value");

        assertFalse(updated);
    }

    @Test
    void getAccessAuditLog() {
        resourceManager.registerResource(TEST_PLUGIN, RESOURCE_ID, "Test", "Value");

        resourceManager.grantResourceAccess(TEST_PLUGIN_2, FULL_RESOURCE_ID);
        resourceManager.revokeResourceAccess(TEST_PLUGIN_2, FULL_RESOURCE_ID);
        resourceManager.updateResource(TEST_PLUGIN, RESOURCE_ID, "Updated");

        List<Map<String, Object>> auditLog = resourceManager.getAccessAuditLog(FULL_RESOURCE_ID);

        assertFalse(auditLog.isEmpty());
        assertTrue(auditLog.size() >= 3);
    }

    @Test
    void getStatistics() {
        resourceManager.registerResource(TEST_PLUGIN, "res1", "Resource 1", "Value 1");
        resourceManager.registerResource(TEST_PLUGIN, "res2", "Resource 2", "Value 2");
        resourceManager.registerResource(TEST_PLUGIN_2, "res3", "Resource 3", "Value 3");

        resourceManager.registerResourceWithMetadata(
                TEST_PLUGIN, "service1", "Service 1", "", "service", "Value", Map.of()
        );
        resourceManager.registerResourceWithMetadata(
                TEST_PLUGIN, "config1", "Config 1", "", "config", "Value", Map.of()
        );

        resourceManager.grantResourceAccess(TEST_PLUGIN_2, TEST_PLUGIN + ":res1");

        Map<String, Object> stats = resourceManager.getStatistics();

        assertNotNull(stats);
        assertTrue(stats.containsKey("totalResources"));
        assertTrue(stats.containsKey("totalProviders"));
        assertTrue(stats.containsKey("totalResourceTypes"));
        assertTrue(stats.containsKey("totalAccessGrants"));
        assertTrue(stats.containsKey("resourcesByType"));
        assertTrue(stats.containsKey("resourcesByProvider"));

        assertEquals(5, stats.get("totalResources"));
        assertEquals(2, stats.get("totalProviders"));
        assertEquals(1, stats.get("totalAccessGrants"));
    }

    @Test
    void multipleResourcesSameType() {
        resourceManager.registerResourceWithMetadata(
                TEST_PLUGIN, "service1", "Service 1", "", "service", "Value 1", Map.of()
        );
        resourceManager.registerResourceWithMetadata(
                TEST_PLUGIN, "service2", "Service 2", "", "service", "Value 2", Map.of()
        );
        resourceManager.registerResourceWithMetadata(
                TEST_PLUGIN, "service3", "Service 3", "", "service", "Value 3", Map.of()
        );

        List<PluginResourceManager.Resource> serviceResources =
                resourceManager.getResourcesByType("service");

        assertEquals(3, serviceResources.size());
    }

    @Test
    void resourceMetadataIsImmutable() {
        Map<String, Object> metadata = new java.util.HashMap<>();
        metadata.put("key", "original");

        resourceManager.registerResourceWithMetadata(
                TEST_PLUGIN, RESOURCE_ID, "Test", "", "type", "value", metadata
        );

        // Modify original map - shouldn't affect stored metadata
        metadata.put("key", "modified");

        PluginResourceManager.Resource resource = resourceManager.getResource(FULL_RESOURCE_ID);
        assertEquals("original", resource.getMetadata().get("key"));
    }
}