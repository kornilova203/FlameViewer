package com.github.kornilova203.flameviewer;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.ide.BuiltInServerManager;

import static com.github.kornilova203.flameviewer.FlameViewerUpdateComponentKt.sendUsageUpdate;
import static com.github.kornilova203.flameviewer.server.ServerNamesKt.CALL_TRACES_PAGE;

public class OpenFlameViewerAction extends AnAction implements DumbAware {
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        sendUsageUpdate();
        BrowserUtil.browse("http://localhost:" +
                BuiltInServerManager.getInstance().getPort() +
                CALL_TRACES_PAGE);
    }
}
