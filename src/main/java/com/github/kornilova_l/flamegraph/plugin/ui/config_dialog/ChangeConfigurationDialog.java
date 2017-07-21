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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

public class ChangeConfigurationDialog extends DialogWrapper {
    @NotNull
    private final Project project;

    private ConfigCheckboxTree includedTree;
    private ConfigCheckboxTree excludedTree;

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
        JPanel mainPanel = new JPanel(new GridLayout(2, 2, 10, 20));

        Configuration configuration = PluginConfigManager.getConfiguration(project);

        includedTree = createTree();
        mainPanel.add(createCheckboxTreeView(includedTree, configuration.getIncludingMethodConfigs()));
        excludedTree = createTree();
        mainPanel.add(createCheckboxTreeView(excludedTree, configuration.getExcludingMethodConfigs()));
        mainPanel.add(getEmptyDetailView());

        return mainPanel;
    }

    private static JComponent getEmptyDetailView() {
        JPanel panel = new JPanel(new GridLayout(1, 1));
        panel.add(new Label("select configuration"));
        return panel;
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

    @NotNull
    private ConfigCheckboxTree createTree() {
        return new ConfigCheckboxTree() {
            @Override
            protected void selectionChanged() {
                resetDetailView();
            }
        };
    }

    private void resetDetailView() {
//        MethodConfig config = includedTree.getSelectedConfig();
//        if (config == null) {
//            splitPane.setSecondComponent(getEmptyDetailView());
//        } else {
//            splitPane.setSecondComponent(DetailViewManager.getDetailView(config));
//        }
    }
}
