package com.github.kornilova_l.profiler;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import org.objectweb.asm.*;
import org.objectweb.asm.util.TraceClassVisitor;

public class Agent {

    public static void premain(String configFile, Instrumentation inst) throws IOException {
        Thread logger = new Thread(new Logger(), "logging thread");
        logger.setDaemon(true);
        logger.start();

        Runtime.getRuntime().addShutdownHook( new WaitingLoggingToFinish("shutdown-hook"));
        inst.addTransformer(new ProfilingClassFileTransformer(configFile));
    }
}

