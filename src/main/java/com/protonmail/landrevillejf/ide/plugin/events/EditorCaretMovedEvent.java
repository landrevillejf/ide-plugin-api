package com.protonmail.landrevillejf.ide.plugin.events;

/**
 * Event fired when the caret (cursor) moves in an editor.
 * <p>
 * This event contains information about the editor, file path, and the
 * new line and column position of the caret.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
public class EditorCaretMovedEvent extends BaseEvent {
    private final String editorId;
    private final String filePath;
    private final int line;
    private final int column;

    /**
     * Creates a new editor caret moved event.
     *
     * @param source the source of this event
     * @param editorId the editor identifier
     * @param filePath the file path being edited
     * @param line the new line number
     * @param column the new column number
     */
    public EditorCaretMovedEvent(String source, String editorId, String filePath, int line, int column) {
        super(source);
        this.editorId = editorId;
        this.filePath = filePath;
        this.line = line;
        this.column = column;
    }

    /**
     * Returns the editor identifier.
     *
     * @return the editor ID
     */
    public String getEditorId() {
        return editorId;
    }

    /**
     * Returns the file path being edited.
     *
     * @return the file path
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Returns the new line number.
     *
     * @return the line number
     */
    public int getLine() {
        return line;
    }

    /**
     * Returns the new column number.
     *
     * @return the column number
     */
    public int getColumn() {
        return column;
    }
}
