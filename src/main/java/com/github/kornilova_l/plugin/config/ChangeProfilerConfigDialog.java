package com.github.kornilova_l.plugin.config;

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
    private DetailViewPanel myInnerPostfixDescriptionPanel;

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

    private JComponent createDetailView() {
        System.out.println("Create detail view");
        myInnerPostfixDescriptionPanel = new DetailViewPanel();
        JPanel panel = new JPanel(new GridLayout(1, 2));
        panel.add(myInnerPostfixDescriptionPanel.getComponent());
        panel.add(new Label("initial detail component"));
        return panel;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        JBSplitter splitPane = new JBSplitter(0.3f);
        splitPane.setSplitterProportionKey(getSplitterProportionKey());

        splitPane.setFirstComponent(createMasterView());
        splitPane.setSecondComponent(createDetailView());

        mainPanel.add(splitPane, BorderLayout.CENTER);

        return mainPanel;
    }

    @NotNull
    private JComponent createMasterView() {
        System.out.println("create master view");
        createTree();
        JPanel panel = createPanelWithTree(checkboxTree);
        Set<Config> configs = getProjectConfigs();
        checkboxTree.initTree(configs);
        return panel;
    }

    @NotNull
    private Set<Config> getProjectConfigs() {
        // TODO: get configs from project
        Set<Config> configs = new HashSet<>();
        configs.add(new Config("config1"));
        configs.add(new Config("config2"));
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
        if (null != checkboxTree && null != myInnerPostfixDescriptionPanel) {
            Config config = checkboxTree.getSelectedConfig();
            if (config != null) {
                myInnerPostfixDescriptionPanel.reset(config);
            }
        }
    }
}
