package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.method_form;

import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ListTableModel;

import java.util.List;

public class MyTableView<Item> extends TableView<Item> {
    private final List<MethodConfig.Parameter> parameters;
    private MethodFormManager.TreeType treeType;

    MyTableView(ListTableModel<Item> listTableModel, List<MethodConfig.Parameter> parameters, MethodFormManager.TreeType treeType) {
        super(listTableModel);
        this.parameters = parameters;
        this.treeType = treeType;
    }

    @Override
    public Class<?> getColumnClass(int column) {
        switch (column) {
            case 0:
                return String.class;
            case 1:
                return Boolean.class;
            default:
                throw new RuntimeException("Too many columns");
        }
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return true;
    }

    @Override
    public void setValueAt(Object newValue, int row, int column) {
        MethodConfig.Parameter parameter = parameters.get(row);
        switch (column) {
            case 0:
                assert newValue instanceof String;
                parameter.setType(((String) newValue));
                return;
            case 1:
                assert newValue instanceof Boolean;
                parameter.setEnabled(((Boolean) newValue));
                return;
            default:
                throw new RuntimeException("Number of column is bigger than 1");
        }
    }
}
