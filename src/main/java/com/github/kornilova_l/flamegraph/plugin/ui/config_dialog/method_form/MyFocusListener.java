package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.method_form;

import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.ConfigCheckboxTree;

import javax.swing.tree.TreePath;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

class MyFocusListener implements FocusListener {
    private ConfigCheckboxTree tree;

    MyFocusListener(ConfigCheckboxTree tree) {
        this.tree = tree;
    }

    @Override
    public void focusGained(FocusEvent e) {

    }

    @Override
    public void focusLost(FocusEvent e) {
        TreePath treePath = tree.getSelectionPath();
        if (treePath != null) {
            tree.updateTreeNodeNames(treePath.getPath());
        }
    }
}