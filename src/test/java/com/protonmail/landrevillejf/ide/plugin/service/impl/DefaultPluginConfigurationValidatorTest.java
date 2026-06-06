package com.protonmail.landrevillejf.ide.plugin.service.impl;

import com.protonmail.landrevillejf.ide.plugin.service.PluginConfigurationValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DefaultPluginConfigurationValidatorTest {

    private DefaultPluginConfigurationValidator validator;
    private static final String TEST_PLUGIN = "test-plugin";

    @BeforeEach
    void setUp() {
        validator = new DefaultPluginConfigurationValidator();
    }

    private void registerTestSchema() {
        Map<String, Object> schema = new HashMap<>();
        Map<String, Object> properties = new HashMap<>();

        properties.put("name", Map.of("type", "string", "required", true));
        properties.put("version", Map.of("type", "number"));
        properties.put("enabled", Map.of("type", "boolean"));
        properties.put("tags", Map.of("type", "array"));
        properties.put("status", Map.of("type", "string", "enum", List.of("active", "inactive", "pending")));
        properties.put("port", Map.of("type", "number", "minimum", 1024, "maximum", 65535));
        properties.put("code", Map.of("type", "string", "minLength", 2, "maxLength", 5, "pattern", "^[A-Z]+$"));
        properties.put("items", Map.of("type", "array", "minItems", 1, "maxItems", 3, "uniqueItems", true));
        properties.put("email", Map.of("type", "string", "format", "email"));
        properties.put("website", Map.of("type", "string", "format", "url"));

        schema.put("properties", properties);
        schema.put("default", Map.of("enabled", true));

        validator.registerSchema(TEST_PLUGIN, schema);
    }

    @Test
    void validateConfiguration() {
        registerTestSchema();

        Map<String, Object> config = new HashMap<>();
        config.put("name", "MyPlugin");
        config.put("version", 1);
        config.put("enabled", true);
        config.put("tags", List.of("tag1", "tag2"));
        config.put("status", "active");

        PluginConfigurationValidator.ValidationResult result = validator.validateConfiguration(TEST_PLUGIN, config);

        // Afficher les erreurs pour debug
        result.getErrors().forEach(error ->
                System.out.println("Error: " + error.getPath() + " - " + error.getMessage())
        );

        assertTrue(result.isValid());
        assertEquals(0, result.getErrors().size());
    }

    @Test
    void validateConfigurationWithMissingRequiredField() {
        registerTestSchema();

        Map<String, Object> config = Map.of("version", 1, "enabled", true);

        PluginConfigurationValidator.ValidationResult result = validator.validateConfiguration(TEST_PLUGIN, config);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.getPath().equals("name") && e.getErrorCode().equals("REQUIRED_FIELD_MISSING")));
    }

    @Test
    void validateConfigurationWithTypeMismatch() {
        registerTestSchema();

        Map<String, Object> config = Map.of("name", "MyPlugin", "version", "not-a-number", "enabled", true);

        PluginConfigurationValidator.ValidationResult result = validator.validateConfiguration(TEST_PLUGIN, config);

        assertFalse(result.isValid());
        assertTrue(result.getErrors().stream()
                .anyMatch(e -> e.getPath().equals("version") && e.getErrorCode().equals("TYPE_MISMATCH")));
    }

    @Test
    void validateConfigurationWithNoSchema() {
        Map<String, Object> config = Map.of("name", "MyPlugin");

        PluginConfigurationValidator.ValidationResult result = validator.validateConfiguration(TEST_PLUGIN, config);

        assertTrue(result.isValid());
    }

    @Test
    void validateValue() {
        registerTestSchema();

        PluginConfigurationValidator.ValidationResult result = validator.validateValue(TEST_PLUGIN, "name", "MyPlugin");

        assertTrue(result.isValid());
    }

    @Test
    void validateValueWithInvalidType() {
        registerTestSchema();

        PluginConfigurationValidator.ValidationResult result = validator.validateValue(TEST_PLUGIN, "version", "not-a-number");

        assertFalse(result.isValid());
        assertEquals("TYPE_MISMATCH", result.getErrors().get(0).getErrorCode());
    }

    @Test
    void validateValueWithNoSchema() {
        PluginConfigurationValidator.ValidationResult result = validator.validateValue(TEST_PLUGIN, "any.path", "value");

        assertTrue(result.isValid());
    }

    @Test
    void getSchema() {
        registerTestSchema();

        Map<String, Object> retrieved = validator.getSchema(TEST_PLUGIN);

        assertNotNull(retrieved);
        assertTrue(retrieved.containsKey("properties"));
    }

    @Test
    void registerSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("properties", Map.of("name", Map.of("type", "string")));

        validator.registerSchema(TEST_PLUGIN, schema);

        assertNotNull(validator.getSchema(TEST_PLUGIN));
    }

    @Test
    void registerCustomValidator() {
        validator.registerCustomValidator(TEST_PLUGIN, "custom.field", value -> {
            if (value == null || value.toString().length() < 3) {
                return "Value must have at least 3 characters";
            }
            return null;
        });

        Map<String, PluginConfigurationValidator.ConfigValidator> validators = validator.getCustomValidators(TEST_PLUGIN);

        assertTrue(validators.containsKey("custom.field"));
    }

    @Test
    void getCustomValidators() {
        validator.registerCustomValidator(TEST_PLUGIN, "field1", value -> null);
        validator.registerCustomValidator(TEST_PLUGIN, "field2", value -> null);

        Map<String, PluginConfigurationValidator.ConfigValidator> validators = validator.getCustomValidators(TEST_PLUGIN);

        assertEquals(2, validators.size());
        assertTrue(validators.containsKey("field1"));
        assertTrue(validators.containsKey("field2"));
    }

    @Test
    void getDefaultConfiguration() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("default", Map.of("name", "DefaultName", "enabled", true));
        schema.put("properties", Map.of("name", Map.of("type", "string"), "enabled", Map.of("type", "boolean")));

        validator.registerSchema(TEST_PLUGIN, schema);

        Map<String, Object> defaults = validator.getDefaultConfiguration(TEST_PLUGIN);

        assertEquals("DefaultName", defaults.get("name"));
        assertEquals(true, defaults.get("enabled"));
    }

    @Test
    void mergeWithDefaults() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("default", Map.of("name", "DefaultName", "version", 1, "enabled", false));
        schema.put("properties", Map.of(
                "name", Map.of("type", "string"),
                "version", Map.of("type", "number"),
                "enabled", Map.of("type", "boolean")
        ));

        validator.registerSchema(TEST_PLUGIN, schema);

        Map<String, Object> partial = Map.of("name", "CustomName", "enabled", true);
        Map<String, Object> merged = validator.mergeWithDefaults(TEST_PLUGIN, partial);

        assertEquals("CustomName", merged.get("name"));
        assertEquals(1, merged.get("version"));
        assertEquals(true, merged.get("enabled"));
    }

    @Test
    void generateSampleConfiguration() {
        registerTestSchema();

        Map<String, Object> sample = validator.generateSampleConfiguration(TEST_PLUGIN);

        assertNotNull(sample);
        assertTrue(sample.containsKey("name"));
        assertTrue(sample.containsKey("version"));
        assertTrue(sample.containsKey("enabled"));
    }

    @Test
    void getValidationRules() {
        registerTestSchema();

        Map<String, Object> rules = validator.getValidationRules(TEST_PLUGIN);

        assertNotNull(rules);
        assertTrue(rules.containsKey("name"));
        assertTrue(rules.containsKey("version"));
    }

    @Test
    void testNumberRangeValidation() {
        registerTestSchema();

        PluginConfigurationValidator.ValidationResult result = validator.validateValue(TEST_PLUGIN, "port", 8080);
        assertTrue(result.isValid());

        result = validator.validateValue(TEST_PLUGIN, "port", 80);
        assertFalse(result.isValid());
        assertEquals("BELOW_MINIMUM", result.getErrors().get(0).getErrorCode());

        result = validator.validateValue(TEST_PLUGIN, "port", 70000);
        assertFalse(result.isValid());
        assertEquals("ABOVE_MAXIMUM", result.getErrors().get(0).getErrorCode());
    }

    @Test
    void testStringConstraintsValidation() {
        registerTestSchema();

        PluginConfigurationValidator.ValidationResult result = validator.validateValue(TEST_PLUGIN, "code", "ABC");
        assertTrue(result.isValid());

        result = validator.validateValue(TEST_PLUGIN, "code", "A");
        assertFalse(result.isValid());
        assertEquals("STRING_TOO_SHORT", result.getErrors().get(0).getErrorCode());

        result = validator.validateValue(TEST_PLUGIN, "code", "ABCDEF");
        assertFalse(result.isValid());
        assertEquals("STRING_TOO_LONG", result.getErrors().get(0).getErrorCode());

        result = validator.validateValue(TEST_PLUGIN, "code", "abc");
        assertFalse(result.isValid());
        assertEquals("PATTERN_MISMATCH", result.getErrors().get(0).getErrorCode());
    }

    @Test
    void testCollectionConstraints() {
        registerTestSchema();

        PluginConfigurationValidator.ValidationResult result = validator.validateValue(TEST_PLUGIN, "items", List.of(1, 2, 3));
        assertTrue(result.isValid());

        result = validator.validateValue(TEST_PLUGIN, "items", List.of());
        assertFalse(result.isValid());
        assertEquals("COLLECTION_TOO_SMALL", result.getErrors().get(0).getErrorCode());

        result = validator.validateValue(TEST_PLUGIN, "items", List.of(1, 2, 3, 4));
        assertFalse(result.isValid());
        assertEquals("COLLECTION_TOO_LARGE", result.getErrors().get(0).getErrorCode());

        result = validator.validateValue(TEST_PLUGIN, "items", List.of(1, 1, 2));
        assertFalse(result.isValid());
        assertEquals("DUPLICATE_ITEMS", result.getErrors().get(0).getErrorCode());
    }

    @Test
    void testEnumValidation() {
        registerTestSchema();

        PluginConfigurationValidator.ValidationResult result = validator.validateValue(TEST_PLUGIN, "status", "active");
        assertTrue(result.isValid());

        result = validator.validateValue(TEST_PLUGIN, "status", "UNKNOWN");
        assertFalse(result.isValid());
        assertEquals("INVALID_ENUM_VALUE", result.getErrors().get(0).getErrorCode());
    }

    @Test
    void testEmailValidation() {
        registerTestSchema();

        PluginConfigurationValidator.ValidationResult result = validator.validateValue(TEST_PLUGIN, "email", "test@example.com");
        assertTrue(result.isValid());

        result = validator.validateValue(TEST_PLUGIN, "email", "invalid-email");
        assertFalse(result.isValid());
        assertEquals("INVALID_EMAIL", result.getErrors().get(0).getErrorCode());
    }

    @Test
    void testUrlValidation() {
        registerTestSchema();

        PluginConfigurationValidator.ValidationResult result = validator.validateValue(TEST_PLUGIN, "website", "https://example.com");
        assertTrue(result.isValid());

        result = validator.validateValue(TEST_PLUGIN, "website", "http://localhost:8080");
        assertTrue(result.isValid());

        result = validator.validateValue(TEST_PLUGIN, "website", "not-a-url");
        assertFalse(result.isValid());
        assertEquals("INVALID_URL", result.getErrors().get(0).getErrorCode());
    }
}