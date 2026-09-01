package com.protonmail.landrevillejf.ide.plugin.events;

import java.io.File;

/**
 * Event fired when a file is deleted in the IDE.
 * <p>
 * This event contains information about the deleted file's path and name.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
public class FileDeletedEvent extends BaseEvent {
    private final String filePath;
    private final String fileName;

    /**
     * Creates a new file deleted event.
     *
     * @param source the source of this event
     * @param filePath the path to the deleted file
     */
    public FileDeletedEvent(String source, String filePath) {
        super(source);
        this.filePath = filePath;
        this.fileName = new File(filePath).getName();
    }

    /**
     * Returns the file path.
     *
     * @return the file path
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Returns the file name.
     *
     * @return the file name
     */
    public String getFileName() {
        return fileName;
    }
}

