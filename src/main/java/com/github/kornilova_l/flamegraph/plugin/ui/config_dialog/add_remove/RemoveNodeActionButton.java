package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.add_remove;

import com.github.kornilova_l.flamegraph.configuration.Configuration;
import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.ConfigCheckboxTree;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;

public class RemoveNodeActionButton implements AnActionButtonRunnable {
    private ConfigCheckboxTree tree;

    public RemoveNodeActionButton(ConfigCheckboxTree tree, Configuration tempConfiguration) {

        this.tree = tree;
    }

    @Override
    public void run(AnActionButton anActionButton) {
        MethodConfig methodConfig = tree.getSelectedConfig();

    }
}
