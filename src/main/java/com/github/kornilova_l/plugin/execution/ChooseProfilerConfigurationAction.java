package com.github.kornilova_l.plugin.execution;

import com.github.kornilova_l.plugin.config.ProfilerSettings;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.util.BaseListPopupStep;
import com.intellij.ui.popup.list.ListPopupImpl;

import java.util.LinkedList;

public class ChooseProfilerConfigurationAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        final Project project = e.getData(CommonDataKeys.PROJECT);
        assert project != null;
        LinkedList<ProfilerSettings> list = new LinkedList<>();
        list.add(new ProfilerSettings("Slim"));
        list.add(new ProfilerSettings("Shady"));
        new ListPopupImpl(
                new BaseListPopupStep<>("Profiler configuration", list)
        ).showCenteredInCurrentWindow(project);
    }
}