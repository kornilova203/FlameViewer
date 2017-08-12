package com.github.kornilova_l.flamegraph.plugin.ui.line_markers;

import com.intellij.openapi.editor.ex.MarkupModelEx;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiRecursiveElementVisitor;

public class UpdatingPsiElementVisitor extends PsiRecursiveElementVisitor {
    private final MarkupModelEx markupModel;
    private final LineMarkersHolder lineMarkersHolder;

    UpdatingPsiElementVisitor(Project project, MarkupModelEx markupModel) {
        this.markupModel = markupModel;
        lineMarkersHolder = project.getComponent(LineMarkersHolder.class);
    }

    @Override
    public void visitElement(PsiElement element) {
        ProgressIndicatorProvider.checkCanceled();
        if (element == null) {
            return;
        }
        if (element instanceof PsiMethod) {
            lineMarkersHolder.updateMethodMarker((PsiMethod) element, markupModel);
            return;
        }
        element.acceptChildren(this);
    }
}
