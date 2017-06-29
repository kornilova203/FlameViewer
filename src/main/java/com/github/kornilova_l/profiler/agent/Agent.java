package com.github.kornilova_l.profiler.agent;

import com.github.kornilova_l.profiler.logger.Logger;

import java.io.IOException;
import java.lang.instrument.Instrumentation;

public class Agent {

    public static void premain(String configFile, Instrumentation inst) throws IOException {
        System.out.println("I am an agent!");
        Logger.getInstance();

        inst.addTransformer(new ProfilingClassFileTransformer(configFile));
    }
}

