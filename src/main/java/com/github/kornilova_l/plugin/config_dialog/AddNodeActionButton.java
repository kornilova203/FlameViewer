package com.github.kornilova_l.plugin.config_dialog;

import com.github.kornilova_l.plugin.ProjectConfigManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;

public class AddNodeActionButton implements AnActionButtonRunnable {
    private final Project project;

    AddNodeActionButton(Project project) {
        this.project = project;
    }

    @Override
    public void run(AnActionButton anActionButton) {
        final AddMethod dialog = new AddMethod(project);
        if (!dialog.showAndGet()) {
            return;
        }
        ProjectConfigManager.getConfig(project).addMethod(
                dialog.getClassPattern(),
                dialog.getMethodPattern(),
                dialog.getParametersPattern());
    }
}
