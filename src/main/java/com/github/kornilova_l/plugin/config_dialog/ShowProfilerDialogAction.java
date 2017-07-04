package com.github.kornilova_l.plugin.config_dialog;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class ShowProfilerDialogAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent e) {
        assert (e.getProject() != null);
        new ChangeProfilerConfigDialog(e.getProject()).show();
    }
}
