package com.protonmail.landrevillejf.ide.plugin.events;

import java.io.File;

/**
 * Event fired when a file is opened in the IDE.
 * <p>
 * This event contains information about the file path, file object,
 * and file type (extension).
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
public class FileOpenedEvent extends BaseEvent {
    private final String filePath;
    private final File file;
    private final String fileType;

    /**
     * Creates a new file opened event.
     *
     * @param source the source of this event
     * @param filePath the path to the opened file
     */
    public FileOpenedEvent(String source, String filePath) {
        super(source);
        this.filePath = filePath;
        this.file = new File(filePath);
        this.fileType = getFileExtension(filePath);
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
     * Returns the file object.
     *
     * @return the file
     */
    public File getFile() {
        return file;
    }

    /**
     * Returns the file type (extension).
     *
     * @return the file extension, or empty string if none
     */
    public String getFileType() {
        return fileType;
    }

    /**
     * Extracts the file extension from a filename.
     *
     * @param filename the filename
     * @return the file extension, or empty string if none
     */
    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot == -1 ? "" : filename.substring(lastDot + 1);
    }
}