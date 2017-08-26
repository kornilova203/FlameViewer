package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.method_form;

import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.tree_nodes.ConfigCheckedTreeNode;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.util.Collection;

class MyDocumentListener implements DocumentListener {
    private final FieldType fieldType;
    private Collection<MethodConfig> methodConfigs;
    @Nullable
    private MethodConfig currentMethodConfig = null;
    @Nullable
    private ConfigCheckedTreeNode treeNode = null;

    MyDocumentListener(FieldType fieldType, Collection<MethodConfig> methodConfigs) {
        this.fieldType = fieldType;
        this.methodConfigs = methodConfigs;
    }

    void setCurrentMethodConfig(@Nullable MethodConfig currentMethodConfig) {
        this.currentMethodConfig = currentMethodConfig;
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        updateConfig(e);
    }

    private void updateConfig(DocumentEvent e) {
        if (currentMethodConfig != null && treeNode != null) {
            try {
                if (!MethodFormManager.isValidField(e.getDocument().getText(0, e.getDocument().getLength()))) {
                    return;
                }
                methodConfigs.remove(currentMethodConfig);

                switch (fieldType) {
                    case CLASS:
                        currentMethodConfig.setClassPatternString(e.getDocument().getText(0, e.getDocument().getLength()));
                        break;
                    case METHOD:
                        currentMethodConfig.setMethodPatternString(e.getDocument().getText(0, e.getDocument().getLength()));
                }
                methodConfigs.add(currentMethodConfig);
            } catch (BadLocationException e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        updateConfig(e);
    }

    @Override
    public void changedUpdate(DocumentEvent e) {

    }

    void setTreeNode(@Nullable ConfigCheckedTreeNode treeNode) {
        this.treeNode = treeNode;
    }

    enum FieldType {
        METHOD,
        CLASS
    }
}
