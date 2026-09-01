package com.protonmail.landrevillejf.ide.plugin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PluginConfigTest {

    private PluginConfig config;

    @BeforeEach
    void setUp() {
        config = new PluginConfig();
    }

    // ==================== CONSTRUCTOR TESTS ====================

    @Test
    void defaultConstructor_ShouldInitializeEmptyConfig() {
        assertNotNull(config);
        assertFalse(config.isAutoEnable());
        assertTrue(config.getSettings().isEmpty());
        assertTrue(config.getEnabledFeatures().isEmpty());
        assertNotNull(config.getMetadata());
        assertTrue(config.getMetadata().containsKey("created"));
        assertTrue(config.getMetadata().containsKey("version"));
    }

    @Test
    void mapConstructor_ShouldLoadFromMap() {
        // Given
        final Map<String, Object> map = new HashMap<>();
        map.put("autoEnable", true);

        // When
        final PluginConfig loadedConfig = new PluginConfig(map);

        // Then
        assertTrue(loadedConfig.isAutoEnable());
    }

    @Test
    void mapConstructor_WithNullMap_ShouldNotThrowException() {
        assertDoesNotThrow(() -> new PluginConfig(null));
    }

    // ==================== AUTO_ENABLE TESTS ====================

    @Test
    void isAutoEnable_ShouldReturnFalse_ByDefault() {
        assertFalse(config.isAutoEnable());
    }

    @Test
    void isAutoEnable_ShouldReturnTrue_AfterSetToTrue() {
        config.setAutoEnable(true);
        assertTrue(config.isAutoEnable());
    }

    @Test
    void setAutoEnable_ShouldUpdateLastModifiedMetadata() {
        final long before = System.currentTimeMillis();
        config.setAutoEnable(true);
        final Object lastModified = config.getMetadata().get("lastModified");

        assertNotNull(lastModified);
        assertTrue((Long) lastModified >= before);
    }

    // ==================== SETTINGS TESTS ====================

    @Test
    void setSetting_ShouldStoreValue() {
        config.setSetting("testKey", "testValue");

        assertEquals("testValue", config.getSetting("testKey"));
    }

    @Test
    void setSetting_ShouldUpdateLastModified() {
        final long before = System.currentTimeMillis();
        config.setSetting("key", "value");

        final Object lastModified = config.getMetadata().get("lastModified");
        assertNotNull(lastModified);
        assertTrue((Long) lastModified >= before);
    }

    @Test
    void getSetting_WithDefault_ShouldReturnValue_WhenKeyExists() {
        config.setSetting("key", "value");

        assertEquals("value", config.getSetting("key", "default"));
    }

    @Test
    void getSetting_WithDefault_ShouldReturnDefault_WhenKeyDoesNotExist() {
        assertEquals("default", config.getSetting("nonexistent", "default"));
    }

    @Test
    void getSetting_WithoutDefault_ShouldReturnValue_WhenKeyExists() {
        config.setSetting("key", "value");

        assertEquals("value", config.getSetting("key"));
    }

    @Test
    void getSetting_WithoutDefault_ShouldReturnNull_WhenKeyDoesNotExist() {
        assertNull(config.getSetting("nonexistent"));
    }

    @Test
    void getSettings_ShouldReturnUnmodifiableMap() {
        final Map<String, Object> settings = config.getSettings();

        assertThrows(UnsupportedOperationException.class, () -> settings.put("key", "value"));
    }

    @Test
    void hasSetting_ShouldReturnTrue_WhenKeyExists() {
        config.setSetting("key", "value");

        assertTrue(config.hasSetting("key"));
    }

    @Test
    void hasSetting_ShouldReturnFalse_WhenKeyDoesNotExist() {
        assertFalse(config.hasSetting("nonexistent"));
    }

    @Test
    void removeSetting_ShouldRemoveAndReturnValue() {
        config.setSetting("key", "value");

        final Object removed = config.removeSetting("key");

        assertEquals("value", removed);
        assertFalse(config.hasSetting("key"));
    }

    @Test
    void removeSetting_ShouldReturnNull_WhenKeyDoesNotExist() {
        assertNull(config.removeSetting("nonexistent"));
    }

    // ==================== GETTER AS TYPE TESTS ====================

    @Test
    void getSettingAsString_ShouldReturnValueAsString() {
        config.setSetting("key", 123);

        assertEquals("123", config.getSettingAsString("key", "default"));
    }

    @Test
    void getSettingAsString_ShouldReturnDefault_WhenKeyDoesNotExist() {
        assertEquals("default", config.getSettingAsString("nonexistent", "default"));
    }

    @Test
    void getSettingAsBoolean_ShouldReturnBoolean_WhenValueIsBoolean() {
        config.setSetting("key", true);

        assertTrue(config.getSettingAsBoolean("key", false));
    }

    @Test
    void getSettingAsBoolean_ShouldParseString_WhenValueIsString() {
        config.setSetting("key", "true");

        assertTrue(config.getSettingAsBoolean("key", false));
    }

    @Test
    void getSettingAsBoolean_ShouldReturnDefault_WhenValueIsNotBooleanOrString() {
        config.setSetting("key", 123);

        assertTrue(config.getSettingAsBoolean("key", true));
    }

    @Test
    void getSettingAsBoolean_ShouldReturnDefault_WhenKeyDoesNotExist() {
        assertTrue(config.getSettingAsBoolean("nonexistent", true));
    }

    @Test
    void getSettingAsInt_ShouldReturnInteger_WhenValueIsInteger() {
        config.setSetting("key", 42);

        assertEquals(42, config.getSettingAsInt("key", 0));
    }

    @Test
    void getSettingAsInt_ShouldConvertNumber_WhenValueIsNumber() {
        config.setSetting("key", 42L);

        assertEquals(42, config.getSettingAsInt("key", 0));
    }

    @Test
    void getSettingAsInt_ShouldParseString_WhenValueIsString() {
        config.setSetting("key", "42");

        assertEquals(42, config.getSettingAsInt("key", 0));
    }

    @Test
    void getSettingAsInt_ShouldReturnDefault_WhenStringIsInvalidNumber() {
        config.setSetting("key", "not a number");

        assertEquals(99, config.getSettingAsInt("key", 99));
    }

    @Test
    void getSettingAsInt_ShouldReturnDefault_WhenKeyDoesNotExist() {
        assertEquals(99, config.getSettingAsInt("nonexistent", 99));
    }

    // ==================== FEATURE TESTS ====================

    @Test
    void enableFeature_ShouldAddFeature() {
        config.enableFeature("testFeature");

        assertTrue(config.isFeatureEnabled("testFeature"));
        assertTrue(config.getEnabledFeatures().contains("testFeature"));
    }

    @Test
    void enableFeature_ShouldNotAddDuplicate() {
        config.enableFeature("testFeature");
        config.enableFeature("testFeature");

        assertEquals(1, config.getEnabledFeatures().size());
    }

    @Test
    void disableFeature_ShouldRemoveFeature() {
        config.enableFeature("testFeature");
        config.disableFeature("testFeature");

        assertFalse(config.isFeatureEnabled("testFeature"));
    }

    @Test
    void disableFeature_ShouldDoNothing_WhenFeatureNotEnabled() {
        config.disableFeature("nonexistent");

        assertTrue(config.getEnabledFeatures().isEmpty());
    }

    @Test
    void getEnabledFeatures_ShouldReturnUnmodifiableList() {
        final List<String> features = config.getEnabledFeatures();

        assertThrows(UnsupportedOperationException.class, () -> features.add("test"));
    }

    // ==================== METADATA TESTS ====================

    @Test
    void setMetadata_ShouldStoreValue() {
        config.setMetadata("customKey", "customValue");

        assertEquals("customValue", config.getMetadata().get("customKey"));
    }

    @Test
    void getMetadata_ShouldReturnUnmodifiableMap() {
        final Map<String, Object> metadata = config.getMetadata();

        assertThrows(UnsupportedOperationException.class, () -> metadata.put("key", "value"));
    }

    // ==================== FROM_MAP TESTS ====================

    @Test
    void fromMap_WithNullMap_ShouldDoNothing() {
        assertDoesNotThrow(() -> config.fromMap(null));
        assertFalse(config.isAutoEnable());
    }

    @Test
    void fromMap_ShouldLoadAutoEnable() {
        final Map<String, Object> map = new HashMap<>();
        map.put("autoEnable", true);

        config.fromMap(map);

        assertTrue(config.isAutoEnable());
    }

    @Test
    void fromMap_WithNonBooleanAutoEnable_ShouldIgnore() {
        final Map<String, Object> map = new HashMap<>();
        map.put("autoEnable", "not a boolean");

        config.fromMap(map);

        assertFalse(config.isAutoEnable());
    }

    @Test
    void fromMap_ShouldLoadSettings() {
        final Map<String, Object> settingsMap = new HashMap<>();
        settingsMap.put("key1", "value1");
        settingsMap.put("key2", 42);

        final Map<String, Object> map = new HashMap<>();
        map.put("settings", settingsMap);

        config.fromMap(map);

        assertEquals("value1", config.getSetting("key1"));
        assertEquals(42, config.getSetting("key2"));
    }

    @Test
    void fromMap_WithNonMapSettings_ShouldIgnore() {
        final Map<String, Object> map = new HashMap<>();
        map.put("settings", "not a map");

        config.fromMap(map);

        assertTrue(config.getSettings().isEmpty());
    }

    @Test
    void fromMap_ShouldLoadEnabledFeatures() {
        final List<String> features = Arrays.asList("feature1", "feature2");

        final Map<String, Object> map = new HashMap<>();
        map.put("enabledFeatures", features);

        config.fromMap(map);

        assertTrue(config.isFeatureEnabled("feature1"));
        assertTrue(config.isFeatureEnabled("feature2"));
    }

    @Test
    void fromMap_WithNonCollectionFeatures_ShouldIgnore() {
        final Map<String, Object> map = new HashMap<>();
        map.put("enabledFeatures", "not a collection");

        config.fromMap(map);

        assertTrue(config.getEnabledFeatures().isEmpty());
    }

    @Test
    void fromMap_ShouldLoadMetadata() {
        final Map<String, Object> metadataMap = new HashMap<>();
        metadataMap.put("customKey", "customValue");

        final Map<String, Object> map = new HashMap<>();
        map.put("metadata", metadataMap);

        config.fromMap(map);

        assertEquals("customValue", config.getMetadata().get("customKey"));
    }

    @Test
    void fromMap_WithNonMapMetadata_ShouldIgnore() {
        final Map<String, Object> map = new HashMap<>();
        map.put("metadata", "not a map");

        config.fromMap(map);

        assertNotNull(config.getMetadata());
    }

    @Test
    void fromMap_ShouldAddLastLoadedMetadata() {
        final Map<String, Object> map = new HashMap<>();
        config.fromMap(map);

        assertTrue(config.getMetadata().containsKey("lastLoaded"));
    }

    // ==================== TO_MAP TESTS ====================

    @Test
    void toMap_ShouldContainAllConfigData() {
        config.setAutoEnable(true);
        config.setSetting("testKey", "testValue");
        config.enableFeature("testFeature");

        final Map<String, Object> map = config.toMap();

        assertTrue((Boolean) map.get("autoEnable"));
        assertNotNull(map.get("settings"));
        assertNotNull(map.get("enabledFeatures"));
        assertNotNull(map.get("metadata"));
    }

    @Test
    void toMap_ShouldReturnUnmodifiableMap() {
        final Map<String, Object> map = config.toMap();

        assertThrows(UnsupportedOperationException.class, () -> map.put("key", "value"));
    }

    // ==================== MERGE TESTS ====================

    @Test
    void merge_WithNull_ShouldDoNothing() {
        assertDoesNotThrow(() -> config.merge(null));
    }

    @Test
    void merge_ShouldMergeAutoEnable() {
        final PluginConfig other = new PluginConfig();
        other.setAutoEnable(true);

        config.merge(other);

        assertTrue(config.isAutoEnable());
    }

    @Test
    void merge_ShouldMergeSettings() {
        config.setSetting("existing", "oldValue");
        final PluginConfig other = new PluginConfig();
        other.setSetting("newKey", "newValue");
        other.setSetting("existing", "newValue");

        config.merge(other);

        assertEquals("newValue", config.getSetting("newKey"));
        assertEquals("newValue", config.getSetting("existing"));
    }

    @Test
    void merge_ShouldMergeEnabledFeatures() {
        config.enableFeature("feature1");
        final PluginConfig other = new PluginConfig();
        other.enableFeature("feature2");

        config.merge(other);

        assertTrue(config.isFeatureEnabled("feature1"));
        assertTrue(config.isFeatureEnabled("feature2"));
    }

    @Test
    void merge_ShouldMergeMetadata() {
        config.setMetadata("key1", "value1");
        final PluginConfig other = new PluginConfig();
        other.setMetadata("key2", "value2");

        config.merge(other);

        assertEquals("value1", config.getMetadata().get("key1"));
        assertEquals("value2", config.getMetadata().get("key2"));
    }

    @Test
    void merge_ShouldAddMergeMetadata() {
        final PluginConfig other = new PluginConfig();
        config.merge(other);

        assertTrue((Boolean) config.getMetadata().get("merged"));
        assertTrue(config.getMetadata().containsKey("lastModified"));
    }

    // ==================== COPY TESTS ====================

    @Test
    void copy_ShouldCreateIndependentCopy() {
        config.setAutoEnable(true);
        config.setSetting("key", "value");
        config.enableFeature("feature");

        final PluginConfig copy = config.copy();

        assertEquals(config.isAutoEnable(), copy.isAutoEnable());
        assertEquals(config.getSetting("key"), copy.getSetting("key"));
        assertTrue(copy.isFeatureEnabled("feature"));

        // Modify copy, original should remain unchanged
        copy.setAutoEnable(false);
        assertTrue(config.isAutoEnable());
    }

    // ==================== RESET TESTS ====================

    @Test
    void reset_ShouldClearAllSettings() {
        config.setAutoEnable(true);
        config.setSetting("key", "value");
        config.enableFeature("feature");

        config.reset();

        assertFalse(config.isAutoEnable());
        assertTrue(config.getSettings().isEmpty());
        assertTrue(config.getEnabledFeatures().isEmpty());
    }

    @Test
    void reset_ShouldAddResetMetadata() {
        config.reset();

        assertTrue((Boolean) config.getMetadata().get("reset"));
        assertEquals("1.0", config.getMetadata().get("version"));
    }

    // ==================== VALIDATE TESTS ====================

    @Test
    void validate_ShouldReturnEmptyList_WhenConfigIsValid() {
        config.setSetting("validKey", "validValue");
        config.enableFeature("validFeature");

        final List<String> errors = config.validate();

        assertTrue(errors.isEmpty());
    }

    @Test
    void validate_ShouldReturnError_WhenSettingKeyIsNull() {
        config.setSetting(null, "value");

        final List<String> errors = config.validate();

        assertTrue(errors.stream().anyMatch(e -> e.contains("Setting key cannot be null")));
    }

    @Test
    void validate_ShouldReturnError_WhenSettingKeyIsEmpty() {
        config.setSetting("", "value");

        final List<String> errors = config.validate();

        assertTrue(errors.stream().anyMatch(e -> e.contains("Setting key cannot be null or empty")));
    }

    @Test
    void validate_ShouldReturnError_WhenSettingValueIsNull() {
        config.setSetting("key", null);

        final List<String> errors = config.validate();

        assertTrue(errors.stream().anyMatch(e -> e.contains("Setting value for key 'key' cannot be null")));
    }

    @Test
    void validate_ShouldReturnError_WhenFeatureNameIsNull() {
        config.enableFeature(null);

        final List<String> errors = config.validate();

        assertTrue(errors.stream().anyMatch(e -> e.contains("Feature name cannot be null or empty")));
    }

    @Test
    void validate_ShouldReturnError_WhenFeatureNameIsEmpty() {
        config.enableFeature("");

        final List<String> errors = config.validate();

        assertTrue(errors.stream().anyMatch(e -> e.contains("Feature name cannot be null or empty")));
    }

    // ==================== EQUALS AND HASHCODE TESTS ====================

    @Test
    void equals_ShouldReturnTrue_ForSameInstance() {
        assertEquals(config, config);
    }

    @Test
    void equals_ShouldReturnFalse_ForNull() {
        assertNotEquals(null, config);
    }

    @Test
    void equals_ShouldReturnFalse_ForDifferentClass() {
        assertNotEquals("not a config", config);
    }

    @Test
    void equals_ShouldReturnFalse_ForDifferentAutoEnable() {
        final PluginConfig other = new PluginConfig();
        config.setAutoEnable(true);
        other.setAutoEnable(false);

        assertNotEquals(config, other);
    }

    @Test
    void hashCode_ShouldBeConsistent() {
        final int firstHash = config.hashCode();
        final int secondHash = config.hashCode();

        assertEquals(firstHash, secondHash);
    }

    @Test
    void hashCode_ShouldDiffer_WhenAutoEnableDiffers() {
        // Covers the true side of the autoEnable ternary in hashCode()
        final PluginConfig autoEnabled = new PluginConfig();
        autoEnabled.setAutoEnable(true);

        assertNotEquals(config.hashCode(), autoEnabled.hashCode());
    }

    // ==================== TOSTRING TESTS ====================

    @Test
    void toString_ShouldNotReturnNull() {
        assertNotNull(config.toString());
        assertTrue(config.toString().contains("PluginConfig"));
    }

    // ==================== DEEP_COPY TESTS (via toMap) ====================

    @Test
    void toMap_ShouldDeepCopyNestedStructures() {
        final Map<String, Object> nestedMap = new HashMap<>();
        nestedMap.put("nestedKey", "nestedValue");

        config.setSetting("nested", nestedMap);
        config.setSetting("list", Arrays.asList(1, 2, 3));

        final Map<String, Object> map = config.toMap();
        final Map<String, Object> settingsMap = (Map<String, Object>) map.get("settings");

        // Modify the returned map, original should not change
        ((Map<String, Object>) settingsMap.get("nested")).put("nestedKey", "modified");

        assertEquals("nestedValue", ((Map<String, Object>) config.getSetting("nested")).get("nestedKey"));
    }

    // ── Additional tests to cover missed branches ──────────────────────────

    @Test
    @DisplayName("equals returns true for same object")
    void testEquals_SameObject_ReturnsTrue() {
        assertEquals(config, config);
    }

    @Test
    @DisplayName("equals returns false for null")
    void testEquals_Null_ReturnsFalse() {
        assertNotEquals(config, null);
    }

    @Test
    @DisplayName("equals returns false for different class")
    void testEquals_DifferentClass_ReturnsFalse() {
        assertNotEquals(config, "not a config");
    }

    @Test
    @DisplayName("equals returns false when autoEnable differs")
    void testEquals_DifferentAutoEnable_ReturnsFalse() {
        PluginConfig other = new PluginConfig();
        config.setAutoEnable(true);
        other.setAutoEnable(false);
        assertNotEquals(config, other);
    }

    @Test
    @DisplayName("equals returns false when settings differ")
    void testEquals_DifferentSettings_ReturnsFalse() {
        PluginConfig other = new PluginConfig();
        config.setSetting("key", "value1");
        other.setSetting("key", "value2");
        assertNotEquals(config, other);
    }

    @Test
    @DisplayName("equals returns false when enabledFeatures differ")
    void testEquals_DifferentFeatures_ReturnsFalse() {
        PluginConfig other = new PluginConfig();
        config.enableFeature("featureA");
        assertNotEquals(config, other);
    }

    @Test
    @DisplayName("equals returns false when metadata differ")
    void testEquals_DifferentMetadata_ReturnsFalse() {
        PluginConfig other = new PluginConfig();
        config.setMetadata("k", "v1");
        other.setMetadata("k", "v2");
        assertNotEquals(config, other);
    }

    @Test
    @DisplayName("equals returns true for equal configs")
    void testEquals_EqualConfigs_ReturnsTrue() {
        PluginConfig a = new PluginConfig();
        PluginConfig b = new PluginConfig();
        a.setSetting("x", 1); b.setSetting("x", 1);
        assertEquals(a, b);
    }

    @Test
    @DisplayName("hashCode is consistent with equals")
    void testHashCode_ConsistentWithEquals() {
        PluginConfig a = new PluginConfig();
        PluginConfig b = new PluginConfig();
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("mergeWith skips feature already present in this config")
    void testMergeWith_FeatureAlreadyPresent_NotDuplicated() {
        config.enableFeature("sharedFeature");
        PluginConfig other = new PluginConfig();
        other.enableFeature("sharedFeature");
        other.enableFeature("newFeature");
        config.merge(other);
        // sharedFeature should appear only once
        long count = config.getEnabledFeatures().stream()
                .filter("sharedFeature"::equals).count();
        assertEquals(1, count);
        assertTrue(config.getEnabledFeatures().contains("newFeature"));
    }
}