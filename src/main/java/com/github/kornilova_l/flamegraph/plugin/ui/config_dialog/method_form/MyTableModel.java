package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.method_form;

import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import org.jetbrains.annotations.NotNull;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class MyTableModel extends AbstractTableModel {
    private final MethodFormManager.TreeType treeType;
    private final String[] includingColumnNames = {"Type", "Save"};
    private List<MethodConfig.Parameter> parameters;

    MyTableModel(@NotNull List<MethodConfig.Parameter> parameters, MethodFormManager.TreeType treeType) {
        this.parameters = parameters;
        this.treeType = treeType;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return String.class;
            case 1:
                return Boolean.class;
            default:
                return String.class;
        }
    }

    @Override
    public String getColumnName(int column) {
        return includingColumnNames[column];
    }

    @Override
    public int getRowCount() {
        return parameters.size();
    }

    @Override
    public int getColumnCount() {
        switch (treeType) {
            case INCLUDING:
                return 2;
            case EXCLUDING:
                return 1;
            default:
                throw new RuntimeException("Not known tree type");
        }
    }

    @Override
    public boolean isCellEditable(int row, int column) {
        return true;
    }

    @Override
    public void setValueAt(Object aValue, int row, int column) {
//        super.setValueAt(aValue, row, column);
    }

    @Override
    public Object getValueAt(int row, int column) {
        MethodConfig.Parameter parameter = parameters.get(row);
        switch (column) {
            case 0:
                return parameter.getType();
            case 1:
                return parameter.isEnabled();
            default:
                throw new RuntimeException("Number of column is bigger than 1");
        }
    }
}
