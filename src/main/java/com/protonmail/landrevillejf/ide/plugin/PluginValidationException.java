package com.protonmail.landrevillejf.ide.plugin;

/**
 * Exception thrown when plugin validation fails.
 * <p>
 * This exception is used to indicate that a plugin has failed validation
 * checks, such as missing required fields, invalid configuration, or
 * incompatible dependencies.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
class PluginValidationException extends Exception {
    /**
     * Creates a new validation exception with a message.
     *
     * @param message the error message
     */
    public PluginValidationException(String message) {
        super(message);
    }

    /**
     * Creates a new validation exception with a message and cause.
     *
     * @param message the error message
     * @param cause the underlying cause
     */
    public PluginValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}