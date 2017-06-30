package com.github.kornilova_l.plugin.config;

import com.intellij.ui.components.JBList;
import com.intellij.util.ui.EditableModel;

class ConfigList extends JBList implements EditableModel {

    @Override
    public void addRow() {

    }

    @Override
    public void exchangeRows(int oldIndex, int newIndex) {

    }

    @Override
    public boolean canExchangeRows(int oldIndex, int newIndex) {
        return false;
    }

    @Override
    public void removeRow(int idx) {

    }
}