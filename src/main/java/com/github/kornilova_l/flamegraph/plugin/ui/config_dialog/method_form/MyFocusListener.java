package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.method_form;

import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.ConfigCheckboxTree;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.ConfigCheckedTreeNode;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreePath;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

class MyFocusListener implements FocusListener {

    private final MethodConfig methodConfig;
    @NotNull
    private ConfigCheckedTreeNode checkedTreeNode;
    private ConfigCheckboxTree tree;

    MyFocusListener(@NotNull ConfigCheckedTreeNode checkedTreeNode, MethodConfig methodConfig, ConfigCheckboxTree tree) {
        this.checkedTreeNode = checkedTreeNode;
        this.methodConfig = methodConfig;
        this.tree = tree;
    }

    @Override
    public void focusGained(FocusEvent e) {

    }

    @Override
    public void focusLost(FocusEvent e) {
        tree.clearSelection();
        tree.removeNode(checkedTreeNode);
        checkedTreeNode = tree.addNode(methodConfig);
        tree.getSelectionModel().setSelectionPath(new TreePath(checkedTreeNode.getPath()));
    }
}