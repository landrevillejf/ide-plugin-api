package com.protonmail.landrevillejf.ide.plugin;

import javax.swing.*;

public interface CodeEditorExtension {
    void onEditorCreated(JTextPane editor);
    void onTextChanged(String text);
}