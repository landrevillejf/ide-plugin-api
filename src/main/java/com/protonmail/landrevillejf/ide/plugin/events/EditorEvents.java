package com.protonmail.landrevillejf.ide.plugin.events;

import java.time.LocalDateTime;

/**
 * Container class for editor-related events.
 * <p>
 * This class contains inner event classes for various editor operations
 * such as active editor changes, document changes, file saves, and file opens.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
public class EditorEvents {

    /**
     * Event fired when the active editor changes.
     */
    // Event déclenché quand l'éditeur actif change
    public static class ActiveEditorChangedEvent implements Event {
        private final Object editorComponent; // Votre type CodeEditor
        private final String filePath;

        /**
         * Creates a new active editor changed event.
         *
         * @param editorComponent the editor component
         * @param filePath the file path in the editor
         */
        public ActiveEditorChangedEvent(Object editorComponent, String filePath) {
            this.editorComponent = editorComponent;
            this.filePath = filePath;
        }

        /**
         * Returns the editor component.
         *
         * @return the editor component
         */
        public Object getEditorComponent() { return editorComponent; }

        /**
         * Returns the file path in the editor.
         *
         * @return the file path
         */
        public String getFilePath() { return filePath; }

        /**
         * {@inheritDoc}
         *
         * @return {@inheritDoc}
         */
        @Override
        public LocalDateTime getTimestamp() {
            return Event.super.getTimestamp();
        }

        /**
         * {@inheritDoc}
         *
         * @return an empty string (editor component is the implicit source)
         */
        @Override
        public String getSource() {
            return "";
        }
    }

    /**
     * Event fired when the editor document content changes.
     */
    // Event déclenché quand le contenu de l'éditeur change
    public static class DocumentChangedEvent implements Event {
        private final String content;
        private final String filePath;
        private final int caretPosition;

        /**
         * Creates a new document changed event.
         *
         * @param content the document content
         * @param filePath the file path
         * @param caretPosition the caret position
         */
        public DocumentChangedEvent(String content, String filePath, int caretPosition) {
            this.content = content;
            this.filePath = filePath;
            this.caretPosition = caretPosition;
        }

        /**
         * Returns the document content.
         *
         * @return the content
         */
        public String getContent() { return content; }

        /**
         * Returns the file path.
         *
         * @return the file path
         */
        public String getFilePath() { return filePath; }

        /**
         * Returns the caret position.
         *
         * @return the caret position
         */
        public int getCaretPosition() { return caretPosition; }

        /**
         * {@inheritDoc}
         *
         * @return {@inheritDoc}
         */
        @Override
        public LocalDateTime getTimestamp() {
            return Event.super.getTimestamp();
        }

        /**
         * {@inheritDoc}
         *
         * @return an empty string (editor component is the implicit source)
         */
        @Override
        public String getSource() {
            return "";
        }
    }

    /**
     * Event fired before a file is saved.
     */
    // Event déclenché avant la sauvegarde d'un fichier
    public static class BeforeFileSaveEvent implements Event {
        private final String content;
        private final String filePath;

        /**
         * Creates a new before file save event.
         *
         * @param content the file content before saving
         * @param filePath the file path
         */
        public BeforeFileSaveEvent(String content, String filePath) {
            this.content = content;
            this.filePath = filePath;
        }

        /**
         * Returns the file content before saving.
         *
         * @return the content
         */
        public String getContent() { return content; }

        /**
         * Returns the file path.
         *
         * @return the file path
         */
        public String getFilePath() { return filePath; }

        /**
         * {@inheritDoc}
         *
         * @return {@inheritDoc}
         */
        @Override
        public LocalDateTime getTimestamp() {
            return Event.super.getTimestamp();
        }

        /**
         * {@inheritDoc}
         *
         * @return an empty string (editor component is the implicit source)
         */
        @Override
        public String getSource() {
            return "";
        }
    }

    /**
     * Event fired after a file is opened.
     */
    // Event déclenché après l'ouverture d'un fichier
    public static class FileOpenedEvent implements Event {
        private final String filePath;
        private final String content;

        /**
         * Creates a new file opened event.
         *
         * @param filePath the file path
         * @param content the file content
         */
        public FileOpenedEvent(String filePath, String content) {
            this.filePath = filePath;
            this.content = content;
        }

        /**
         * Returns the file path.
         *
         * @return the file path
         */
        public String getFilePath() { return filePath; }

        /**
         * Returns the file content.
         *
         * @return the content
         */
        public String getContent() { return content; }

        /**
         * {@inheritDoc}
         *
         * @return {@inheritDoc}
         */
        @Override
        public LocalDateTime getTimestamp() {
            return Event.super.getTimestamp();
        }

        /**
         * {@inheritDoc}
         *
         * @return an empty string (editor component is the implicit source)
         */
        @Override
        public String getSource() {
            return "";
        }
    }
}
