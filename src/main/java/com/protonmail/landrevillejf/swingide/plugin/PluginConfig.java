package com.protonmail.landrevillejf.swingide.plugin;

import java.util.*;

/**
 * Configuration container for plugin settings and features.
 * <p>
 * This class provides a structured way to manage plugin configuration,
 * including automatic enabling, custom settings, and feature toggles.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
public class PluginConfig {

    /**
     * Default configuration instance with all settings at their default values.
     */
    public static final PluginConfig DEFAULT = new PluginConfig();

    private boolean autoEnable = false;
    private Map<String, Object> settings = new HashMap<>();
    private List<String> enabledFeatures = new ArrayList<>();
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * Creates a new PluginConfig with default values.
     */
    public PluginConfig() {
        // Initialize with default metadata
        metadata.put("created", System.currentTimeMillis());
        metadata.put("version", "1.0");
    }

    /**
     * Creates a new PluginConfig from a map representation.
     *
     * @param map The map containing configuration data.
     */
    public PluginConfig(Map<String, Object> map) {
        this();
        fromMap(map);
    }

    /**
     * Checks if the plugin should be enabled automatically on startup.
     *
     * @return {@code true} if auto-enable is enabled, {@code false} otherwise.
     */
    public boolean isAutoEnable() {
        return autoEnable;
    }

    /**
     * Sets whether the plugin should be enabled automatically on startup.
     *
     * @param autoEnable {@code true} to enable auto-enable, {@code false} otherwise.
     */
    public void setAutoEnable(boolean autoEnable) {
        this.autoEnable = autoEnable;
        metadata.put("lastModified", System.currentTimeMillis());
    }

    /**
     * Gets the settings map.
     *
     * @return An unmodifiable view of the settings map.
     */
    public Map<String, Object> getSettings() {
        return Collections.unmodifiableMap(settings);
    }

    /**
     * Sets a specific setting.
     *
     * @param key   The setting key.
     * @param value The setting value.
     */
    public void setSetting(String key, Object value) {
        settings.put(key, value);
        metadata.put("lastModified", System.currentTimeMillis());
    }

    /**
     * Gets a specific setting with a default value if not present.
     *
     * @param key          The setting key.
     * @param defaultValue The default value to return if the key is not found.
     * @return The setting value, or the default value if not found.
     */
    public Object getSetting(String key, Object defaultValue) {
        return settings.getOrDefault(key, defaultValue);
    }

    /**
     * Gets a specific setting.
     *
     * @param key The setting key.
     * @return The setting value, or {@code null} if not found.
     */
    public Object getSetting(String key) {
        return settings.get(key);
    }

    /**
     * Gets a specific setting as a string.
     *
     * @param key          The setting key.
     * @param defaultValue The default value to return if not found or not a string.
     * @return The setting value as a string, or the default value.
     */
    public String getSettingAsString(String key, String defaultValue) {
        Object value = settings.get(key);
        return value != null ? value.toString() : defaultValue;
    }

    /**
     * Gets a specific setting as a boolean.
     *
     * @param key          The setting key.
     * @param defaultValue The default value to return if not found or not a boolean.
     * @return The setting value as a boolean, or the default value.
     */
    public boolean getSettingAsBoolean(String key, boolean defaultValue) {
        Object value = settings.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return defaultValue;
    }

    /**
     * Gets a specific setting as an integer.
     *
     * @param key          The setting key.
     * @param defaultValue The default value to return if not found or not an integer.
     * @return The setting value as an integer, or the default value.
     */
    public int getSettingAsInt(String key, int defaultValue) {
        Object value = settings.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    /**
     * Removes a specific setting.
     *
     * @param key The setting key to remove.
     * @return The removed value, or {@code null} if not found.
     */
    public Object removeSetting(String key) {
        Object removed = settings.remove(key);
        if (removed != null) {
            metadata.put("lastModified", System.currentTimeMillis());
        }
        return removed;
    }

    /**
     * Checks if a setting exists.
     *
     * @param key The setting key to check.
     * @return {@code true} if the setting exists, {@code false} otherwise.
     */
    public boolean hasSetting(String key) {
        return settings.containsKey(key);
    }

    /**
     * Gets the list of enabled features.
     *
     * @return An unmodifiable view of the enabled features list.
     */
    public List<String> getEnabledFeatures() {
        return Collections.unmodifiableList(enabledFeatures);
    }

    /**
     * Enables a specific feature.
     *
     * @param feature The feature to enable.
     */
    public void enableFeature(String feature) {
        if (!enabledFeatures.contains(feature)) {
            enabledFeatures.add(feature);
            metadata.put("lastModified", System.currentTimeMillis());
        }
    }

    /**
     * Disables a specific feature.
     *
     * @param feature The feature to disable.
     */
    public void disableFeature(String feature) {
        if (enabledFeatures.remove(feature)) {
            metadata.put("lastModified", System.currentTimeMillis());
        }
    }

    /**
     * Checks if a feature is enabled.
     *
     * @param feature The feature to check.
     * @return {@code true} if the feature is enabled, {@code false} otherwise.
     */
    public boolean isFeatureEnabled(String feature) {
        return enabledFeatures.contains(feature);
    }

    /**
     * Gets the metadata map.
     *
     * @return An unmodifiable view of the metadata map.
     */
    public Map<String, Object> getMetadata() {
        return Collections.unmodifiableMap(metadata);
    }

    /**
     * Sets a metadata entry.
     *
     * @param key   The metadata key.
     * @param value The metadata value.
     */
    public void setMetadata(String key, Object value) {
        metadata.put(key, value);
    }

    /**
     * Converts the configuration to a map representation.
     * <p>
     * The resulting map contains all configuration data in a serializable format
     * suitable for storage or transmission.
     * </p>
     *
     * @return A map containing all configuration data.
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();

        // Add basic configuration
        map.put("autoEnable", autoEnable);

        // Add settings (deep copy)
        Map<String, Object> settingsCopy = new HashMap<>();
        for (Map.Entry<String, Object> entry : settings.entrySet()) {
            settingsCopy.put(entry.getKey(), deepCopy(entry.getValue()));
        }
        map.put("settings", settingsCopy);

        // Add enabled features (copy)
        map.put("enabledFeatures", new ArrayList<>(enabledFeatures));

        // Add metadata (deep copy)
        Map<String, Object> metadataCopy = new HashMap<>();
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            metadataCopy.put(entry.getKey(), deepCopy(entry.getValue()));
        }
        map.put("metadata", metadataCopy);

        return Collections.unmodifiableMap(map);
    }

    /**
     * Loads configuration from a map representation.
     *
     * @param map The map containing configuration data.
     */
    public void fromMap(Map<String, Object> map) {
        if (map == null) {
            return;
        }

        // Load autoEnable
        Object autoEnableObj = map.get("autoEnable");
        if (autoEnableObj instanceof Boolean) {
            this.autoEnable = (Boolean) autoEnableObj;
        }

        // Load settings
        Object settingsObj = map.get("settings");
        if (settingsObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> settingsMap = (Map<String, Object>) settingsObj;
            this.settings.clear();
            this.settings.putAll(settingsMap);
        }

        // Load enabled features
        Object featuresObj = map.get("enabledFeatures");
        if (featuresObj instanceof Collection) {
            @SuppressWarnings("unchecked")
            Collection<Object> featuresCollection = (Collection<Object>) featuresObj;
            this.enabledFeatures.clear();
            for (Object feature : featuresCollection) {
                if (feature != null) {
                    this.enabledFeatures.add(feature.toString());
                }
            }
        }

        // Load metadata
        Object metadataObj = map.get("metadata");
        if (metadataObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> metadataMap = (Map<String, Object>) metadataObj;
            this.metadata.clear();
            this.metadata.putAll(metadataMap);
        }

        // Update metadata
        this.metadata.put("lastLoaded", System.currentTimeMillis());
    }

    /**
     * Creates a deep copy of an object for serialization.
     * <p>
     * This method handles basic types, collections, and maps.
     * For complex objects, it returns a string representation.
     * </p>
     *
     * @param obj The object to copy.
     * @return A serializable copy of the object.
     */
    private Object deepCopy(Object obj) {
        if (obj == null) {
            return null;
        }

        if (obj instanceof String ||
                obj instanceof Number ||
                obj instanceof Boolean ||
                obj instanceof Character) {
            return obj;
        }

        if (obj instanceof List) {
            List<Object> list = new ArrayList<>();
            for (Object item : (List<?>) obj) {
                list.add(deepCopy(item));
            }
            return list;
        }

        if (obj instanceof Set) {
            Set<Object> set = new HashSet<>();
            for (Object item : (Set<?>) obj) {
                set.add(deepCopy(item));
            }
            return set;
        }

        if (obj instanceof Map) {
            Map<Object, Object> map = new HashMap<>();
            @SuppressWarnings("unchecked")
            Map<Object, Object> original = (Map<Object, Object>) obj;
            for (Map.Entry<Object, Object> entry : original.entrySet()) {
                map.put(deepCopy(entry.getKey()), deepCopy(entry.getValue()));
            }
            return map;
        }

        // For other objects, return string representation
        return obj.toString();
    }

    /**
     * Merges another configuration into this one.
     * <p>
     * Existing settings are preserved unless overridden by the other configuration.
     * </p>
     *
     * @param other The configuration to merge.
     */
    public void merge(PluginConfig other) {
        if (other == null) {
            return;
        }

        // Merge autoEnable
        this.autoEnable = other.autoEnable;

        // Merge settings
        this.settings.putAll(other.settings);

        // Merge enabled features (union)
        for (String feature : other.enabledFeatures) {
            if (!this.enabledFeatures.contains(feature)) {
                this.enabledFeatures.add(feature);
            }
        }

        // Merge metadata (prefer other's metadata for conflicting keys)
        this.metadata.putAll(other.metadata);

        metadata.put("lastModified", System.currentTimeMillis());
        metadata.put("merged", true);
    }

    /**
     * Creates a copy of this configuration.
     *
     * @return A new PluginConfig with the same data.
     */
    public PluginConfig copy() {
        PluginConfig copy = new PluginConfig();
        copy.autoEnable = this.autoEnable;
        copy.settings = new HashMap<>(this.settings);
        copy.enabledFeatures = new ArrayList<>(this.enabledFeatures);
        copy.metadata = new HashMap<>(this.metadata);
        return copy;
    }

    /**
     * Resets the configuration to default values.
     */
    public void reset() {
        this.autoEnable = false;
        this.settings.clear();
        this.enabledFeatures.clear();
        this.metadata.clear();
        this.metadata.put("created", System.currentTimeMillis());
        this.metadata.put("version", "1.0");
        this.metadata.put("reset", true);
    }

    /**
     * Validates the configuration.
     *
     * @return A list of validation errors, empty if valid.
     */
    public List<String> validate() {
        List<String> errors = new ArrayList<>();

        // Validate settings values
        for (Map.Entry<String, Object> entry : settings.entrySet()) {
            if (entry.getKey() == null || entry.getKey().trim().isEmpty()) {
                errors.add("Setting key cannot be null or empty");
            }
            if (entry.getValue() == null) {
                errors.add("Setting value for key '" + entry.getKey() + "' cannot be null");
            }
        }

        // Validate feature names
        for (String feature : enabledFeatures) {
            if (feature == null || feature.trim().isEmpty()) {
                errors.add("Feature name cannot be null or empty");
            }
        }

        return errors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PluginConfig that = (PluginConfig) o;

        if (autoEnable != that.autoEnable) return false;
        if (!settings.equals(that.settings)) return false;
        if (!enabledFeatures.equals(that.enabledFeatures)) return false;
        return metadata.equals(that.metadata);
    }

    @Override
    public int hashCode() {
        int result = (autoEnable ? 1 : 0);
        result = 31 * result + settings.hashCode();
        result = 31 * result + enabledFeatures.hashCode();
        result = 31 * result + metadata.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "PluginConfig{" +
                "autoEnable=" + autoEnable +
                ", settings=" + settings.size() + " entries" +
                ", enabledFeatures=" + enabledFeatures.size() + " features" +
                ", metadata=" + metadata.size() + " entries" +
                '}';
    }
}