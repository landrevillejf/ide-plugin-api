package com.protonmail.landrevillejf.ide.plugin.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Event fired when code is compiled.
 * <p>
 * This event contains detailed information about the compilation including
 * class name, file path, error/warning counts, and compilation time.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeCompiledEvent implements Event {
    private String source;
    private LocalDateTime timestamp = LocalDateTime.now();

    // Champs spécifiques à la compilation
    private String className;
    private String filePath;
    private int lineCount;
    private int errorCount;
    private int warningCount;
    private boolean success;
    private long compilationTime; // en millisecondes
    private String outputDirectory;
    private String compiler;
    private String[] errorMessages;
    private String[] warningMessages;

    // Constructeurs

    /**
     * Creates a new code compiled event.
     *
     * @param source the source of this event
     * @param className the class name that was compiled
     * @param filePath the file path that was compiled
     */
    public CodeCompiledEvent(String source, String className, String filePath) {
        this.source = source;
        this.className = className;
        this.filePath = filePath;
    }

    /**
     * Creates a new code compiled event with result information.
     *
     * @param source the source of this event
     * @param className the class name that was compiled
     * @param filePath the file path that was compiled
     * @param success whether compilation succeeded
     * @param compilationTime the compilation time in milliseconds
     */
    public CodeCompiledEvent(String source, String className, String filePath,
                             boolean success, long compilationTime) {
        this.source = source;
        this.className = className;
        this.filePath = filePath;
        this.success = success;
        this.compilationTime = compilationTime;
    }

    // Méthodes utilitaires

    /**
     * Returns the compilation status as a string.
     *
     * @return "SUCCESS" or "FAILED"
     */
    public String getStatus() {
        return success ? "SUCCESS" : "FAILED";
    }

    /**
     * Returns whether there are compilation errors.
     *
     * @return {@code true} if errors exist, {@code false} otherwise
     */
    public boolean hasErrors() {
        return errorCount > 0;
    }

    /**
     * Returns whether there are compilation warnings.
     *
     * @return {@code true} if warnings exist, {@code false} otherwise
     */
    public boolean hasWarnings() {
        return warningCount > 0;
    }

    /**
     * Returns the compilation speed in lines per second.
     *
     * @return the compilation speed
     */
    public double getCompilationSpeed() {
        if (compilationTime == 0 || lineCount == 0) {
            return 0;
        }
        return (double) lineCount / compilationTime * 1000; // lignes par seconde
    }

    /**
     * Returns the file name from the file path.
     *
     * @return the file name
     */
    public String getFileName() {
        if (filePath != null) {
            int lastSlash = filePath.lastIndexOf('/');
            int lastBackslash = filePath.lastIndexOf('\\');
            int lastSeparator = Math.max(lastSlash, lastBackslash);
            if (lastSeparator != -1) {
                return filePath.substring(lastSeparator + 1);
            }
        }
        return filePath;
    }

    /**
     * Returns the file extension from the file path.
     *
     * @return the file extension, or empty string if none
     */
    public String getFileExtension() {
        if (filePath != null) {
            int dotIndex = filePath.lastIndexOf('.');
            if (dotIndex > 0) {
                return filePath.substring(dotIndex + 1).toLowerCase();
            }
        }
        return "";
    }
}