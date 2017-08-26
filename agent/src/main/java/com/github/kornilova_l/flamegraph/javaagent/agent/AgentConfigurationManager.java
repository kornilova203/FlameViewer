package com.github.kornilova_l.flamegraph.javaagent.agent;

import com.github.kornilova_l.flamegraph.configuration.Configuration;
import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static com.github.kornilova_l.flamegraph.configuration.MethodConfig.jvmTypeToParam;
import static com.github.kornilova_l.flamegraph.configuration.MethodConfig.splitDesc;

class AgentConfigurationManager {
    private final Configuration configuration = new Configuration();

    AgentConfigurationManager(List<String> methodConfigLines) {
        for (String methodConfigLine : methodConfigLines) {
            boolean isExcluding = methodConfigLine.charAt(0) == '!';
            if (isExcluding) {
                methodConfigLine = methodConfigLine.substring(1, methodConfigLine.length());
            }
            configuration.addMethodConfig(methodConfigLine, isExcluding);
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
    static List<MethodConfig> findIncludingConfigs(List<MethodConfig> includingConfigs,
                                                   @NotNull MethodConfig methodConfig) {
        List<MethodConfig> finalConfigs = new ArrayList<>();
        for (MethodConfig includingConfig : includingConfigs) {
            if (includingConfig.isApplicableTo(methodConfig)) {
                finalConfigs.add(includingConfig);
            }
        }
        return finalConfigs;
    }

    static void setSaveParameters(@NotNull MethodConfig trueMethodConfig,
                                  @NotNull List<MethodConfig> methodConfigs) {
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
    List<MethodConfig> findIncludingConfigs(String className) {
        className = className.replace('/', '.');
        List<MethodConfig> applicableMethodConfigs = new ArrayList<>();
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
