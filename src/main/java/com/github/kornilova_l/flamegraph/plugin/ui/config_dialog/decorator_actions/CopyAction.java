package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.decorator_actions;

import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.ConfigCheckboxTree;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.tree_nodes.ConfigCheckedTreeNode;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.tree_nodes.MethodTreeNode;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.util.PlatformIcons;

public class CopyAction extends AnAction {
    private ConfigCheckboxTree tree;

    public CopyAction(ConfigCheckboxTree tree) {
        super("Copy pattern", "Copy pattern", PlatformIcons.COPY_ICON);
        this.tree = tree;
    }

    @Override
    public void actionPerformed(AnActionEvent anActionEvent) {
        ConfigCheckedTreeNode node = tree.getSelectedNode();
        if (node instanceof MethodTreeNode) {
            tree.duplicate(((MethodTreeNode) node));
        }
    }

    @Override
    public void update(AnActionEvent e) {
        ConfigCheckedTreeNode node = tree.getSelectedNode();
        e.getPresentation().setEnabled(node instanceof MethodTreeNode);
    }
}
