package com.github.kornilova_l.flamegraph.javaagent.generate.test_classes;

import com.github.kornilova_l.flamegraph.javaagent.logger.LoggerQueue;
import com.github.kornilova_l.flamegraph.proxy.StartData;

public class HasCatchExpected {
    public static void main(String[] args) {
        StartData startData = LoggerQueue.createStartData(System.currentTimeMillis(), null);
        try {
            startData.setThrownByMethod();
            startData.setDuration(System.currentTimeMillis());
            if (startData.getDuration() > 1) {
                LoggerQueue.addToQueue(null,
                        startData.getStartTime(),
                        startData.getDuration(),
                        startData.getParameters(),
                        Thread.currentThread(),
                        "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/HasCatch",
                        "main",
                        "([Ljava/lang/String;)V",
                        true,
                        ""
                );
            }
            try {
                startData.setDuration(System.currentTimeMillis());
                if (startData.getDuration() > 1) {
                    LoggerQueue.addToQueue(null,
                            false,
                            startData.getStartTime(),
                            startData.getDuration(),
                            startData.getParameters(),
                            Thread.currentThread(),
                            "HasCatch",
                            "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/HasCatch",
                            "main",
                            true,
                            "([Ljava/lang/String;)V");
                }
                throw new AssertionError("");
            } catch (Error throwable) {
                System.out.println("Normal");
            }
        } catch (Throwable throwable) {
            if (!startData.isThrownByMethod()) {
                startData.setDuration(System.currentTimeMillis());
                if (startData.getDuration() > 1) {
                    LoggerQueue.addToQueue(null,
                            false,
                            startData.getStartTime(),
                            startData.getDuration(),
                            startData.getParameters(),
                            Thread.currentThread(),
                            "HasCatch",
                            "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/HasCatch",
                            "main",
                            true,
                            "([Ljava/lang/String;)V");
                }
            }
            throw throwable;
        }
    }
}
