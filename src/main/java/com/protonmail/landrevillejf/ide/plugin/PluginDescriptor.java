package com.protonmail.landrevillejf.ide.plugin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Descriptor containing metadata about a plugin.
 * <p>
 * This class holds all the metadata information about a plugin,
 * including identification, versioning, and descriptive information.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PluginDescriptor {

    // Basic information
    private String id;
    private String name;
    private String version;
    private String mainClass;
    private String description;
    private String author;

    // Extended information for Plugin interface compatibility
    private String authorEmail = "";
    private String category = "General";
    private String requiredHostVersion = "1.0.0";

    // Manifest information
    private String specificationTitle = "";
    private String specificationVersion = "";
    private String specificationVendor = "";
    private String implementationVersion = "";

    // Plugin metadata
    private Date creationDate = new Date();
    private Date lastModifiedDate = new Date();
    private boolean enabledByDefault = false;
    private boolean autoStart = false;

    // Plugin capabilities
    private boolean providesMenu = false;
    private boolean providesToolbar = false;
    private boolean providesServices = false;
    private boolean requiresNetwork = false;

    /**
     * Creates a minimal PluginDescriptor with required fields.
     *
     * @param id The unique plugin identifier.
     * @param name The plugin name.
     * @param version The plugin version.
     * @param mainClass The main class of the plugin.
     * @param description The plugin description.
     * @param author The plugin author.
     */
    public PluginDescriptor(String id, String name, String version,
                            String mainClass, String description, String author) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.mainClass = mainClass;
        this.description = description;
        this.author = author;
        this.creationDate = new Date();
        this.lastModifiedDate = new Date();
    }

    /**
     * Creates a copy of this PluginDescriptor.
     *
     * @return A new PluginDescriptor with the same data.
     */
    public PluginDescriptor copy() {
        PluginDescriptor copy = new PluginDescriptor();
        copy.id = this.id;
        copy.name = this.name;
        copy.version = this.version;
        copy.mainClass = this.mainClass;
        copy.description = this.description;
        copy.author = this.author;
        copy.authorEmail = this.authorEmail;
        copy.category = this.category;
        copy.requiredHostVersion = this.requiredHostVersion;
        copy.specificationTitle = this.specificationTitle;
        copy.specificationVersion = this.specificationVersion;
        copy.specificationVendor = this.specificationVendor;
        copy.implementationVersion = this.implementationVersion;
        copy.creationDate = this.creationDate != null ? new Date(this.creationDate.getTime()) : null;
        copy.lastModifiedDate = new Date();
        copy.enabledByDefault = this.enabledByDefault;
        copy.autoStart = this.autoStart;
        copy.providesMenu = this.providesMenu;
        copy.providesToolbar = this.providesToolbar;
        copy.providesServices = this.providesServices;
        copy.requiresNetwork = this.requiresNetwork;
        return copy;
    }

    /**
     * Checks if this descriptor is valid.
     *
     * @return true if the descriptor has all required fields, false otherwise.
     */
    public boolean isValid() {
        return id != null && !id.trim().isEmpty() &&
                name != null && !name.trim().isEmpty() &&
                version != null && !version.trim().isEmpty() &&
                mainClass != null && !mainClass.trim().isEmpty() &&
                description != null && !description.trim().isEmpty() &&
                author != null && !author.trim().isEmpty();
    }

    /**
     * Gets a string representation of the plugin identifier.
     *
     * @return The plugin identifier in format "name-version".
     */
    public String getPluginId() {
        return name + "-" + version;
    }

    /**
     * Updates the last modified date to the current time.
     */
    public void touch() {
        this.lastModifiedDate = new Date();
    }

    /**
     * Returns a summary of the plugin descriptor.
     *
     * @return A string summary of the plugin.
     */
    public String getSummary() {
        return String.format("%s v%s by %s - %s",
                name, version, author,
                description.length() > 50 ? description.substring(0, 47) + "..." : description);
    }

    /**
     * Checks if this plugin is compatible with the given host version.
     *
     * @param hostVersion The host application version.
     * @return true if compatible, false otherwise.
     */
    public boolean isCompatibleWith(String hostVersion) {
        if (requiredHostVersion == null || requiredHostVersion.isEmpty()) {
            return true;
        }

        try {
            return compareVersions(hostVersion, requiredHostVersion) >= 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Compares two version strings.
     *
     * @param version1 First version string.
     * @param version2 Second version string.
     * @return Negative if version1 < version2, zero if equal, positive if version1 > version2.
     */
    private int compareVersions(String version1, String version2) {
        String[] parts1 = version1.split("\\.");
        String[] parts2 = version2.split("\\.");

        int maxLength = Math.max(parts1.length, parts2.length);

        for (int i = 0; i < maxLength; i++) {
            int part1 = i < parts1.length ? Integer.parseInt(parts1[i]) : 0;
            int part2 = i < parts2.length ? Integer.parseInt(parts2[i]) : 0;

            if (part1 != part2) {
                return part1 - part2;
            }
        }

        return 0;
    }

    @Override
    public String toString() {
        return "PluginDescriptor{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", mainClass='" + mainClass + '\'' +
                ", author='" + author + '\'' +
                ", category='" + category + '\'' +
                '}';
    }
}