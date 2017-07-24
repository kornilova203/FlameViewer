package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog;

import com.github.kornilova_l.flamegraph.configuration.Configuration;
import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import com.github.kornilova_l.flamegraph.plugin.configuration.PluginConfigManager;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.add_remove.AddNodeActionButton;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.add_remove.RemoveNodeActionButton;
import com.intellij.openapi.actionSystem.ActionToolbarPosition;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.uiDesigner.core.GridConstraints;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.text.BadLocationException;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.Collection;

public class ChangeConfigurationDialog extends DialogWrapper {
    @NotNull
    private final Project project;
    private final JPanel mainPanel = new JPanel(new GridLayout(1, 2, 20, 0));
    private final JPanel includedPanel = new JPanel(new GridLayout(2, 1));
    private final JPanel excludedPanel = new JPanel(new GridLayout(2, 1));
    private ConfigCheckboxTree includedTree;
    private ConfigCheckboxTree excludedTree;
    private Configuration trueConfiguration;
    private Configuration tempConfiguration;

    ChangeConfigurationDialog(@NotNull Project project) {
        super(project);
        this.project = project;

        setTitle("Profiler Configuration");
        setModal(false);
        init();
        setOKButtonText("Done");
    }

    private static JPanel getEmptyDetailView() {
        JPanel panel = new JPanel(new GridLayout(1, 1));
        panel.add(new Label("select configuration"));
        return panel;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        trueConfiguration = PluginConfigManager.getConfiguration(project);
        tempConfiguration = trueConfiguration.clone();

        ConfigurationForm configurationForm = new ConfigurationForm();
        includedTree = createTree(configurationForm.methodFormIncluded, tempConfiguration.getIncludingMethodConfigs());
        configurationForm.includingPanel.add(
                createCheckboxTreeView(includedTree, tempConfiguration.getIncludingMethodConfigs()),
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false)
        );

        excludedTree = createTree(configurationForm.methodFormExcluded, tempConfiguration.getExcludingMethodConfigs());
        configurationForm.excludingPanel.add(
                createCheckboxTreeView(excludedTree, tempConfiguration.getExcludingMethodConfigs()),
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false)
        );

        return configurationForm.$$$getRootComponent$$$();
    }

    @Override
    protected void doOKAction() {
        trueConfiguration = tempConfiguration.clone();
        super.doOKAction();
    }

    @NotNull
    private JComponent createCheckboxTreeView(ConfigCheckboxTree checkboxTree, Collection<MethodConfig> configs) {
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(checkboxTree);
        decorator.setToolbarPosition(ActionToolbarPosition.RIGHT);
        decorator.setAddAction(new AddNodeActionButton(project, this));
        decorator.setRemoveAction(new RemoveNodeActionButton(project, this));
        JPanel panel = decorator.createPanel();
        checkboxTree.initTree(configs);
        return panel;
    }

    public void addNodeToTree(MethodConfig methodConfig) {
        includedTree.addNode(methodConfig);
    }

    private ConfigCheckboxTree createTree(MethodForm methodForm, Collection<MethodConfig> methodConfigs) {
        MyDocumentListener methodDocumentListener = new MyDocumentListener(MyDocumentListener.FieldType.METHOD, methodConfigs);
        MyDocumentListener classDocumentListener = new MyDocumentListener(MyDocumentListener.FieldType.CLASS, methodConfigs);
        return new ConfigCheckboxTree() {
            @Override
            protected void selectionChanged(TreeSelectionEvent event) {
                TreePath treePath = event.getPath();
                if (treePath.getPathCount() < 4) {
                    methodForm.methodNamePatternTextField.setText("");
                    methodForm.methodNamePatternTextField.getDocument().removeDocumentListener(methodDocumentListener);
                    methodForm.classNamePatternTextField.getDocument().removeDocumentListener(classDocumentListener);
                } else {
                    String classNamePattern = getClassNamePattern(treePath);
                    String methodAndParametersPattern = treePath.getLastPathComponent().toString();
                    methodForm.methodNamePatternTextField.setText(methodAndParametersPattern.substring(0, methodAndParametersPattern.indexOf("(")));
                    methodForm.classNamePatternTextField.setText(classNamePattern);
                    MethodConfig currentMethodConfig = Configuration.getConfig(methodConfigs, classNamePattern, methodAndParametersPattern);
                    methodDocumentListener.setCurrentMethodConfig(currentMethodConfig);
                    classDocumentListener.setCurrentMethodConfig(currentMethodConfig);
                    methodForm.methodNamePatternTextField.getDocument().addDocumentListener(methodDocumentListener);
                    methodForm.classNamePatternTextField.getDocument().addDocumentListener(classDocumentListener);
                }
            }
        };
    }

    private String getClassNamePattern(TreePath path) {
        return path.getPathComponent(1) + "." + path.getPathComponent(2);
    }

    static class MyDocumentListener implements DocumentListener {
        private final FieldType fieldType;
        private Collection<MethodConfig> methodConfigs;
        @Nullable
        private MethodConfig currentMethodConfig = null;

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
            if (currentMethodConfig != null) {
                try {
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

        enum FieldType {
            METHOD,
            CLASS
        }
    }
}
