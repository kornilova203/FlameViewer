package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.method_form;

import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.ConfigCheckboxTree;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

public class MyTableView<Item> extends TableView<Item> {
    private static final ColumnInfo<MethodConfig.Parameter, String> TYPE_COLUMN = new ColumnInfo<MethodConfig.Parameter, String>("Type") {
        @Override
        public String valueOf(MethodConfig.Parameter parameter) {
            return parameter.getType();
        }
    };
    private static final ColumnInfo<MethodConfig.Parameter, Boolean> SAVE_COLUMN = new ColumnInfo<MethodConfig.Parameter, Boolean>("Save") {
        @NotNull
        @Override
        public Boolean valueOf(MethodConfig.Parameter parameter) {
            return parameter.isEnabled();
        }
    };

    private final List<MethodConfig.Parameter> parameters;

    private MyTableView(ListTableModel<Item> listTableModel, List<MethodConfig.Parameter> parameters) {
        super(listTableModel);
        this.parameters = parameters;

        DefaultCellEditor doubleClick = new DefaultCellEditor(new JTextField());
        doubleClick.setClickCountToStart(2);
        setDefaultEditor(String.class, doubleClick);
    }

    @NotNull
    public static JPanel createTablePanel(List<MethodConfig.Parameter> parameters, ConfigCheckboxTree.TreeType treeType) {
        ColumnInfo[] columns;
        switch (treeType) {
            case EXCLUDING:
                columns = new ColumnInfo[]{TYPE_COLUMN};
                break;
            case INCLUDING:
                columns = new ColumnInfo[]{TYPE_COLUMN, SAVE_COLUMN};
                break;
            default:
                throw new RuntimeException("Not known tree type");
        }
        TableView<MethodConfig.Parameter> myTableView = new MyTableView<>(
                new ListTableModel<>(columns, parameters),
                parameters
        );
        if (treeType == ConfigCheckboxTree.TreeType.INCLUDING) {
            myTableView.getColumnModel().getColumn(1).setMaxWidth(50);
        }
        return ToolbarDecorator.createDecorator(myTableView, null)
                .setAddAction(anActionButton -> {
                    myTableView.stopEditing();
                    parameters.add(new MethodConfig.Parameter("", false));
                    myTableView.repaint();
                })
                .setRemoveAction(anActionButton -> {
                    myTableView.stopEditing();
                    MethodConfig.Parameter parameter = ((MethodConfig.Parameter) ((MyTableView) anActionButton.getContextComponent()).getSelectedObject());
                    parameters.remove(parameter);
                    myTableView.repaint();
                })
                .createPanel();
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
