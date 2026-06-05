package com.protonmail.landrevillejf.ide.plugin.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Permission management service for plugins with role-based access control.
 */
public interface PluginPermissionService {

    /**
     * Represents a permission.
     */
    interface Permission {
        String getId();
        String getDescription();
        String getCategory();
        boolean isSystemPermission();
    }

    /**
     * Represents a role.
     */
    interface Role {
        String getId();
        String getName();
        String getDescription();
        Set<String> getPermissionIds();
        boolean addPermission(String permissionId);
        boolean removePermission(String permissionId);
    }

    /**
     * Grants a permission to a plugin.
     *
     * @param pluginId the plugin identifier
     * @param permissionId the permission identifier
     * @return true if permission was granted
     */
    boolean grantPermission(String pluginId, String permissionId);

    /**
     * Revokes a permission from a plugin.
     *
     * @param pluginId the plugin identifier
     * @param permissionId the permission identifier
     * @return true if permission was revoked
     */
    boolean revokePermission(String pluginId, String permissionId);

    /**
     * Checks if a plugin has a specific permission.
     *
     * @param pluginId the plugin identifier
     * @param permissionId the permission identifier
     * @return true if the plugin has the permission
     */
    boolean hasPermission(String pluginId, String permissionId);

    /**
     * Checks if a plugin has all specified permissions.
     *
     * @param pluginId the plugin identifier
     * @param permissionIds the permission identifiers
     * @return true if the plugin has all permissions
     */
    boolean hasAllPermissions(String pluginId, String... permissionIds);

    /**
     * Checks if a plugin has any of the specified permissions.
     *
     * @param pluginId the plugin identifier
     * @param permissionIds the permission identifiers
     * @return true if the plugin has at least one permission
     */
    boolean hasAnyPermission(String pluginId, String... permissionIds);

    /**
     * Gets all permissions for a plugin.
     *
     * @param pluginId the plugin identifier
     * @return set of permission identifiers
     */
    Set<String> getPluginPermissions(String pluginId);

    /**
     * Assigns a role to a plugin.
     *
     * @param pluginId the plugin identifier
     * @param roleId the role identifier
     * @return true if role was assigned
     */
    boolean assignRole(String pluginId, String roleId);

    /**
     * Removes a role from a plugin.
     *
     * @param pluginId the plugin identifier
     * @param roleId the role identifier
     * @return true if role was removed
     */
    boolean removeRole(String pluginId, String roleId);

    /**
     * Gets all roles assigned to a plugin.
     *
     * @param pluginId the plugin identifier
     * @return list of role identifiers
     */
    List<String> getPluginRoles(String pluginId);

    /**
     * Creates a new permission.
     *
     * @param permissionId the permission identifier
     * @param description the permission description
     * @param category the permission category
     * @return the created permission
     */
    Permission createPermission(String permissionId, String description, String category);

    /**
     * Registers a system permission (built-in).
     *
     * @param permission the permission to register
     */
    void registerSystemPermission(Permission permission);

    /**
     * Gets a permission by id.
     *
     * @param permissionId the permission identifier
     * @return the permission, or null if not found
     */
    Permission getPermission(String permissionId);

    /**
     * Gets all available permissions.
     *
     * @return list of all permissions
     */
    List<Permission> getAllPermissions();

    /**
     * Gets permissions filtered by category.
     *
     * @param category the permission category
     * @return list of permissions in the category
     */
    List<Permission> getPermissionsByCategory(String category);

    /**
     * Creates a new role.
     *
     * @param roleId the role identifier
     * @param name the role name
     * @param description the role description
     * @return the created role
     */
    Role createRole(String roleId, String name, String description);

    /**
     * Gets a role by id.
     *
     * @param roleId the role identifier
     * @return the role, or null if not found
     */
    Role getRole(String roleId);

    /**
     * Gets all available roles.
     *
     * @return list of all roles
     */
    List<Role> getAllRoles();

    /**
     * Exports permission audit log for a plugin.
     *
     * @param pluginId the plugin identifier
     * @return list of permission audit events
     */
    List<Map<String, Object>> getAuditLog(String pluginId);

    /**
     * Clears audit log for a plugin.
     *
     * @param pluginId the plugin identifier
     */
    void clearAuditLog(String pluginId);
}

