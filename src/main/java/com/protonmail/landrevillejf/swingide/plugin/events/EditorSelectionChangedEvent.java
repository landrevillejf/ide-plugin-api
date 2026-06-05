package com.protonmail.landrevillejf.swingide.plugin.events;

public class EditorSelectionChangedEvent extends BaseEvent {
    private final String editorId;
    private final String filePath;
    private final String selectedText;
    private final int selectionStart;
    private final int selectionEnd;

    public EditorSelectionChangedEvent(String source, String editorId, String filePath,
                                       String selectedText, int selectionStart, int selectionEnd) {
        super(source);
        this.editorId = editorId;
        this.filePath = filePath;
        this.selectedText = selectedText;
        this.selectionStart = selectionStart;
        this.selectionEnd = selectionEnd;
    }

    public String getEditorId() {
        return editorId;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getSelectedText() {
        return selectedText;
    }

    public int getSelectionStart() {
        return selectionStart;
    }

    public int getSelectionEnd() {
        return selectionEnd;
    }
}