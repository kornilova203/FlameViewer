package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog;

import javax.swing.table.DefaultTableModel;

public class MyTableModel extends DefaultTableModel {
    MyTableModel(Object[][] data, Object[] columnNames) {
        super(data, columnNames);
    }
}
