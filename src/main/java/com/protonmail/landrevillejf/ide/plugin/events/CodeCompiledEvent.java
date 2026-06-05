package com.protonmail.landrevillejf.ide.plugin.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    public CodeCompiledEvent(String source, String className, String filePath) {
        this.source = source;
        this.className = className;
        this.filePath = filePath;
    }

    public CodeCompiledEvent(String source, String className, String filePath,
                             boolean success, long compilationTime) {
        this.source = source;
        this.className = className;
        this.filePath = filePath;
        this.success = success;
        this.compilationTime = compilationTime;
    }

    // Méthodes utilitaires

    public String getStatus() {
        return success ? "SUCCESS" : "FAILED";
    }

    public boolean hasErrors() {
        return errorCount > 0;
    }

    public boolean hasWarnings() {
        return warningCount > 0;
    }

    public double getCompilationSpeed() {
        if (compilationTime == 0 || lineCount == 0) {
            return 0;
        }
        return (double) lineCount / compilationTime * 1000; // lignes par seconde
    }

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