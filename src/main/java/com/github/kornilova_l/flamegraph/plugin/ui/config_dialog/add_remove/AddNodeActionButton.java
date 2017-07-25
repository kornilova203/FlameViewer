package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.add_remove;

import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.method_form.MethodFormManager;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.method_form.MyTableView;
import com.intellij.openapi.project.Project;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;

public class AddNodeActionButton implements AnActionButtonRunnable {
    @NotNull
    private final Project project;
    @NotNull
    private MethodFormManager.TreeType treeType;

    public AddNodeActionButton(@NotNull Project project, @NotNull MethodFormManager.TreeType treeType) {
        this.project = project;
        this.treeType = treeType;
    }

    @Override
    public void run(AnActionButton anActionButton) {
        final AddMethodDialog dialog = new AddMethodDialog(project);
        dialog.pack();
        JPanel paramTableCards = dialog.methodForm.paramTableCards;
        String key = "new-table";
        paramTableCards.add(MyTableView.createTablePanel(new LinkedList<>(), treeType), key);
        ((CardLayout) paramTableCards.getLayout()).show(paramTableCards, key);
        dialog.setVisible(true);
    }
}
