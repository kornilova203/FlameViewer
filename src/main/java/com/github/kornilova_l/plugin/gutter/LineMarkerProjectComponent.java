package com.github.kornilova_l.plugin.gutter;

import com.github.kornilova_l.plugin.ProjectConfigManager;
import com.github.kornilova_l.plugin.config.ConfigStorage;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class LineMarkerProjectComponent extends AbstractProjectComponent {
    protected LineMarkerProjectComponent(Project project) {
        super(project);
    }

    @Override
    public void projectOpened() {
        ConfigStorage.Config config = ProjectConfigManager.getConfig(myProject);
        LineMarkersHolder lineMarkersHolder = myProject.getComponent(LineMarkersHolder.class);
        MessageBusConnection connection = myProject.getMessageBus().connect();
        connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
            @Override
            public void fileOpened(@NotNull FileEditorManager source, @NotNull VirtualFile file) {
                System.out.println(file.getName());
                if (Objects.equals(file.getFileType().getDefaultExtension(), "java")) {
                    PsiFile[] psiFiles = FilenameIndex.getFilesByName(
                            myProject,
                            file.getName(),
                            GlobalSearchScope.fileScope(myProject, file));
                    assert psiFiles.length == 1;
                    PsiFile psiFile = psiFiles[0];
                    for (PsiElement child : psiFile.getChildren()) {
                        if (child instanceof PsiClass) {
                            processMethods(((PsiClass) child), psiFile.getViewProvider().getDocument());
                        }
                    }
                }
            }

            private void processMethods(PsiClass psiClass, Document document) {
                for (PsiElement maybeMethod : psiClass.getChildren()) {
                    if (maybeMethod instanceof PsiMethod) {
                        if (config.contains(((PsiMethod) maybeMethod))) {
                            lineMarkersHolder.setIcon(
                                    (PsiMethod) maybeMethod,
                                    myProject,
                                    document
                            );
                        }
                    }
                }
            }
        });
    }
}
