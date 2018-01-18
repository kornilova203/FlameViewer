package com.github.kornilova_l.flamegraph.javaagent.generate.test_classes;

import com.github.kornilova_l.flamegraph.javaagent.logger.LoggerQueue;
import com.github.kornilova_l.flamegraph.proxy.StartData;

import java.util.ArrayList;

public class SaveReturnValueExpected {
    @SuppressWarnings("unused")
    static ArrayList<String> returnGeneric() {
        StartData startData = new StartData(System.currentTimeMillis(), null);
        try {
            System.out.println("Hello, world!");
            ArrayList<String> list = new ArrayList<>();
            startData.setDuration(System.currentTimeMillis());
            if (startData.getDuration() > 1) {
                LoggerQueue.addToQueue(list,
                        startData.getStartTime(),
                        startData.getDuration(),
                        startData.getParameters(),
                        Thread.currentThread(),
                        "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/SaveReturnValue",
                        "returnGeneric",
                        "()Ljava/util/ArrayList;",
                        true,
                        ""
                );
            }
            return list;
        } catch (Throwable t) {
            if (!startData.isThrownByMethod()) {
                startData.setDuration(System.currentTimeMillis());
                if (startData.getDuration() > 1) {
                    LoggerQueue.addToQueue(t,
                            true,
                            startData.getStartTime(),
                            startData.getDuration(),
                            startData.getParameters(),
                            Thread.currentThread(),
                            "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/SaveReturnValue",
                            "returnGeneric",
                            "()Ljava/util/ArrayList;",
                            true,
                            ""
                    );
                }
            }
            throw t;
        }

    }

    @SuppressWarnings("unused")
    int returnInt() {
        StartData startData = new StartData(System.currentTimeMillis(), null);
        try {
            System.out.println("Hello, world!");
            startData.setDuration(System.currentTimeMillis());
            if (startData.getDuration() > 1) {
                LoggerQueue.addToQueue(23,
                        startData.getStartTime(),
                        startData.getDuration(),
                        startData.getParameters(),
                        Thread.currentThread(),
                        "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/SaveReturnValue",
                        "returnInt",
                        "()I",
                        false,
                        ""
                );
            }
            return 23;
        } catch (Throwable t) {
            if (!startData.isThrownByMethod()) {
                startData.setDuration(System.currentTimeMillis());
                if (startData.getDuration() > 1) {
                    LoggerQueue.addToQueue(t,
                            true,
                            startData.getStartTime(),
                            startData.getDuration(),
                            startData.getParameters(),
                            Thread.currentThread(),
                            "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/SaveReturnValue",
                            "returnInt",
                            "()I",
                            false,
                            ""
                    );
                }
            }
            throw t;
        }
    }

    @SuppressWarnings("unused")
    void returnVoid(int i) {
        StartData startData = new StartData(System.currentTimeMillis(), null);
        try {
            System.out.println("Hello, world!");
            startData.setDuration(System.currentTimeMillis());
            if (startData.getDuration() > 1) {
                LoggerQueue.addToQueue(null,
                        startData.getStartTime(),
                        startData.getDuration(),
                        startData.getParameters(),
                        Thread.currentThread(),
                        "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/SaveReturnValue",
                        "returnVoid",
                        "(I)V",
                        false,
                        ""
                );
            }
        } catch (Throwable t) {
            if (!startData.isThrownByMethod()) {
                startData.setDuration(System.currentTimeMillis());
                if (startData.getDuration() > 1) {
                    LoggerQueue.addToQueue(t,
                            true,
                            startData.getStartTime(),
                            startData.getDuration(),
                            startData.getParameters(),
                            Thread.currentThread(),
                            "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/SaveReturnValue",
                            "returnVoid",
                            "(I)V",
                            false,
                            ""
                    );
                }
            }
            throw t;
        }
    }

    @SuppressWarnings("unused")
    String returnString() {
        StartData startData = new StartData(System.currentTimeMillis(), null);
        try {
            System.out.println("Hello, world!");
            String hello = "hello";
            startData.setDuration(System.currentTimeMillis());
            if (startData.getDuration() > 1) {
                LoggerQueue.addToQueue(hello,
                        startData.getStartTime(),
                        startData.getDuration(),
                        startData.getParameters(),
                        Thread.currentThread(),
                        "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/SaveReturnValue",
                        "returnString",
                        "()Ljava/lang/String;",
                        false,
                        ""
                );
            }
            return hello;
        } catch (Throwable t) {
            if (!startData.isThrownByMethod()) {
                startData.setDuration(System.currentTimeMillis());
                if (startData.getDuration() > 1) {
                    LoggerQueue.addToQueue(t,
                            true,
                            startData.getStartTime(),
                            startData.getDuration(),
                            startData.getParameters(),
                            Thread.currentThread(),
                            "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/SaveReturnValue",
                            "returnString",
                            "()Ljava/lang/String;",
                            false,
                            ""
                    );
                }
            }
            throw t;
        }
    }

    @SuppressWarnings("unused")
    long returnLong() {
        StartData startData = new StartData(System.currentTimeMillis(), null);
        try {
            System.out.println("Hello, world!");
            startData.setDuration(System.currentTimeMillis());
            if (startData.getDuration() > 1) {
                LoggerQueue.addToQueue(32,
                        startData.getStartTime(),
                        startData.getDuration(),
                        startData.getParameters(),
                        Thread.currentThread(),
                        "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/SaveReturnValue",
                        "returnLong",
                        "()J",
                        false,
                        ""
                );
            }
            return 32;
        } catch (Throwable t) {
            if (!startData.isThrownByMethod()) {
                startData.setDuration(System.currentTimeMillis());
                if (startData.getDuration() > 1) {
                    LoggerQueue.addToQueue(t,
                            true,
                            startData.getStartTime(),
                            startData.getDuration(),
                            startData.getParameters(),
                            Thread.currentThread(),
                            "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/SaveReturnValue",
                            "returnLong",
                            "()J",
                            false,
                            ""
                    );
                }
            }
            throw t;
        }
    }
}
