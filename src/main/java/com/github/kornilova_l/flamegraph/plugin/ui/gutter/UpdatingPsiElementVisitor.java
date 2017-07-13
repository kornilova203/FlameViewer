package com.github.kornilova_l.flamegraph.plugin.ui.gutter;

import com.intellij.openapi.editor.ex.MarkupModelEx;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;

public class UpdatingPsiElementVisitor extends PsiRecursiveElementVisitor {
    private final MarkupModelEx markupModel;
    private final LineMarkersHolder lineMarkersHolder;

    public UpdatingPsiElementVisitor(Project project, MarkupModelEx markupModel) {
        this.markupModel = markupModel;
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
            lineMarkersHolder.updateMethodMarker((PsiMethod) element, markupModel);
        }
    }
}
