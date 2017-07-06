package com.github.kornilova_l.plugin.gutter;

import com.github.kornilova_l.plugin.ProjectConfigManager;
import com.github.kornilova_l.plugin.config.ConfigStorage;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
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
        PsiDocumentManager.getInstance(myProject)
                .addListener(new PsiDocumentManager.Listener() {
                    @Override
                    public void documentCreated(@NotNull final Document document, PsiFile psiFile) {
                        if (psiFile == null) {
                            return;
                        }
                        System.out.println(psiFile.getName());
                        if (Objects.equals(psiFile.getFileType().getDefaultExtension(), "java")) {
                            for (PsiElement child : psiFile.getChildren()) {
                                if (child instanceof PsiClass) {
                                    processMethods(((PsiClass) child), document);
                                }
                            }
                        }
                    }

                    @Override
                    public void fileCreated(@NotNull PsiFile psiFile, @NotNull Document document) {
                        System.out.println(psiFile.getName());
                        if (Objects.equals(psiFile.getFileType().getDefaultExtension(), "java")) {
                            for (PsiElement child : psiFile.getChildren()) {
                                if (child instanceof PsiClass) {
                                    processMethods(((PsiClass) child), document);
                                }
                            }
                        }
                    }

                    private void processMethods(PsiClass psiClass, Document document) {
                        System.out.println("Class: " + psiClass.getName());
                        for (PsiElement maybeMethod : psiClass.getChildren()) {
                            if (maybeMethod instanceof PsiMethod) {
                                System.out.println("Method: " + ((PsiMethod) maybeMethod).getName());
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
