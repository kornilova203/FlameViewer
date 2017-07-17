package com.github.kornilova_l.flamegraph.plugin.server;

import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.ide.BuiltInServerManager;

public class ShowProfilingResultsAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent event) {
        BrowserUtil.browse("http://localhost:" +
                BuiltInServerManager.getInstance().getPort() +
                getUri(event.getProject()));
    }

    @NotNull
    private static String getUri(@Nullable Project project) {
        if (project == null) {
            return ServerNames.SELECT_FILE;
        }
        return ServerNames.SELECT_FILE +
                "?project=" +
                project.getName();
    }
}
