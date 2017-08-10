package com.github.kornilova_l.flamegraph.javaagent.generate.test_classes;

import java.util.ArrayList;

public class SaveParameters {
    void noParams() {
//        StartData startData = LoggerQueue.createStartData(System.currentTimeMillis(), null);
        System.out.println("Hello, world!");
//        startData.setDuration(System.currentTimeMillis());
//        if (startData.getDuration() > 1) {
//            LoggerQueue.addToQueue(null,
//                    startData,
//                    Thread.currentThread(),
//                    "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/SaveParameters",
//                    "noParams",
//                    "()V",
//                    false);
//        }
    }

    void oneParam(int i) {
//        StartData startData = LoggerQueue.createStartData(System.currentTimeMillis(), new Object[]{i});
        System.out.println("Hello, world!");
//        startData.setDuration(System.currentTimeMillis());
//        if (startData.getDuration() > 1) {
//            LoggerQueue.addToQueue(null,
//                    startData,
//                    Thread.currentThread(),
//                    "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/SaveParameters",
//                    "oneParam",
//                    "(I)V",
//                    false);
//        }
    }

    void twoParams(int i, ArrayList<String> list) {
//        StartData startData = LoggerQueue.createStartData(System.currentTimeMillis(), new Object[]{i, list});
        System.out.println("Hello, world!");
//        startData.setDuration(System.currentTimeMillis());
//        if (startData.getDuration() > 1) {
//            LoggerQueue.addToQueue(null,
//                    startData,
//                    Thread.currentThread(),
//                    "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/SaveParameters",
//                    "twoParams",
//                    "(Ljava/lang/ArrayList;)V",
//                    false);
//        }
    }

    static void threeParams(boolean b, long l, String s) {
//        StartData startData = LoggerQueue.createStartData(System.currentTimeMillis(), new Object[]{b, l, s});
        System.out.println("Hello, world!");
//        startData.setDuration(System.currentTimeMillis());
//        if (startData.getDuration() > 1) {
//            LoggerQueue.addToQueue(null,
//                    startData,
//                    Thread.currentThread(),
//                    "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/SaveParameters",
//                    "threeParams",
//                    "(ZJLjava/lang/String;)V",
//                    true);
//        }
    }
}
