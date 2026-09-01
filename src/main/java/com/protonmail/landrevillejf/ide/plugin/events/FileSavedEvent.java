package com.protonmail.landrevillejf.ide.plugin.events;

import java.io.File;

/**
 * Event fired when a file is saved in the IDE.
 * <p>
 * This event contains information about the file path, file object,
 * file type (extension), and file size.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
public class FileSavedEvent extends BaseEvent {
    private final String filePath;
    private final File file;
    private final String fileType;
    private final long fileSize;

    /**
     * Creates a new file saved event.
     *
     * @param source the source of this event
     * @param filePath the path to the saved file
     */
    public FileSavedEvent(String source, String filePath) {
        super(source);
        this.filePath = filePath;
        this.file = new File(filePath);
        this.fileType = getFileExtension(filePath);
        this.fileSize = file.length();
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
     * Returns the file size in bytes.
     *
     * @return the file size
     */
    public long getFileSize() {
        return fileSize;
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