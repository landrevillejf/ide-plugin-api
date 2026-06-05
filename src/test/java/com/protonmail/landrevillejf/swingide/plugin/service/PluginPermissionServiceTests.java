package com.protonmail.landrevillejf.swingide.plugin.service;

import org.junit.jupiter.api.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for PluginPermissionService interface
 */
@DisplayName("PluginPermissionService Tests")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class PluginPermissionServiceTests {

    private PluginPermissionService permissionService;
    private static final String PLUGIN_ID = "test-plugin";
    private static final String PERMISSION_ID = "filesystem.read";

    @BeforeEach
    void setUp() {
        permissionService = new MockPluginPermissionService();
    }

    @Test
    @DisplayName("should grant and check permission")
    void test_grant_and_check_permission() {
        permissionService.grantPermission(PLUGIN_ID, PERMISSION_ID);

        boolean hasPermission = permissionService.hasPermission(PLUGIN_ID, PERMISSION_ID);

        assertThat(hasPermission).isTrue();
    }

    @Test
    @DisplayName("should revoke permission")
    void test_revoke_permission() {
        permissionService.grantPermission(PLUGIN_ID, PERMISSION_ID);
        permissionService.revokePermission(PLUGIN_ID, PERMISSION_ID);

        boolean hasPermission = permissionService.hasPermission(PLUGIN_ID, PERMISSION_ID);

        assertThat(hasPermission).isFalse();
    }

    @Test
    @DisplayName("should check all required permissions")
    void test_has_all_permissions() {
        permissionService.grantPermission(PLUGIN_ID, "perm1");
        permissionService.grantPermission(PLUGIN_ID, "perm2");

        boolean hasAll = permissionService.hasAllPermissions(PLUGIN_ID, "perm1", "perm2");

        assertThat(hasAll).isTrue();
    }

    @Test
    @DisplayName("should check any permission")
    void test_has_any_permission() {
        permissionService.grantPermission(PLUGIN_ID, "perm1");

        boolean hasAny = permissionService.hasAnyPermission(PLUGIN_ID, "perm1", "perm2");

        assertThat(hasAny).isTrue();
    }

    @Test
    @DisplayName("should get plugin permissions")
    void test_get_plugin_permissions() {
        permissionService.grantPermission(PLUGIN_ID, "perm1");
        permissionService.grantPermission(PLUGIN_ID, "perm2");

        Set<String> permissions = permissionService.getPluginPermissions(PLUGIN_ID);

        assertThat(permissions).isNotNull().isNotEmpty();
    }

    // Mock implementation for testing
    public static class MockPluginPermissionService implements PluginPermissionService {
        private final java.util.Map<String, Set<String>> pluginPermissions = new java.util.HashMap<>();

        @Override
        public boolean grantPermission(String pluginId, String permissionId) {
            pluginPermissions.computeIfAbsent(pluginId, k -> new HashSet<>()).add(permissionId);
            return true;
        }

        @Override
        public boolean revokePermission(String pluginId, String permissionId) {
            Set<String> perms = pluginPermissions.get(pluginId);
            return perms != null && perms.remove(permissionId);
        }

        @Override
        public boolean hasPermission(String pluginId, String permissionId) {
            Set<String> perms = pluginPermissions.get(pluginId);
            return perms != null && perms.contains(permissionId);
        }

        @Override
        public boolean hasAllPermissions(String pluginId, String... permissionIds) {
            Set<String> perms = pluginPermissions.get(pluginId);
            if (perms == null) return false;
            for (String perm : permissionIds) {
                if (!perms.contains(perm)) return false;
            }
            return true;
        }

        @Override
        public boolean hasAnyPermission(String pluginId, String... permissionIds) {
            Set<String> perms = pluginPermissions.get(pluginId);
            if (perms == null) return false;
            for (String perm : permissionIds) {
                if (perms.contains(perm)) return true;
            }
            return false;
        }

        @Override
        public Set<String> getPluginPermissions(String pluginId) {
            return new HashSet<>(pluginPermissions.getOrDefault(pluginId, new HashSet<>()));
        }

        @Override
        public boolean assignRole(String pluginId, String roleId) { return true; }

        @Override
        public boolean removeRole(String pluginId, String roleId) { return true; }

        @Override
        public List<String> getPluginRoles(String pluginId) { return java.util.Collections.emptyList(); }

        @Override
        public Permission createPermission(String permissionId, String description, String category) { return null; }

        @Override
        public void registerSystemPermission(Permission permission) {}

        @Override
        public Permission getPermission(String permissionId) { return null; }

        @Override
        public List<Permission> getAllPermissions() { return java.util.Collections.emptyList(); }

        @Override
        public List<Permission> getPermissionsByCategory(String category) { return java.util.Collections.emptyList(); }

        @Override
        public Role createRole(String roleId, String name, String description) { return null; }

        @Override
        public Role getRole(String roleId) { return null; }

        @Override
        public List<Role> getAllRoles() { return java.util.Collections.emptyList(); }

        @Override
        public List<java.util.Map<String, Object>> getAuditLog(String pluginId) { return java.util.Collections.emptyList(); }

        @Override
        public void clearAuditLog(String pluginId) {}
    }
}

