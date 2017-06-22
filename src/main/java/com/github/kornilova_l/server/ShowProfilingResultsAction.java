package com.github.kornilova_l.server;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class ShowProfilingResultsAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent event) {
        BrowserUtil.browse("https://github.com");
    }
}
