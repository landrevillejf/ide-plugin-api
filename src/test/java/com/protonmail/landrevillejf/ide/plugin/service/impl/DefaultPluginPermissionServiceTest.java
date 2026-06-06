package com.protonmail.landrevillejf.ide.plugin.service.impl;

import com.protonmail.landrevillejf.ide.plugin.service.PluginPermissionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class DefaultPluginPermissionServiceTest {

    private DefaultPluginPermissionService permissionService;
    private static final String TEST_PLUGIN = "test-plugin";
    private static final String TEST_PLUGIN_2 = "test-plugin-2";

    @BeforeEach
    void setUp() {
        permissionService = new DefaultPluginPermissionService();
    }

    @Test
    void grantPermission() {
        boolean granted = permissionService.grantPermission(TEST_PLUGIN, "file.read");

        assertTrue(granted);
        assertTrue(permissionService.hasPermission(TEST_PLUGIN, "file.read"));
    }

    @Test
    void grantNonExistentPermission() {
        boolean granted = permissionService.grantPermission(TEST_PLUGIN, "non.existent.permission");

        assertFalse(granted);
        assertFalse(permissionService.hasPermission(TEST_PLUGIN, "non.existent.permission"));
    }

    @Test
    void revokePermission() {
        permissionService.grantPermission(TEST_PLUGIN, "file.write");
        assertTrue(permissionService.hasPermission(TEST_PLUGIN, "file.write"));

        boolean revoked = permissionService.revokePermission(TEST_PLUGIN, "file.write");

        assertTrue(revoked);
        assertFalse(permissionService.hasPermission(TEST_PLUGIN, "file.write"));
    }

    @Test
    void revokeNonExistentPermission() {
        boolean revoked = permissionService.revokePermission(TEST_PLUGIN, "non.existent");

        assertFalse(revoked);
    }

    @Test
    void hasPermission() {
        permissionService.grantPermission(TEST_PLUGIN, "file.read");

        assertTrue(permissionService.hasPermission(TEST_PLUGIN, "file.read"));
        assertFalse(permissionService.hasPermission(TEST_PLUGIN, "file.delete"));
    }

    @Test
    void hasAllPermissions() {
        permissionService.grantPermission(TEST_PLUGIN, "file.read");
        permissionService.grantPermission(TEST_PLUGIN, "file.write");

        assertTrue(permissionService.hasAllPermissions(TEST_PLUGIN, "file.read", "file.write"));
        assertFalse(permissionService.hasAllPermissions(TEST_PLUGIN, "file.read", "file.delete"));
    }

    @Test
    void hasAnyPermission() {
        permissionService.grantPermission(TEST_PLUGIN, "file.read");

        assertTrue(permissionService.hasAnyPermission(TEST_PLUGIN, "file.read", "file.write"));
        assertFalse(permissionService.hasAnyPermission(TEST_PLUGIN, "file.delete", "file.execute"));
    }

    @Test
    void getPluginPermissions() {
        permissionService.grantPermission(TEST_PLUGIN, "file.read");
        permissionService.grantPermission(TEST_PLUGIN, "file.write");
        permissionService.assignRole(TEST_PLUGIN, "user");

        Set<String> permissions = permissionService.getPluginPermissions(TEST_PLUGIN);

        assertTrue(permissions.contains("file.read"));
        assertTrue(permissions.contains("file.write"));
        assertTrue(permissions.contains("ui.dialog.show")); // from user role
    }

    @Test
    void assignRole() {
        boolean assigned = permissionService.assignRole(TEST_PLUGIN, "developer");

        assertTrue(assigned);
        assertTrue(permissionService.getPluginRoles(TEST_PLUGIN).contains("developer"));
        assertTrue(permissionService.hasPermission(TEST_PLUGIN, "debug.execute"));
    }

    @Test
    void assignNonExistentRole() {
        boolean assigned = permissionService.assignRole(TEST_PLUGIN, "non.existent.role");

        assertFalse(assigned);
    }

    @Test
    void removeRole() {
        permissionService.assignRole(TEST_PLUGIN, "user");
        assertTrue(permissionService.getPluginRoles(TEST_PLUGIN).contains("user"));

        boolean removed = permissionService.removeRole(TEST_PLUGIN, "user");

        assertTrue(removed);
        assertFalse(permissionService.getPluginRoles(TEST_PLUGIN).contains("user"));
    }

    @Test
    void getPluginRoles() {
        permissionService.assignRole(TEST_PLUGIN, "user");
        permissionService.assignRole(TEST_PLUGIN, "developer");

        List<String> roles = permissionService.getPluginRoles(TEST_PLUGIN);

        assertEquals(2, roles.size());
        assertTrue(roles.contains("user"));
        assertTrue(roles.contains("developer"));
    }

    @Test
    void createPermission() {
        PluginPermissionService.Permission permission = permissionService.createPermission(
                "custom.permission", "Custom permission description", "custom"
        );

        assertNotNull(permission);
        assertEquals("custom.permission", permission.getId());
        assertEquals("Custom permission description", permission.getDescription());
        assertEquals("custom", permission.getCategory());
        assertFalse(permission.isSystemPermission());
    }

    @Test
    void registerSystemPermission() {
        PluginPermissionService.Permission customPermission = new DefaultPluginPermissionService.PermissionImpl(
                "custom.system", "Custom system permission", "system", true
        );

        permissionService.registerSystemPermission(customPermission);

        PluginPermissionService.Permission retrieved = permissionService.getPermission("custom.system");
        assertNotNull(retrieved);
        assertTrue(retrieved.isSystemPermission());
    }

    @Test
    void getPermission() {
        PluginPermissionService.Permission permission = permissionService.getPermission("file.read");

        assertNotNull(permission);
        assertEquals("file.read", permission.getId());
        assertEquals("file", permission.getCategory());
    }

    @Test
    void getAllPermissions() {
        List<PluginPermissionService.Permission> permissions = permissionService.getAllPermissions();

        assertFalse(permissions.isEmpty());
        assertTrue(permissions.stream().anyMatch(p -> p.getId().equals("file.read")));
        assertTrue(permissions.stream().anyMatch(p -> p.getId().equals("system.admin")));
    }

    @Test
    void getPermissionsByCategory() {
        List<PluginPermissionService.Permission> filePermissions =
                permissionService.getPermissionsByCategory("file");

        assertFalse(filePermissions.isEmpty());
        assertTrue(filePermissions.stream().anyMatch(p -> p.getId().equals("file.read")));
        assertTrue(filePermissions.stream().anyMatch(p -> p.getId().equals("file.write")));
        assertTrue(filePermissions.stream().anyMatch(p -> p.getId().equals("file.delete")));
    }

    @Test
    void createRole() {
        PluginPermissionService.Role role = permissionService.createRole(
                "tester", "Tester", "Testing role with limited permissions"
        );

        assertNotNull(role);
        assertEquals("tester", role.getId());
        assertEquals("Tester", role.getName());
        assertEquals("Testing role with limited permissions", role.getDescription());
    }

    @Test
    void createDuplicateRole() {
        permissionService.createRole("duplicate", "Duplicate", "First creation");
        PluginPermissionService.Role duplicate = permissionService.createRole("duplicate", "Duplicate 2", "Second creation");

        assertNotNull(duplicate);
        assertEquals("duplicate", duplicate.getId()); // Should return existing role
    }

    @Test
    void addPermissionToRole() {
        PluginPermissionService.Role role = permissionService.getRole("developer");

        assertNotNull(role);

        boolean added = role.addPermission("custom.permission");
        assertTrue(added);
        assertTrue(role.getPermissionIds().contains("custom.permission"));
    }

    @Test
    void removePermissionFromRole() {
        PluginPermissionService.Role role = permissionService.getRole("developer");

        assertNotNull(role);
        assertTrue(role.getPermissionIds().contains("file.delete"));

        boolean removed = role.removePermission("file.delete");
        assertTrue(removed);
        assertFalse(role.getPermissionIds().contains("file.delete"));
    }

    @Test
    void getRole() {
        PluginPermissionService.Role role = permissionService.getRole("developer");

        assertNotNull(role);
        assertEquals("developer", role.getId());
        assertEquals("Developer", role.getName());
    }

    @Test
    void getAllRoles() {
        List<PluginPermissionService.Role> roles = permissionService.getAllRoles();

        assertEquals(4, roles.size()); // guest, user, developer, admin
        assertTrue(roles.stream().anyMatch(r -> r.getId().equals("guest")));
        assertTrue(roles.stream().anyMatch(r -> r.getId().equals("user")));
        assertTrue(roles.stream().anyMatch(r -> r.getId().equals("developer")));
        assertTrue(roles.stream().anyMatch(r -> r.getId().equals("admin")));
    }

    @Test
    void getAuditLog() {
        permissionService.grantPermission(TEST_PLUGIN, "file.read");
        permissionService.assignRole(TEST_PLUGIN, "developer");
        permissionService.revokePermission(TEST_PLUGIN, "file.read");

        List<Map<String, Object>> auditLog = permissionService.getAuditLog(TEST_PLUGIN);

        assertFalse(auditLog.isEmpty());
        assertTrue(auditLog.size() >= 3);

        Map<String, Object> firstEntry = auditLog.get(0);
        assertTrue(firstEntry.containsKey("timestamp"));
        assertTrue(firstEntry.containsKey("action"));
        assertTrue(firstEntry.containsKey("resourceId"));
    }

    @Test
    void clearAuditLog() {
        permissionService.grantPermission(TEST_PLUGIN, "file.read");
        permissionService.grantPermission(TEST_PLUGIN, "file.write");

        assertFalse(permissionService.getAuditLog(TEST_PLUGIN).isEmpty());

        permissionService.clearAuditLog(TEST_PLUGIN);

        assertTrue(permissionService.getAuditLog(TEST_PLUGIN).isEmpty());
    }

    @Test
    void multiplePluginsIsolation() {
        permissionService.grantPermission(TEST_PLUGIN, "file.read");
        permissionService.grantPermission(TEST_PLUGIN_2, "file.write");

        assertTrue(permissionService.hasPermission(TEST_PLUGIN, "file.read"));
        assertFalse(permissionService.hasPermission(TEST_PLUGIN, "file.write"));
        assertFalse(permissionService.hasPermission(TEST_PLUGIN_2, "file.read"));
        assertTrue(permissionService.hasPermission(TEST_PLUGIN_2, "file.write"));
    }

    @Test
    void roleInheritance() {
        // Admin role should have all permissions
        permissionService.assignRole(TEST_PLUGIN, "admin");

        assertTrue(permissionService.hasPermission(TEST_PLUGIN, "system.admin"));
        assertTrue(permissionService.hasPermission(TEST_PLUGIN, "plugin.install"));
        assertTrue(permissionService.hasPermission(TEST_PLUGIN, "debug.execute"));
        assertTrue(permissionService.hasPermission(TEST_PLUGIN, "editor.refactor"));
    }

    @Test
    void guestRoleHasMinimalPermissions() {
        permissionService.assignRole(TEST_PLUGIN, "guest");

        assertTrue(permissionService.hasPermission(TEST_PLUGIN, "file.read"));
        assertFalse(permissionService.hasPermission(TEST_PLUGIN, "file.write"));
        assertFalse(permissionService.hasPermission(TEST_PLUGIN, "project.create"));
        assertFalse(permissionService.hasPermission(TEST_PLUGIN, "debug.execute"));
    }
}