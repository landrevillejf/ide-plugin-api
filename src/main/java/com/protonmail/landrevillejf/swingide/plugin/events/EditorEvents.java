package com.protonmail.landrevillejf.swingide.plugin.events;

import java.time.LocalDateTime;

public class EditorEvents {

    // Event déclenché quand l'éditeur actif change
    public static class ActiveEditorChangedEvent implements Event {
        private final Object editorComponent; // Votre type CodeEditor
        private final String filePath;

        public ActiveEditorChangedEvent(Object editorComponent, String filePath) {
            this.editorComponent = editorComponent;
            this.filePath = filePath;
        }

        public Object getEditorComponent() { return editorComponent; }
        public String getFilePath() { return filePath; }

        @Override
        public LocalDateTime getTimestamp() {
            return Event.super.getTimestamp();
        }

        @Override
        public String getSource() {
            return "";
        }
    }

    // Event déclenché quand le contenu de l'éditeur change
    public static class DocumentChangedEvent implements Event {
        private final String content;
        private final String filePath;
        private final int caretPosition;

        public DocumentChangedEvent(String content, String filePath, int caretPosition) {
            this.content = content;
            this.filePath = filePath;
            this.caretPosition = caretPosition;
        }

        public String getContent() { return content; }
        public String getFilePath() { return filePath; }
        public int getCaretPosition() { return caretPosition; }

        @Override
        public LocalDateTime getTimestamp() {
            return Event.super.getTimestamp();
        }

        @Override
        public String getSource() {
            return "";
        }
    }

    // Event déclenché avant la sauvegarde d'un fichier
    public static class BeforeFileSaveEvent implements Event {
        private final String content;
        private final String filePath;

        public BeforeFileSaveEvent(String content, String filePath) {
            this.content = content;
            this.filePath = filePath;
        }

        public String getContent() { return content; }
        public String getFilePath() { return filePath; }

        @Override
        public LocalDateTime getTimestamp() {
            return Event.super.getTimestamp();
        }

        @Override
        public String getSource() {
            return "";
        }
    }

    // Event déclenché après l'ouverture d'un fichier
    public static class FileOpenedEvent implements Event {
        private final String filePath;
        private final String content;

        public FileOpenedEvent(String filePath, String content) {
            this.filePath = filePath;
            this.content = content;
        }

        public String getFilePath() { return filePath; }
        public String getContent() { return content; }

        @Override
        public LocalDateTime getTimestamp() {
            return Event.super.getTimestamp();
        }

        @Override
        public String getSource() {
            return "";
        }
    }
}
