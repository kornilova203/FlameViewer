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
        tempConfiguration = new Configuration(trueConfiguration);

        ConfigurationForm configurationForm = new ConfigurationForm();
        includedTree = new ConfigCheckboxTree(
                configurationForm.cardPanelIncluded,
                configurationForm.methodFormIncluded,
                tempConfiguration.getIncludingMethodConfigs(),
                ConfigCheckboxTree.TreeType.INCLUDING
        );
        configurationForm.includingPanel.add(
                createCheckboxTreeView(includedTree, tempConfiguration.getIncludingMethodConfigs()),
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false)
        );

        excludedTree = new ConfigCheckboxTree(
                configurationForm.cardPanelExcluded,
                configurationForm.methodFormExcluded,
                tempConfiguration.getExcludingMethodConfigs(),
                ConfigCheckboxTree.TreeType.EXCLUDING
        );
        configurationForm.excludingPanel.add(
                createCheckboxTreeView(excludedTree, tempConfiguration.getExcludingMethodConfigs()),
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false)
        );

        return configurationForm.$$$getRootComponent$$$();
    }

    @Override
    protected void doOKAction() {
        System.out.println("OK");
        trueConfiguration.assign(tempConfiguration);
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
}
