package com.github.kornilova_l.plugin.gui;

import com.intellij.execution.configurations.RunConfigurationBase;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.util.Map;

public class ConfigChangeListener implements DocumentListener {

    private final Map<Integer, String> map;
    private final Integer key;

    ConfigChangeListener(RunConfigurationBase configuration, Map<Integer, String> map) {
        this.map = map;
        key = configuration.hashCode();
    }

    @Override
    public void insertUpdate(DocumentEvent documentEvent) {
        getTextAndUpdateList(documentEvent);
    }

    private void getTextAndUpdateList(DocumentEvent documentEvent) {
        Document document = documentEvent.getDocument();
        try {
            updateList(document.getText(0, document.getLength()));
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeUpdate(DocumentEvent documentEvent) {
        getTextAndUpdateList(documentEvent);
    }

    @Override
    public void changedUpdate(DocumentEvent documentEvent) {
        getTextAndUpdateList(documentEvent);
    }

    private synchronized void updateList(String text) {
        System.out.println(key);
        map.put(key, text);
    }
}
