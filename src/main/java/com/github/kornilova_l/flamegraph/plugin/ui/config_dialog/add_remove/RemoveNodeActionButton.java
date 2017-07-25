package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.add_remove;

import com.github.kornilova_l.flamegraph.configuration.Configuration;
import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.ConfigCheckboxTree;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.ConfigCheckedTreeNode;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;

public class RemoveNodeActionButton implements AnActionButtonRunnable {
    private ConfigCheckboxTree tree;
    private Configuration tempConfiguration;

    public RemoveNodeActionButton(ConfigCheckboxTree tree, Configuration tempConfiguration) {
        this.tree = tree;
        this.tempConfiguration = tempConfiguration;
    }

    @Override
    public void run(AnActionButton anActionButton) {
        MethodConfig methodConfig = tree.getSelectedConfig();
        if (methodConfig == null) {
            return;
        }
        switch (tree.treeType) {
            case EXCLUDING:
                tempConfiguration.maybeRemoveExactExcludingConfig(methodConfig);
                break;
            case INCLUDING:
                tempConfiguration.maybeRemoveExactIncludingConfig(methodConfig);
                break;
            default:
                throw new RuntimeException("Not known tree type");
        }
        ConfigCheckedTreeNode node = tree.getSelectedNode();
        if (node != null) {
            tree.removeNode(node);
        }
    }
}
