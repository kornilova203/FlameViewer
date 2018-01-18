package com.github.kornilova_l.flamegraph.javaagent.generate.test_classes;

import java.util.ArrayList;

public class SaveSecondParam {
    void noParams() {
        System.out.println("Hello, world!");
    }

    void oneParam(long l) {
//        StartData startData = new StartData(System.currentTimeMillis(), null);
        System.out.println("Hello, world!");
//        startData.setDuration(System.currentTimeMillis());
//        if (startData.getDuration() > 1) {
//            LoggerQueue.addToQueue(null,
//                    startData,
//                    Thread.currentThread(),
//                    "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/SaveSecondParam",
//                    "oneParam",
//                    "(J)V",
//                    false);
//        }
    }

    void twoParams(long l, ArrayList<String> list) {
//        StartData startData = new StartData(System.currentTimeMillis(), new Object[]{list});
        System.out.println("Hello, world!");
//        startData.setDuration(System.currentTimeMillis());
//        if (startData.getDuration() > 1) {
//            LoggerQueue.addToQueue(null,
//                    startData,
//                    Thread.currentThread(),
//                    "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/SaveSecondParam",
//                    "twoParams",
//                    "(JLjava/util/ArrayList;)V",
//                    false);
//        }
    }

    static void threeParams(long l1, long l, String s) {
//        StartData startData = new StartData(System.currentTimeMillis(), new Object[]{l, s});
        System.out.println("Hello, world!");
//        startData.setDuration(System.currentTimeMillis());
//        if (startData.getDuration() > 1) {
//            LoggerQueue.addToQueue(null,
//                    startData,
//                    Thread.currentThread(),
//                    "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/SaveSecondParam",
//                    "threeParams",
//                    "(JJLjava/lang/String;)V",
//                    true);
//        }
    }
}
