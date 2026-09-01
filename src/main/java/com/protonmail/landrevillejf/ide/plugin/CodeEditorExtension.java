package com.protonmail.landrevillejf.ide.plugin;

import javax.swing.*;

/**
 * Extension interface for plugins that want to interact with the code editor.
 * <p>
 * Plugins implementing this interface can receive callbacks when editors
 * are created or when text content changes.
 * </p>
 *
 * @author landrevillejf
 * @version 1.0.0
 * @since 1.0.0
 */
public interface CodeEditorExtension {
    /**
     * Called when a new editor is created.
     *
     * @param editor the newly created text editor
     */
    void onEditorCreated(JTextPane editor);

    /**
     * Called when the text content of an editor changes.
     *
     * @param text the new text content
     */
    void onTextChanged(String text);
}