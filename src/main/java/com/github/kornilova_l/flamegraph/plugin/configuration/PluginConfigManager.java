package com.github.kornilova_l.flamegraph.plugin.configuration;

import com.github.kornilova_l.flamegraph.configuration.Configuration;
import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PluginConfigManager {
    private static final Map<Project, Configuration> states = new HashMap<>();

    public static Configuration getConfiguration(@NotNull Project project) {
        return states.computeIfAbsent(
                project,
                k -> ((ConfigStorage) project.getComponent(PersistentStateComponent.class)).getState());
    }

    public static void exportConfig(@NotNull File file, @NotNull Configuration configuration) {
        try (OutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(configuration.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @NotNull
    public static MethodConfig newMethodConfig(@NotNull String classPattern,
                                               @NotNull String methodPattern,
                                               @NotNull String parametersPattern) {
        boolean saveReturnValue = parametersPattern.charAt(parametersPattern.length() - 1) == '+';
        List<MethodConfig.Parameter> parameters = parametersPatternToList(parametersPattern);
        return new MethodConfig(classPattern, methodPattern, parameters, true, saveReturnValue);
    }

    @NotNull
    private static List<MethodConfig.Parameter> parametersPatternToList(@NotNull String parametersPattern) {
        LinkedList<MethodConfig.Parameter> parameters = new LinkedList<>();
        parametersPattern = parametersPattern.substring(1, parametersPattern.lastIndexOf(")"));
        String[] stringParameters = parametersPattern.split(" *, *");
        for (String stringParameter : stringParameters) {
            parameters.addLast(new MethodConfig.Parameter(stringParameter, true));
        }
        return parameters;
    }
}
