package com.github.kornilova_l.plugin.gutter;

import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

public class LineMarkerProjectComponent extends AbstractProjectComponent {
    protected LineMarkerProjectComponent(Project project) {
        super(project);
    }

    @Override
    public void projectOpened() {
        System.out.println("ya rodilsya");
        PsiDocumentManager.getInstance(myProject)
                .addListener(new PsiDocumentManager.Listener() {
                    @Override
                    public void documentCreated(@NotNull final Document document, PsiFile psiFile) {
                        System.out.println("doc created");
                    }

                    @Override
                    public void fileCreated(@NotNull PsiFile file, @NotNull Document document) {
                        System.out.println("file created");
                    }
                });
    }
}
