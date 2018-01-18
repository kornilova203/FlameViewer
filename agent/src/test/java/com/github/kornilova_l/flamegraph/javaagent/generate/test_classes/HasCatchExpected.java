package com.github.kornilova_l.flamegraph.javaagent.generate.test_classes;

import com.github.kornilova_l.flamegraph.javaagent.logger.LoggerQueue;
import com.github.kornilova_l.flamegraph.proxy.StartData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class HasCatchExpected {
    public static void main(String[] args) {
        StartData startData = new StartData(System.currentTimeMillis(), null);
        try {
            try (OutputStream outputStream = new FileOutputStream(new File(""))) {
                outputStream.write(new byte[0]);
            } catch (IOException e) {
                e.printStackTrace();
            }
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
                            "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/HasCatch",
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
