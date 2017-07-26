package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.method_form;

import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.ConfigCheckboxTree;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

class MyFocusListener implements FocusListener {
    private ConfigCheckboxTree tree;
    private Object[] path;

    MyFocusListener(Object[] path,
                    ConfigCheckboxTree tree) {
        this.path = path;
        this.tree = tree;
    }

    @Override
    public void focusGained(FocusEvent e) {

    }

    @Override
    public void focusLost(FocusEvent e) {
        tree.updateTreeNodeNames(path);
    }
}