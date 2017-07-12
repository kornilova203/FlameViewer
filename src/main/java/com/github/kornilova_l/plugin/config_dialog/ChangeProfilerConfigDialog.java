package com.github.kornilova_l.plugin.config_dialog;

import com.github.kornilova_l.plugin.ProjectConfigManager;
import com.github.kornilova_l.config.ConfigStorage;
import com.github.kornilova_l.config.MethodConfig;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.ui.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;

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
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(checkboxTree);
        decorator.setAddAction(new AddNodeActionButton(project, this));
        JPanel panel = decorator.createPanel();
        Collection<MethodConfig> configs = getProjectConfigs();
        if (configs == null) {
            // TODO: show that config is empty
            return panel;
        }
        checkboxTree.initTree(configs);
        return panel;
    }

    public void addNodeToTree(MethodConfig methodConfig) {
        checkboxTree.addNode(methodConfig);
    }

    @Nullable
    private Collection<MethodConfig> getProjectConfigs() {
        ConfigStorage.Config config = ProjectConfigManager.getConfig(project);
        if (config.methodConfigs.size() == 0) {
            return null;
        }
        return config.methodConfigs;
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
        MethodConfig config = checkboxTree.getSelectedConfig();
        if (config == null) {
            splitPane.setSecondComponent(getEmptyDetailView());
        } else {
            splitPane.setSecondComponent(DetailViewManager.getDetailView(config));
        }
    }
}
