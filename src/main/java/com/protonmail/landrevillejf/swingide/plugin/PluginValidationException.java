package com.protonmail.landrevillejf.swingide.plugin;

public // ========== EXCEPTION DE VALIDATION ==========
class PluginValidationException extends Exception {
    public PluginValidationException(String message) {
        super(message);
    }

    public PluginValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}