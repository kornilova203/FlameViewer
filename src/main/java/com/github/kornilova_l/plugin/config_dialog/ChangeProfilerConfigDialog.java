package com.github.kornilova_l.plugin.config_dialog;

import com.github.kornilova_l.plugin.config.ConfigNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class ChangeProfilerConfigDialog extends DialogWrapper {
    @NotNull
    private final Project project;

    private ConfigCheckboxTree checkboxTree;
    private JBSplitter splitPane;

    protected ChangeProfilerConfigDialog(@NotNull Project project) {
        super(project);
        this.project = project;

        setTitle("Profiler Configuration");
        setModal(false);
        init();
        setOKButtonText("Done");
    }

    protected String getDimensionServiceKey() {
        return getClass().getName();
    }

    private String getSplitterProportionKey() {
        return getDimensionServiceKey() + ".splitter";
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        splitPane = new JBSplitter(0.3f);
        splitPane.setSplitterProportionKey(getSplitterProportionKey());

        splitPane.setFirstComponent(createMasterView());
        splitPane.setSecondComponent(getEmptyDetailView());

        mainPanel.add(splitPane, BorderLayout.CENTER);

        return mainPanel;
    }

    private static JComponent getEmptyDetailView() {
        JPanel panel = new JPanel(new GridLayout(1, 1));
        panel.add(new Label("select config"));
        return panel;
    }

    @NotNull
    private JComponent createMasterView() {
        System.out.println("create master view");
        createTree();
        JPanel panel = createPanelWithTree(checkboxTree);
        Set<ConfigNode> configs = getProjectConfigs();
        checkboxTree.initTree(configs);
        return panel;
    }

    @NotNull
    private Set<ConfigNode> getProjectConfigs() {
        // TODO: get configs from project
        Set<ConfigNode> configs = new HashSet<>();
        configs.add(new ConfigNode("method1", ConfigNode.Type.METHOD));
        configs.add(new ConfigNode("method2", ConfigNode.Type.METHOD));
        return configs;
    }

    private static JPanel createPanelWithTree(ConfigCheckboxTree checkboxTree) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(ScrollPaneFactory.createScrollPane(checkboxTree));

        return panel;
    }

    private void createTree() {
        checkboxTree = new ConfigCheckboxTree() {
            @Override
            protected void selectionChanged() {
                resetDetailView();
            }
        };
    }

    private void resetDetailView() {
        ConfigNode config = checkboxTree.getSelectedConfig();
        if (config == null) {
            splitPane.setSecondComponent(getEmptyDetailView());
        } else {
            splitPane.setSecondComponent(DetailViewManager.getDetailView(config));
        }
    }
}
