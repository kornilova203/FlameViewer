package com.github.kornilova_l.plugin;

import com.intellij.execution.*;
import com.intellij.execution.executors.DefaultRunExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.ui.RunContentDescriptor;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;


public class ProfileAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
            List<RunnerAndConfigurationSettings> configurationList = RunManager.getInstance(getEventProject(event)).getAllSettings();
            ProgramRunnerUtil.executeConfiguration(
                    getEventProject(event),
                    configurationList.get(0),
                    DefaultRunExecutor.getRunExecutorInstance()
            );
    }

    @Nullable
    protected ExecutionEnvironment getEnvironment(@NotNull AnActionEvent event) {
        ExecutionEnvironment environment = event.getData(LangDataKeys.EXECUTION_ENVIRONMENT);
        if (environment == null) {
            Project project = event.getProject();
            RunContentDescriptor contentDescriptor = project == null ? null : ExecutionManager.getInstance(project).getContentManager().getSelectedContent();
            if (contentDescriptor != null) {
                JComponent component = contentDescriptor.getComponent();
                if (component != null) {
                    environment = LangDataKeys.EXECUTION_ENVIRONMENT.getData(DataManager.getInstance().getDataContext(component));
                }
            }
        }
        return environment;
    }
}
