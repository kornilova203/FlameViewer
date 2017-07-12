package com.github.kornilova_l.profiler.agent;

import com.github.kornilova_l.config.ConfigStorage;
import com.github.kornilova_l.config.MethodConfig;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

class Configuration {
    private static final ConfigStorage.Config config = new ConfigStorage.Config();

    static void readMethods(List<String> methodConfigLines) {
        for (String methodConfigLine : methodConfigLines) {
            boolean isExcluding = methodConfigLine.charAt(0) == '!';
            if (isExcluding) {
                methodConfigLine = methodConfigLine.substring(1, methodConfigLine.length());
            }
            config.addMethodConfig(methodConfigLine, isExcluding);
        }
    }

    @NotNull
    static List<MethodConfig> findIncludingConfigs(String className) {
        return config.findIncludingConfigs(className.replaceAll("/", "."));
    }

    @Nullable
    public static MethodConfig getMethodIfPresent(List<MethodConfig> methodConfigs, String methodName, String desc) {
        for (MethodConfig methodConfig : methodConfigs) {
            if (Objects.equals(methodConfig.methodPatternString, methodName) &&
                    desc.startsWith(MethodConfig.parametersToStringForJvm(methodConfig.parameters))) {
                return methodConfig;
            }
        }
        return null;
    }

    public static boolean isMethodExcluded(String className, String methodName, String desc) {
        return config.isMethodExcluded(className.replaceAll("/", "."), methodName, desc);
    }

    @NotNull
    public static List<MethodConfig> findIncludingConfigs(List<MethodConfig> methodConfigs,
                                                          String methodName,
                                                          String jvmDesc) {
        return null;
    }
}
