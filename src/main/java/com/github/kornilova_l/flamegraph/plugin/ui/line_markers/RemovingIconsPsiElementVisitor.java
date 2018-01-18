package com.github.kornilova_l.flamegraph.plugin.ui.line_markers;

import com.intellij.openapi.editor.ex.MarkupModelEx;
import com.intellij.openapi.progress.ProgressIndicatorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;

public class RemovingIconsPsiElementVisitor extends UpdatingPsiElementVisitor {

    RemovingIconsPsiElementVisitor(Project project, MarkupModelEx markupModel) {
        super(project, markupModel);
    }

    @Override
    public void visitElement(PsiElement element) {
        ProgressIndicatorProvider.checkCanceled();
        if (element == null) {
            return;
        }
        if (element instanceof PsiMethod) {
            lineMarkersHolder.removeInvalidMethods(markupModel);
            lineMarkersHolder.removeIconIfPresent((PsiMethod) element, markupModel);
            return;
        }
        element.acceptChildren(this);
    }
}
