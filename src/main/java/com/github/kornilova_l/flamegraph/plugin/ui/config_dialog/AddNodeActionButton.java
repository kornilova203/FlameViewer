package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog;

import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import com.github.kornilova_l.flamegraph.plugin.configuration.PluginConfigManager;
import com.github.kornilova_l.flamegraph.plugin.ui.gutter.LineMarkersHolder;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.AnActionButton;
import com.intellij.ui.AnActionButtonRunnable;
import org.jetbrains.annotations.NotNull;

public class AddNodeActionButton implements AnActionButtonRunnable {
    private final Project project;
    @NotNull
    private final LineMarkersHolder lineMarkersHolder;
    private final ChangeConfigurationDialog changeProfilerConfigDialog;

    AddNodeActionButton(Project project, ChangeConfigurationDialog changeProfilerConfigDialog) {
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
        MethodConfig methodConfig = PluginConfigManager.newMethodConfig(
                dialog.getClassPattern(),
                dialog.getMethodPattern(),
                dialog.getParametersPattern());
        PluginConfigManager.getConfiguration(project).addMethodConfig(
                methodConfig,
                false);
        changeProfilerConfigDialog.addNodeToTree(methodConfig);
        updateOpenedDocuments();
    }

    private void updateOpenedDocuments() {
        for (Editor editor : EditorFactory.getInstance().getAllEditors()) {
            if (editor.getProject() != project) {
                continue;
            }
            Document document = editor.getDocument();
            VirtualFile virtualFile = FileDocumentManager.getInstance().getFile(document);
            if (virtualFile != null) {
                lineMarkersHolder.updateMethodMarker(virtualFile);
            }
        }
    }
}
