package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog;

import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ListTableModel;
import com.intellij.util.ui.table.JBListTable;
import com.intellij.util.ui.table.JBTableRow;
import com.intellij.util.ui.table.JBTableRowEditor;
import com.intellij.util.ui.table.JBTableRowRenderer;

import javax.swing.*;

public class ParametersJBTable extends JBListTable {
    ParametersJBTable(ListTableModel<MyItem> listTableModel) {
        super(new TableView<>(listTableModel), () -> {

        });
    }

    @Override
    protected JBTableRowRenderer getRowRenderer(int row) {
        return (table, row1, selected, focused) -> new JLabel("hello, " + row1);
    }

    @Override
    protected JBTableRowEditor getRowEditor(int row) {
        return new JBTableRowEditor() {
            @Override
            public void prepareEditor(JTable table, int row) {

            }

            @Override
            public JBTableRow getValue() {
                return column -> "hello value";
            }

            @Override
            public JComponent getPreferredFocusedComponent() {
                return null;
            }

            @Override
            public JComponent[] getFocusableComponents() {
                return new JComponent[0];
            }
        };
    }
}

class MyItem {
    @Override
    public String toString() {
        return "MyItem";
    }
}
