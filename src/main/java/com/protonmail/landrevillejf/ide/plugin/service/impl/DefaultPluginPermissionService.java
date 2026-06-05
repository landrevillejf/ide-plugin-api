package com.protonmail.landrevillejf.ide.plugin.service.impl;

import com.protonmail.landrevillejf.ide.plugin.service.PluginPermissionService;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

@Slf4j
public class DefaultPluginPermissionService implements PluginPermissionService {

    private final Map<String, Set<String>> pluginPermissions = new ConcurrentHashMap<>();
    private final Map<String, List<String>> pluginRoles = new ConcurrentHashMap<>();
    private final Map<String, Permission> permissions = new ConcurrentHashMap<>();
    private final Map<String, Role> roles = new ConcurrentHashMap<>();
    private final Map<String, List<AuditEntry>> auditLogs = new ConcurrentHashMap<>();

    public DefaultPluginPermissionService() {
        initializeDefaultPermissions();
        initializeDefaultRoles();
        log.info("DefaultPluginPermissionService initialized");
    }

    @Override
    public boolean grantPermission(String pluginId, String permissionId) {
        Permission permission = permissions.get(permissionId);
        if (permission == null) {
            log.warn("Cannot grant permission '{}' to plugin '{}' - permission not found", permissionId, pluginId);
            return false;
        }

        Set<String> perms = pluginPermissions.computeIfAbsent(pluginId, k -> new CopyOnWriteArraySet<>());
        boolean granted = perms.add(permissionId);

        if (granted) {
            log.debug("Permission '{}' granted to plugin '{}'", permissionId, pluginId);
            addAuditEntry(pluginId, "GRANT_PERMISSION", permissionId, null);
        }

        return granted;
    }

    @Override
    public boolean revokePermission(String pluginId, String permissionId) {
        Set<String> perms = pluginPermissions.get(pluginId);
        if (perms == null) {
            return false;
        }

        boolean revoked = perms.remove(permissionId);
        if (revoked) {
            log.debug("Permission '{}' revoked from plugin '{}'", permissionId, pluginId);
            addAuditEntry(pluginId, "REVOKE_PERMISSION", permissionId, null);
        }

        return revoked;
    }

    @Override
    public boolean hasPermission(String pluginId, String permissionId) {
        Set<String> perms = pluginPermissions.get(pluginId);
        if (perms != null && perms.contains(permissionId)) {
            return true;
        }

        // Check role-based permissions
        List<String> pluginRoleIds = pluginRoles.get(pluginId);
        if (pluginRoleIds != null) {
            for (String roleId : pluginRoleIds) {
                Role role = roles.get(roleId);
                if (role != null && role.getPermissionIds().contains(permissionId)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean hasAllPermissions(String pluginId, String... permissionIds) {
        for (String permissionId : permissionIds) {
            if (!hasPermission(pluginId, permissionId)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean hasAnyPermission(String pluginId, String... permissionIds) {
        for (String permissionId : permissionIds) {
            if (hasPermission(pluginId, permissionId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<String> getPluginPermissions(String pluginId) {
        Set<String> allPermissions = new CopyOnWriteArraySet<>();

        // Add direct permissions
        Set<String> directPerms = pluginPermissions.get(pluginId);
        if (directPerms != null) {
            allPermissions.addAll(directPerms);
        }

        // Add role-based permissions
        List<String> pluginRoleIds = pluginRoles.get(pluginId);
        if (pluginRoleIds != null) {
            for (String roleId : pluginRoleIds) {
                Role role = roles.get(roleId);
                if (role != null) {
                    allPermissions.addAll(role.getPermissionIds());
                }
            }
        }

        return Collections.unmodifiableSet(allPermissions);
    }

    @Override
    public boolean assignRole(String pluginId, String roleId) {
        Role role = roles.get(roleId);
        if (role == null) {
            log.warn("Cannot assign role '{}' to plugin '{}' - role not found", roleId, pluginId);
            return false;
        }

        List<String> pluginRoleList = pluginRoles.computeIfAbsent(pluginId, k -> new CopyOnWriteArrayList<>());
        boolean assigned = pluginRoleList.add(roleId);

        if (assigned) {
            log.debug("Role '{}' assigned to plugin '{}'", roleId, pluginId);
            addAuditEntry(pluginId, "ASSIGN_ROLE", roleId, null);
        }

        return assigned;
    }

    @Override
    public boolean removeRole(String pluginId, String roleId) {
        List<String> pluginRoleList = pluginRoles.get(pluginId);
        if (pluginRoleList == null) {
            return false;
        }

        boolean removed = pluginRoleList.remove(roleId);
        if (removed) {
            log.debug("Role '{}' removed from plugin '{}'", roleId, pluginId);
            addAuditEntry(pluginId, "REMOVE_ROLE", roleId, null);
        }

        return removed;
    }

    @Override
    public List<String> getPluginRoles(String pluginId) {
        List<String> pluginRoleList = pluginRoles.get(pluginId);
        if (pluginRoleList == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(pluginRoleList);
    }

    @Override
    public Permission createPermission(String permissionId, String description, String category) {
        if (permissions.containsKey(permissionId)) {
            log.warn("Permission '{}' already exists", permissionId);
            return permissions.get(permissionId);
        }

        PermissionImpl permission = new PermissionImpl(permissionId, description, category, false);
        permissions.put(permissionId, permission);
        log.debug("Permission created: id={}, category={}", permissionId, category);
        return permission;
    }

    @Override
    public void registerSystemPermission(Permission permission) {
        if (permissions.containsKey(permission.getId())) {
            log.warn("System permission '{}' already exists, overriding", permission.getId());
        }
        permissions.put(permission.getId(), permission);
        log.debug("System permission registered: {}", permission.getId());
    }

    @Override
    public Permission getPermission(String permissionId) {
        return permissions.get(permissionId);
    }

    @Override
    public List<Permission> getAllPermissions() {
        return new ArrayList<>(permissions.values());
    }

    @Override
    public List<Permission> getPermissionsByCategory(String category) {
        return permissions.values().stream()
                .filter(p -> p.getCategory().equals(category))
                .collect(Collectors.toList());
    }

    @Override
    public Role createRole(String roleId, String name, String description) {
        if (roles.containsKey(roleId)) {
            log.warn("Role '{}' already exists", roleId);
            return roles.get(roleId);
        }

        RoleImpl role = new RoleImpl(roleId, name, description);
        roles.put(roleId, role);
        log.debug("Role created: id={}, name={}", roleId, name);
        return role;
    }

    @Override
    public Role getRole(String roleId) {
        return roles.get(roleId);
    }

    @Override
    public List<Role> getAllRoles() {
        return new ArrayList<>(roles.values());
    }

    @Override
    public List<Map<String, Object>> getAuditLog(String pluginId) {
        List<AuditEntry> entries = auditLogs.get(pluginId);
        if (entries == null || entries.isEmpty()) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (AuditEntry entry : entries) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("timestamp", entry.timestamp);
            map.put("action", entry.action);
            map.put("resourceId", entry.resourceId);
            map.put("details", entry.details);
            result.add(map);
        }
        return Collections.unmodifiableList(result);
    }

    @Override
    public void clearAuditLog(String pluginId) {
        List<AuditEntry> entries = auditLogs.get(pluginId);
        if (entries != null) {
            entries.clear();
            log.debug("Audit log cleared for plugin '{}'", pluginId);
        }
    }

    private void initializeDefaultPermissions() {
        // System permissions
        registerSystemPermission(new PermissionImpl("system.admin", "Full system access", "system", true));
        registerSystemPermission(new PermissionImpl("system.config.read", "Read system configuration", "system", true));
        registerSystemPermission(new PermissionImpl("system.config.write", "Modify system configuration", "system", true));

        // File permissions
        registerSystemPermission(new PermissionImpl("file.read", "Read files", "file", true));
        registerSystemPermission(new PermissionImpl("file.write", "Write files", "file", true));
        registerSystemPermission(new PermissionImpl("file.delete", "Delete files", "file", true));
        registerSystemPermission(new PermissionImpl("file.execute", "Execute files", "file", true));

        // Network permissions
        registerSystemPermission(new PermissionImpl("network.http", "Make HTTP requests", "network", true));
        registerSystemPermission(new PermissionImpl("network.websocket", "Use WebSocket connections", "network", true));
        registerSystemPermission(new PermissionImpl("network.socket", "Open socket connections", "network", true));

        // UI permissions
        registerSystemPermission(new PermissionImpl("ui.menu.modify", "Modify UI menus", "ui", true));
        registerSystemPermission(new PermissionImpl("ui.toolbar.modify", "Modify toolbars", "ui", true));
        registerSystemPermission(new PermissionImpl("ui.dialog.show", "Show dialogs", "ui", true));

        // Project permissions
        registerSystemPermission(new PermissionImpl("project.create", "Create projects", "project", true));
        registerSystemPermission(new PermissionImpl("project.modify", "Modify projects", "project", true));
        registerSystemPermission(new PermissionImpl("project.delete", "Delete projects", "project", true));
        registerSystemPermission(new PermissionImpl("project.export", "Export projects", "project", true));

        // Plugin permissions
        registerSystemPermission(new PermissionImpl("plugin.install", "Install plugins", "plugin", true));
        registerSystemPermission(new PermissionImpl("plugin.uninstall", "Uninstall plugins", "plugin", true));
        registerSystemPermission(new PermissionImpl("plugin.enable", "Enable plugins", "plugin", true));
        registerSystemPermission(new PermissionImpl("plugin.disable", "Disable plugins", "plugin", true));

        // Editor permissions
        registerSystemPermission(new PermissionImpl("editor.read", "Read editor content", "editor", true));
        registerSystemPermission(new PermissionImpl("editor.write", "Modify editor content", "editor", true));
        registerSystemPermission(new PermissionImpl("editor.format", "Format code", "editor", true));
        registerSystemPermission(new PermissionImpl("editor.refactor", "Refactor code", "editor", true));

        // Debug permissions
        registerSystemPermission(new PermissionImpl("debug.breakpoint", "Set breakpoints", "debug", true));
        registerSystemPermission(new PermissionImpl("debug.execute", "Execute debug commands", "debug", true));
        registerSystemPermission(new PermissionImpl("debug.inspect", "Inspect variables", "debug", true));
    }

    private void initializeDefaultRoles() {
        // Guest role - minimal permissions
        Role guest = createRole("guest", "Guest", "Minimal permissions for basic operations");
        guest.addPermission("file.read");
        guest.addPermission("ui.dialog.show");
        guest.addPermission("editor.read");

        // User role - standard permissions
        Role user = createRole("user", "User", "Standard user permissions");
        user.addPermission("file.read");
        user.addPermission("file.write");
        user.addPermission("ui.menu.modify");
        user.addPermission("ui.toolbar.modify");
        user.addPermission("ui.dialog.show");
        user.addPermission("project.create");
        user.addPermission("project.modify");
        user.addPermission("editor.read");
        user.addPermission("editor.write");
        user.addPermission("editor.format");

        // Developer role - advanced permissions
        Role developer = createRole("developer", "Developer", "Development permissions");
        developer.addPermission("file.read");
        developer.addPermission("file.write");
        developer.addPermission("file.delete");
        developer.addPermission("network.http");
        developer.addPermission("ui.menu.modify");
        developer.addPermission("ui.toolbar.modify");
        developer.addPermission("ui.dialog.show");
        developer.addPermission("project.create");
        developer.addPermission("project.modify");
        developer.addPermission("project.delete");
        developer.addPermission("project.export");
        developer.addPermission("editor.read");
        developer.addPermission("editor.write");
        developer.addPermission("editor.format");
        developer.addPermission("editor.refactor");
        developer.addPermission("debug.breakpoint");
        developer.addPermission("debug.execute");
        developer.addPermission("debug.inspect");

        // Admin role - full permissions
        Role admin = createRole("admin", "Administrator", "Full system access");
        for (Permission permission : permissions.values()) {
            admin.addPermission(permission.getId());
        }

        log.info("Default roles initialized: guest, user, developer, admin");
    }

    private void addAuditEntry(String pluginId, String action, String resourceId, String details) {
        List<AuditEntry> entries = auditLogs.computeIfAbsent(pluginId, k -> new CopyOnWriteArrayList<>());
        AuditEntry entry = new AuditEntry(action, resourceId, details);
        entries.add(entry);

        // Keep only last 1000 entries per plugin
        if (entries.size() > 1000) {
            entries.remove(0);
        }
    }

    /**
     * Implementation of Permission interface
     */
    private static class PermissionImpl implements Permission {
        private final String id;
        private final String description;
        private final String category;
        private final boolean system;

        public PermissionImpl(String id, String description, String category, boolean system) {
            this.id = id;
            this.description = description;
            this.category = category;
            this.system = system;
        }

        @Override
        public String getId() { return id; }

        @Override
        public String getDescription() { return description; }

        @Override
        public String getCategory() { return category; }

        @Override
        public boolean isSystemPermission() { return system; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PermissionImpl that = (PermissionImpl) o;
            return Objects.equals(id, that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public String toString() {
            return String.format("Permission{id='%s', category='%s', system=%s}", id, category, system);
        }
    }

    /**
     * Implementation of Role interface
     */
    private static class RoleImpl implements Role {
        private final String id;
        private final String name;
        private final String description;
        private final Set<String> permissionIds = new CopyOnWriteArraySet<>();

        public RoleImpl(String id, String name, String description) {
            this.id = id;
            this.name = name;
            this.description = description;
        }

        @Override
        public String getId() { return id; }

        @Override
        public String getName() { return name; }

        @Override
        public String getDescription() { return description; }

        @Override
        public Set<String> getPermissionIds() { return Collections.unmodifiableSet(permissionIds); }

        @Override
        public boolean addPermission(String permissionId) {
            boolean added = permissionIds.add(permissionId);
            if (added) {
                log.debug("Permission '{}' added to role '{}'", permissionId, id);
            }
            return added;
        }

        @Override
        public boolean removePermission(String permissionId) {
            boolean removed = permissionIds.remove(permissionId);
            if (removed) {
                log.debug("Permission '{}' removed from role '{}'", permissionId, id);
            }
            return removed;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RoleImpl role = (RoleImpl) o;
            return Objects.equals(id, role.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }

        @Override
        public String toString() {
            return String.format("Role{id='%s', name='%s', permissions=%d}", id, name, permissionIds.size());
        }
    }

    /**
     * Audit entry for permission changes
     */
    private static class AuditEntry {
        final long timestamp;
        final String action;
        final String resourceId;
        final String details;

        AuditEntry(String action, String resourceId, String details) {
            this.timestamp = System.currentTimeMillis();
            this.action = action;
            this.resourceId = resourceId;
            this.details = details;
        }
    }
}