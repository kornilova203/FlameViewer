package com.github.kornilova_l.flamegraph.javaagent.agent;

import com.github.kornilova_l.flamegraph.javaagent.AgentFileManager;
import com.github.kornilova_l.flamegraph.javaagent.logger.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public class Agent {

    public static void premain(String config, Instrumentation inst) throws IOException {
        System.out.println("I am an agent!");
        String[] parameters = config.split("&");
        List<String> methods = getMethodsList(new File(parameters[1]));
        if (methods == null) {
            return;
        }
        Logger.init(new AgentFileManager(parameters[0]));
        AgentConfigurationManager.readMethods(methods);
        inst.addTransformer(new ProfilingClassFileTransformer());
    }

    @Nullable
    private static List<String> getMethodsList(File file) {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            return bufferedReader.lines().filter((line) -> !Objects.equals(line, "")).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}

