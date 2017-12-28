package com.github.kornilova_l.flamegraph.configuration;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MethodConfig implements Comparable<MethodConfig>, Cloneable {
    private final static Pattern paramsPattern = Pattern.compile("\\[*(C|Z|S|I|J|F|D|B|(:?L[^;]+;))");
    @NotNull
    private String methodPatternString = "";
    @NotNull
    private String classPatternString = "";
    @NotNull
    private List<Parameter> parameters = new LinkedList<>();
    private boolean isEnabled = true;
    private boolean saveReturnValue = false;

    private Pattern classPattern;
    private Pattern methodPattern;

    @SuppressWarnings("unused")
    public MethodConfig() {
    }

    public MethodConfig(@NotNull String classPatternString,
                        @NotNull String methodPatternString,
                        @NotNull List<Parameter> parameters,
                        boolean isEnabled,
                        boolean saveReturnValue) {
        this.classPatternString = classPatternString;
        this.methodPatternString = methodPatternString;
        this.parameters = parameters;
        this.isEnabled = isEnabled;
        this.saveReturnValue = saveReturnValue;
        compilePatterns();
    }

    public MethodConfig(@NotNull String classPatternString,
                        @NotNull String methodPatternString,
                        @NotNull String parametersPattern) {
        this.classPatternString = classPatternString;
        this.methodPatternString = methodPatternString;
        this.isEnabled = true;
        this.saveReturnValue = parametersPattern.charAt(parametersPattern.length() - 1) == '+';
        parameters = parametersPatternToList(parametersPattern.substring(parametersPattern.indexOf("(") + 1,
                parametersPattern.indexOf(")")));
    }

    /**
     * Copy constructor
     *
     * @param methodConfig config to copy
     */
    public MethodConfig(MethodConfig methodConfig) {
        classPatternString = methodConfig.classPatternString;
        methodPatternString = methodConfig.methodPatternString;
        parameters = new LinkedList<>();
        for (Parameter parameter : methodConfig.parameters) {
            parameters.add(new Parameter(parameter));
        }
        isEnabled = methodConfig.isEnabled;
        saveReturnValue = methodConfig.saveReturnValue;
        compilePatterns();
    }

    @NotNull
    private static List<Parameter> parametersPatternToList(String parametersPatternInnerPart) {
        LinkedList<MethodConfig.Parameter> parameters = new LinkedList<>();
        if (Objects.equals(parametersPatternInnerPart, "")) {
            return parameters;
        }
        String[] stringParameters = parametersPatternInnerPart.split(" *, *");
        for (String stringParameter : stringParameters) {
            boolean isEnabled = stringParameter.charAt(stringParameter.length() - 1) == '+';
            if (isEnabled) {
                stringParameter = stringParameter.substring(0, stringParameter.length() - 1);
            }
            parameters.addLast(new MethodConfig.Parameter(stringParameter, isEnabled));
        }
        return parameters;
    }

    private static boolean areParametersApplicable(@NotNull List<Parameter> applicableParams,
                                                   @NotNull List<Parameter> testedParams) {
        if (applicableParams.size() == 0) {
            return testedParams.size() == 0;
        }
        if (applicableParams.size() == 1 &&
                Objects.equals(applicableParams.get(0).type, "*")) {
            return true;
        }
        if (applicableParams.size() > testedParams.size() &&
                !(applicableParams.size() == testedParams.size() + 1 && // for example "boolean, *" and "boolean"
                        Objects.equals(applicableParams.get(applicableParams.size() - 1).type, "*"))) {
            return false;
        }
        int i = 0;
        for (; i < applicableParams.size(); i++) {
            Parameter parameter = applicableParams.get(i);
            if (Objects.equals(parameter.type, "*")) {
                return true;
            }
            if (!testedParams.get(i).getType().endsWith(parameter.getType())) {
                return false;
            }
        }
        return testedParams.size() == i;
    }

    public MethodConfig clone() {
        try {
            return (MethodConfig) super.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Cannot clone method config");
    }

    private void compilePatterns() {
        if (classPattern == null || methodPattern == null) {
            classPattern = Pattern.compile(
                    classPatternString
                            .replaceAll("\\.", "\\.")
                            .replaceAll("\\*", ".*")
                            .replaceAll("\\$", "\\\\\\$"));
            methodPattern = Pattern.compile(
                    methodPatternString
                            .replaceAll("\\.", "\\.")
                            .replaceAll("\\*", ".*")
                            .replaceAll("\\$", "\\\\\\$"));
        }
    }

    public boolean isSaveReturnValue() {
        return saveReturnValue;
    }

    public void setSaveReturnValue(boolean saveReturnValue) {
        this.saveReturnValue = saveReturnValue;
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

    private String parametersWithSaveToString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("(");
        for (int i = 0; i < parameters.size(); i++) {
            MethodConfig.Parameter parameter = parameters.get(i);
            stringBuilder.append(parameter.type);
            if (parameter.isEnabled) {
                stringBuilder.append("+");
            }
            if (i != parameters.size() - 1) {
                stringBuilder.append(", ");
            }
        }
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    @Override
    public String toString() {
        return getQualifiedName() + parametersWithSaveToString() + (saveReturnValue ? "+" : "");
    }

    String getQualifiedName() {
        return classPatternString + "." + methodPatternString;
    }

    @Override
    public int compareTo(@NotNull MethodConfig o) {
        return toString().compareTo(o.toString());
    }

    @NotNull
    public String getMethodPatternString() {
        return methodPatternString;
    }

    public void setMethodPatternString(@NotNull String methodPatternString) {
        this.methodPatternString = methodPatternString;
    }

    @NotNull
    public String getClassPatternString() {
        return classPatternString;
    }

    public void setClassPatternString(@NotNull String classPatternString) {
        this.classPatternString = classPatternString;
    }

    @NotNull
    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(@NotNull List<Parameter> parameters) {
        this.parameters = parameters;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    @NotNull
    public String getPackagePattern() {
        int dot = classPatternString.lastIndexOf(".");
        if (dot == -1) {
            return "";
        } else {
            return classPatternString.substring(0, dot);
        }
    }

    @NotNull
    public String getClassPattern() {
        int dot = classPatternString.lastIndexOf(".");
        if (dot == -1) {
            return classPatternString;
        } else {
            return classPatternString.substring(dot + 1, classPatternString.length());
        }
    }

    public boolean isApplicableTo(@NotNull MethodConfig testedConfig) {
        if (!isEnabled) {
            return false;
        }
        compilePatterns();
        return classPattern.matcher(testedConfig.classPatternString).matches() &&
                methodPattern.matcher(testedConfig.methodPatternString).matches() &&
                areParametersApplicable(parameters, testedConfig.parameters);
    }

    public boolean isApplicableTo(String className) {
        compilePatterns();
        return classPattern.matcher(className).matches();
    }

    void removeEmptyParams() {
        List<Parameter> newParameters = new LinkedList<>();
        for (Parameter parameter : parameters) {
            if (!Objects.equals(parameter.type, "")) {
                newParameters.add(parameter);
            }
        }
        parameters = newParameters;
    }

    @NotNull
    public static List<String> splitDesc(@NotNull String descInnerPart) {
        List<String> jvmParams = new LinkedList<>();
        Matcher m = paramsPattern.matcher(descInnerPart);
        while (m.find()) {
            jvmParams.add(m.group());
        }
        return jvmParams;
    }

    @NotNull
    public static String jvmTypeToParam(@NotNull String typeWithoutDimensions) {
        switch (typeWithoutDimensions) {
            case "I":
                return "int";
            case "J":
                return "long";
            case "Z":
                return "boolean";
            case "C":
                return "char";
            case "S":
                return "short";
            case "B":
                return "byte";
            case "F":
                return "float";
            case "D":
                return "double";
            case "V":
                return "void";
            default:
                String nameWithoutLAndSemicolon = typeWithoutDimensions.substring(1, typeWithoutDimensions.length() - 1);
                return nameWithoutLAndSemicolon.substring(nameWithoutLAndSemicolon.lastIndexOf("/") + 1, nameWithoutLAndSemicolon.length());
        }
    }

    void clearPatterns() {
        classPattern = null;
        methodPattern = null;
    }

    public static class Parameter {
        private String type;
        private boolean isEnabled;

        @SuppressWarnings("unused")
        public Parameter() {
        }

        public Parameter(@NotNull String type, boolean isEnabled) {
            this.type = type;
            this.isEnabled = isEnabled;
        }

        /**
         * Copy constructor
         *
         * @param parameter parameter to copy
         */
        Parameter(Parameter parameter) {
            type = parameter.type;
            isEnabled = parameter.isEnabled;
        }

        public boolean isEnabled() {
            return isEnabled;
        }

        public void setEnabled(boolean enabled) {
            isEnabled = enabled;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }
}
