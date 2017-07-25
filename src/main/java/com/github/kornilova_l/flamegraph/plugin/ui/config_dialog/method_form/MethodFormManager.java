package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.method_form;

import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.ConfigCheckboxTree;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.ConfigCheckedTreeNode;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.ConfigurationForm;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.MethodForm;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.Set;

import static com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.method_form.MyTableView.createTablePanel;

public class MethodFormManager {
    private final TreeType treeType;
    private final JPanel cardPanel;
    private final MethodForm methodForm;
    private final Set<MethodConfig> methodConfigs;
    private ConfigCheckboxTree tree;
    private MyDocumentListener methodDocumentListener;
    private MyDocumentListener classDocumentListener;
    private MyFocusListener myFocusListener;

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
                createTablePanel(methodConfig.getParameters(), treeType),
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

    public enum TreeType {
        INCLUDING,
        EXCLUDING
    }
}
