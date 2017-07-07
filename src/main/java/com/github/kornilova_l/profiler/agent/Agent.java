package com.github.kornilova_l.profiler.agent;

import com.github.kornilova_l.profiler.ProfilerFileManager;
import com.github.kornilova_l.profiler.logger.Logger;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.Arrays;

public class Agent {

    public static void premain(String config, Instrumentation inst) throws IOException {
        System.out.println("I am an agent!");
        String[] parameters = config.split("&");
        ProfilerFileManager.setPathToLogDir(parameters[0]);
        Logger.init();
        inst.addTransformer(
                new ProfilingClassFileTransformer(
                        Arrays.asList(parameters).subList(1, parameters.length)
                ));
    }
}

