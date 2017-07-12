package com.github.kornilova_l.plugin.config_dialog;

import com.github.kornilova_l.config.MethodConfig;
import com.github.kornilova_l.plugin.ProjectConfigManager;
import com.intellij.openapi.project.Project;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;

public class AddNodeActionButton implements AnActionButtonRunnable {
    private final Project project;
    private ChangeProfilerConfigDialog changeProfilerConfigDialog;

    AddNodeActionButton(Project project, ChangeProfilerConfigDialog changeProfilerConfigDialog) {
        this.project = project;
        this.changeProfilerConfigDialog = changeProfilerConfigDialog;
    }

    @Override
    public void run(AnActionButton anActionButton) {
        final AddMethod dialog = new AddMethod(project);
        if (!dialog.showAndGet()) {
            return;
        }
        MethodConfig methodConfig = new MethodConfig(dialog.getClassPattern(),
                dialog.getMethodPattern(),
                dialog.getParametersPattern());
        ProjectConfigManager.getConfig(project).methodConfigs.add(methodConfig);
        changeProfilerConfigDialog.addNodeToTree(methodConfig);
    }
}
