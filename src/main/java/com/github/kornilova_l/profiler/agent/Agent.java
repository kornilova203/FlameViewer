package com.github.kornilova_l.profiler.agent;

import java.io.IOException;
import java.lang.instrument.Instrumentation;

import com.github.kornilova_l.profiler.logger.Logger;
import com.github.kornilova_l.profiler.logger.WaitingLoggingToFinish;

public class Agent {

    public static void premain(String configFile, Instrumentation inst) throws IOException {
        System.out.println("I am an agent!");
        Thread logger = new Thread(new Logger(), "logging thread");
        logger.setDaemon(true);
        logger.start();

        Runtime.getRuntime().addShutdownHook( new WaitingLoggingToFinish("shutdown-hook"));
        inst.addTransformer(new ProfilingClassFileTransformer(configFile));
    }
}

