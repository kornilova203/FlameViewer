package com.github.kornilova_l.flamegraph.javaagent.agent;

import com.github.kornilova_l.flamegraph.javaagent.logger.Logger;
import com.github.kornilova_l.flamegraph.javaagent.logger.LoggerQueue;
import com.github.kornilova_l.flamegraph.javaagent.logger.WaitingLoggingToFinish;
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
        LoggerQueue.initLoggerQueue();
        String[] parameters = config.split("&");
        List<String> methods = getMethodsList(new File(parameters[1]));
        if (methods == null) {
            return;
        }
        createLogger(new File(parameters[0]));
        AgentConfigurationManager configurationManager = new AgentConfigurationManager(methods);
        inst.addTransformer(new ProfilingClassFileTransformer(configurationManager));
    }

    private static void createLogger(File logFile) {
        Logger logger = new Logger(logFile);

        Thread loggerThread = new Thread(logger, "logging thread");
        loggerThread.setDaemon(true);
        loggerThread.start();

        Runtime.getRuntime().addShutdownHook(new WaitingLoggingToFinish("shutdown-hook", logger));
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

