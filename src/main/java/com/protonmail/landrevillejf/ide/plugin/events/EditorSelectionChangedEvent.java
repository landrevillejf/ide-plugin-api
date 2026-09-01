package com.protonmail.landrevillejf.ide.plugin.events;

/**
 * Event fired when the text selection in an editor changes.
 * <p>
 * This event contains information about the editor, file path, selected text,
 * and the selection range.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
public class EditorSelectionChangedEvent extends BaseEvent {
    private final String editorId;
    private final String filePath;
    private final String selectedText;
    private final int selectionStart;
    private final int selectionEnd;

    /**
     * Creates a new editor selection changed event.
     *
     * @param source the source of this event
     * @param editorId the editor identifier
     * @param filePath the file path being edited
     * @param selectedText the selected text
     * @param selectionStart the selection start position
     * @param selectionEnd the selection end position
     */
    public EditorSelectionChangedEvent(String source, String editorId, String filePath,
                                       String selectedText, int selectionStart, int selectionEnd) {
        super(source);
        this.editorId = editorId;
        this.filePath = filePath;
        this.selectedText = selectedText;
        this.selectionStart = selectionStart;
        this.selectionEnd = selectionEnd;
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
     * Returns the selected text.
     *
     * @return the selected text
     */
    public String getSelectedText() {
        return selectedText;
    }

    /**
     * Returns the selection start position.
     *
     * @return the selection start
     */
    public int getSelectionStart() {
        return selectionStart;
    }

    /**
     * Returns the selection end position.
     *
     * @return the selection end
     */
    public int getSelectionEnd() {
        return selectionEnd;
    }
}