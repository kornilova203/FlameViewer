package com.github.kornilova_l.flamegraph.javaagent.generate.test_classes;

import com.github.kornilova_l.flamegraph.javaagent.logger.LoggerQueue;
import com.github.kornilova_l.flamegraph.proxy.StartData;

public class ThrowsExceptionExpected {
    public static void main(String[] args) {
        StartData startData = new StartData(System.currentTimeMillis(), null);
        try {
            startData.setDuration(System.currentTimeMillis());
            startData.setThrownByMethod();
            if (startData.getDuration() > 1) {
                LoggerQueue.addToQueue(null,
                        false,
                        startData.getStartTime(),
                        startData.getDuration(),
                        startData.getParameters(),
                        Thread.currentThread(),
                        "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/ThrowsException",
                        "main",
                        "([Ljava/lang/String;)V",
                        true,
                        ""
                );
            }
        throw new AssertionError("error");
        } catch (Throwable throwable) {
            if (!startData.isThrownByMethod()) {
                startData.setDuration(System.currentTimeMillis());
                if (startData.getDuration() > 1) {
                    LoggerQueue.addToQueue(throwable,
                            false,
                            startData.getStartTime(),
                            startData.getDuration(),
                            startData.getParameters(),
                            Thread.currentThread(),
                            "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/ThrowsException",
                            "main",
                            "([Ljava/lang/String;)V",
                            true,
                            ""
                    );
                }
            }
            throw throwable;
        }
    }
}
