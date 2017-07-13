package com.github.kornilova_l.config;

import com.intellij.openapi.project.DumbService;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiParameter;
import com.intellij.psi.PsiTypeElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@SuppressWarnings("PublicField")
public class MethodConfig implements Comparable<MethodConfig> {
    private final static Pattern emptyParametersPattern = Pattern.compile("\\(\\)\\+?");
    @NotNull
    public String methodPatternString;
    @NotNull
    public String classPatternString;
    // empty list means no parameters
    // list with one "*" element means any parameters set
    @NotNull
    public LinkedList<Parameter> parameters = new LinkedList<>();
    public boolean isEnabled = true;
    public boolean saveReturnValue = false;
    @Nullable
    private Pattern classPattern;
    @Nullable
    private Pattern methodPattern;

    @SuppressWarnings("unused")
    public MethodConfig() {
        methodPatternString = "";
        classPatternString = "";
    }

    MethodConfig(PsiMethod psiMethod) {
        methodPatternString = "";
        classPatternString = "";
        setNames(psiMethod);
    }

    MethodConfig(String methodConfigLine) {
        methodPatternString = "";
        classPatternString = "";
        setNames(methodConfigLine);
    }

    public MethodConfig(@NotNull String classPatternString, @NotNull String methodPatternString, @NotNull String parametersPattern) {
        this.classPatternString = classPatternString;
        this.methodPatternString = methodPatternString;
        if (parametersPattern.charAt(parametersPattern.length() - 1) == '+') {
            saveReturnValue = true;
        }
        if (emptyParametersPattern.matcher(parametersPattern).matches()) { // if parameters are empty
            return;
        }
        parameters = getParametersList(parametersPattern);
    }

    @NotNull
    private static LinkedList<Parameter> getParametersList(@NotNull String parametersPattern) {
        LinkedList<Parameter> parameters = new LinkedList<>();
        parametersPattern = parametersPattern.substring(1, parametersPattern.lastIndexOf(")"));
        String[] stringParameters = parametersPattern.split(" *, *");
        for (String stringParameter : stringParameters) {
            parameters.addLast(new Parameter(stringParameter, ""));
        }
        return parameters;
    }

    public static String parametersToStringForJvm(List<Parameter> parameters) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(");
        for (Parameter parameter : parameters) {
            stringBuilder.append(parameter.getJvmType());
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    private static boolean areParametersSame(PsiParameter[] psiParameters, LinkedList<Parameter> configParameters) {
        if (configParameters.size() == 0) {
            return psiParameters.length == 0;
        }
        if (configParameters.size() > psiParameters.length) {
            return false;
        }
        int i = 0;
        for (; i < configParameters.size(); i++) {
            Parameter configParameter = configParameters.get(i);
            if (Objects.equals(configParameter.type, "*")) {
                return true;
            }
            PsiTypeElement typeElement = psiParameters[i].getTypeElement();
            if (typeElement == null) {
                return false;
            }
            if (DumbService.isDumb(typeElement.getProject())) { // do dumb compare
                if (!configParameter.type.endsWith(typeElement.getText())) {
                    return false;
                }
            } else if (!Objects.equals(configParameter.type, psiTypeToString(typeElement))) {
                return false;
            }
        }
        return psiParameters.length == i;
    }

    private static boolean areParametersSame(String[] jvmParameters, LinkedList<Parameter> parameters) {
        if (parameters.size() == 0) {
            return jvmParameters.length == 0;
        }
        if (parameters.size() > jvmParameters.length) {
            return false;
        }
        int i = 0;
        for (; i < parameters.size(); i++) {
            Parameter parameter = parameters.get(i);
            if (Objects.equals(parameter.type, "*")) {
                return true;
            }
            if (!Objects.equals(parameter.getJvmType(), jvmParameters[i])) {
                return false;
            }
        }
        return jvmParameters.length == i;
    }

    @NotNull
    private static LinkedList<MethodConfig.Parameter> getParametersList(PsiParameter[] psiParameters) {
        LinkedList<MethodConfig.Parameter> parameters = new LinkedList<>();
        for (PsiParameter psiParameter : psiParameters) {
            if (psiParameter.getTypeElement() == null ||
                    psiParameter.getName() == null) {
                continue;
            }
            parameters.add(new MethodConfig.Parameter(
                            psiTypeToString(psiParameter.getTypeElement()),
                            psiParameter.getName()
                    )
            );
        }
        return parameters;
    }

    @NotNull
    private static String psiTypeToString(@NotNull PsiTypeElement typeElement) {
        if (typeElement.getInnermostComponentReferenceElement() == null) { // primitive type
            return typeElement.getType().getPresentableText();
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < typeElement.getType().getArrayDimensions(); i++) {
            result.append("[]");
        }
        return typeElement.getInnermostComponentReferenceElement().getQualifiedName() +
                result.toString();
    }

    public String parametersToString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(");
        for (int i = 0; i < parameters.size(); i++) {
            MethodConfig.Parameter parameter = parameters.get(i);
            stringBuilder.append(parameter.type);
            if (i != parameters.size() - 1) {
                stringBuilder.append(", ");
            }
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    public String parametersToStringForExport() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(");
        for (int i = 0; i < parameters.size(); i++) {
            MethodConfig.Parameter parameter = parameters.get(i);
            stringBuilder.append(parameter.type);
            if (parameter.isEnable) {
                stringBuilder.append("+");
            }
            if (i != parameters.size() - 1) {
                stringBuilder.append(", ");
            }
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    private void setNames(@NotNull String methodConfigLine) {
        String classAndMethod = methodConfigLine.substring(0, methodConfigLine.indexOf("("));
        classPatternString = classAndMethod.substring(0, classAndMethod.lastIndexOf("."));
        methodPatternString = classAndMethod.substring(classAndMethod.lastIndexOf(".") + 1, classAndMethod.length());
        parameters = getParametersList(methodConfigLine.substring(
                methodConfigLine.indexOf("("),
                methodConfigLine.indexOf(")") + 1
                )
        );
        saveReturnValue = methodConfigLine.charAt(methodConfigLine.length() - 1) == '+';
        compilePatterns();
    }

    @Override
    public String toString() {
        return getQualifiedName() + parametersToString();
    }

    public String toStringForExport() {
        return getQualifiedName() + parametersToStringForExport() + (saveReturnValue ? "+" : "");
    }

    public String getQualifiedName() {
        return classPatternString + "." + methodPatternString;
    }

    @Override
    public int compareTo(@NotNull MethodConfig o) {
        return toString().compareTo(o.toString());
    }

    private void setNames(@NotNull PsiMethod psiMethod) {
        if (psiMethod.getContainingClass() == null ||
                psiMethod.getContainingClass().getQualifiedName() == null) {
            classPatternString = "";
        } else {
            classPatternString = psiMethod.getContainingClass().getQualifiedName();
        }
        methodPatternString = psiMethod.getName();
        parameters = getParametersList(psiMethod.getParameterList().getParameters());
    }

    public Object getJvmClassName() {
        return classPatternString.replaceAll("\\.", "/");
    }

    public boolean isApplicableTo(PsiMethod psiMethod) {
        compilePatterns();
        assert classPattern != null;
        assert methodPattern != null;
        //noinspection SimplifiableIfStatement
        if (psiMethod.getContainingClass() == null ||
                psiMethod.getContainingClass().getQualifiedName() == null) {
            return false;
        }
        return classPattern.matcher(psiMethod.getContainingClass().getQualifiedName()).matches() &&
                methodPattern.matcher(psiMethod.getName()).matches() &&
                areParametersSame(psiMethod.getParameterList().getParameters(), parameters);
    }

    private void compilePatterns() {
        if (classPattern == null || methodPattern == null) {
            classPattern = Pattern.compile(
                    classPatternString
                            .replaceAll("\\.", "\\.")
                            .replaceAll("\\*", ".*"));
            methodPattern = Pattern.compile(
                    methodPatternString
                            .replaceAll("\\.", "\\.")
                            .replaceAll("\\*", ".*"));
        }
    }

    @NotNull
    public String getPackagePattern() {
        int dot = classPatternString.lastIndexOf(".");
        if (dot == -1) {
            return "";
        }
        return classPatternString.substring(0, dot);
    }

    @NotNull
    public String getClassPattern() {
        int dot = classPatternString.lastIndexOf(".");
        if (dot == -1) {
            return classPatternString;
        }
        return classPatternString.substring(dot + 1, classPatternString.length());
    }

    public boolean isApplicableToClass(@NotNull String className) {
        assert classPattern != null;
        return classPattern.matcher(className).matches();
    }

    public boolean isApplicableTo(@NotNull String className, @NotNull String methodName, @NotNull String[] jvmParameters) {
        assert classPattern != null;
        assert methodPattern != null;
        return classPattern.matcher(className).matches() &&
                methodPattern.matcher(methodName).matches() &&
                areParametersSame(jvmParameters, parameters);
    }

    public boolean isApplicableTo(@NotNull String methodName, @NotNull String jvmDescPart) {
        assert methodPattern != null;
        return methodPattern.matcher(methodName).matches() &&
                areParametersSame(ConfigStorage.Config.splitJvmParams(jvmDescPart), parameters);
    }

    public static class Parameter {
        public String type;
        public String name;
        public boolean isEnable = false;

        @SuppressWarnings("unused")
        Parameter() {
        }

        Parameter(@NotNull String type, @NotNull String name) {
            this.type = type;
            this.name = name;
        }

        @NotNull
        private static String getJvmType(@NotNull String typeWithoutDimensions) {
            switch (typeWithoutDimensions) {
                case "int":
                    return "I";
                case "long":
                    return "J";
                case "boolean":
                    return "Z";
                case "char":
                    return "C";
                case "short":
                    return "S";
                case "byte":
                    return "B";
                case "float":
                    return "F";
                case "double":
                    return "D";
                default:
                    return "L" +
                            typeWithoutDimensions.replaceAll("\\.", "/") +
                            ";";
            }
        }

        @Nullable
        public String getJvmType() {
            if (Objects.equals(type, "*")) {
                return null;
            }
            StringBuilder dimensions = new StringBuilder();
            for (int i = 0; i < type.length(); i++) {
                if (type.charAt(i) == '[') {
                    dimensions.append("[");
                }
            }
            String typeWithoutDimensions;
            if (dimensions.length() == 0) {
                typeWithoutDimensions = type;
            } else {
                typeWithoutDimensions = type.substring(0, type.indexOf("["));
            }
            String jvmType = getJvmType(typeWithoutDimensions);
            return dimensions.toString() + jvmType;
        }
    }
}
