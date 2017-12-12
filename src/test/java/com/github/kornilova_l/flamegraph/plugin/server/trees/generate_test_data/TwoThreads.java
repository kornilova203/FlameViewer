package com.github.kornilova_l.flamegraph.plugin.server.trees.generate_test_data;

import com.github.kornilova_l.flamegraph.javaagent.logger.LoggerQueue;
import com.github.kornilova_l.flamegraph.javaagent.logger.event_data_storage.StartData;

/**
 * _       _
 * |2|_   _|4|
 * _|1__|_|3__|_
 * |run__________|
 * _       _
 * |2|_   _|4|
 * _|1__|_|3__|_
 * ...|run__________|
 */
public class TwoThreads implements Runnable {
    public static String fileName = "two-threads";

    @Override
    public void run() {
        new Thread(new MyTask(0)).start();
        try {
            Thread.sleep(15);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        new Thread(new MyTask(15)).start();
    }
}

class MyTask implements Runnable {
    private long offset;

    MyTask(long offset) {
        this.offset = offset;
    }

    @Override
    public void run() {
        StartData startData = LoggerQueue.createStartData(100 + offset, null);
        fun1();
        fun3();
        startData.setDuration(165 + offset);
//        if (startData.getDuration() > 1) {
//            LoggerQueue.addToQueue(null,
//                    startData.getStartTime(),
//                    startData.getDuration(),
//                    startData.getParameters(),
//                    Thread.currentThread(),
//                    "com/github/kornilova_l/flamegraph/plugin/server/trees/generate_test_data/MyTask",
//                    "run",
//                    "()V",
//                    false);
//        }
    }

    private void fun3() {
        StartData startData = LoggerQueue.createStartData(140 + offset, null);
        fun4();
        startData.setDuration(155 + offset);
//        if (startData.getDuration() > 1) {
//            LoggerQueue.addToQueue(null,
//                    startData.getStartTime(),
//                    startData.getDuration(),
//                    startData.getParameters(),
//                    Thread.currentThread(),
//                    "com/github/kornilova_l/flamegraph/plugin/server/trees/generate_test_data/MyTask",
//                    "fun3",
//                    "()V",
//                    false);
//        }
    }

    private void fun4() {
        StartData startData = LoggerQueue.createStartData(150 + offset, null);
        startData.setDuration(155 + offset);
//        if (startData.getDuration() > 1) {
//            LoggerQueue.addToQueue(null,
//                    startData.getStartTime(),
//                    startData.getDuration(),
//                    startData.getParameters(),
//                    Thread.currentThread(),
//                    "com/github/kornilova_l/flamegraph/plugin/server/trees/generate_test_data/MyTask",
//                    "fun4",
//                    "()V",
//                    false);
//        }
    }

    private void fun1() {
        StartData startData = LoggerQueue.createStartData(110 + offset, null);
        fun2();
        startData.setDuration(125 + offset);
//        if (startData.getDuration() > 1) {
//            LoggerQueue.addToQueue(null,
//                    startData.getStartTime(),
//                    startData.getDuration(),
//                    startData.getParameters(),
//                    Thread.currentThread(),
//                    "com/github/kornilova_l/flamegraph/plugin/server/trees/generate_test_data/MyTask",
//                    "fun1",
//                    "()V",
//                    false);
//        }
    }

    private void fun2() {
        StartData startData = LoggerQueue.createStartData(110 + offset, null);
        startData.setDuration(115 + offset);
//        if (startData.getDuration() > 1) {
//            LoggerQueue.addToQueue(null,
//                    startData.getStartTime(),
//                    startData.getDuration(),
//                    startData.getParameters(),
//                    Thread.currentThread(),
//                    "com/github/kornilova_l/flamegraph/plugin/server/trees/generate_test_data/MyTask",
//                    "fun2",
//                    "()V",
//                    false);
//        }
    }
}
