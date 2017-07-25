package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog;

import com.github.kornilova_l.flamegraph.configuration.Configuration;
import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import com.github.kornilova_l.flamegraph.plugin.configuration.PluginConfigManager;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.add_remove.AddNodeActionButton;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.add_remove.RemoveNodeActionButton;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.method_form.MethodFormManager;
import com.github.kornilova_l.flamegraph.plugin.ui.line_markers.LineMarkersHolder;
import com.intellij.openapi.actionSystem.ActionToolbarPosition;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.uiDesigner.core.GridConstraints;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Collection;

public class ChangeConfigurationDialog extends DialogWrapper {
    @NotNull
    private final Project project;
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

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        trueConfiguration = PluginConfigManager.getConfiguration(project);
        tempConfiguration = new Configuration(trueConfiguration);

        ConfigurationForm configurationForm = new ConfigurationForm();
        ConfigCheckboxTree includedTree = new ConfigCheckboxTree(
                configurationForm.cardPanelIncluded,
                configurationForm.methodFormIncluded,
                tempConfiguration.getIncludingMethodConfigs(),
                MethodFormManager.TreeType.INCLUDING
        );
        configurationForm.includingPanel.add(
                createCheckboxTreeView(includedTree,
                        tempConfiguration.getIncludingMethodConfigs(),
                        MethodFormManager.TreeType.INCLUDING),
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false)
        );

        ConfigCheckboxTree excludedTree = new ConfigCheckboxTree(
                configurationForm.cardPanelExcluded,
                configurationForm.methodFormExcluded,
                tempConfiguration.getExcludingMethodConfigs(),
                MethodFormManager.TreeType.EXCLUDING
        );
        configurationForm.excludingPanel.add(
                createCheckboxTreeView(excludedTree,
                        tempConfiguration.getExcludingMethodConfigs(),
                        MethodFormManager.TreeType.EXCLUDING),
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false)
        );

        return configurationForm.$$$getRootComponent$$$();
    }

    @Override
    protected void doOKAction() {
        trueConfiguration.assign(tempConfiguration);
        project.getComponent(LineMarkersHolder.class).updateOpenedDocuments();
        super.doOKAction();
    }

    private JComponent createCheckboxTreeView(ConfigCheckboxTree tree,
                                              Collection<MethodConfig> configs,
                                              MethodFormManager.TreeType treeType) {
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(tree);
        decorator.setToolbarPosition(ActionToolbarPosition.RIGHT);
        decorator.setAddAction(new AddNodeActionButton(treeType, tree, tempConfiguration));
        decorator.setRemoveAction(new RemoveNodeActionButton(project, this));
        JPanel panel = decorator.createPanel();
        tree.initTree(configs);
        return panel;
    }
}
