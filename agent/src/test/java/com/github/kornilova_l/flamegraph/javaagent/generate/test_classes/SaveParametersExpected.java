package com.github.kornilova_l.flamegraph.javaagent.generate.test_classes;

import com.github.kornilova_l.flamegraph.javaagent.logger.LoggerQueue;
import com.github.kornilova_l.flamegraph.proxy.StartData;

import java.util.ArrayList;

public class SaveParametersExpected {
    @SuppressWarnings("unused")
    void noParams() {
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
                        "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/SaveParameters",
                        "noParams",
                        "()V",
                        false,
                        "");
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
                            "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/SaveParameters",
                            "noParams",
                            "()V",
                            false,
                            "");
                }
            }
            throw throwable;
        }
    }

    @SuppressWarnings("unused")
    void oneParam(int i) {
        StartData startData = LoggerQueue.createStartData(System.currentTimeMillis(), new Object[]{i});
        try {
            System.out.println("Hello, world!");
            startData.setDuration(System.currentTimeMillis());
            if (startData.getDuration() > 1) {
                LoggerQueue.addToQueue(null,
                        startData.getStartTime(),
                        startData.getDuration(),
                        startData.getParameters(),
                        Thread.currentThread(),
                        "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/SaveParameters",
                        "oneParam",
                        "(I)V",
                        false,
                        "1");
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
                            "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/SaveParameters",
                            "oneParam",
                            "(I)V",
                            false,
                            "1");
                }
            }
            throw throwable;
        }
    }

    @SuppressWarnings("unused")
    void twoParams(int i, ArrayList<String> list) {
        StartData startData = LoggerQueue.createStartData(System.currentTimeMillis(), new Object[]{i, list});
        try {
            System.out.println("Hello, world!");
            startData.setDuration(System.currentTimeMillis());
            if (startData.getDuration() > 1) {
                LoggerQueue.addToQueue(null,
                        startData.getStartTime(),
                        startData.getDuration(),
                        startData.getParameters(),
                        Thread.currentThread(),
                        "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/SaveParameters",
                        "twoParams",
                        "(ILjava/util/ArrayList;)V",
                        false,
                        "1,2");
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
                            "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/SaveParameters",
                            "twoParams",
                            "(ILjava/util/ArrayList;)V",
                            false,
                            "1,2");
                }
            }
            throw throwable;
        }
    }

    @SuppressWarnings("unused")
    static void threeParams(boolean b, long l, String s) {
        StartData startData = LoggerQueue.createStartData(System.currentTimeMillis(), new Object[]{b, l, s});
        try {
            System.out.println("Hello, world!");
            startData.setDuration(System.currentTimeMillis());
            if (startData.getDuration() > 1) {
                LoggerQueue.addToQueue(null,
                        startData.getStartTime(),
                        startData.getDuration(),
                        startData.getParameters(),
                        Thread.currentThread(),
                        "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/SaveParameters",
                        "threeParams",
                        "(ZJLjava/lang/String;)V",
                        true,
                        "1,2,3");
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
                            "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/SaveParameters",
                            "threeParams",
                            "(ZJLjava/lang/String;)V",
                            true,
                            "1,2,3");
                }
            }
            throw throwable;
        }
    }
}
