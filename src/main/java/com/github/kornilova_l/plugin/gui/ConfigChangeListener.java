package com.github.kornilova_l.plugin.gui;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class ConfigChangeListener implements DocumentListener {
    @Override
    public void insertUpdate(DocumentEvent e) {
        System.out.println("insert");
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        System.out.println("remove");
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        System.out.println("change");
    }
}
