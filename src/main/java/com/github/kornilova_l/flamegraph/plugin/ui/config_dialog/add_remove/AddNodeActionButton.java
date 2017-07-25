package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.add_remove;

import com.github.kornilova_l.flamegraph.configuration.Configuration;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.ConfigCheckboxTree;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import org.jetbrains.annotations.NotNull;

public class AddNodeActionButton implements AnActionButtonRunnable {
    @NotNull
    private ConfigCheckboxTree tree;
    private Configuration tempConfiguration;

    public AddNodeActionButton(@NotNull ConfigCheckboxTree tree,
                               Configuration tempConfiguration) {
        this.tree = tree;
        this.tempConfiguration = tempConfiguration;
    }

    @Override
    public void run(AnActionButton anActionButton) {
        switch (tree.treeType) {
            case INCLUDING:
                final AddIncludingConfigDialog includingDialog = new AddIncludingConfigDialog(tree, tempConfiguration);
                includingDialog.pack();
                includingDialog.setVisible(true);
                break;
            case EXCLUDING:
                final AddExcludingConfigDialog excludingDialog = new AddExcludingConfigDialog(tree, tempConfiguration);
                excludingDialog.pack();
                excludingDialog.setVisible(true);
                break;
            default:
                throw new RuntimeException("Not known tree type");
        }
    }
}
