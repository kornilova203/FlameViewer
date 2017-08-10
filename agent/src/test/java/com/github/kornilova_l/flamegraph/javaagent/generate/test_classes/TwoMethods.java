package com.github.kornilova_l.flamegraph.javaagent.generate.test_classes;

import java.util.Random;

public class TwoMethods {
    public static void main(String[] args) {
//        StartData startData = LoggerQueue.createStartData(System.currentTimeMillis(), null);
//        try {
        String res = new TwoMethods().returnString();
        System.out.println(res);
//            startData.setDuration(System.currentTimeMillis());
//            if (startData.getDuration() > 1) {
//                LoggerQueue.addToQueue(null,
//                        startData,
//                        Thread.currentThread(),
//                        "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/TwoMethods",
//                        "main",
//                        "([Ljava/lang/String;)V",
//                        true);
//            }
//        } catch (Throwable throwable) {
//            if (!startData.isThrownByMethod()) {
//                startData.setDuration(System.currentTimeMillis());
//                if (startData.getDuration() > 1) {
//                    LoggerQueue.addToQueue(null,
//                            startData,
//                            Thread.currentThread(),
//                            "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/TwoMethods",
//                            "main",
//                            false,
//                            "([Ljava/lang/String;)V");
//                }
//            }
//            throw throwable;
//        }
    }

    private String returnString() {
//        StartData startData = LoggerQueue.createStartData(System.currentTimeMillis(), null);
//        try {
        Random random = new Random(System.currentTimeMillis());
        if (!random.nextBoolean()) {
//                startData.setDuration(System.currentTimeMillis());
//                if (startData.getDuration() > 1) {
//                    LoggerQueue.addToQueue(null,
//                            startData,
//                            Thread.currentThread(),
//                            "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/TwoMethods",
//                            "returnString",
//                            "()Ljava/lang/String;",
//                            false);
//                }
            return null;
        }
//            startData.setDuration(System.currentTimeMillis());
//            if (startData.getDuration() > 1) {
//                LoggerQueue.addToQueue(null,
//                        startData,
//                        Thread.currentThread(),
//                        "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/TwoMethods",
//                        "returnString",
//                        "()Ljava/lang/String;",
//                        false);
//            }
        return "Lucinda";
//        } catch (Throwable throwable) {
//            if (!startData.isThrownByMethod()) {
//                startData.setDuration(System.currentTimeMillis());
//                if (startData.getDuration() > 1) {
//                    LoggerQueue.addToQueue(null,
//                            startData,
//                            Thread.currentThread(),
//                            "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/TwoMethods",
//                            "returnString",
//                            false,
//                            "()Ljava/lang/String;");
//                }
//            }
//            throw throwable;
//        }
    }
}
