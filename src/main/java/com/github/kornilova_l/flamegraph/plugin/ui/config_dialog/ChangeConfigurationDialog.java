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
import javax.swing.event.TreeSelectionEvent;
import java.awt.*;
import java.util.Collection;

public class ChangeConfigurationDialog extends DialogWrapper {
    @NotNull
    private final Project project;

    private ConfigCheckboxTree includedTree;
    private ConfigCheckboxTree excludedTree;
    private final JPanel mainPanel = new JPanel(new GridLayout(1, 2, 20, 0));
    private final JPanel includedPanel = new JPanel(new GridLayout(2, 1));
    private final JPanel excludedPanel = new JPanel(new GridLayout(2, 1));

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
        Configuration configuration = PluginConfigManager.getConfiguration(project);

        ConfigurationForm configurationForm = new ConfigurationForm();
        includedTree = createTree();
//        configurationForm.includingPanel.add(createCheckboxTreeView(includedTree, configuration.getIncludingMethodConfigs()));
        configurationForm.includingPanel.add(
                createCheckboxTreeView(includedTree, configuration.getIncludingMethodConfigs()),
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false)
        );

        excludedTree = createTree();
        configurationForm.excludingPanel.add(
                createCheckboxTreeView(excludedTree, configuration.getExcludingMethodConfigs()),
                new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false)
        );

        return configurationForm.$$$getRootComponent$$$();

//        mainPanel.add(includedPanel);
//        mainPanel.add(excludedPanel);
//
//        JPanel detailCards = new JPanel(new CardLayout());
//        detailCards.add(getEmptyDetailView(), "empty");
//        detailCards.add(new JPanel(), "detail");
//        includedPanel.add(createCheckboxTreeView(includedTree, configuration.getIncludingMethodConfigs()));
//        includedPanel.add(detailCards);
//
//        detailCards = new JPanel(new CardLayout());
//        detailCards.add(getEmptyDetailView(), "empty");
//        detailCards.add(new JPanel(), "detail");
//        excludedTree = createTree(detailCards);
//        excludedPanel.add(createCheckboxTreeView(excludedTree, configuration.getExcludingMethodConfigs()));
//        excludedPanel.add(detailCards);
//
//        return mainPanel;
    }

    private static JPanel getEmptyDetailView() {
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
            protected void selectionChanged(TreeSelectionEvent event) {
//                System.out.println(event.getSource());
//                CardLayout cl = (CardLayout) (cards.getLayout());
//                cl.show(cards, "empty");
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
