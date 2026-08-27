package com.protonmail.landrevillejf.ide.plugin.service.impl;

import com.protonmail.landrevillejf.ide.plugin.service.PluginConfigurationValidator;
import com.protonmail.landrevillejf.ide.plugin.service.PluginConfigurationValidator.ConfigValidator;
import com.protonmail.landrevillejf.ide.plugin.service.PluginConfigurationValidator.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

/**
 * Mutation-killing tests for {@link DefaultPluginConfigurationValidator}.
 * Every assertion targets a specific mutant (boundary, conditional,
 * return-value) so that no equivalent behaviour slips through.
 */
@DisplayName("DefaultPluginConfigurationValidator mutation tests")
class DefaultPluginConfigurationValidatorMutationTest {

    private static final String PLUGIN = "validator-plugin";

    private DefaultPluginConfigurationValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DefaultPluginConfigurationValidator();
    }

    private static Map<String, Object> field(String type, Object... extras) {
        Map<String, Object> field = new LinkedHashMap<>();
        if (type != null) {
            field.put("type", type);
        }
        for (int i = 0; i < extras.length; i += 2) {
            field.put((String) extras[i], extras[i + 1]);
        }
        return field;
    }

    private static Map<String, Object> schema(String name1, Map<String, Object> field1) {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put(name1, field1);
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("properties", properties);
        return schema;
    }

    private static Map<String, Object> schema(String name1, Map<String, Object> field1,
                                              String name2, Map<String, Object> field2) {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put(name1, field1);
        properties.put(name2, field2);
        Map<String, Object> schema = new LinkedHashMap<>();
        schema.put("properties", properties);
        return schema;
    }

    @Nested
    @DisplayName("validateConfiguration")
    class ValidateConfigurationTests {

        @Test
        @DisplayName("no schema registered -> valid")
        void noSchema() {
            ValidationResult result = validator.validateConfiguration(PLUGIN, Map.of());
            assertThat(result.isValid()).isTrue();
            assertThat(result.getErrors()).isEmpty();
        }

        @Test
        @DisplayName("missing required field -> REQUIRED_FIELD_MISSING")
        void missingRequiredField() {
            validator.registerSchema(PLUGIN, schema("name", field("string", "required", true)));
            ValidationResult result = validator.validateConfiguration(PLUGIN, Map.of());
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors()).hasSize(1);
            assertThat(result.getErrors().get(0).getPath()).isEqualTo("name");
            assertThat(result.getErrors().get(0).getErrorCode()).isEqualTo("REQUIRED_FIELD_MISSING");
            assertThat(result.getErrors().get(0).getMessage()).contains("Required field missing");
            assertThat(result.getErrors().get(0).toString())
                    .isEqualTo("[REQUIRED_FIELD_MISSING] name: Required field missing: name");
        }

        @Test
        @DisplayName("required field present -> valid")
        void requiredFieldPresent() {
            validator.registerSchema(PLUGIN, schema("name", field("string", "required", true)));
            ValidationResult result = validator.validateConfiguration(PLUGIN, Map.of("name", "x"));
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("required=false is not enforced")
        void optionalField() {
            validator.registerSchema(PLUGIN, schema("name", field("string", "required", false)));
            ValidationResult result = validator.validateConfiguration(PLUGIN, Map.of());
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("nested configuration is validated at full path")
        void nestedConfiguration() {
            Map<String, Object> portSchema = field("integer", "minimum", 10);
            Map<String, Object> properties = new LinkedHashMap<>();
            properties.put("server", Map.of("port", portSchema));
            Map<String, Object> schemaMap = new LinkedHashMap<>();
            schemaMap.put("properties", properties);
            validator.registerSchema(PLUGIN, schemaMap);

            ValidationResult invalid = validator.validateConfiguration(PLUGIN,
                    Map.of("server", Map.of("port", 5)));
            assertThat(invalid.isValid()).isFalse();
            assertThat(invalid.getErrors().get(0).getPath()).isEqualTo("server.port");
            assertThat(invalid.getErrors().get(0).getErrorCode()).isEqualTo("BELOW_MINIMUM");

            ValidationResult valid = validator.validateConfiguration(PLUGIN,
                    Map.of("server", Map.of("port", 8080)));
            assertThat(valid.isValid()).isTrue();
        }

        @Test
        @DisplayName("null config values are reported as null string")
        void nullConfigValues() {
            validator.registerSchema(PLUGIN, schema("name", field("string")));
            Map<String, Object> config = new LinkedHashMap<>();
            config.put("name", null);
            ValidationResult result = validator.validateConfiguration(PLUGIN, config);
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("failing custom validator blocks configuration")
        void failingCustomValidator() {
            validator.registerSchema(PLUGIN, schema("name", field("string")));
            validator.registerCustomValidator(PLUGIN, "name", value -> "always wrong");
            ValidationResult result = validator.validateConfiguration(PLUGIN, Map.of("name", "x"));
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors().get(0).getErrorCode()).isEqualTo("CUSTOM_VALIDATION_FAILED");
            assertThat(result.getErrors().get(0).getMessage()).isEqualTo("always wrong");
        }

        @Test
        @DisplayName("passing custom validator keeps configuration valid")
        void passingCustomValidator() {
            validator.registerSchema(PLUGIN, schema("name", field("string")));
            validator.registerCustomValidator(PLUGIN, "name", value -> null);
            ValidationResult result = validator.validateConfiguration(PLUGIN, Map.of("name", "x"));
            assertThat(result.isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("number range validation")
    class NumberRangeTests {

        @BeforeEach
        void registerSchema() {
            validator.registerSchema(PLUGIN,
                    schema("port", field("number", "minimum", 10, "maximum", 20)));
        }

        @Test
        @DisplayName("value below minimum is rejected")
        void belowMinimum() {
            ValidationResult result = validator.validateConfiguration(PLUGIN, Map.of("port", 9));
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors().get(0).getErrorCode()).isEqualTo("BELOW_MINIMUM");
        }

        @Test
        @DisplayName("value equal to minimum is accepted (boundary)")
        void equalToMinimum() {
            ValidationResult result = validator.validateConfiguration(PLUGIN, Map.of("port", 10));
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("value equal to maximum is accepted (boundary)")
        void equalToMaximum() {
            ValidationResult result = validator.validateConfiguration(PLUGIN, Map.of("port", 20));
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("value above maximum is rejected")
        void aboveMaximum() {
            ValidationResult result = validator.validateConfiguration(PLUGIN, Map.of("port", 21));
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors().get(0).getErrorCode()).isEqualTo("ABOVE_MAXIMUM");
        }
    }

    @Nested
    @DisplayName("string constraint validation")
    class StringConstraintTests {

        @BeforeEach
        void registerSchema() {
            validator.registerSchema(PLUGIN,
                    schema("code", field("string", "minLength", 2, "maxLength", 4)));
        }

        @Test
        @DisplayName("string shorter than minLength is rejected")
        void tooShort() {
            ValidationResult result = validator.validateConfiguration(PLUGIN, Map.of("code", "a"));
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors().get(0).getErrorCode()).isEqualTo("STRING_TOO_SHORT");
        }

        @Test
        @DisplayName("string of exactly minLength is accepted (boundary)")
        void exactMinLength() {
            ValidationResult result = validator.validateConfiguration(PLUGIN, Map.of("code", "ab"));
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("string of exactly maxLength is accepted (boundary)")
        void exactMaxLength() {
            ValidationResult result = validator.validateConfiguration(PLUGIN, Map.of("code", "abcd"));
            assertThat(result.isValid()).isTrue();
        }

        @Test
        @DisplayName("string longer than maxLength is rejected")
        void tooLong() {
            ValidationResult result = validator.validateConfiguration(PLUGIN, Map.of("code", "abcde"));
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors().get(0).getErrorCode()).isEqualTo("STRING_TOO_LONG");
        }

        @Test
        @DisplayName("pattern mismatch is rejected")
        void patternMismatch() {
            validator.registerSchema(PLUGIN,
                    schema("id", field("string", "pattern", "[0-9]+")));
            ValidationResult bad = validator.validateConfiguration(PLUGIN, Map.of("id", "abc"));
            assertThat(bad.isValid()).isFalse();
            assertThat(bad.getErrors().get(0).getErrorCode()).isEqualTo("PATTERN_MISMATCH");

            ValidationResult good = validator.validateConfiguration(PLUGIN, Map.of("id", "123"));
            assertThat(good.isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("email and url formats")
    class FormatTests {

        private ValidationResult validateFormat(String format, String value) {
            validator.registerSchema(PLUGIN, schema("field", field("string", "format", format)));
            return validator.validateConfiguration(PLUGIN, Map.of("field", value));
        }

        @Test
        @DisplayName("valid emails are accepted")
        void validEmails() {
            assertThat(validateFormat("email", "user@example.com").isValid()).isTrue();
            assertThat(validateFormat("email", "a@b.c").isValid()).isTrue();
        }

        @Test
        @DisplayName("invalid emails are rejected")
        void invalidEmails() {
            assertThat(validateFormat("email", "@b.c").isValid())
                    .as("empty local part").isFalse();
            assertThat(validateFormat("email", "a@").isValid())
                    .as("no domain").isFalse();
            assertThat(validateFormat("email", "a@b.").isValid())
                    .as("dot immediately after at").isFalse();
            assertThat(validateFormat("email", "a@.c").isValid())
                    .as("dot right after at sign").isFalse();
            assertThat(validateFormat("email", "plainaddress").isValid()).isFalse();
        }

        @Test
        @DisplayName("email error code is INVALID_EMAIL")
        void emailErrorCode() {
            ValidationResult result = validateFormat("email", "nope");
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors().get(0).getErrorCode()).isEqualTo("INVALID_EMAIL");
        }

        @Test
        @DisplayName("valid urls are accepted")
        void validUrls() {
            assertThat(validateFormat("url", "http://example.com").isValid()).isTrue();
            assertThat(validateFormat("url", "https://example.com").isValid()).isTrue();
        }

        @Test
        @DisplayName("invalid urls are rejected with INVALID_URL")
        void invalidUrls() {
            ValidationResult result = validateFormat("url", "ftp://example.com");
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors().get(0).getErrorCode()).isEqualTo("INVALID_URL");
        }
    }

    @Nested
    @DisplayName("collection constraints")
    class CollectionConstraintTests {

        @Test
        @DisplayName("minItems boundary")
        void minItemsBoundary() {
            validator.registerSchema(PLUGIN,
                    schema("tags", field("array", "minItems", 2, "maxItems", 3)));

            ValidationResult tooSmall = validator.validateConfiguration(PLUGIN,
                    Map.of("tags", List.of("a")));
            assertThat(tooSmall.isValid()).isFalse();
            assertThat(tooSmall.getErrors().get(0).getErrorCode()).isEqualTo("COLLECTION_TOO_SMALL");

            ValidationResult exact = validator.validateConfiguration(PLUGIN,
                    Map.of("tags", List.of("a", "b")));
            assertThat(exact.isValid()).isTrue();
        }

        @Test
        @DisplayName("maxItems boundary")
        void maxItemsBoundary() {
            validator.registerSchema(PLUGIN,
                    schema("tags", field("array", "maxItems", 2)));

            ValidationResult exact = validator.validateConfiguration(PLUGIN,
                    Map.of("tags", List.of("a", "b")));
            assertThat(exact.isValid()).isTrue();

            ValidationResult tooLarge = validator.validateConfiguration(PLUGIN,
                    Map.of("tags", List.of("a", "b", "c")));
            assertThat(tooLarge.isValid()).isFalse();
            assertThat(tooLarge.getErrors().get(0).getErrorCode()).isEqualTo("COLLECTION_TOO_LARGE");
        }

        @Test
        @DisplayName("uniqueItems detects duplicates")
        void uniqueItems() {
            validator.registerSchema(PLUGIN,
                    schema("tags", field("array", "uniqueItems", true)));

            ValidationResult duplicates = validator.validateConfiguration(PLUGIN,
                    Map.of("tags", List.of("a", "a")));
            assertThat(duplicates.isValid()).isFalse();
            assertThat(duplicates.getErrors().get(0).getErrorCode()).isEqualTo("DUPLICATE_ITEMS");

            ValidationResult unique = validator.validateConfiguration(PLUGIN,
                    Map.of("tags", List.of("a", "b")));
            assertThat(unique.isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("type matching")
    class TypeMatchTests {

        private ValidationResult validateType(String type, Object value) {
            validator.registerSchema(PLUGIN, schema("field", field(type)));
            Map<String, Object> config = new LinkedHashMap<>();
            config.put("field", value);
            return validator.validateConfiguration(PLUGIN, config);
        }

        @Test
        @DisplayName("string type matching")
        void stringType() {
            assertThat(validateType("string", "text").isValid()).isTrue();
            ValidationResult bad = validateType("string", 42);
            assertThat(bad.isValid()).isFalse();
            assertThat(bad.getErrors().get(0).getErrorCode()).isEqualTo("TYPE_MISMATCH");
        }

        @Test
        @DisplayName("number type matching")
        void numberType() {
            assertThat(validateType("number", 42).isValid()).isTrue();
            assertThat(validateType("number", 4.2).isValid()).isTrue();
            assertThat(validateType("number", "text").isValid()).isFalse();
        }

        @Test
        @DisplayName("integer type matching")
        void integerType() {
            assertThat(validateType("integer", 42).isValid()).isTrue();
            assertThat(validateType("integer", 42L).isValid()).isTrue();
            assertThat(validateType("integer", 4.2).isValid()).isFalse();
        }

        @Test
        @DisplayName("boolean type matching")
        void booleanType() {
            assertThat(validateType("boolean", true).isValid()).isTrue();
            assertThat(validateType("boolean", "true").isValid()).isFalse();
        }

        @Test
        @DisplayName("array type matching")
        void arrayType() {
            assertThat(validateType("array", List.of(1)).isValid()).isTrue();
            assertThat(validateType("array", new Object[]{"a"}).isValid()).isTrue();
            assertThat(validateType("array", "not-array").isValid()).isFalse();
        }

        @Test
        @DisplayName("object type matching")
        void objectType() {
            assertThat(validateType("object", Map.of()).isValid()).isTrue();
            assertThat(validateType("object", "not-object").isValid()).isFalse();
        }

        @Test
        @DisplayName("unknown type always matches")
        void unknownType() {
            assertThat(validateType("weird", "anything").isValid()).isTrue();
        }

        @Test
        @DisplayName("null value matches any type and skips range checks")
        void nullValue() {
            validator.registerSchema(PLUGIN,
                    schema("field", field("number", "minimum", 10)));
            Map<String, Object> config = new LinkedHashMap<>();
            config.put("field", null);
            ValidationResult result = validator.validateConfiguration(PLUGIN, config);
            assertThat(result.isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("enum validation")
    class EnumValidationTests {

        @Test
        @DisplayName("value in enum list is accepted")
        void allowedValue() {
            validator.registerSchema(PLUGIN,
                    schema("level", field("string", "enum", List.of("low", "high"))));
            assertThat(validator.validateConfiguration(PLUGIN, Map.of("level", "low")).isValid())
                    .isTrue();
        }

        @Test
        @DisplayName("value outside enum list is rejected")
        void disallowedValue() {
            validator.registerSchema(PLUGIN,
                    schema("level", field("string", "enum", List.of("low", "high"))));
            ValidationResult result = validator.validateConfiguration(PLUGIN, Map.of("level", "medium"));
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors().get(0).getErrorCode()).isEqualTo("INVALID_ENUM_VALUE");
        }
    }

    @Nested
    @DisplayName("validateValue")
    class ValidateValueTests {

        @Test
        @DisplayName("no schema -> valid")
        void noSchema() {
            assertThat(validator.validateValue(PLUGIN, "x", 1).isValid()).isTrue();
        }

        @Test
        @DisplayName("schema without properties map -> valid")
        void noProperties() {
            validator.registerSchema(PLUGIN, Map.of("title", "schema"));
            assertThat(validator.validateValue(PLUGIN, "x", 1).isValid()).isTrue();
        }

        @Test
        @DisplayName("unknown path -> valid")
        void unknownPath() {
            validator.registerSchema(PLUGIN, schema("name", field("string")));
            assertThat(validator.validateValue(PLUGIN, "missing", 1).isValid()).isTrue();
        }

        @Test
        @DisplayName("path validated against its field schema")
        void knownPath() {
            validator.registerSchema(PLUGIN, schema("port", field("integer", "minimum", 1)));
            assertThat(validator.validateValue(PLUGIN, "port", 0).isValid()).isFalse();
            assertThat(validator.validateValue(PLUGIN, "port", 1).isValid()).isTrue();
        }

        @Test
        @DisplayName("custom validator for specific path")
        void customValidatorForPath() {
            validator.registerSchema(PLUGIN, schema("name", field("string")));
            validator.registerCustomValidator(PLUGIN, "name", value -> "bad name");
            ValidationResult result = validator.validateValue(PLUGIN, "name", "x");
            assertThat(result.isValid()).isFalse();
            assertThat(result.getErrors().get(0).getMessage()).isEqualTo("bad name");

            validator.registerCustomValidator(PLUGIN, "name", value -> null);
            assertThat(validator.validateValue(PLUGIN, "name", "x").isValid()).isTrue();
        }

        @Test
        @DisplayName("custom validator for other path is ignored")
        void customValidatorOtherPath() {
            validator.registerSchema(PLUGIN,
                    schema("name", field("string"), "other", field("string")));
            validator.registerCustomValidator(PLUGIN, "other", value -> "boom");
            assertThat(validator.validateValue(PLUGIN, "name", "x").isValid()).isTrue();
        }
    }

    @Nested
    @DisplayName("defaults, merging and samples")
    class DefaultsAndSamplesTests {

        @Test
        @DisplayName("mergeWithDefaults without registered defaults copies config")
        void mergeWithoutDefaults() {
            Map<String, Object> partial = Map.of("a", 1);
            Map<String, Object> merged = validator.mergeWithDefaults(PLUGIN, partial);
            assertThat(merged).containsExactly(entry("a", 1));
            assertThat(merged).isNotSameAs(partial);
        }

        @Test
        @DisplayName("mergeWithDefaults deep-merges nested maps")
        void deepMerge() {
            Map<String, Object> defaults = new LinkedHashMap<>();
            defaults.put("default", Map.of("server", Map.of("host", "localhost", "port", 80)));
            validator.registerSchema(PLUGIN, defaults);

            Map<String, Object> merged = validator.mergeWithDefaults(PLUGIN,
                    Map.of("server", Map.of("port", 8080)));
            assertThat(merged).containsKeys("server");
            @SuppressWarnings("unchecked")
            Map<String, Object> server = (Map<String, Object>) merged.get("server");
            assertThat(server).containsEntry("host", "localhost");
            assertThat(server).containsEntry("port", 8080);
        }

        @Test
        @DisplayName("mergeWithDefaults overwrites non-map values")
        void mergeOverwrite() {
            Map<String, Object> defaults = new LinkedHashMap<>();
            defaults.put("default", Map.of("mode", "fast"));
            validator.registerSchema(PLUGIN, defaults);

            Map<String, Object> merged = validator.mergeWithDefaults(PLUGIN, Map.of("mode", "slow"));
            assertThat(merged).containsEntry("mode", "slow");
        }

        @Test
        @DisplayName("getDefaultConfiguration returns registered defaults")
        void defaultConfiguration() {
            assertThat(validator.getDefaultConfiguration(PLUGIN)).isEmpty();
            Map<String, Object> withDefaults = new LinkedHashMap<>();
            withDefaults.put("default", Map.of("a", 1));
            validator.registerSchema(PLUGIN, withDefaults);
            assertThat(validator.getDefaultConfiguration(PLUGIN)).containsEntry("a", 1);
        }

        @Test
        @DisplayName("generateSampleConfiguration without schema is empty")
        void sampleWithoutSchema() {
            assertThat(validator.generateSampleConfiguration(PLUGIN)).isEmpty();
        }

        @Test
        @DisplayName("generateSampleConfiguration uses example and default values")
        void sampleFromExampleAndDefault() {
            Map<String, Object> properties = new LinkedHashMap<>();
            properties.put("withExample", field("string", "example", "ex-value"));
            properties.put("withDefault", field("string", "default", "def-value"));
            Map<String, Object> schemaMap = new LinkedHashMap<>();
            schemaMap.put("properties", properties);
            validator.registerSchema(PLUGIN, schemaMap);

            Map<String, Object> sample = validator.generateSampleConfiguration(PLUGIN);
            assertThat(sample).containsEntry("withExample", "ex-value");
            assertThat(sample).containsEntry("withDefault", "def-value");
        }

        @Test
        @DisplayName("generateSampleConfiguration creates typed samples")
        void samplePerType() {
            Map<String, Object> properties = new LinkedHashMap<>();
            properties.put("s", field("string"));
            properties.put("enumField", field("string", "enum", List.of("first", "second")));
            properties.put("n", field("number"));
            properties.put("i", field("integer"));
            properties.put("b", field("boolean"));
            properties.put("arr", field("array"));
            properties.put("obj", field("object"));
            properties.put("weird", field("unknown-type"));
            Map<String, Object> schemaMap = new LinkedHashMap<>();
            schemaMap.put("properties", properties);
            validator.registerSchema(PLUGIN, schemaMap);

            Map<String, Object> sample = validator.generateSampleConfiguration(PLUGIN);
            assertThat(sample).containsEntry("s", "sample_value");
            assertThat(sample).containsEntry("enumField", "first");
            assertThat(sample).containsEntry("n", 0.0);
            assertThat(sample).containsEntry("i", 0);
            assertThat(sample).containsEntry("b", false);
            assertThat(sample.get("arr")).isInstanceOf(ArrayList.class);
            assertThat(sample.get("obj")).isInstanceOf(LinkedHashMap.class);
            assertThat(sample).doesNotContainKey("weird");
        }

        @Test
        @DisplayName("getValidationRules extracts all rule kinds")
        void validationRules() {
            assertThat(validator.getValidationRules(PLUGIN)).isEmpty();

            Map<String, Object> properties = new LinkedHashMap<>();
            properties.put("port", field("integer",
                    "required", true, "minimum", 1, "maximum", 65535));
            properties.put("name", field("string",
                    "minLength", 1, "maxLength", 50, "pattern", "[a-z]+",
                    "enum", List.of("a", "b")));
            properties.put("empty", new LinkedHashMap<String, Object>());
            Map<String, Object> schemaMap = new LinkedHashMap<>();
            schemaMap.put("properties", properties);
            validator.registerSchema(PLUGIN, schemaMap);

            Map<String, Object> rules = validator.getValidationRules(PLUGIN);
            assertThat(rules).containsKeys("port", "name");
            assertThat(rules).doesNotContainKey("empty");

            @SuppressWarnings("unchecked")
            Map<String, Object> portRules = (Map<String, Object>) rules.get("port");
            assertThat(portRules).containsExactlyInAnyOrderEntriesOf(Map.of(
                    "type", "integer", "required", true, "minimum", 1, "maximum", 65535));

            @SuppressWarnings("unchecked")
            Map<String, Object> nameRules = (Map<String, Object>) rules.get("name");
            assertThat(nameRules).containsKeys(
                    "type", "minLength", "maxLength", "pattern", "enum");
        }
    }

    @Nested
    @DisplayName("custom validators registry")
    class CustomValidatorRegistryTests {

        @Test
        @DisplayName("getCustomValidators returns registered validators")
        void registry() {
            assertThat(validator.getCustomValidators(PLUGIN)).isEmpty();
            ConfigValidator cv = value -> null;
            validator.registerCustomValidator(PLUGIN, "path", cv);
            assertThat(validator.getCustomValidators(PLUGIN)).containsEntry("path", cv);
        }
    }

    @Test
    @DisplayName("validation result warnings list is an ArrayList")
    void warningsListType() {
        ValidationResult result = validator.validateConfiguration(PLUGIN, Map.of());
        assertThat(result.getWarnings()).isExactlyInstanceOf(ArrayList.class);
        assertThat(result.getErrors()).isExactlyInstanceOf(ArrayList.class);
    }
}
