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
    private static final String PLUGIN_A = "pluginA";
    private static final String PLUGIN_B = "pluginB";
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

    @Test
    void testRegisterResourceSuccess() {
        boolean registered = resourceManager.registerResource(PLUGIN_A, "res1", "Resource 1", "value1");
        assertTrue(registered);
        PluginResourceManager.Resource resource = resourceManager.getResource(PLUGIN_A + ":res1");
        assertNotNull(resource);
        assertEquals("Resource 1", resource.getName());
        assertEquals("value1", resource.getValue());
        assertEquals("generic", resource.getResourceType());
    }

    @Test
    void testRegisterResourceWithMetadata() {
        Map<String, Object> metadata = Map.of("key", "value");
        boolean registered = resourceManager.registerResourceWithMetadata(
                PLUGIN_A, "res2", "Resource 2", "desc", "typeX", "value2", metadata);
        assertTrue(registered);
        PluginResourceManager.Resource resource = resourceManager.getResource(PLUGIN_A + ":res2");
        assertNotNull(resource);
        assertEquals("desc", resource.getDescription());
        assertEquals("typeX", resource.getResourceType());
        assertEquals("value2", resource.getValue());
        assertEquals("value", resource.getMetadata().get("key"));
    }

    @Test
    void testRegisterResourceDuplicate() {
        resourceManager.registerResource(PLUGIN_A, "res1", "Resource 1", "value1");
        boolean duplicate = resourceManager.registerResource(PLUGIN_A, "res1", "Resource 1 duplicate", "value2");
        assertFalse(duplicate);
    }

    @Test
    void testUnregisterResourceSuccess() {
        resourceManager.registerResource(PLUGIN_A, "res1", "Resource 1", "value1");
        boolean unregistered = resourceManager.unregisterResource(PLUGIN_A, "res1");
        assertTrue(unregistered);
        assertNull(resourceManager.getResource(PLUGIN_A + ":res1"));
    }

    @Test
    void testUnregisterResourceNotFound() {
        boolean unregistered = resourceManager.unregisterResource(PLUGIN_A, "unknown");
        assertFalse(unregistered);
    }

    @Test
    void testGetResource() {
        resourceManager.registerResource(PLUGIN_A, "res1", "Resource 1", "value1");
        PluginResourceManager.Resource resource = resourceManager.getResource(PLUGIN_A + ":res1");
        assertNotNull(resource);
        assertEquals("value1", resource.getValue());
    }

    @Test
    void testGetResourceValue() {
        resourceManager.registerResource(PLUGIN_A, "res1", "Resource 1", "value1");
        String value = resourceManager.getResourceValue(PLUGIN_A + ":res1", String.class);
        assertEquals("value1", value);
    }

    @Test
    void testGetResourceValueWrongType() {
        resourceManager.registerResource(PLUGIN_A, "res1", "Resource 1", "value1");
        Integer value = resourceManager.getResourceValue(PLUGIN_A + ":res1", Integer.class);
        assertNull(value);
    }

    @Test
    void testGetResourceValueNotFound() {
        String value = resourceManager.getResourceValue("unknown", String.class);
        assertNull(value);
    }

    @Test
    void testGetPluginResources() {
        resourceManager.registerResource(PLUGIN_A, "res1", "Resource 1", "value1");
        resourceManager.registerResource(PLUGIN_A, "res2", "Resource 2", "value2");
        resourceManager.registerResource(PLUGIN_B, "res3", "Resource 3", "value3");

        List<PluginResourceManager.Resource> resources = resourceManager.getPluginResources(PLUGIN_A);
        assertEquals(2, resources.size());
        List<String> names = resources.stream().map(PluginResourceManager.Resource::getName).toList();
        assertTrue(names.contains("Resource 1"));
        assertTrue(names.contains("Resource 2"));
    }

    @Test
    void testGetPluginResourcesEmpty() {
        List<PluginResourceManager.Resource> resources = resourceManager.getPluginResources("unknown");
        assertTrue(resources.isEmpty());
    }

    @Test
    void testGetAllResources() {
        resourceManager.registerResource(PLUGIN_A, "res1", "Resource 1", "value1");
        resourceManager.registerResource(PLUGIN_B, "res2", "Resource 2", "value2");
        List<PluginResourceManager.Resource> all = resourceManager.getAllResources();
        assertEquals(2, all.size());
    }

    @Test
    void testGetResourcesByType() {
        resourceManager.registerResourceWithMetadata(PLUGIN_A, "res1", "R1", "", "typeA", "v1", Map.of());
        resourceManager.registerResourceWithMetadata(PLUGIN_A, "res2", "R2", "", "typeA", "v2", Map.of());
        resourceManager.registerResourceWithMetadata(PLUGIN_B, "res3", "R3", "", "typeB", "v3", Map.of());

        List<PluginResourceManager.Resource> typeA = resourceManager.getResourcesByType("typeA");
        assertEquals(2, typeA.size());
        List<PluginResourceManager.Resource> typeB = resourceManager.getResourcesByType("typeB");
        assertEquals(1, typeB.size());
        List<PluginResourceManager.Resource> typeC = resourceManager.getResourcesByType("typeC");
        assertTrue(typeC.isEmpty());
    }

    @Test
    void testGrantResourceAccessSuccess() {
        resourceManager.registerResource(PLUGIN_A, "res1", "Resource 1", "value1");
        boolean granted = resourceManager.grantResourceAccess(PLUGIN_B, PLUGIN_A + ":res1");
        assertTrue(granted);
        assertTrue(resourceManager.hasResourceAccess(PLUGIN_B, PLUGIN_A + ":res1"));
    }

    @Test
    void testGrantResourceAccessAlreadyGranted() {
        resourceManager.registerResource(PLUGIN_A, "res1", "Resource 1", "value1");
        resourceManager.grantResourceAccess(PLUGIN_B, PLUGIN_A + ":res1");
        boolean grantedAgain = resourceManager.grantResourceAccess(PLUGIN_B, PLUGIN_A + ":res1");
        assertFalse(grantedAgain); // car déjà accordé
    }

    @Test
    void testGrantResourceAccessResourceNotFound() {
        boolean granted = resourceManager.grantResourceAccess(PLUGIN_B, "unknown");
        assertFalse(granted);
    }

    @Test
    void testRevokeResourceAccessSuccess() {
        resourceManager.registerResource(PLUGIN_A, "res1", "Resource 1", "value1");
        resourceManager.grantResourceAccess(PLUGIN_B, PLUGIN_A + ":res1");
        boolean revoked = resourceManager.revokeResourceAccess(PLUGIN_B, PLUGIN_A + ":res1");
        assertTrue(revoked);
        assertFalse(resourceManager.hasResourceAccess(PLUGIN_B, PLUGIN_A + ":res1"));
    }

    @Test
    void testRevokeResourceAccessNotGranted() {
        boolean revoked = resourceManager.revokeResourceAccess(PLUGIN_B, PLUGIN_A + ":res1");
        assertFalse(revoked);
    }

    @Test
    void testHasResourceAccessOwner() {
        resourceManager.registerResource(PLUGIN_A, "res1", "Resource 1", "value1");
        assertTrue(resourceManager.hasResourceAccess(PLUGIN_A, PLUGIN_A + ":res1"));
    }

    @Test
    void testHasResourceAccessGranted() {
        resourceManager.registerResource(PLUGIN_A, "res1", "Resource 1", "value1");
        resourceManager.grantResourceAccess(PLUGIN_B, PLUGIN_A + ":res1");
        assertTrue(resourceManager.hasResourceAccess(PLUGIN_B, PLUGIN_A + ":res1"));
    }

    @Test
    void testHasResourceAccessNotGranted() {
        resourceManager.registerResource(PLUGIN_A, "res1", "Resource 1", "value1");
        assertFalse(resourceManager.hasResourceAccess(PLUGIN_B, PLUGIN_A + ":res1"));
    }

    @Test
    void testGetAccessibleResources() {
        resourceManager.registerResource(PLUGIN_A, "res1", "Resource 1", "value1");
        resourceManager.registerResource(PLUGIN_A, "res2", "Resource 2", "value2");
        resourceManager.registerResource(PLUGIN_B, "res3", "Resource 3", "value3");
        resourceManager.grantResourceAccess(PLUGIN_A, PLUGIN_B + ":res3");

        List<PluginResourceManager.Resource> accessible = resourceManager.getAccessibleResources(PLUGIN_A);
        assertEquals(3, accessible.size()); // ses 2 ressources + la res3 accordée
        List<String> ids = accessible.stream().map(PluginResourceManager.Resource::getId).toList();
        assertTrue(ids.contains(PLUGIN_A + ":res1"));
        assertTrue(ids.contains(PLUGIN_A + ":res2"));
        assertTrue(ids.contains(PLUGIN_B + ":res3"));
    }

    @Test
    void testUpdateResourceSuccess() {
        resourceManager.registerResource(PLUGIN_A, "res1", "Resource 1", "oldValue");
        boolean updated = resourceManager.updateResource(PLUGIN_A, "res1", "newValue");
        assertTrue(updated);
        PluginResourceManager.Resource resource = resourceManager.getResource(PLUGIN_A + ":res1");
        assertEquals("newValue", resource.getValue());
    }

    @Test
    void testUpdateResourceNotFound() {
        boolean updated = resourceManager.updateResource(PLUGIN_A, "unknown", "newValue");
        assertFalse(updated);
    }

    @Test
    void testGetAccessAuditLog() {
        resourceManager.registerResource(PLUGIN_A, "res1", "Resource 1", "value1");
        resourceManager.grantResourceAccess(PLUGIN_B, PLUGIN_A + ":res1");
        resourceManager.revokeResourceAccess(PLUGIN_B, PLUGIN_A + ":res1");
        resourceManager.updateResource(PLUGIN_A, "res1", "newValue");

        List<Map<String, Object>> logs = resourceManager.getAccessAuditLog(PLUGIN_A + ":res1");
        // Il y a 3 entrées : GRANT_ACCESS, REVOKE_ACCESS, UPDATE_RESOURCE
        assertEquals(3, logs.size());
        // Vérification des actions
        List<String> actions = logs.stream()
                .map(m -> (String) m.get("action"))
                .toList();
        assertTrue(actions.contains("GRANT_ACCESS"));
        assertTrue(actions.contains("REVOKE_ACCESS"));
        assertTrue(actions.contains("UPDATE_RESOURCE"));
    }

    @Test
    void testGetAccessAuditLogEmpty() {
        List<Map<String, Object>> logs = resourceManager.getAccessAuditLog("unknown");
        assertTrue(logs.isEmpty());
    }

    @Test
    void testAuditLogLimit() {
        // On ajoute plus de 1000 entrées pour tester la limite
        // On simule en ajoutant des entrées directement via addAuditEntry (privé)
        // On ne peut pas appeler directement, donc on utilise des opérations qui génèrent des audits
        for (int i = 0; i < 1005; i++) {
            String resId = "res" + i;
            resourceManager.registerResource(PLUGIN_A, resId, "R" + i, "v" + i);
            resourceManager.grantResourceAccess(PLUGIN_B, PLUGIN_A + ":" + resId);
        }

        // On vérifie que l'auditLog a été réduit à 1000 (ou moins)
        // On peut inspecter via getAccessAuditLog pour un resourceId spécifique, mais cela ne vérifie pas la taille globale.
        // On utilise la réflexion pour accéder à auditLog, mais c'est privé.
        // On peut vérifier que les statistiques indiquent une taille <= 1000
        Map<String, Object> stats = resourceManager.getStatistics();
        int auditSize = (int) stats.get("auditLogSize");
        assertTrue(auditSize <= 1000, "auditLog size should be capped at 1000, but was " + auditSize);
    }

    // ========== TESTS POUR ResourceImpl.toString() ==========
    @Test
    void testResourceImplToString() {
        // Créer un resource via le manager
        resourceManager.registerResource(PLUGIN_A, "res1", "MyResource", "myValue");
        PluginResourceManager.Resource resource = resourceManager.getResource(PLUGIN_A + ":res1");
        // Le resource est un ResourceImpl, on peut appeler toString()
        String str = resource.toString();
        // Vérifier qu'il contient les informations attendues
        assertTrue(str.contains("pluginA:res1"));
        assertTrue(str.contains("generic"));
        assertTrue(str.contains("MyResource"));
        // La méthode toString est définie dans ResourceImpl, donc on la teste directement.
    }
}