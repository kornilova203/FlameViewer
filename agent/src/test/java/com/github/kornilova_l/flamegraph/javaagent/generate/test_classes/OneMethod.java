package com.github.kornilova_l.flamegraph.javaagent.generate.test_classes;


import com.github.kornilova_l.flamegraph.javaagent.logger.LoggerQueue;
import com.github.kornilova_l.flamegraph.javaagent.logger.event_data_storage.StartData;

public class OneMethod {
    public static int main(String[] args) {
        StartData startData = LoggerQueue.createStartData(System.currentTimeMillis(), null);
        try {
        System.out.println("Hello, world!");
            startData.setDuration(System.currentTimeMillis());
            if (startData.getDuration() > 1) {
                LoggerQueue.addToQueue(null,
                        startData.getStartTime(),
                        startData.getDuration(),
                        startData.getParameters(),
                        Thread.currentThread(),
                        "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/OneMethod",
                        "main",
                        "([Ljava/lang/String;)I",
                        true);
            }
        return 23;
        } catch (Throwable throwable) {
            if (!startData.isThrownByMethod()) {
                startData.setDuration(System.currentTimeMillis());
                if (startData.getDuration() > 1) {
                    LoggerQueue.addToQueue(null,
                            startData.getStartTime(),
                            startData.getDuration(),
                            startData.getParameters(),
                            Thread.currentThread(),
                            "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/OneMethod",
                            "main",
                            true,
                            "([Ljava/lang/String;)I");
                }
            }
            throw throwable;
        }
    }
}
