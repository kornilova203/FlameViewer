package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.decorator_actions;

import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.ConfigCheckboxTree;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.tree_nodes.ConfigCheckedTreeNode;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import com.intellij.util.ui.tree.TreeUtil;

public class RemoveNodeActionButton implements AnActionButtonRunnable {
    private ConfigCheckboxTree tree;

    public RemoveNodeActionButton(ConfigCheckboxTree tree) {
        this.tree = tree;
    }

    @Override
    public void run(AnActionButton anActionButton) {
        ConfigCheckedTreeNode selectedNode = tree.getSelectedNode();
        if (selectedNode != null) {
            selectedNode.removeItselfFromConfigsSet();
            tree.removeNode(selectedNode);
            TreeUtil.expandAll(tree);
        }
    }
}
