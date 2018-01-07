package com.github.kornilova_l.flamegraph.plugin.server;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.ide.BuiltInServerManager;

public class ShowUploadFilePageAction extends AnAction implements DumbAware {
    @Override
    public void actionPerformed(AnActionEvent e) {
        BrowserUtil.browse("http://localhost:" +
                BuiltInServerManager.getInstance().getPort() +
                ServerNames.OUTGOING_CALLS +
                "?" +
                "project=uploaded-files");
    }
}
