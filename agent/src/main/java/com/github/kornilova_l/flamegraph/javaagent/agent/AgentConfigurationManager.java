package com.github.kornilova_l.flamegraph.javaagent.agent;

import com.github.kornilova_l.flamegraph.configuration.Configuration;
import com.github.kornilova_l.flamegraph.configuration.MethodConfig;

import java.util.List;

class AgentConfigurationManager {
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
                    classAndMethod.indexOf(".") + 1,
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
//
//    @NotNull
//    static List<MethodConfig> findIncludingConfigs(String className) {
//        return config.findIncludingConfigs(className.replaceAll("/", "."));
//    }
//
//    public static boolean isMethodExcluded(String className, String methodName, String desc) {
//        return config.isMethodExcluded(className.replaceAll("/", "."), methodName, desc);
//    }
//
//    @NotNull
//    public static List<MethodConfig> findIncludingConfigs(List<MethodConfig> methodConfigs,
//                                                          String methodName,
//                                                          String jvmDescPart) {
//        LinkedList<MethodConfig> applicableConfigs = new LinkedList<>();
//        for (MethodConfig methodConfig : methodConfigs) {
//            if (methodConfig.isApplicableTo(methodName, jvmDescPart)) {
//                applicableConfigs.add(methodConfig);
//            }
//        }
//        return applicableConfigs;
//    }
}
