package com.github.kornilova_l.profiler.agent;

import java.io.IOException;
import java.lang.instrument.Instrumentation;

public class Agent {

    public static void premain(String configFile, Instrumentation inst) throws IOException {
        System.out.println("I am an agent!");

        inst.addTransformer(new ProfilingClassFileTransformer(configFile));
    }
}

