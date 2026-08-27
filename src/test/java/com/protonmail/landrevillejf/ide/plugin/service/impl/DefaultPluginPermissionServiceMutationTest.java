package com.protonmail.landrevillejf.ide.plugin.service.impl;

import com.protonmail.landrevillejf.ide.plugin.service.PluginPermissionService.Permission;
import com.protonmail.landrevillejf.ide.plugin.service.PluginPermissionService.Role;
import com.protonmail.landrevillejf.ide.plugin.utils.LogCapture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Mutation-killing tests for {@link DefaultPluginPermissionService}.
 */
@DisplayName("DefaultPluginPermissionService mutation tests")
class DefaultPluginPermissionServiceMutationTest {

    private static final String PLUGIN = "perm-plugin";

    private DefaultPluginPermissionService service;

    @BeforeEach
    void setUp() {
        service = new DefaultPluginPermissionService();
    }

    @Nested
    @DisplayName("default permissions and roles")
    class DefaultsTests {

        @Test
        @DisplayName("all 28 system permissions are registered")
        void allSystemPermissionsRegistered() {
            List<Permission> all = service.getAllPermissions();
            assertThat(all).hasSize(28);

            String[] expectedIds = {
                    "system.admin", "system.config.read", "system.config.write",
                    "file.read", "file.write", "file.delete", "file.execute",
                    "network.http", "network.websocket", "network.socket",
                    "ui.menu.modify", "ui.toolbar.modify", "ui.dialog.show",
                    "project.create", "project.modify", "project.delete", "project.export",
                    "plugin.install", "plugin.uninstall", "plugin.enable", "plugin.disable",
                    "editor.read", "editor.write", "editor.format", "editor.refactor",
                    "debug.breakpoint", "debug.execute", "debug.inspect"
            };
            for (String id : expectedIds) {
                Permission permission = service.getPermission(id);
                assertThat(permission).as("permission %s", id).isNotNull();
                assertThat(permission.getId()).isEqualTo(id);
                assertThat(permission.isSystemPermission()).isTrue();
                assertThat(permission.getDescription()).isNotBlank();
            }
        }

        @Test
        @DisplayName("permission categories are correct")
        void permissionCategories() {
            assertThat(service.getPermissionsByCategory("system")).hasSize(3);
            assertThat(service.getPermissionsByCategory("file")).hasSize(4);
            assertThat(service.getPermissionsByCategory("network")).hasSize(3);
            assertThat(service.getPermissionsByCategory("ui")).hasSize(3);
            assertThat(service.getPermissionsByCategory("project")).hasSize(4);
            assertThat(service.getPermissionsByCategory("plugin")).hasSize(4);
            assertThat(service.getPermissionsByCategory("editor")).hasSize(4);
            assertThat(service.getPermissionsByCategory("debug")).hasSize(3);
            assertThat(service.getPermissionsByCategory("nonexistent")).isEmpty();
        }

        @Test
        @DisplayName("default roles exist with expected permissions")
        void defaultRoles() {
            List<Role> allRoles = service.getAllRoles();
            assertThat(allRoles).hasSize(4);

            Role guest = service.getRole("guest");
            assertThat(guest.getPermissionIds())
                    .containsExactlyInAnyOrder("file.read", "ui.dialog.show", "editor.read");

            Role user = service.getRole("user");
            assertThat(user.getPermissionIds()).hasSize(10)
                    .contains("file.read", "file.write", "editor.format")
                    .doesNotContain("file.delete");

            Role developer = service.getRole("developer");
            assertThat(developer.getPermissionIds()).hasSize(18)
                    .contains("file.delete", "debug.inspect", "network.http");

            Role admin = service.getRole("admin");
            assertThat(admin.getPermissionIds()).hasSize(28);
        }
    }

    @Nested
    @DisplayName("grant and revoke")
    class GrantRevokeTests {

        @Test
        @DisplayName("granting an unknown permission fails")
        void grantUnknown() {
            assertThat(service.grantPermission(PLUGIN, "no.such.permission")).isFalse();
        }

        @Test
        @DisplayName("grant twice returns false the second time")
        void grantTwice() {
            try (LogCapture capture = LogCapture.attach(DefaultPluginPermissionService.class)) {
                assertThat(service.grantPermission(PLUGIN, "file.read")).isTrue();
                assertThat(service.grantPermission(PLUGIN, "file.read")).isFalse();
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("granted to plugin"));
            }
        }

        @Test
        @DisplayName("grant warns for unknown permission")
        void grantWarns() {
            try (LogCapture capture = LogCapture.attach(DefaultPluginPermissionService.class)) {
                service.grantPermission(PLUGIN, "nope");
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("permission not found"));
            }
        }

        @Test
        @DisplayName("revoke without prior grants fails")
        void revokeFromUnknownPlugin() {
            assertThat(service.revokePermission("ghost", "file.read")).isFalse();
        }

        @Test
        @DisplayName("revoke of ungranted permission fails")
        void revokeUngranted() {
            service.grantPermission(PLUGIN, "file.read");
            assertThat(service.revokePermission(PLUGIN, "file.write")).isFalse();
        }

        @Test
        @DisplayName("revoke of granted permission succeeds once")
        void revokeGranted() {
            service.grantPermission(PLUGIN, "file.read");
            assertThat(service.revokePermission(PLUGIN, "file.read")).isTrue();
            assertThat(service.revokePermission(PLUGIN, "file.read")).isFalse();
            assertThat(service.hasPermission(PLUGIN, "file.read")).isFalse();
        }
    }

    @Nested
    @DisplayName("role assignment")
    class RoleAssignmentTests {

        @Test
        @DisplayName("assigning an unknown role fails")
        void assignUnknown() {
            assertThat(service.assignRole(PLUGIN, "no.such.role")).isFalse();
        }

        @Test
        @DisplayName("assigning the same role twice fails the second time")
        void assignTwice() {
            assertThat(service.assignRole(PLUGIN, "guest")).isTrue();
            assertThat(service.assignRole(PLUGIN, "guest")).isFalse();
            assertThat(service.getPluginRoles(PLUGIN)).containsExactly("guest");
        }

        @Test
        @DisplayName("assign warns for unknown role")
        void assignWarns() {
            try (LogCapture capture = LogCapture.attach(DefaultPluginPermissionService.class)) {
                service.assignRole(PLUGIN, "nope");
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("role not found"));
            }
        }

        @Test
        @DisplayName("removeRole from plugin without roles fails")
        void removeRoleNoList() {
            assertThat(service.removeRole("ghost", "guest")).isFalse();
        }

        @Test
        @DisplayName("removeRole succeeds once then fails")
        void removeRoleOnce() {
            service.assignRole(PLUGIN, "guest");
            assertThat(service.removeRole(PLUGIN, "guest")).isTrue();
            assertThat(service.removeRole(PLUGIN, "guest")).isFalse();
            assertThat(service.getPluginRoles(PLUGIN)).isEmpty();
        }

        @Test
        @DisplayName("removeRole of unassigned role fails")
        void removeUnassignedRole() {
            service.assignRole(PLUGIN, "guest");
            assertThat(service.removeRole(PLUGIN, "admin")).isFalse();
        }

        @Test
        @DisplayName("getPluginRoles returns empty list for unknown plugin")
        void rolesOfUnknownPlugin() {
            assertThat(service.getPluginRoles("ghost")).isEmpty();
        }

        @Test
        @DisplayName("role-based permissions are visible via hasPermission")
        void roleBasedPermissions() {
            service.assignRole(PLUGIN, "guest");
            assertThat(service.hasPermission(PLUGIN, "file.read")).isTrue();
            assertThat(service.hasPermission(PLUGIN, "file.write")).isFalse();
        }

        @Test
        @DisplayName("role with unknown id in list is skipped")
        void staleRoleReference() {
            service.assignRole(PLUGIN, "guest");
            service.removeRole(PLUGIN, "guest");
            // after removal no role-based access remains
            assertThat(service.hasPermission(PLUGIN, "file.read")).isFalse();
        }
    }

    @Nested
    @DisplayName("permission queries")
    class QueryTests {

        @Test
        @DisplayName("hasAllPermissions requires every permission")
        void hasAll() {
            service.grantPermission(PLUGIN, "file.read");
            service.grantPermission(PLUGIN, "file.write");
            assertThat(service.hasAllPermissions(PLUGIN, "file.read", "file.write")).isTrue();
            assertThat(service.hasAllPermissions(PLUGIN, "file.read", "file.delete")).isFalse();
        }

        @Test
        @DisplayName("hasAnyPermission accepts a single match")
        void hasAny() {
            service.grantPermission(PLUGIN, "file.read");
            assertThat(service.hasAnyPermission(PLUGIN, "file.delete", "file.read")).isTrue();
            assertThat(service.hasAnyPermission(PLUGIN, "file.delete", "file.write")).isFalse();
        }

        @Test
        @DisplayName("getPluginPermissions combines direct and role permissions")
        void combinedPermissions() {
            service.grantPermission(PLUGIN, "network.http");
            service.assignRole(PLUGIN, "guest");
            Set<String> all = service.getPluginPermissions(PLUGIN);
            assertThat(all).containsExactlyInAnyOrder(
                    "network.http", "file.read", "ui.dialog.show", "editor.read");
        }

        @Test
        @DisplayName("getPluginPermissions of unknown plugin is empty")
        void emptyPermissions() {
            assertThat(service.getPluginPermissions("ghost")).isEmpty();
        }

        @Test
        @DisplayName("getPluginPermissions skips unknown role ids")
        void unknownRoleSkipped() {
            assertThat(service.getPluginPermissions(PLUGIN)).isEmpty();
        }
    }

    @Nested
    @DisplayName("permission and role creation")
    class CreationTests {

        @Test
        @DisplayName("createPermission returns existing instance on duplicate")
        void createDuplicatePermission() {
            Permission first = service.createPermission("custom.perm", "desc", "custom");
            Permission second = service.createPermission("custom.perm", "other", "custom");
            assertThat(first).isNotNull();
            assertThat(second).isSameAs(first);
            assertThat(first.getDescription()).isEqualTo("desc");
        }

        @Test
        @DisplayName("createRole returns existing instance on duplicate")
        void createDuplicateRole() {
            Role first = service.createRole("custom-role", "Custom", "desc");
            Role second = service.createRole("custom-role", "Other", "desc");
            assertThat(first).isNotNull();
            assertThat(second).isSameAs(first);
            assertThat(first.getName()).isEqualTo("Custom");
        }

        @Test
        @DisplayName("registerSystemPermission overrides existing entry")
        void registerSystemPermissionOverride() {
            Permission replacement =
                    new DefaultPluginPermissionService.PermissionImpl(
                            "file.read", "overridden", "file", true);
            try (LogCapture capture = LogCapture.attach(DefaultPluginPermissionService.class)) {
                service.registerSystemPermission(replacement);
                assertThat(capture.formattedMessages())
                        .anyMatch(m -> m.contains("already exists, overriding"));
            }
            assertThat(service.getPermission("file.read")).isSameAs(replacement);
            assertThat(service.getPermission("file.read").getDescription()).isEqualTo("overridden");
        }

        @Test
        @DisplayName("registerSystemPermission adds new permission")
        void registerSystemPermissionNew() {
            Permission permission =
                    new DefaultPluginPermissionService.PermissionImpl(
                            "brand.new", "new one", "custom", true);
            service.registerSystemPermission(permission);
            assertThat(service.getPermission("brand.new")).isSameAs(permission);
        }
    }

    @Nested
    @DisplayName("audit log")
    class AuditLogTests {

        @Test
        @DisplayName("audit entries are recorded for grants and revokes")
        void auditEntries() {
            service.grantPermission(PLUGIN, "file.read");
            service.revokePermission(PLUGIN, "file.read");
            service.assignRole(PLUGIN, "guest");
            service.removeRole(PLUGIN, "guest");

            List<Map<String, Object>> log = service.getAuditLog(PLUGIN);
            assertThat(log).hasSize(4);
            assertThat(log.get(0).get("action")).isEqualTo("GRANT_PERMISSION");
            assertThat(log.get(0).get("resourceId")).isEqualTo("file.read");
            assertThat(log.get(1).get("action")).isEqualTo("REVOKE_PERMISSION");
            assertThat(log.get(2).get("action")).isEqualTo("ASSIGN_ROLE");
            assertThat(log.get(2).get("resourceId")).isEqualTo("guest");
            assertThat(log.get(3).get("action")).isEqualTo("REMOVE_ROLE");
            assertThat(log.get(0).get("timestamp")).isInstanceOf(Long.class);
            assertThat(log.get(0).get("details")).isNull();
        }

        @Test
        @DisplayName("audit log of unknown plugin is empty")
        void emptyAuditLog() {
            assertThat(service.getAuditLog("ghost")).isEmpty();
        }

        @Test
        @DisplayName("audit log is capped at 1000 entries")
        void auditLogCap() {
            for (int i = 0; i < 1001; i++) {
                service.grantPermission(PLUGIN, "file.read");
                service.revokePermission(PLUGIN, "file.read");
            }
            List<Map<String, Object>> log = service.getAuditLog(PLUGIN);
            assertThat(log).hasSize(1000);
            // oldest entry was evicted, the newest is a revoke
            assertThat(log.get(log.size() - 1).get("action")).isEqualTo("REVOKE_PERMISSION");
        }

        @Test
        @DisplayName("clearAuditLog empties the log")
        void clearAuditLog() {
            service.grantPermission(PLUGIN, "file.read");
            service.clearAuditLog(PLUGIN);
            assertThat(service.getAuditLog(PLUGIN)).isEmpty();
            // clearing an unknown plugin is a no-op
            service.clearAuditLog("ghost");
        }
    }

    @Nested
    @DisplayName("RoleImpl behaviour")
    class RoleImplTests {

        @Test
        @DisplayName("addPermission is idempotent")
        void addPermissionIdempotent() {
            Role role = service.createRole("test-role", "Test", "desc");
            assertThat(role.addPermission("file.read")).isTrue();
            assertThat(role.addPermission("file.read")).isFalse();
            assertThat(role.getPermissionIds()).containsExactly("file.read");
        }

        @Test
        @DisplayName("removePermission succeeds once")
        void removePermissionOnce() {
            Role role = service.createRole("test-role2", "Test", "desc");
            role.addPermission("file.read");
            assertThat(role.removePermission("file.read")).isTrue();
            assertThat(role.removePermission("file.read")).isFalse();
            assertThat(role.getPermissionIds()).isEmpty();
        }

        @Test
        @DisplayName("role metadata accessors")
        void roleMetadata() {
            Role role = service.createRole("meta", "Meta Role", "a description");
            assertThat(role.getId()).isEqualTo("meta");
            assertThat(role.getName()).isEqualTo("Meta Role");
            assertThat(role.getDescription()).isEqualTo("a description");
        }
    }

    @Nested
    @DisplayName("PermissionImpl value semantics")
    class PermissionImplTests {

        @Test
        @DisplayName("equals and hashCode are id-based")
        void equalsAndHashCode() {
            var a = new DefaultPluginPermissionService.PermissionImpl("x", "d1", "c", false);
            var b = new DefaultPluginPermissionService.PermissionImpl("x", "d2", "c2", true);
            var c = new DefaultPluginPermissionService.PermissionImpl("y", "d1", "c", false);

            assertThat(a).isEqualTo(a);
            assertThat(a).isEqualTo(b);
            assertThat(a).isNotEqualTo(c);
            assertThat(a).isNotEqualTo(null);
            assertThat(a).isNotEqualTo("string");
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("toString contains id and category")
        void toStringFormat() {
            var a = new DefaultPluginPermissionService.PermissionImpl("x", "d", "cat", true);
            assertThat(a.toString()).isEqualTo("Permission{id='x', category='cat', system=true}");
        }
    }
}
