package com.github.kornilova_l.flamegraph.javaagent.agent;

import com.github.kornilova_l.flamegraph.configuration.Configuration;
import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class AgentConfigurationManager {
    private final static Pattern paramsPattern = Pattern.compile("(\\[*)(C|Z|S|I|J|F|D|B|(:?L[^;]+;))");
    private final Configuration configuration = new Configuration();

    AgentConfigurationManager(List<String> methodConfigLines) {
        for (String methodConfigLine : methodConfigLines) {
            boolean isExcluding = methodConfigLine.charAt(0) == '!';
            if (isExcluding) {
                methodConfigLine = methodConfigLine.substring(1, methodConfigLine.length());
            }
            String classAndMethod = methodConfigLine.substring(0, methodConfigLine.indexOf("("));
            String classPatternString = classAndMethod.substring(0, classAndMethod.lastIndexOf("."));
            String methodPatternString = classAndMethod.substring(
                    classAndMethod.lastIndexOf(".") + 1,
                    classAndMethod.length()
            );
            String parametersPattern = methodConfigLine.substring(methodConfigLine.indexOf("("), methodConfigLine.length());
            configuration.addMethodConfig(
                    new MethodConfig(
                            classPatternString,
                            methodPatternString,
                            parametersPattern
                    ),
                    isExcluding);
        }
        System.out.println("Configuration:");
        System.out.println("Including methods: " + configuration.getIncludingMethodConfigs());
        System.out.println("Excluding methods: " + configuration.getExcludingMethodConfigs());
    }

    @NotNull
    static MethodConfig newMethodConfig(@NotNull String className,
                                        @NotNull String methodName,
                                        @NotNull String desc) {
        String descInnerPart = desc.substring(desc.indexOf("(") + 1, desc.indexOf(")"));
        List<String> jvmParams = splitDesc(descInnerPart);
        return new MethodConfig(className.replaceAll("/", "."),
                methodName,
                Objects.equals(descInnerPart, "") ?
                        "()" :
                        jvmParamsToPattern(jvmParams));
    }

    @NotNull
    static List<String> splitDesc(@NotNull String descInnerPart) {
        List<String> jvmParams = new LinkedList<>();
        Matcher m = paramsPattern.matcher(descInnerPart);
        while (m.find()) {
            jvmParams.add(m.group());
        }
        return jvmParams;
    }

    @NotNull
    private static String jvmParamsToPattern(List<String> jvmParams) {
        LinkedList<String> params = new LinkedList<>();
        for (String jvmParam : jvmParams) {
            StringBuilder paramBuilder = new StringBuilder();
            int dimensions = countDimensions(jvmParam);
            paramBuilder.append(
                    jvmTypeToParam(jvmParam.substring(dimensions, jvmParam.length()))
            );
            for (int i = 0; i < dimensions; i++) {
                paramBuilder.append("[]");
            }
            params.add(paramBuilder.toString());
        }
        return "(" + String.join(", ", params) + ")";
    }

    private static int countDimensions(String jvmType) {
        int count = 0;
        for (int i = 0; i < jvmType.length(); i++) {
            if (jvmType.charAt(i) == '[') {
                count++;
            } else {
                break;
            }
        }
        return count;
    }

    @NotNull
    private static String jvmTypeToParam(@NotNull String typeWithoutDimensions) {
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
            default:
                String nameWithoutLAndSemicolon = typeWithoutDimensions.substring(1, typeWithoutDimensions.length() - 1);
                return nameWithoutLAndSemicolon.substring(nameWithoutLAndSemicolon.lastIndexOf("/") + 1, nameWithoutLAndSemicolon.length());
        }
    }

    @NotNull
    static Set<MethodConfig> findIncludingConfigs(Set<MethodConfig> includingConfigs,
                                                  @NotNull MethodConfig methodConfig) {
        Set<MethodConfig> finalConfigs = new TreeSet<>();
        for (MethodConfig includingConfig : includingConfigs) {
            if (includingConfig.isApplicableTo(methodConfig)) {
                finalConfigs.add(includingConfig);
            }
        }
        return finalConfigs;
    }

    static void setSaveParameters(@NotNull MethodConfig trueMethodConfig,
                                  @NotNull Set<MethodConfig> methodConfigs) {
        for (MethodConfig methodConfig : methodConfigs) {
            mergeSavingParameters(trueMethodConfig.getParameters(), methodConfig.getParameters());
            if (methodConfig.isSaveReturnValue()) {
                trueMethodConfig.setSaveReturnValue(true);
            }
        }
    }

    private static void mergeSavingParameters(List<MethodConfig.Parameter> toParameters,
                                              @NotNull List<MethodConfig.Parameter> fromParameters) {
        for (int i = 0; i < fromParameters.size(); i++) {
            MethodConfig.Parameter fromParameter = fromParameters.get(i);
            if (Objects.equals(fromParameter.getType(), "*")) {
                if (fromParameter.isEnabled()) {
                    setAllEnabledFrom(toParameters, i);
                }
                break;
            }
            if (fromParameter.isEnabled()) {
                toParameters.get(i).setEnabled(true);
            }
        }
    }

    private static void setAllEnabledFrom(List<MethodConfig.Parameter> toParameters, int from) {
        for (int i = from; i < toParameters.size(); i++) {
            toParameters.get(i).setEnabled(true);
        }
    }

    @NotNull
    Set<MethodConfig> findIncludingConfigs(String className) {
        className = className.replaceAll("/", ".");
        Set<MethodConfig> applicableMethodConfigs = new TreeSet<>();
        for (MethodConfig methodConfig : configuration.getIncludingMethodConfigs()) {
            if (methodConfig.isApplicableTo(className)) {
                applicableMethodConfigs.add(methodConfig);
            }
        }
        return applicableMethodConfigs;
    }

    boolean isMethodExcluded(MethodConfig methodConfig) {
        return configuration.getExcludingConfigs(methodConfig).size() != 0;
    }
}
