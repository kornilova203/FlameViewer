package com.github.kornilova_l.plugin.config;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.github.kornilova_l.plugin.config.ConfigStorage.Config.getParametersList;

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

    static String parametersToString(List<MethodConfig.Parameter> parameters) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(");
        for (int i = 0; i < parameters.size(); i++) {
            MethodConfig.Parameter parameter = parameters.get(i);
            stringBuilder.append(parameter.type).append(" ").append(parameter.name);
            if (i != parameters.size() - 1) {
                stringBuilder.append(", ");
            }
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        return getQualifiedName() + parametersToString(parameters);
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

    public String toStringForJvm() {
        return getQualifiedNameWithSlashes() + parametersToStringForJvm(parameters);
    }

    private static String parametersToStringForJvm(List<Parameter> parameters) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(");
        for (Parameter parameter : parameters) {
            stringBuilder.append(parameter.jvmType);
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    public String getWhichParamsAreEnabled() {
        StringBuilder stringBuilder = new StringBuilder();
        for (Parameter parameter : parameters) {
            stringBuilder.append(parameter.isEnable ? "y" : "n");
        }
        return stringBuilder.toString();
    }

    public static class Parameter {
        public String type;
        public String name;
        public String jvmType;
        public boolean isEnable = false;

        @SuppressWarnings("unused")
        Parameter() {
        }

        Parameter(String type, String name) {
            this.type = type;
            this.name = name;
        }

        Parameter(String type, String name, String jvmType) {
            this.type = type;
            this.name = name;
            this.jvmType = jvmType;
        }
    }
}
