package com.github.kornilova_l.server;

import com.intellij.ide.browsers.actions.BaseOpenInBrowserAction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

import static com.intellij.ide.browsers.actions.OpenFileInDefaultBrowserActionKt.findUsingBrowser;

public class ShowProfilingResultsAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent event) {
        System.out.println("ShowProfilingResultsAction");
        BaseOpenInBrowserAction.Companion.open(event, findUsingBrowser());
    }
}
