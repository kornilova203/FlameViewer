package com.github.kornilova_l.flamegraph.javaagent.generate.test_classes;

import com.github.kornilova_l.flamegraph.javaagent.logger.LoggerQueue;
import com.github.kornilova_l.flamegraph.proxy.StartData;

public class HasIfExpected {
    public static int main(int val) {
        StartData startData = new StartData(System.currentTimeMillis(), null);
        try {
            int res = 0;
            if (val > 0) {
                res++;
            }
            startData.setDuration(System.currentTimeMillis());
            if (startData.getDuration() > 1) {
                LoggerQueue.addToQueue(null,
                        startData.getStartTime(),
                        startData.getDuration(),
                        startData.getParameters(),
                        Thread.currentThread(),
                        "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/HasIf",
                        "main",
                        "(I)I",
                        true,
                        "");
            }
            return res;
        } catch (Throwable t) {
            if (!startData.isThrownByMethod()) {
                startData.setDuration(System.currentTimeMillis());
                if (startData.getDuration() > 1) {
                    LoggerQueue.addToQueue(t,
                            false,
                            startData.getStartTime(),
                            startData.getDuration(),
                            startData.getParameters(),
                            Thread.currentThread(),
                            "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/HasIf",
                            "main",
                            "(I)I",
                            true,
                            "");
                }
            }
            throw t;
        }
    }
}
