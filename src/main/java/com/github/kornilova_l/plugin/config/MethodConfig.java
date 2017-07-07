package com.github.kornilova_l.plugin.config;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.github.kornilova_l.plugin.config.ConfigStorage.Config.getParametersList;
import static com.github.kornilova_l.plugin.config.ConfigStorage.Config.parametersToString;

@SuppressWarnings("PublicField")
public class MethodConfig implements Comparable<MethodConfig> {

    @Nullable
    public String packageName;
    public String methodName;
    public String className;
    public List<Parameter> parameters;
    public boolean isEnabled = true;

    @SuppressWarnings("unused")
    public MethodConfig() {
    }

    public MethodConfig(@NotNull PsiMethod psiMethod) {
        setNames(psiMethod);
        System.out.println(this);
    }

    @Override
    public String toString() {
        return getQualifiedName() + ConfigStorage.Config.parametersToString(parameters);
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
        return toString().compareTo(o.toString());
    }

    private void setNames(PsiMethod psiMethod) {
        className = null;
        methodName = psiMethod.getName();
        parameters = getParametersList(psiMethod.getParameterList().getParameters());
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

    public String toStringForConfig() {
        return getQualifiedNameWithSlashes() + parametersToString(parameters);
    }

    public static class Parameter {
        public String type;
        public String name;

        @SuppressWarnings("unused")
        Parameter() {
        }

        Parameter(String type, String name) {
            this.type = type;
            this.name = name;
        }
    }
}
