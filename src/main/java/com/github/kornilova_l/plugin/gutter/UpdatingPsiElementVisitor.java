package com.github.kornilova_l.plugin.gutter;

import com.github.kornilova_l.plugin.config.ConfigStorage;
import com.intellij.openapi.editor.ex.MarkupModelEx;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;

public class UpdatingPsiElementVisitor extends PsiRecursiveElementVisitor {
    private final MarkupModelEx markupModel;
    private final ConfigStorage.Config config;
    private final LineMarkersHolder lineMarkersHolder;

    public UpdatingPsiElementVisitor(Project project, MarkupModelEx markupModel, ConfigStorage.Config config) {
        this.markupModel = markupModel;
        this.config = config;
        lineMarkersHolder = project.getComponent(LineMarkersHolder.class);
    }

    @Override
    public void visitElement(PsiElement element) {
        ProgressIndicatorProvider.checkCanceled();
        if (element instanceof PsiClass ||
                element instanceof PsiFile) {
            element.acceptChildren(this);
        }
        if (element instanceof PsiMethod) {
            if (config.contains(((PsiMethod) element))) {
                lineMarkersHolder.setIcon((PsiMethod) element, markupModel);
            } else {
                lineMarkersHolder.removeIconIfPresent(((PsiMethod) element), markupModel);
            }
        }
    }
}
