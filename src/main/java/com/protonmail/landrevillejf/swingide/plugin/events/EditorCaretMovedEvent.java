package com.protonmail.landrevillejf.swingide.plugin.events;

public class EditorCaretMovedEvent extends BaseEvent {
    private final String editorId;
    private final String filePath;
    private final int line;
    private final int column;

    public EditorCaretMovedEvent(String source, String editorId, String filePath, int line, int column) {
        super(source);
        this.editorId = editorId;
        this.filePath = filePath;
        this.line = line;
        this.column = column;
    }

    public String getEditorId() {
        return editorId;
    }

    public String getFilePath() {
        return filePath;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }
}
