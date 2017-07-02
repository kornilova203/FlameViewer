package com.github.kornilova_l.plugin.execution;

import com.intellij.execution.ExecutorRegistry;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;

public class ChooseProfilerConfigurationAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getData(CommonDataKeys.PROJECT);
        assert project != null;

        new ChooseProfilerConfigurationPopup(
                project,
                ExecutorRegistry.getInstance().getExecutorById(ProfilerExecutor.EXECUTOR_ID)
        ).show();
    }
}