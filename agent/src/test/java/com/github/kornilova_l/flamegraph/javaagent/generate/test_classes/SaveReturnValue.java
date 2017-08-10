package com.github.kornilova_l.flamegraph.javaagent.generate.test_classes;

import java.util.ArrayList;

public class SaveReturnValue {
    static ArrayList<String> returnGeneric() {
//        StartData startData = LoggerQueue.createStartData(System.currentTimeMillis(), null);
        System.out.println("Hello, world!");
        ArrayList<String> list = new ArrayList<>();
//        startData.setDuration(System.currentTimeMillis());
//        if (startData.getDuration() > 1) {
//            LoggerQueue.addToQueue(list,
//                    startData,
//                    Thread.currentThread(),
//                    "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/SaveReturnValue",
//                    "returnGeneric",
//                    "()Ljava/util/ArrayList;",
//                    true);
//        }
        return list;
    }

    int returnInt() {
//        StartData startData = LoggerQueue.createStartData(System.currentTimeMillis(), null);
        System.out.println("Hello, world!");
//        startData.setDuration(System.currentTimeMillis());
//        if (startData.getDuration() > 1) {
//            LoggerQueue.addToQueue(23,
//                    startData,
//                    Thread.currentThread(),
//                    "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/SaveReturnValue",
//                    "returnInt",
//                    "()I",
//                    false);
//        }
        return 23;
    }

    void returnVoid(int i) {
//        StartData startData = LoggerQueue.createStartData(System.currentTimeMillis(), null);
        System.out.println("Hello, world!");
//        startData.setDuration(System.currentTimeMillis());
//        if (startData.getDuration() > 1) {
//            LoggerQueue.addToQueue(null,
//                    startData,
//                    Thread.currentThread(),
//                    "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/SaveReturnValue",
//                    "returnVoid",
//                    "(I)V",
//                    false);
//        }
    }

    String returnString() {
//        StartData startData = LoggerQueue.createStartData(System.currentTimeMillis(), null);
        System.out.println("Hello, world!");
        String hello = "hello";
//        startData.setDuration(System.currentTimeMillis());
//        if (startData.getDuration() > 1) {
//            LoggerQueue.addToQueue(hello,
//                    startData,
//                    Thread.currentThread(),
//                    "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/SaveReturnValue",
//                    "returnString",
//                    "()Ljava/lang/String;",
//                    false);
//        }
        return hello;
    }

    long returnLong() {
//        StartData startData = LoggerQueue.createStartData(System.currentTimeMillis(), null);
        System.out.println("Hello, world!");
//        startData.setDuration(System.currentTimeMillis());
//        if (startData.getDuration() > 1) {
//            LoggerQueue.addToQueue(32,
//                    startData,
//                    Thread.currentThread(),
//                    "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/SaveReturnValue",
//                    "returnLong",
//                    "()J",
//                    false);
//        }
        return 32;
    }
}
