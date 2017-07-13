package com.github.kornilova_l.profiler.agent;

import com.github.kornilova_l.config.ConfigStorage;
import com.github.kornilova_l.config.MethodConfig;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

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
        System.out.println("Configuration:");
        System.out.println("Including methods: " + config.includingMethodConfigs);
        System.out.println("Excluding methods: " + config.excludingMethodConfigs);
    }

    @NotNull
    static List<MethodConfig> findIncludingConfigs(String className) {
        return config.findIncludingConfigs(className.replaceAll("/", "."));
    }

    public static boolean isMethodExcluded(String className, String methodName, String desc) {
        return config.isMethodExcluded(className.replaceAll("/", "."), methodName, desc);
    }

    @NotNull
    public static List<MethodConfig> findIncludingConfigs(List<MethodConfig> methodConfigs,
                                                          String methodName,
                                                          String jvmDescPart) {
        LinkedList<MethodConfig> applicableConfigs = new LinkedList<>();
        for (MethodConfig methodConfig : methodConfigs) {
            if (methodConfig.isApplicableTo(methodName, jvmDescPart)) {
                applicableConfigs.add(methodConfig);
            }
        }
        return applicableConfigs;
    }
}
