package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.method_form;

import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.ConfigCheckboxTree;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.ConfigCheckedTreeNode;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.ConfigurationForm;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.MethodForm;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.table.TableView;
import com.intellij.util.ui.ColumnInfo;
import com.intellij.util.ui.ListTableModel;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.Set;

public class MethodFormManager {
    private final TreeType treeType;
    private final JPanel cardPanel;
    private final MethodForm methodForm;
    private final Set<MethodConfig> methodConfigs;
    private ConfigCheckboxTree tree;
    private MyDocumentListener methodDocumentListener;
    private MyDocumentListener classDocumentListener;
    private MyFocusListener myFocusListener;
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

    public MethodFormManager(TreeType treeType,
                             JPanel cardPanel,
                             MethodForm methodForm,
                             Set<MethodConfig> methodConfigs,
                             ConfigCheckboxTree tree) {
        this.treeType = treeType;
        this.cardPanel = cardPanel;
        this.methodForm = methodForm;
        this.methodConfigs = methodConfigs;
        this.tree = tree;
        setDocumentsListeners();
    }


    private void setDocumentsListeners() {
        methodDocumentListener = new MyDocumentListener(
                MyDocumentListener.FieldType.METHOD,
                methodConfigs
        );
        classDocumentListener = new MyDocumentListener(
                MyDocumentListener.FieldType.CLASS,
                methodConfigs
        );
    }

    public void selectionChanged(TreePath path) {
        methodForm.methodNamePatternTextField.getDocument().removeDocumentListener(methodDocumentListener);
        methodForm.classNamePatternTextField.getDocument().removeDocumentListener(classDocumentListener);
        if (myFocusListener != null) {
            methodForm.methodNamePatternTextField.removeFocusListener(myFocusListener);
            methodForm.classNamePatternTextField.removeFocusListener(myFocusListener);
        }
        if (path.getPathCount() < 4) {
            ((CardLayout) cardPanel.getLayout()).show(cardPanel, ConfigurationForm.EMPTY_CARD_KEY);
        } else {
            showMethodForm(path);
        }
    }

    private void showMethodForm(TreePath treePath) {
        ((CardLayout) cardPanel.getLayout()).show(cardPanel, ConfigurationForm.FORM_CARD_KEY);
        MethodConfig methodConfig = tree.getSelectedConfig();
        if (methodConfig == null) {
            return;
        }
        String key = methodConfig.toString();
        methodForm.paramTableCards.add(
                createTablePanel(methodConfig),
                key
        );
        ((CardLayout) methodForm.paramTableCards.getLayout()).show(methodForm.paramTableCards, key);
        myFocusListener = new MyFocusListener(
                (ConfigCheckedTreeNode) treePath.getLastPathComponent(),
                methodConfig,
                tree
        );
        methodForm.methodNamePatternTextField.setText(methodConfig.getMethodPatternString());
        methodForm.classNamePatternTextField.setText(methodConfig.getClassPatternString());
        methodDocumentListener.setCurrentMethodConfig(methodConfig);
        classDocumentListener.setCurrentMethodConfig(methodConfig);
        methodDocumentListener.setTreeNode(((ConfigCheckedTreeNode) treePath.getLastPathComponent()));
        classDocumentListener.setTreeNode(((ConfigCheckedTreeNode) treePath.getLastPathComponent()));
        methodForm.classNamePatternTextField.addFocusListener(myFocusListener);
        methodForm.methodNamePatternTextField.addFocusListener(myFocusListener);
        methodForm.methodNamePatternTextField.getDocument().addDocumentListener(methodDocumentListener);
        methodForm.classNamePatternTextField.getDocument().addDocumentListener(classDocumentListener);
    }

    @NotNull
    private JPanel createTablePanel(MethodConfig methodConfig) {
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
                new ListTableModel<>(columns, methodConfig.getParameters()),
                methodConfig.getParameters(),
                treeType
        );
        return ToolbarDecorator.createDecorator(myTableView, null)
                .setAddAction(anActionButton -> {

                })
                .createPanel();
    }

    public enum TreeType {
        INCLUDING,
        EXCLUDING
    }
}
