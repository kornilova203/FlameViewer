package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog;

import com.intellij.ui.table.JBTable;

import javax.swing.table.TableModel;

public class ParametersTable extends JBTable {

    ParametersTable(TableModel tableModel) {
        super(tableModel);
    }

    @Override
    public Class<?> getColumnClass(int column) {
        switch (column) {
            case 0:
                return String.class;
            case 1:
                return Boolean.class;
            default:
                return String.class;
        }
    }
}
