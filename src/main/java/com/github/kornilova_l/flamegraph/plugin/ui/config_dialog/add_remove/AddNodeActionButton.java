package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.add_remove;

import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import com.github.kornilova_l.flamegraph.plugin.configuration.PluginConfigManager;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.ChangeConfigurationDialog;
import com.github.kornilova_l.flamegraph.plugin.ui.line_markers.LineMarkersHolder;
import com.intellij.openapi.project.Project;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import org.jetbrains.annotations.NotNull;

public class AddNodeActionButton implements AnActionButtonRunnable {
    private final Project project;
    @NotNull
    private final LineMarkersHolder lineMarkersHolder;
    private final ChangeConfigurationDialog changeProfilerConfigDialog;

    public AddNodeActionButton(Project project, ChangeConfigurationDialog changeProfilerConfigDialog) {
        lineMarkersHolder = project.getComponent(LineMarkersHolder.class);
        this.project = project;
        this.changeProfilerConfigDialog = changeProfilerConfigDialog;
    }

    @Override
    public void run(AnActionButton anActionButton) {
        final AddMethod dialog = new AddMethod(project);
        if (!dialog.showAndGet()) {
            return;
        }
        MethodConfig methodConfig = new MethodConfig(
                dialog.getClassPattern(),
                dialog.getMethodPattern(),
                dialog.getParametersPattern());
        PluginConfigManager.getConfiguration(project).addMethodConfig(
                methodConfig,
                false);
        changeProfilerConfigDialog.addNodeToTree(methodConfig);
    }


}
