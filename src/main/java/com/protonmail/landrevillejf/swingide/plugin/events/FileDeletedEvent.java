package com.protonmail.landrevillejf.swingide.plugin.events;

import java.io.File;

public class FileDeletedEvent extends BaseEvent {
    private final String filePath;
    private final String fileName;

    public FileDeletedEvent(String source, String filePath) {
        super(source);
        this.filePath = filePath;
        this.fileName = new File(filePath).getName();
    }

    public String getFilePath() {
        return filePath;
    }

    public String getFileName() {
        return fileName;
    }
}

