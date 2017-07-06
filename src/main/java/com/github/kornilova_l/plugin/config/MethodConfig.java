package com.github.kornilova_l.plugin.config;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("PublicField")
public class MethodConfig implements Comparable<MethodConfig> {

    @Nullable public String packageName;
    public String methodName;
    public String className;
    public boolean isEnabled = true;

    public MethodConfig() {
    }

    public MethodConfig(@NotNull PsiMethod psiMethod) {
        setNames(psiMethod);
    }

    @Override
    public String toString() {
        return getQualifiedName();
    }

    public String getQualifiedName() {
        if (packageName == null) {
            return className + "." + methodName;
        }
        return packageName + "." + className + "." + methodName;
    }

    public String getQualifiedNameWithSlashes() {
        if (packageName == null) {
            return className + "." + methodName;
        }
        return packageName.replace(".", "/") + "/" + className + "." + methodName;
    }

    @Override
    public int compareTo(@NotNull MethodConfig o) {
        return getQualifiedName().compareTo(o.getQualifiedName());
    }

    private void setNames(PsiMethod psiMethod) {
        className = null;
        methodName = psiMethod.getName();
        PsiClass psiClass = psiMethod.getContainingClass();
        assert psiClass != null;

        while (psiClass != null) {
            className = className == null ?
                    psiClass.getName() :
                    psiClass.getName() + "." + className;
            psiClass = psiClass.getContainingClass();
        }
        String fullName = psiMethod.getContainingClass().getQualifiedName();
        assert fullName != null;
        int beginningOfClassName = fullName.indexOf(className);
        packageName = fullName.substring(0, beginningOfClassName - 1);
    }
}
