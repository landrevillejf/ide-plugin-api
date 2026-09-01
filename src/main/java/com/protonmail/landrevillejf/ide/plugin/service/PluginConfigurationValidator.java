package com.protonmail.landrevillejf.ide.plugin.service;

import java.util.List;
import java.util.Map;

/**
 * Configuration validation and schema management service for plugins.
 * <p>
 * Provides schema registration, custom validators, default configuration
 * generation, and configuration merging capabilities.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
public interface PluginConfigurationValidator {

    /**
     * Configuration validation result.
     */
    interface ValidationResult {
        boolean isValid();
        List<ValidationError> getErrors();
        List<String> getWarnings();
    }

    /**
     * Represents a validation error.
     */
    interface ValidationError {
        String getPath();
        String getMessage();
        String getErrorCode();
    }

    /**
     * Validates a configuration against the plugin's schema.
     *
     * @param pluginId the plugin identifier
     * @param config the configuration to validate
     * @return the validation result
     */
    ValidationResult validateConfiguration(String pluginId, Map<String, Object> config);

    /**
     * Validates a configuration value.
     *
     * @param pluginId the plugin identifier
     * @param path the configuration path
     * @param value the value to validate
     * @return the validation result
     */
    ValidationResult validateValue(String pluginId, String path, Object value);

    /**
     * Gets the schema for a plugin's configuration.
     *
     * @param pluginId the plugin identifier
     * @return the configuration schema
     */
    Map<String, Object> getSchema(String pluginId);

    /**
     * Registers a schema for a plugin.
     *
     * @param pluginId the plugin identifier
     * @param schema the configuration schema
     */
    void registerSchema(String pluginId, Map<String, Object> schema);

    /**
     * Registers a custom validator for a specific path.
     *
     * @param pluginId the plugin identifier
     * @param path the configuration path
     * @param validator the validator function
     */
    void registerCustomValidator(String pluginId, String path, ConfigValidator validator);

    /**
     * Gets all registered custom validators for a plugin.
     *
     * @param pluginId the plugin identifier
     * @return map of paths to validators
     */
    Map<String, ConfigValidator> getCustomValidators(String pluginId);

    /**
     * Gets the default configuration for a plugin.
     *
     * @param pluginId the plugin identifier
     * @return the default configuration
     */
    Map<String, Object> getDefaultConfiguration(String pluginId);

    /**
     * Merges a partial configuration with defaults.
     *
     * @param pluginId the plugin identifier
     * @param partialConfig the partial configuration
     * @return the merged configuration
     */
    Map<String, Object> mergeWithDefaults(String pluginId, Map<String, Object> partialConfig);

    /**
     * Generates a sample configuration based on the schema.
     *
     * @param pluginId the plugin identifier
     * @return a sample configuration
     */
    Map<String, Object> generateSampleConfiguration(String pluginId);

    /**
     * Gets validation rules for a plugin.
     *
     * @param pluginId the plugin identifier
     * @return a map of paths to validation rules
     */
    Map<String, Object> getValidationRules(String pluginId);

    /**
     * Custom configuration validator function.
     */
    interface ConfigValidator {
        /**
         * Validates a configuration value.
         *
         * @param value the value to validate
         * @return null if valid, error message if invalid
         */
        String validate(Object value);
    }
}

