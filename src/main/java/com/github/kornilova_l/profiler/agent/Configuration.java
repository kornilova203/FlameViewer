package com.github.kornilova_l.profiler.agent;

import com.github.kornilova_l.config.MethodConfig;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

class Configuration {
    private static List<MethodConfig> methodConfigs = new LinkedList<>();
    private static final ArrayList<Pattern> fullNamePatterns = new ArrayList<>();
    private static final ArrayList<Pattern> classNamePatterns = new ArrayList<>();
    private static final ArrayList<Pattern> excludePatterns = new ArrayList<>();

    static void addIncludePattern(String line) {
        String[] parts = line.split("\\.");
        addPattern(parts[0], classNamePatterns);
        addPattern(line.replace(".", "\\."), fullNamePatterns);
    }

    private static void readPatterns(List<String> parameters) {
        System.out.println("Config: " + parameters);
        for (String parameter : parameters) {
            if (parameter.startsWith("!")) {
                if (!Objects.equals(parameter, "!")) {
                    addExcludePattern(parameter.substring(1));
                }
            } else if (!Objects.equals(parameter, "")) {
                addIncludePattern(parameter);
            }
        }
    }

    static void readMethods(List<String> methodConfigLines) {
        for (String methodConfigLine : methodConfigLines) {
            methodConfigs.add(new MethodConfig(methodConfigLine));
        }
    }

    static void addExcludePattern(String line) {
        addPattern(line, excludePatterns);
    }

    private static void addPattern(String line, ArrayList<Pattern> patterns) {
        patterns.add(
                Pattern.compile(
                        line.replaceAll("\\*", ".*")
                )
        );
    }

    @Nullable
    static List<MethodConfig> findMethodsOfClass(String className) {
        List<MethodConfig> methodsOfClass = new LinkedList<>();
        for (MethodConfig methodConfig : methodConfigs) {
            if (Objects.equals(methodConfig.getJvmClassName(), className)) {
                methodsOfClass.add(methodConfig);
            }
        }
        if (methodsOfClass.size() == 0) {
            return null;
        }
        return methodsOfClass;
    }

    static boolean isClassIncluded(String className) {
        return matchesAnyPattern(className, classNamePatterns);
    }

    static boolean isMethodIncluded(String fullName) {
        return matchesAnyPattern(fullName, fullNamePatterns);
    }

    static boolean isMethodExcluded(String fullName) {
        return matchesAnyPattern(fullName, excludePatterns);
    }

    private static boolean matchesAnyPattern(String line, ArrayList<Pattern> patterns) {
        for (Pattern pattern : patterns) {
            if (pattern.matcher(line).matches()) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    public static MethodConfig getMethodIfPresent(List<MethodConfig> methodConfigs, String methodName, String desc) {
        for (MethodConfig methodConfig : methodConfigs) {
            if (Objects.equals(methodConfig.methodName, methodName) &&
                    desc.startsWith(MethodConfig.parametersToStringForJvm(methodConfig.parameters))) {
                return methodConfig;
            }
        }
        return null;
    }
}
