package com.github.kornilova_l.plugin.config;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.*;
import com.intellij.ui.popup.util.DetailViewImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;

public class ChangeProfilerConfigDialog extends DialogWrapper {
    @NotNull
    private final Project project;

    private ConfigCheckboxTree checkboxTree;

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
        DetailViewImpl detailView = new DetailViewImpl(project);
        detailView.setEmptyLabel("Select configuration");
        return detailView;
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

    private JComponent createMasterView() {
        System.out.println("create master view");
        JPanel panel = createTree();
        HashSet<Config> configs = new HashSet<>();
        configs.add(new Config("config1"));
        configs.add(new Config("config2"));
        checkboxTree.initTree(configs);

        return panel;
    }

    private JPanel createTree() {
        checkboxTree = new ConfigCheckboxTree() {
            @Override
            protected void selectionChanged() {
                resetDescriptionPanel();
            }
        };
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(ScrollPaneFactory.createScrollPane(checkboxTree));

        return panel;
    }

    private void resetDescriptionPanel() {

    }
}
