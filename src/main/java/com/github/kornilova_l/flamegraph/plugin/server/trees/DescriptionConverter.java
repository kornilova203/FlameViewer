package com.github.kornilova_l.flamegraph.plugin.server.trees;

import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

import static com.github.kornilova_l.flamegraph.configuration.MethodConfig.splitDesc;

public class DescriptionConverter {
    /**
     * @param fullDesc file specific description
     * @return (Ljava...lang...String...Z)V -> (String, boolean)void
     */
    @NotNull
    public static String getBeautifulDesc(String fullDesc) {
        return getBeautifulParameters(fullDesc) +
                getBeautifulReturnValue(fullDesc);
    }

    @NotNull
    public static String getBeautifulParameters(String fullDescription) {
        List<String> jvmParams = splitDesc(fullDescription.substring(1, fullDescription.indexOf(")")));
        List<String> parameters = jvmParams.stream()
                .map(parameter -> {
                    int lastArr = parameter.lastIndexOf('[');
                    lastArr++;
                    StringBuilder parameterBuilder = new StringBuilder(
                            MethodConfig.jvmTypeToParam(parameter.substring(lastArr, parameter.length()))
                    );
                    for (int i = 0; i < lastArr; i++) {
                        parameterBuilder.append("[]");
                    }
                    parameter = parameterBuilder.toString();
                    return parameter;
                })
                .collect(Collectors.toList());

        return "(" +
                String.join(", ", parameters) +
                ")";
    }

    @NotNull
    public static String getBeautifulReturnValue(String fullDesc) {
        String jvmRetVal = fullDesc.substring(fullDesc.indexOf(")") + 1, fullDesc.length());
        int lastArr = jvmRetVal.lastIndexOf('[');
        lastArr++;
        StringBuilder parameterBuilder = new StringBuilder(
                MethodConfig.jvmTypeToParam(jvmRetVal.substring(lastArr, jvmRetVal.length()))
        );
        for (int i = 0; i < lastArr; i++) {
            parameterBuilder.append("[]");
        }
        return parameterBuilder.toString();
    }
}
