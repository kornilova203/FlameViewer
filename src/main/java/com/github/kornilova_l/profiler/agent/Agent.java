package com.github.kornilova_l.profiler.agent;

import com.github.kornilova_l.profiler.ProfilerFileManager;
import com.github.kornilova_l.profiler.logger.Logger;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class Agent {

    public static void premain(String config, Instrumentation inst) throws IOException {
        System.out.println("I am an agent!");
        String[] parameters = config.split("&");
        List<String> methods = getMethodsList(new File(parameters[1]));
        if (methods == null) {
            return;
        }
        Logger.init(new ProfilerFileManager(parameters[0]));
        inst.addTransformer(
                new ProfilingClassFileTransformer(methods));
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

