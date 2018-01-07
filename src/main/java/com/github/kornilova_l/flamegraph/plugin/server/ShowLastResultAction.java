package com.github.kornilova_l.flamegraph.plugin.server;

import com.github.kornilova_l.flamegraph.plugin.PluginFileManager;
import com.intellij.ide.BrowserUtil;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.ide.BuiltInServerManager;

public class ShowLastResultAction extends AnAction implements DumbAware {
    @Override
    public void actionPerformed(AnActionEvent event) {
        if (event.getProject() != null) {
            BrowserUtil.browse("http://localhost:" +
                    BuiltInServerManager.getInstance().getPort() +
                    getUri(event.getProject()));
        }
    }

    @NotNull
    private static String getUri(@NotNull Project project) {
        String fileName = PluginFileManager.getInstance().getLatestFileName(project.getName());
        if (fileName != null) {
            return ServerNames.OUTGOING_CALLS +
                    "?project=" +
                    project.getName() +
                    "&file=" +
                    fileName;
        }
        return ServerNames.OUTGOING_CALLS +
                "?project=" +
                project.getName();
    }
}
