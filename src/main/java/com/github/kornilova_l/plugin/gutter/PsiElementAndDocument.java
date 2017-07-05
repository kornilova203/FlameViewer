package com.github.kornilova_l.plugin.gutter;

import com.intellij.openapi.editor.Document;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class PsiElementAndDocument {
    private final PsiElement psiElement;
    private final Document document;

    public PsiElementAndDocument(@NotNull PsiElement psiElement, @NotNull Document document) {
        this.psiElement = psiElement;
        this.document = document;
    }

    public PsiElement getPsiElement() {
        return psiElement;
    }

    public Document getDocument() {
        return document;
    }
}
