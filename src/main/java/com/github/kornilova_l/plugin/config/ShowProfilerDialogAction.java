package com.github.kornilova_l.plugin.config;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class ShowProfilerDialogAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        new ProfilerDialog(e.getProject()).show();
    }
}
