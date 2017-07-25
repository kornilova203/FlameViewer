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
        final AddMethodDialog dialog = new AddMethodDialog(tree, tempConfiguration);
        dialog.pack();
        dialog.setVisible(true);
    }
}
