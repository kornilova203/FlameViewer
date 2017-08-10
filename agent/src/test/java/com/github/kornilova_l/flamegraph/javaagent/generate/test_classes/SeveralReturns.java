package com.github.kornilova_l.flamegraph.javaagent.generate.test_classes;

import java.util.Random;

public class SeveralReturns {
    String returnString() {
//        StartData startData = LoggerQueue.createStartData(System.currentTimeMillis(), null);
//        try {
        Random random = new Random(System.currentTimeMillis());
        if (random.nextBoolean()) {
//                startData.setDuration(System.currentTimeMillis());
//                if (startData.getDuration() > 1) {
//                    LoggerQueue.addToQueue(null,
//                            startData,
//                            Thread.currentThread(),
//                            "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/SeveralReturns",
//                            "returnString",
//                            "()Ljava/lang/String;",
//                            false);
//                }
            return "Hello";
        }
        if (!random.nextBoolean()) {
//                startData.setDuration(System.currentTimeMillis());
//                if (startData.getDuration() > 1) {
//                    LoggerQueue.addToQueue(null,
//                            startData,
//                            Thread.currentThread(),
//                            "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/SeveralReturns",
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
//                        "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/SeveralReturns",
//                        "returnString",
//                        "()Ljava/lang/String;",
//                        false);
//            }
        return "Lucinda";
//        } catch (Throwable throwable) {
//            startData.setDuration(System.currentTimeMillis());
//            if (startData.getDuration() > 1) {
//                LoggerQueue.addToQueue(null,
//                        startData,
//                        Thread.currentThread(),
//                        "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/SeveralReturns",
//                        "returnString",
//                        false,
//                        "()Ljava/lang/String;");
//            }
//            throw throwable;
//        }
    }
}
