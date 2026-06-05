package com.protonmail.landrevillejf.swingide.plugin.service.impl;

import com.protonmail.landrevillejf.swingide.plugin.service.PluginConfigurationValidator;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Slf4j
public class DefaultPluginConfigurationValidator implements PluginConfigurationValidator {

    private final Map<String, Map<String, Object>> schemas = new ConcurrentHashMap<>();
    private final Map<String, Map<String, ConfigValidator>> customValidators = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Object>> defaultConfigs = new ConcurrentHashMap<>();

    public DefaultPluginConfigurationValidator() {
        log.info("DefaultPluginConfigurationValidator initialized");
    }

    @Override
    public ValidationResult validateConfiguration(String pluginId, Map<String, Object> config) {
        Map<String, Object> schema = getSchema(pluginId);
        if (schema == null || schema.isEmpty()) {
            log.warn("No schema registered for plugin: {}", pluginId);
            return ValidationResultImpl.valid();
        }

        List<ValidationError> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Validate required fields
        validateRequiredFields(pluginId, schema, config, errors);

        // Validate field types and rules
        validateFieldTypes(pluginId, schema, config, errors, warnings);

        // Validate custom validators
        validateCustomValidators(pluginId, config, errors);

        boolean isValid = errors.isEmpty();

        log.debug("Configuration validation for plugin {}: valid={}, errors={}, warnings={}",
                pluginId, isValid, errors.size(), warnings.size());

        return new ValidationResultImpl(isValid, errors, warnings);
    }

    @Override
    public ValidationResult validateValue(String pluginId, String path, Object value) {
        Map<String, Object> schema = getSchema(pluginId);
        if (schema == null || schema.isEmpty()) {
            return ValidationResultImpl.valid();
        }

        List<ValidationError> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Get the schema for the specific path
        Object pathSchema = getValueByPath(schema, path);
        if (pathSchema instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> fieldSchema = (Map<String, Object>) pathSchema;
            validateValueAgainstSchema(pluginId, path, value, fieldSchema, errors, warnings);

            // Check custom validator for this path
            validateCustomValidatorForPath(pluginId, path, value, errors);
        } else {
            errors.add(new ValidationErrorImpl(path, "No schema defined for path: " + path, "SCHEMA_NOT_FOUND"));
        }

        boolean isValid = errors.isEmpty();
        return new ValidationResultImpl(isValid, errors, warnings);
    }

    @Override
    public Map<String, Object> getSchema(String pluginId) {
        return schemas.get(pluginId);
    }

    @Override
    public void registerSchema(String pluginId, Map<String, Object> schema) {
        schemas.put(pluginId, new ConcurrentHashMap<>(schema));

        // Also store default configuration if present
        if (schema.containsKey("default")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> defaults = (Map<String, Object>) schema.get("default");
            defaultConfigs.put(pluginId, new ConcurrentHashMap<>(defaults));
        }

        log.debug("Schema registered for plugin: {}", pluginId);
    }

    @Override
    public void registerCustomValidator(String pluginId, String path, ConfigValidator validator) {
        customValidators.computeIfAbsent(pluginId, k -> new ConcurrentHashMap<>())
                .put(path, validator);
        log.debug("Custom validator registered for plugin {} at path: {}", pluginId, path);
    }

    @Override
    public Map<String, ConfigValidator> getCustomValidators(String pluginId) {
        return customValidators.getOrDefault(pluginId, Collections.emptyMap());
    }

    @Override
    public Map<String, Object> getDefaultConfiguration(String pluginId) {
        return defaultConfigs.getOrDefault(pluginId, Collections.emptyMap());
    }

    @Override
    public Map<String, Object> mergeWithDefaults(String pluginId, Map<String, Object> partialConfig) {
        Map<String, Object> defaults = getDefaultConfiguration(pluginId);
        if (defaults.isEmpty()) {
            return new HashMap<>(partialConfig);
        }

        Map<String, Object> merged = new LinkedHashMap<>(defaults);
        mergeDeep(merged, partialConfig);

        log.debug("Merged configuration for plugin {}: {} fields", pluginId, merged.size());
        return merged;
    }

    @Override
    public Map<String, Object> generateSampleConfiguration(String pluginId) {
        Map<String, Object> schema = getSchema(pluginId);
        if (schema == null || schema.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Object> sample = new LinkedHashMap<>();
        generateSampleFromSchema(schema, sample);

        log.debug("Generated sample configuration for plugin: {}", pluginId);
        return sample;
    }

    @Override
    public Map<String, Object> getValidationRules(String pluginId) {
        Map<String, Object> schema = getSchema(pluginId);
        if (schema == null) {
            return Collections.emptyMap();
        }

        Map<String, Object> rules = new LinkedHashMap<>();
        extractValidationRules(schema, "", rules);
        return rules;
    }

    private void validateRequiredFields(String pluginId, Map<String, Object> schema,
                                        Map<String, Object> config, List<ValidationError> errors) {
        if (schema.containsKey("properties")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> properties = (Map<String, Object>) schema.get("properties");

            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                String fieldName = entry.getKey();
                @SuppressWarnings("unchecked")
                Map<String, Object> fieldSchema = (Map<String, Object>) entry.getValue();

                boolean required = fieldSchema.containsKey("required") &&
                        (Boolean) fieldSchema.get("required");

                if (required && !config.containsKey(fieldName)) {
                    errors.add(new ValidationErrorImpl(fieldName,
                            "Required field missing: " + fieldName, "REQUIRED_FIELD_MISSING"));
                }
            }
        }
    }

    private void validateFieldTypes(String pluginId, Map<String, Object> schema,
                                    Map<String, Object> config, List<ValidationError> errors,
                                    List<String> warnings) {
        if (schema.containsKey("properties")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> properties = (Map<String, Object>) schema.get("properties");

            for (Map.Entry<String, String> entry : flattenConfig(config).entrySet()) {
                String path = entry.getKey();
                Object value = entry.getValue();

                // Find schema for this path
                Object pathSchema = getValueByPath(properties, path);
                if (pathSchema instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> fieldSchema = (Map<String, Object>) pathSchema;
                    validateValueAgainstSchema(pluginId, path, value, fieldSchema, errors, warnings);
                }
            }
        }
    }

    private void validateValueAgainstSchema(String pluginId, String path, Object value,
                                            Map<String, Object> fieldSchema,
                                            List<ValidationError> errors,
                                            List<String> warnings) {
        // Type validation
        if (fieldSchema.containsKey("type")) {
            String expectedType = (String) fieldSchema.get("type");
            if (!isTypeMatch(value, expectedType)) {
                errors.add(new ValidationErrorImpl(path,
                        String.format("Expected type %s but got %s", expectedType,
                                value != null ? value.getClass().getSimpleName() : "null"),
                        "TYPE_MISMATCH"));
                return;
            }
        }

        if (value == null) {
            return;
        }

        // Range validation for numbers
        if (value instanceof Number) {
            validateNumberRange(path, (Number) value, fieldSchema, errors);
        }

        // String validation
        if (value instanceof String) {
            validateStringConstraints(path, (String) value, fieldSchema, errors, warnings);
        }

        // Array/Collection validation
        if (value instanceof Collection) {
            validateCollectionConstraints(path, (Collection<?>) value, fieldSchema, errors);
        }

        // Enum validation
        if (fieldSchema.containsKey("enum")) {
            validateEnumValue(path, value, fieldSchema, errors);
        }
    }

    private void validateNumberRange(String path, Number value, Map<String, Object> fieldSchema,
                                     List<ValidationError> errors) {
        double doubleValue = value.doubleValue();

        if (fieldSchema.containsKey("minimum")) {
            double min = ((Number) fieldSchema.get("minimum")).doubleValue();
            if (doubleValue < min) {
                errors.add(new ValidationErrorImpl(path,
                        String.format("Value %.2f is below minimum %.2f", doubleValue, min),
                        "BELOW_MINIMUM"));
            }
        }

        if (fieldSchema.containsKey("maximum")) {
            double max = ((Number) fieldSchema.get("maximum")).doubleValue();
            if (doubleValue > max) {
                errors.add(new ValidationErrorImpl(path,
                        String.format("Value %.2f exceeds maximum %.2f", doubleValue, max),
                        "ABOVE_MAXIMUM"));
            }
        }
    }

    private void validateStringConstraints(String path, String value, Map<String, Object> fieldSchema,
                                           List<ValidationError> errors, List<String> warnings) {
        if (fieldSchema.containsKey("minLength")) {
            int minLength = (int) fieldSchema.get("minLength");
            if (value.length() < minLength) {
                errors.add(new ValidationErrorImpl(path,
                        String.format("String length %d is below minimum %d", value.length(), minLength),
                        "STRING_TOO_SHORT"));
            }
        }

        if (fieldSchema.containsKey("maxLength")) {
            int maxLength = (int) fieldSchema.get("maxLength");
            if (value.length() > maxLength) {
                errors.add(new ValidationErrorImpl(path,
                        String.format("String length %d exceeds maximum %d", value.length(), maxLength),
                        "STRING_TOO_LONG"));
            }
        }

        if (fieldSchema.containsKey("pattern")) {
            String pattern = (String) fieldSchema.get("pattern");
            if (!Pattern.matches(pattern, value)) {
                errors.add(new ValidationErrorImpl(path,
                        String.format("Value '%s' does not match pattern %s", value, pattern),
                        "PATTERN_MISMATCH"));
            }
        }

        // Email validation
        if (fieldSchema.containsKey("format") && "email".equals(fieldSchema.get("format"))) {
            if (!isValidEmail(value)) {
                errors.add(new ValidationErrorImpl(path, "Invalid email format", "INVALID_EMAIL"));
            }
        }

        // URL validation
        if (fieldSchema.containsKey("format") && "url".equals(fieldSchema.get("format"))) {
            if (!isValidUrl(value)) {
                errors.add(new ValidationErrorImpl(path, "Invalid URL format", "INVALID_URL"));
            }
        }
    }

    private void validateCollectionConstraints(String path, Collection<?> value,
                                               Map<String, Object> fieldSchema,
                                               List<ValidationError> errors) {
        if (fieldSchema.containsKey("minItems")) {
            int minItems = (int) fieldSchema.get("minItems");
            if (value.size() < minItems) {
                errors.add(new ValidationErrorImpl(path,
                        String.format("Collection size %d is below minimum %d", value.size(), minItems),
                        "COLLECTION_TOO_SMALL"));
            }
        }

        if (fieldSchema.containsKey("maxItems")) {
            int maxItems = (int) fieldSchema.get("maxItems");
            if (value.size() > maxItems) {
                errors.add(new ValidationErrorImpl(path,
                        String.format("Collection size %d exceeds maximum %d", value.size(), maxItems),
                        "COLLECTION_TOO_LARGE"));
            }
        }

        if (fieldSchema.containsKey("uniqueItems") && (Boolean) fieldSchema.get("uniqueItems")) {
            if (value.size() != new HashSet<>(value).size()) {
                errors.add(new ValidationErrorImpl(path, "Collection contains duplicate items", "DUPLICATE_ITEMS"));
            }
        }
    }

    private void validateEnumValue(String path, Object value, Map<String, Object> fieldSchema,
                                   List<ValidationError> errors) {
        @SuppressWarnings("unchecked")
        List<Object> enumValues = (List<Object>) fieldSchema.get("enum");
        if (!enumValues.contains(value)) {
            errors.add(new ValidationErrorImpl(path,
                    String.format("Value '%s' is not in allowed values: %s", value, enumValues),
                    "INVALID_ENUM_VALUE"));
        }
    }

    private void validateCustomValidators(String pluginId, Map<String, Object> config,
                                          List<ValidationError> errors) {
        Map<String, ConfigValidator> validators = customValidators.get(pluginId);
        if (validators != null) {
            for (Map.Entry<String, ConfigValidator> entry : validators.entrySet()) {
                String path = entry.getKey();
                ConfigValidator validator = entry.getValue();
                Object value = getValueByPath(config, path);

                String error = validator.validate(value);
                if (error != null) {
                    errors.add(new ValidationErrorImpl(path, error, "CUSTOM_VALIDATION_FAILED"));
                }
            }
        }
    }

    private void validateCustomValidatorForPath(String pluginId, String path, Object value,
                                                List<ValidationError> errors) {
        Map<String, ConfigValidator> validators = customValidators.get(pluginId);
        if (validators != null && validators.containsKey(path)) {
            ConfigValidator validator = validators.get(path);
            String error = validator.validate(value);
            if (error != null) {
                errors.add(new ValidationErrorImpl(path, error, "CUSTOM_VALIDATION_FAILED"));
            }
        }
    }

    private boolean isTypeMatch(Object value, String expectedType) {
        if (value == null) return true;

        switch (expectedType) {
            case "string": return value instanceof String;
            case "number": return value instanceof Number;
            case "integer": return value instanceof Integer || value instanceof Long;
            case "boolean": return value instanceof Boolean;
            case "array": return value instanceof Collection || value instanceof Object[];
            case "object": return value instanceof Map;
            default: return true;
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return Pattern.matches(emailRegex, email);
    }

    private boolean isValidUrl(String url) {
        String urlRegex = "^(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})([/\\w .-]*)*/?$";
        return Pattern.matches(urlRegex, url);
    }

    @SuppressWarnings("unchecked")
    private Object getValueByPath(Map<String, Object> map, String path) {
        String[] parts = path.split("\\.");
        Object current = map;

        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(part);
            } else {
                return null;
            }
        }

        return current;
    }

    private Map<String, String> flattenConfig(Map<String, Object> config) {
        Map<String, String> flat = new LinkedHashMap<>();
        flattenConfigRecursive(config, "", flat);
        return flat;
    }

    @SuppressWarnings("unchecked")
    private void flattenConfigRecursive(Map<String, Object> config, String prefix,
                                        Map<String, String> result) {
        for (Map.Entry<String, Object> entry : config.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map) {
                flattenConfigRecursive((Map<String, Object>) value, key, result);
            } else {
                result.put(key, value != null ? value.toString() : "null");
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void generateSampleFromSchema(Map<String, Object> schema, Map<String, Object> sample) {
        if (schema.containsKey("properties")) {
            Map<String, Object> properties = (Map<String, Object>) schema.get("properties");

            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                String fieldName = entry.getKey();
                Map<String, Object> fieldSchema = (Map<String, Object>) entry.getValue();

                Object sampleValue = generateSampleValue(fieldSchema);
                if (sampleValue != null) {
                    sample.put(fieldName, sampleValue);
                }
            }
        }
    }

    private Object generateSampleValue(Map<String, Object> fieldSchema) {
        if (fieldSchema.containsKey("example")) {
            return fieldSchema.get("example");
        }

        if (fieldSchema.containsKey("default")) {
            return fieldSchema.get("default");
        }

        String type = (String) fieldSchema.getOrDefault("type", "string");

        switch (type) {
            case "string":
                if (fieldSchema.containsKey("enum")) {
                    @SuppressWarnings("unchecked")
                    List<String> enumValues = (List<String>) fieldSchema.get("enum");
                    return enumValues.get(0);
                }
                return "sample_value";
            case "number":
                return 0.0;
            case "integer":
                return 0;
            case "boolean":
                return false;
            case "array":
                return new ArrayList<>();
            case "object":
                return new LinkedHashMap<>();
            default:
                return null;
        }
    }

    private void extractValidationRules(Map<String, Object> schema, String prefix,
                                        Map<String, Object> rules) {
        if (schema.containsKey("properties")) {
            @SuppressWarnings("unchecked")
            Map<String, Object> properties = (Map<String, Object>) schema.get("properties");

            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                String fieldName = entry.getKey();
                String fullPath = prefix.isEmpty() ? fieldName : prefix + "." + fieldName;
                @SuppressWarnings("unchecked")
                Map<String, Object> fieldSchema = (Map<String, Object>) entry.getValue();

                Map<String, Object> fieldRules = new LinkedHashMap<>();
                if (fieldSchema.containsKey("type")) fieldRules.put("type", fieldSchema.get("type"));
                if (fieldSchema.containsKey("required")) fieldRules.put("required", fieldSchema.get("required"));
                if (fieldSchema.containsKey("minimum")) fieldRules.put("minimum", fieldSchema.get("minimum"));
                if (fieldSchema.containsKey("maximum")) fieldRules.put("maximum", fieldSchema.get("maximum"));
                if (fieldSchema.containsKey("minLength")) fieldRules.put("minLength", fieldSchema.get("minLength"));
                if (fieldSchema.containsKey("maxLength")) fieldRules.put("maxLength", fieldSchema.get("maxLength"));
                if (fieldSchema.containsKey("pattern")) fieldRules.put("pattern", fieldSchema.get("pattern"));
                if (fieldSchema.containsKey("enum")) fieldRules.put("enum", fieldSchema.get("enum"));

                if (!fieldRules.isEmpty()) {
                    rules.put(fullPath, fieldRules);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void mergeDeep(Map<String, Object> target, Map<String, Object> source) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map && target.containsKey(key) && target.get(key) instanceof Map) {
                mergeDeep((Map<String, Object>) target.get(key), (Map<String, Object>) value);
            } else {
                target.put(key, value);
            }
        }
    }

    /**
     * Implementation of ValidationResult
     */
    private static class ValidationResultImpl implements ValidationResult {
        private final boolean valid;
        private final List<ValidationError> errors;
        private final List<String> warnings;

        public ValidationResultImpl(boolean valid, List<ValidationError> errors, List<String> warnings) {
            this.valid = valid;
            this.errors = errors;
            this.warnings = warnings;
        }

        public static ValidationResultImpl valid() {
            return new ValidationResultImpl(true, new ArrayList<>(), new ArrayList<>());
        }

        @Override
        public boolean isValid() { return valid; }

        @Override
        public List<ValidationError> getErrors() { return errors; }

        @Override
        public List<String> getWarnings() { return warnings; }
    }

    /**
     * Implementation of ValidationError
     */
    private static class ValidationErrorImpl implements ValidationError {
        private final String path;
        private final String message;
        private final String errorCode;

        public ValidationErrorImpl(String path, String message, String errorCode) {
            this.path = path;
            this.message = message;
            this.errorCode = errorCode;
        }

        @Override
        public String getPath() { return path; }

        @Override
        public String getMessage() { return message; }

        @Override
        public String getErrorCode() { return errorCode; }

        @Override
        public String toString() {
            return String.format("[%s] %s: %s", errorCode, path, message);
        }
    }
}