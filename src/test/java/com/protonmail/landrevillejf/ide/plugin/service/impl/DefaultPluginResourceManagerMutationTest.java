package com.protonmail.landrevillejf.ide.plugin.service.impl;

import com.protonmail.landrevillejf.ide.plugin.service.PluginResourceManager.Resource;
import com.protonmail.landrevillejf.ide.plugin.utils.LogCapture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Mutation-killing tests for {@link DefaultPluginResourceManager}.
 */
@DisplayName("DefaultPluginResourceManager mutation tests")
class DefaultPluginResourceManagerMutationTest {

    private static final String PROVIDER = "res-provider";
    private static final String CONSUMER = "res-consumer";

    private DefaultPluginResourceManager manager;

    @BeforeEach
    void setUp() {
        manager = new DefaultPluginResourceManager();
    }

    @Test
    @DisplayName("constructor logs initialization")
    void constructorLogs() {
        try (LogCapture capture = LogCapture.attach(DefaultPluginResourceManager.class)) {
            new DefaultPluginResourceManager();
            assertThat(capture.formattedMessages())
                    .anyMatch(m -> m.contains("DefaultPluginResourceManager initialized"));
        }
    }

    @Nested
    @DisplayName("registration")
    class RegistrationTests {

        @Test
        @DisplayName("registerResource stores the resource with full metadata")
        void registerResource() {
            try (LogCapture capture = LogCapture.attach(DefaultPluginResourceManager.class)) {
                assertThat(manager.registerResource(PROVIDER, "r1", "Resource One", "the-value"))
                        .isTrue();
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("Resource registered"));
            }

            Resource resource = manager.getResource("res-provider:r1");
            assertThat(resource).isNotNull();
            assertThat(resource.getId()).isEqualTo("res-provider:r1");
            assertThat(resource.getProviderId()).isEqualTo(PROVIDER);
            assertThat(resource.getName()).isEqualTo("Resource One");
            assertThat(resource.getDescription()).isEmpty();
            assertThat(resource.getResourceType()).isEqualTo("generic");
            assertThat(resource.getValue()).isEqualTo("the-value");
            assertThat(resource.getMetadata()).isEmpty();
            assertThat(resource.getCreatedTime()).isPositive();
        }

        @Test
        @DisplayName("registerResourceWithMetadata keeps description, type and metadata")
        void registerWithMetadata() {
            boolean registered = manager.registerResourceWithMetadata(
                    PROVIDER, "r2", "Named", "desc", "datasource", 42, Map.of("k", "v"));
            assertThat(registered).isTrue();

            Resource resource = manager.getResource("res-provider:r2");
            assertThat(resource.getDescription()).isEqualTo("desc");
            assertThat(resource.getResourceType()).isEqualTo("datasource");
            assertThat(resource.getValue()).isEqualTo(42);
            assertThat(resource.getMetadata()).containsEntry("k", "v");
        }

        @Test
        @DisplayName("null metadata becomes an empty map")
        void nullMetadata() {
            assertThat(manager.registerResourceWithMetadata(
                    PROVIDER, "r3", "Named", "desc", "generic", "v", null)).isTrue();
            assertThat(manager.getResource("res-provider:r3").getMetadata()).isEmpty();
        }

        @Test
        @DisplayName("duplicate registration is rejected with a warning")
        void duplicateRegistration() {
            manager.registerResource(PROVIDER, "dup", "First", 1);

            try (LogCapture capture = LogCapture.attach(DefaultPluginResourceManager.class)) {
                assertThat(manager.registerResource(PROVIDER, "dup", "Second", 2)).isFalse();
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("Resource already registered"));
            }
            assertThat(manager.getResource("res-provider:dup").getValue()).isEqualTo(1);
        }

        @Test
        @DisplayName("unregisterResource removes indexes and grants")
        void unregisterResource() {
            manager.registerResource(PROVIDER, "r1", "One", 1);
            manager.registerResource(PROVIDER, "r2", "Two", 2);
            manager.grantResourceAccess(CONSUMER, "res-provider:r1");

            try (LogCapture capture = LogCapture.attach(DefaultPluginResourceManager.class)) {
                assertThat(manager.unregisterResource(PROVIDER, "r1")).isTrue();
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("Resource unregistered"));
            }

            assertThat(manager.getResource("res-provider:r1")).isNull();
            assertThat(manager.getPluginResources(PROVIDER))
                    .extracting(Resource::getId).containsExactly("res-provider:r2");
            assertThat(manager.getResourcesByType("generic"))
                    .extracting(Resource::getId).containsExactly("res-provider:r2");
            assertThat(manager.hasResourceAccess(CONSUMER, "res-provider:r1")).isFalse();

            Map<String, Object> stats = manager.getStatistics();
            assertThat(stats).containsEntry("totalResources", 1);
            @SuppressWarnings("unchecked")
            Map<String, Object> byProvider =
                    (Map<String, Object>) stats.get("resourcesByProvider");
            assertThat(byProvider).containsEntry(PROVIDER, 1);

            assertThat(manager.unregisterResource(PROVIDER, "r1")).isFalse();
            assertThat(manager.unregisterResource("ghost", "r1")).isFalse();
        }
    }

    @Nested
    @DisplayName("queries")
    class QueryTests {

        @Test
        @DisplayName("getResourceValue filters by class")
        void resourceValue() {
            manager.registerResource(PROVIDER, "num", "Num", 42);
            manager.registerResourceWithMetadata(
                    PROVIDER, "nil", "Nil", "", "generic", null, null);

            assertThat(manager.getResourceValue("res-provider:num", Integer.class)).isEqualTo(42);
            assertThat(manager.getResourceValue("res-provider:num", String.class)).isNull();
            assertThat(manager.getResourceValue("res-provider:nil", Object.class)).isNull();
            assertThat(manager.getResourceValue("res-provider:ghost", Object.class)).isNull();
        }

        @Test
        @DisplayName("plugin and type queries return exact matches")
        void pluginAndTypeQueries() {
            manager.registerResourceWithMetadata(
                    PROVIDER, "a", "A", "", "type-x", 1, null);
            manager.registerResourceWithMetadata(
                    PROVIDER, "b", "B", "", "type-y", 2, null);

            assertThat(manager.getPluginResources(PROVIDER)).hasSize(2);
            assertThat(manager.getResourcesByType("type-x"))
                    .extracting(Resource::getId).containsExactly("res-provider:a");
            assertThat(manager.getAllResources()).hasSize(2);

            assertThat(manager.getPluginResources("ghost")).isEmpty();
            assertThat(manager.getResourcesByType("ghost")).isEmpty();
        }

        @Test
        @DisplayName("emptied indexes yield empty results")
        void emptiedIndexes() throws Exception {
            manager.registerResource(PROVIDER, "gone", "Gone", 1);
            manager.unregisterResource(PROVIDER, "gone");

            assertThat(manager.getPluginResources(PROVIDER)).isEmpty();
            assertThat(manager.getResourcesByType("generic")).isEmpty();
        }

        @Test
        @DisplayName("stale index entries are filtered out")
        @SuppressWarnings("unchecked")
        void staleIndexEntries() throws Exception {
            manager.registerResource(PROVIDER, "live", "Live", 1);

            Field providerField = DefaultPluginResourceManager.class
                    .getDeclaredField("resourcesByProvider");
            providerField.setAccessible(true);
            ((Map<String, List<String>>) providerField.get(manager))
                    .get(PROVIDER).add("res-provider:stale");

            Field typeField = DefaultPluginResourceManager.class
                    .getDeclaredField("resourcesByType");
            typeField.setAccessible(true);
            ((Map<String, List<String>>) typeField.get(manager))
                    .get("generic").add("res-provider:stale");

            assertThat(manager.getPluginResources(PROVIDER))
                    .extracting(Resource::getId).containsExactly("res-provider:live");
            assertThat(manager.getResourcesByType("generic"))
                    .extracting(Resource::getId).containsExactly("res-provider:live");
        }
    }

    @Nested
    @DisplayName("access control")
    class AccessTests {

        @Test
        @DisplayName("grant and revoke are idempotent")
        void grantRevoke() {
            manager.registerResource(PROVIDER, "r1", "One", 1);

            try (LogCapture capture = LogCapture.attach(DefaultPluginResourceManager.class)) {
                assertThat(manager.grantResourceAccess(CONSUMER, "res-provider:r1")).isTrue();
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("Resource access granted"));
            }
            assertThat(manager.grantResourceAccess(CONSUMER, "res-provider:r1")).isFalse();
            assertThat(manager.hasResourceAccess(CONSUMER, "res-provider:r1")).isTrue();

            try (LogCapture capture = LogCapture.attach(DefaultPluginResourceManager.class)) {
                assertThat(manager.revokeResourceAccess(CONSUMER, "res-provider:r1")).isTrue();
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("Resource access revoked"));
            }
            assertThat(manager.revokeResourceAccess(CONSUMER, "res-provider:r1")).isFalse();
            assertThat(manager.hasResourceAccess(CONSUMER, "res-provider:r1")).isFalse();
        }

        @Test
        @DisplayName("granting access to an unknown resource fails with a warning")
        void grantUnknown() {
            try (LogCapture capture = LogCapture.attach(DefaultPluginResourceManager.class)) {
                assertThat(manager.grantResourceAccess(CONSUMER, "no:such")).isFalse();
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("Cannot grant access"));
            }
        }

        @Test
        @DisplayName("revoking from an unknown plugin or without grants fails")
        void revokeFailures() {
            manager.registerResource(PROVIDER, "r1", "One", 1);
            assertThat(manager.revokeResourceAccess("ghost", "res-provider:r1")).isFalse();
            assertThat(manager.revokeResourceAccess(CONSUMER, "res-provider:r1")).isFalse();
        }

        @Test
        @DisplayName("providers always have access to their own resources")
        void providerAccess() {
            manager.registerResource(PROVIDER, "r1", "One", 1);
            assertThat(manager.hasResourceAccess(PROVIDER, "res-provider:r1")).isTrue();
            assertThat(manager.hasResourceAccess(CONSUMER, "res-provider:r1")).isFalse();
            assertThat(manager.hasResourceAccess("ghost", "nothing")).isFalse();
        }

        @Test
        @DisplayName("accessible resources combine owned and granted entries")
        @SuppressWarnings("unchecked")
        void accessibleResources() throws Exception {
            manager.registerResource(PROVIDER, "own", "Own", 1);
            manager.registerResource("other", "shared", "Shared", 2);
            manager.grantResourceAccess(CONSUMER, "other:shared");

            assertThat(manager.getAccessibleResources(CONSUMER))
                    .extracting(Resource::getId).containsExactly("other:shared");
            assertThat(manager.getAccessibleResources(PROVIDER))
                    .extracting(Resource::getId).containsExactly("res-provider:own");

            // a stale grant must be skipped silently
            Field grantsField = DefaultPluginResourceManager.class
                    .getDeclaredField("accessGrants");
            grantsField.setAccessible(true);
            Map<String, Set<String>> grants =
                    (Map<String, Set<String>>) grantsField.get(manager);
            grants.get(CONSUMER).add("vanished:resource");

            assertThat(manager.getAccessibleResources(CONSUMER))
                    .extracting(Resource::getId).containsExactly("other:shared");
        }
    }

    @Nested
    @DisplayName("updates")
    class UpdateTests {

        @Test
        @DisplayName("providers can update their resources and keep the created time")
        void providerUpdate() {
            manager.registerResource(PROVIDER, "r1", "One", "old");
            long created = manager.getResource("res-provider:r1").getCreatedTime();

            try (LogCapture capture = LogCapture.attach(DefaultPluginResourceManager.class)) {
                assertThat(manager.updateResource(PROVIDER, "r1", "new")).isTrue();
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("Resource updated"));
            }

            Resource updated = manager.getResource("res-provider:r1");
            assertThat(updated.getValue()).isEqualTo("new");
            assertThat(updated.getCreatedTime()).isEqualTo(created);
            assertThat(updated.getName()).isEqualTo("One");
        }

        @Test
        @DisplayName("updating under another plugin's namespace does not touch the resource")
        void nonProviderUpdate() {
            manager.registerResource(PROVIDER, "r1", "One", "old");

            try (LogCapture capture = LogCapture.attach(DefaultPluginResourceManager.class)) {
                assertThat(manager.updateResource(CONSUMER, "r1", "hacked")).isFalse();
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("Cannot update resource - not found"));
            }

            assertThat(manager.getResource("res-provider:r1").getValue()).isEqualTo("old");
            assertThat(manager.getAccessAuditLog("res-provider:r1")).isEmpty();
        }

        @Test
        @DisplayName("updating an unknown resource fails with a warning")
        void updateUnknown() {
            try (LogCapture capture = LogCapture.attach(DefaultPluginResourceManager.class)) {
                assertThat(manager.updateResource(PROVIDER, "ghost", "x")).isFalse();
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("Cannot update resource"));
            }
        }
    }

    @Nested
    @DisplayName("audit log and statistics")
    class AuditTests {

        @Test
        @DisplayName("audit log is filtered per resource with full entries")
        void auditFiltering() {
            manager.registerResource(PROVIDER, "a", "A", 1);
            manager.registerResource(PROVIDER, "b", "B", 2);

            manager.grantResourceAccess(CONSUMER, "res-provider:a");
            manager.grantResourceAccess(CONSUMER, "res-provider:b");

            List<Map<String, Object>> audit = manager.getAccessAuditLog("res-provider:a");
            assertThat(audit).hasSize(1);
            assertThat(audit.get(0)).containsKeys(
                    "timestamp", "pluginId", "resourceId", "action", "success");
            assertThat(audit.get(0)).containsEntry("pluginId", CONSUMER);
            assertThat(audit.get(0)).containsEntry("resourceId", "res-provider:a");
            assertThat(audit.get(0)).containsEntry("action", "GRANT_ACCESS");
            assertThat(audit.get(0)).containsEntry("success", true);

            assertThat(manager.getAccessAuditLog("unrelated")).isEmpty();
        }

        @Test
        @DisplayName("audit log is capped at 1000 entries")
        void auditCap() {
            manager.registerResource(PROVIDER, "r1", "One", 1);
            for (int i = 0; i < 1001; i++) {
                manager.updateResource(PROVIDER, "r1", "v" + i);
            }
            assertThat(manager.getAccessAuditLog("res-provider:r1")).hasSize(1000);
        }

        @Test
        @DisplayName("statistics report exact counts")
        void statistics() {
            manager.registerResourceWithMetadata(
                    PROVIDER, "a", "A", "", "type-x", 1, null);
            manager.registerResourceWithMetadata(
                    "other", "b", "B", "", "type-x", 2, null);
            manager.grantResourceAccess(CONSUMER, "other:b");
            manager.grantResourceAccess(CONSUMER, "res-provider:a");

            Map<String, Object> stats = manager.getStatistics();
            assertThat(stats).containsEntry("totalResources", 2);
            assertThat(stats).containsEntry("totalProviders", 2);
            assertThat(stats).containsEntry("totalResourceTypes", 1);
            assertThat(stats).containsEntry("totalAccessGrants", 2);
            assertThat(stats).containsEntry("auditLogSize", 2);
            @SuppressWarnings("unchecked")
            Map<String, Object> byType = (Map<String, Object>) stats.get("resourcesByType");
            assertThat(byType).containsEntry("type-x", 2);
            @SuppressWarnings("unchecked")
            Map<String, Object> byProvider =
                    (Map<String, Object>) stats.get("resourcesByProvider");
            assertThat(byProvider)
                    .containsEntry(PROVIDER, 1)
                    .containsEntry("other", 1);
        }
    }
}
