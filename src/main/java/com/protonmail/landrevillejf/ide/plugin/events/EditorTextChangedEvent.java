package com.protonmail.landrevillejf.ide.plugin.events;

/**
 * Event fired when text in an editor changes.
 * <p>
 * This event contains information about the editor, file path, caret position,
 * selection, and the changed text.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
public class EditorTextChangedEvent extends BaseEvent {
    private final String editorId;
    private final String filePath;
    private final int caretPosition;
    private final int selectionStart;
    private final int selectionEnd;
    private final String text;

    /**
     * Creates a new editor text changed event.
     *
     * @param source the source of this event
     * @param editorId the editor identifier
     * @param filePath the file path being edited
     * @param caretPosition the caret position
     * @param selectionStart the selection start position
     * @param selectionEnd the selection end position
     * @param text the changed text
     */
    public EditorTextChangedEvent(String source, String editorId, String filePath,
                                  int caretPosition, int selectionStart, int selectionEnd, String text) {
        super(source);
        this.editorId = editorId;
        this.filePath = filePath;
        this.caretPosition = caretPosition;
        this.selectionStart = selectionStart;
        this.selectionEnd = selectionEnd;
        this.text = text;
    }

    /**
     * Returns the changed text.
     *
     * @return the text
     */
    public String getText() {
        return text;
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
     * Returns the caret position.
     *
     * @return the caret position
     */
    public int getCaretPosition() {
        return caretPosition;
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



