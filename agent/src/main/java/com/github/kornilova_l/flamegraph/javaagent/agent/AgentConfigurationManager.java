package com.github.kornilova_l.flamegraph.javaagent.agent;

import com.github.kornilova_l.flamegraph.configuration.Configuration;
import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class AgentConfigurationManager {
    private final static Pattern paramsPattern = Pattern.compile("(\\[?)(C|Z|S|I|J|F|D|B|(:?L[^;]+;))");
    private static final Configuration configuration = new Configuration();

    static void readMethods(List<String> methodConfigLines) {
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
    static List<MethodConfig> findIncludingConfigs(String className) {
        List<MethodConfig> applicableMethodConfigs = new LinkedList<>();
        for (MethodConfig methodConfig : configuration.getIncludingMethodConfigs()) {
            if (methodConfig.isApplicableTo(className)) {
                applicableMethodConfigs.add(methodConfig);
            }
        }
        return applicableMethodConfigs;
    }

    @NotNull
    static MethodConfig newMethodConfig(@NotNull String className,
                                        @NotNull String methodName,
                                        @NotNull String desc) {
        String descInnerPart = desc.substring(desc.indexOf("(") + 1, desc.indexOf(")"));
        return new MethodConfig(className.replaceAll("/", "."),
                methodName,
                Objects.equals(descInnerPart, "") ?
                        "()" :
                        descToParametersPattern(descInnerPart));
    }

    @NotNull
    private static String descToParametersPattern(@NotNull String descInnerPart) {
        Matcher m = paramsPattern.matcher(descInnerPart);
        LinkedList<String> params = new LinkedList<>();
        while (m.find()) {
            StringBuilder paramBuilder = new StringBuilder();
            int dimensions = countDimensions(m.group());
            paramBuilder.append(
                    jvmTypeToParam(m.group().substring(dimensions, m.group().length()))
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
                return "Byte";
            case "F":
                return "float";
            case "D":
                return "double";
            default:
                return typeWithoutDimensions.substring(1, typeWithoutDimensions.length() - 1)
                        .replaceAll("/", ".");
        }
    }

    public static boolean isMethodExcluded(MethodConfig methodConfig) {
        return configuration.getExcludingConfigs(methodConfig).size() != 0;
    }

    @NotNull
    public static List<MethodConfig> findIncludingConfigs(@NotNull List<MethodConfig> includingConfigs,
                                                          @NotNull MethodConfig methodConfig) {
        System.out.println("findIncludingConfigs for " + methodConfig);
        List<MethodConfig> finalConfigs = new LinkedList<>();
        for (MethodConfig includingConfig : includingConfigs) {
            System.out.println("check: " + includingConfig);
            if (includingConfig.isApplicableTo(methodConfig)) {
                System.out.println("not");
                finalConfigs.add(includingConfig);
            }
        }
        return finalConfigs;
    }
}
