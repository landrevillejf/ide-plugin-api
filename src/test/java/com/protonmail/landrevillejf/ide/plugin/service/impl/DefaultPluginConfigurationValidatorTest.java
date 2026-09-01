package com.protonmail.landrevillejf.ide.plugin.service.impl;

import com.protonmail.landrevillejf.ide.plugin.service.PluginConfigurationValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Nested
    @DisplayName("Coverage completion tests")
    class CoverageCompletionTests {

        @Test
        @DisplayName("Should treat an empty registered schema as having no constraints")
        void emptySchemaIsAccepted() {
            validator.registerSchema(TEST_PLUGIN, new HashMap<>());

            assertTrue(validator.validateConfiguration(TEST_PLUGIN, Map.of("anything", 1)).isValid());
            assertTrue(validator.validateValue(TEST_PLUGIN, "anything", 42).isValid());
            assertTrue(validator.generateSampleConfiguration(TEST_PLUGIN).isEmpty());
        }

        @Test
        @DisplayName("Should skip property based checks when the schema has no properties")
        void schemaWithoutProperties() {
            Map<String, Object> schema = new HashMap<>();
            schema.put("title", "My plugin schema");
            validator.registerSchema(TEST_PLUGIN, schema);

            assertTrue(validator.validateConfiguration(TEST_PLUGIN, Map.of("a", 1)).isValid());
            assertTrue(validator.generateSampleConfiguration(TEST_PLUGIN).isEmpty());
            assertTrue(validator.getValidationRules(TEST_PLUGIN).isEmpty());
        }

        @Test
        @DisplayName("Should ignore config fields and schema entries without constraints")
        void untypedFieldsAndUnknownPaths() {
            Map<String, Object> schema = new HashMap<>();
            Map<String, Object> properties = new HashMap<>();
            properties.put("name", Map.of("type", "string"));
            properties.put("free", Map.of("description", "no type constraint"));
            schema.put("properties", properties);
            validator.registerSchema(TEST_PLUGIN, schema);

            // "extra" has no schema entry and "free" has no type constraint
            Map<String, Object> config = new HashMap<>();
            config.put("name", "ok");
            config.put("free", "anything");
            config.put("extra", 5);
            assertTrue(validator.validateConfiguration(TEST_PLUGIN, config).isValid());
        }

        @Test
        @DisplayName("Should accept duplicates when uniqueItems is disabled")
        void uniqueItemsDisabled() {
            Map<String, Object> schema = new HashMap<>();
            schema.put("properties", Map.of("items",
                    Map.of("type", "array", "uniqueItems", false)));
            validator.registerSchema(TEST_PLUGIN, schema);

            Map<String, Object> config = Map.of("items", List.of("a", "a"));
            assertTrue(validator.validateConfiguration(TEST_PLUGIN, config).isValid());
        }

        @Test
        @DisplayName("Should reject empty strings for email and url formats")
        void emptyEmailAndUrlAreInvalid() {
            registerTestSchema();

            PluginConfigurationValidator.ValidationResult email =
                    validator.validateValue(TEST_PLUGIN, "email", "");
            assertFalse(email.isValid());
            assertEquals("INVALID_EMAIL", email.getErrors().get(0).getErrorCode());

            PluginConfigurationValidator.ValidationResult url =
                    validator.validateValue(TEST_PLUGIN, "website", "");
            assertFalse(url.isValid());
            assertEquals("INVALID_URL", url.getErrors().get(0).getErrorCode());
        }

        @Test
        @DisplayName("Should resolve paths crossing non-map values as missing")
        void pathThroughNonMapValue() {
            registerTestSchema();

            // "name" schema is a map but "name.type" resolves to the "string" scalar
            PluginConfigurationValidator.ValidationResult result =
                    validator.validateValue(TEST_PLUGIN, "name.type.sub", "x");
            assertTrue(result.isValid());
        }

        @Test
        @DisplayName("Should merge nested maps, replace scalars and copy new map entries")
        void mergeWithDefaultsCoversAllCases() {
            Map<String, Object> schema = new HashMap<>();
            Map<String, Object> defaults = new HashMap<>();
            Map<String, Object> nested = new HashMap<>();
            nested.put("x", 1);
            defaults.put("a", nested);
            defaults.put("b", "text");
            schema.put("default", defaults);
            validator.registerSchema(TEST_PLUGIN, schema);

            Map<String, Object> partial = new HashMap<>();
            partial.put("a", Map.of("y", 2));          // merge into existing map
            partial.put("b", Map.of("n", 1));          // replace a scalar with a map
            partial.put("c", Map.of("z", 3));          // new map entry (deep copy)
            partial.put("d", 4);                       // scalar overwrite

            Map<String, Object> merged = validator.mergeWithDefaults(TEST_PLUGIN, partial);

            @SuppressWarnings("unchecked")
            Map<String, Object> mergedA = (Map<String, Object>) merged.get("a");
            assertEquals(1, mergedA.get("x"));
            assertEquals(2, mergedA.get("y"));
            assertEquals(Map.of("n", 1), merged.get("b"));
            assertEquals(Map.of("z", 3), merged.get("c"));
            assertEquals(4, merged.get("d"));
        }
    }
}