package com.github.kornilova_l.plugin.config;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

public class ConfigChangeListener implements DocumentListener {

    private final ConfigStorage.State.Config config;
    private final boolean isIncluded;

    public ConfigChangeListener(ConfigStorage.State.Config config, boolean b) {
        this.config = config;
        this.isIncluded = b;
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
        if (isIncluded) {
            config.included = text;
        } else {
            config.excluded = text;
        }
    }
}
