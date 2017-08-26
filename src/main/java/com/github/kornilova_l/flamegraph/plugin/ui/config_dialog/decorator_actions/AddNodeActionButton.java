package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.decorator_actions;

import com.github.kornilova_l.flamegraph.configuration.Configuration;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.ConfigCheckboxTree;
import com.intellij.openapi.project.Project;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import org.jetbrains.annotations.NotNull;

public class AddNodeActionButton implements AnActionButtonRunnable {
    @NotNull
    private ConfigCheckboxTree tree;
    private Configuration tempConfiguration;
    private Project project;

    public AddNodeActionButton(@NotNull ConfigCheckboxTree tree,
                               Configuration tempConfiguration,
                               Project project) {
        this.tree = tree;
        this.tempConfiguration = tempConfiguration;
        this.project = project;
    }

    @Override
    public void run(AnActionButton anActionButton) {
        switch (tree.treeType) {
            case INCLUDING:
                new AddIncludingDialogWrapper(project, tree, tempConfiguration).show();
                break;
            case EXCLUDING:
                new AddExcludingDialogWrapper(project, tree, tempConfiguration).show();
                break;
            default:
                throw new RuntimeException("Not known tree type");
        }
    }
}
