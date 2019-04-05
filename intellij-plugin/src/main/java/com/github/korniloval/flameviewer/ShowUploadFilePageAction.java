package com.github.korniloval.flameviewer;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.ide.BuiltInServerManager;

import static com.github.korniloval.flameviewer.server.ServerNamesKt.OUTGOING_CALLS_FULL;

public class ShowUploadFilePageAction extends AnAction implements DumbAware {
    @Override
    public void actionPerformed(AnActionEvent e) {
        BrowserUtil.browse("http://localhost:" +
                BuiltInServerManager.getInstance().getPort() +
                OUTGOING_CALLS_FULL);
    }
}
