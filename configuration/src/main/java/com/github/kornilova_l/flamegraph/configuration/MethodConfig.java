package com.github.kornilova_l.flamegraph.configuration;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

class MethodConfig implements Comparable<MethodConfig> {
    @NotNull
    private String methodPatternString = "";
    @NotNull
    private String classPatternString = "";
    @NotNull
    private List<Parameter> parameters = new LinkedList<>();
    private boolean isEnabled = true;
    private boolean saveReturnValue = false;

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
        return getQualifiedName() + parametersToString() + (saveReturnValue ? "+" : "");
    }

    public String getQualifiedName() {
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
        return methodPatternString;
    }

    public void setClassPatternString(@NotNull String methodPatternString) {
        this.methodPatternString = methodPatternString;
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

    public static class Parameter {
        private String type;
        private boolean isEnabled;

        @SuppressWarnings("unused")
        Parameter() {
        }

        Parameter(@NotNull String type, boolean isEnabled) {
            this.type = type;
            this.isEnabled = isEnabled;
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
