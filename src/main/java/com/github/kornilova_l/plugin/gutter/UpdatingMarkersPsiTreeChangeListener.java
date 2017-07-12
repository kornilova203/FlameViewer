package com.github.kornilova_l.plugin.gutter;

import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UpdatingMarkersPsiTreeChangeListener implements PsiTreeChangeListener {
    private final LineMarkersHolder lineMarkersHolder;
    private final Project project;

    UpdatingMarkersPsiTreeChangeListener(LineMarkersHolder lineMarkersHolder, Project project) {
        this.lineMarkersHolder = lineMarkersHolder;
        this.project = project;
    }

    @Nullable
    private static PsiFile getPsiFile(@NotNull PsiMethod psiMethod) {
        PsiElement element = psiMethod.getParent();
        while (element != null) {
            if (element instanceof PsiFile) {
                return (PsiFile) element;
            }
            element = element.getParent();
        }
        return null;
    }

    @Override
    public void childAdded(@NotNull PsiTreeChangeEvent event) {
        if (event.getChild() instanceof  PsiMethod) {
            PsiMethod psiMethod = ((PsiMethod) event.getChild());
            updateMethodMarker(psiMethod);
        }
        if (event.getParent() instanceof PsiMethod) {
            PsiMethod psiMethod = ((PsiMethod) event.getParent());
            updateMethodMarker(psiMethod);
        }
    }

    private void updateMethodMarker(PsiMethod psiMethod) {
        PsiFile psiFile = getPsiFile(psiMethod);
        if (psiFile != null) {
            lineMarkersHolder.updateMethodMarker(
                    psiMethod,
                    LineMarkersHolder.getMarkupModel(psiFile.getViewProvider().getDocument(), project)
            );
        }
    }

    @Override
    public void childReplaced(@NotNull PsiTreeChangeEvent event) {
        if (event.getParent() instanceof PsiParameterList) {
            if (event.getParent().getParent() instanceof PsiMethod) {
                PsiMethod psiMethod = (PsiMethod) event.getParent().getParent();
                if (psiMethod.getContainingClass() == null) {
                    return;
                }
                updateMethodMarker(psiMethod);
                return;
            }
        }
        if (event.getParent() instanceof PsiMethod) {
            PsiMethod psiMethod = (PsiMethod) event.getParent();
            if (psiMethod.getContainingClass() == null) {
                return;
            }
            updateMethodMarker(psiMethod);
        }
    }

    @Override
    public void beforeChildAddition(@NotNull PsiTreeChangeEvent event) {
    }

    @Override
    public void beforeChildRemoval(@NotNull PsiTreeChangeEvent event) {
    }

    @Override
    public void beforeChildReplacement(@NotNull PsiTreeChangeEvent event) {
    }

    @Override
    public void beforeChildMovement(@NotNull PsiTreeChangeEvent event) {
    }

    @Override
    public void beforeChildrenChange(@NotNull PsiTreeChangeEvent event) {
    }

    @Override
    public void beforePropertyChange(@NotNull PsiTreeChangeEvent event) {
    }

    @Override
    public void childRemoved(@NotNull PsiTreeChangeEvent event) {
    }

    @Override
    public void childrenChanged(@NotNull PsiTreeChangeEvent event) {
    }

    @Override
    public void childMoved(@NotNull PsiTreeChangeEvent event) {
    }

    @Override
    public void propertyChanged(@NotNull PsiTreeChangeEvent event) {
    }
}
