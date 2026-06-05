package com.protonmail.landrevillejf.ide.plugin.events;

public class EditorTextChangedEvent extends BaseEvent {
    private final String editorId;
    private final String filePath;
    private final int caretPosition;
    private final int selectionStart;
    private final int selectionEnd;
    private final String text;

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

    public String getText() {
        return text;
    }

    public String getEditorId() {
        return editorId;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getCaretPosition() {
        return caretPosition;
    }

    public int getSelectionStart() {
        return selectionStart;
    }

    public int getSelectionEnd() {
        return selectionEnd;
    }
}



