package com.github.kornilova_l.flamegraph.plugin.server.trees;

import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

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
    private static String getBeautifulParameters(String fullDescription) {
        List<String> jvmParams = splitDesc(fullDescription.substring(1, fullDescription.indexOf(")")));
        List<String> parameters = new ArrayList<>();
        for (String jvmParam : jvmParams) {
            int lastArr = jvmParam.lastIndexOf('[');
            lastArr++;
            StringBuilder parameterBuilder = new StringBuilder(
                    MethodConfig.jvmTypeToParam(jvmParam.substring(lastArr, jvmParam.length()))
            );
            for (int i = 0; i < lastArr; i++) {
                parameterBuilder.append("[]");
            }
            jvmParam = parameterBuilder.toString();
            parameters.add(jvmParam);
        }
        return "(" +
                String.join(", ", parameters) +
                ")";
    }

    @NotNull
    private static String getBeautifulReturnValue(String fullDesc) {
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
