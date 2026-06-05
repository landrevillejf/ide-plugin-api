package com.protonmail.landrevillejf.swingide.plugin.events;

import java.io.File;

public class FileCreatedEvent extends BaseEvent {
    private final String filePath;
    private final File file;
    private final String fileType;

    public FileCreatedEvent(String source, String filePath) {
        super(source);
        this.filePath = filePath;
        this.file = new File(filePath);
        this.fileType = getFileExtension(filePath);
    }

    public String getFilePath() {
        return filePath;
    }

    public File getFile() {
        return file;
    }

    public String getFileType() {
        return fileType;
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot == -1 ? "" : filename.substring(lastDot + 1);
    }
}