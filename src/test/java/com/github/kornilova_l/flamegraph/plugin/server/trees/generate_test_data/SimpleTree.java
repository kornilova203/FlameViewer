package com.github.kornilova_l.flamegraph.plugin.server.trees.generate_test_data;

import com.github.kornilova_l.flamegraph.proxy.StartData;

/**
 * _   _
 * |2|_|3|_     _     _
 * |1______|___|4|___|5|
 * |run________________|
 */
public class SimpleTree implements Runnable {
    public static String fileName = "simple-tree";

    @Override
    public void run() {
        StartData startData = new StartData(100, null);
        fun1();
        fun4();
        fun5();
        startData.setDuration(195);
//        if (startData.getDuration() > 1) {
//            LoggerQueue.addToQueue(null,
//                    startData.getStartTime(),
//                    startData.getDuration(),
//                    startData.getParameters(),
//                    Thread.currentThread(),
//                    "com/github/kornilova_l/flamegraph/plugin/server/trees/generate_test_data/SimpleTree",
//                    "run",
//                    "()V",
//                    false);
//        }
    }

    private void fun5() {
        StartData startData = new StartData(190, null);
        startData.setDuration(195);
//        if (startData.getDuration() > 1) {
//            LoggerQueue.addToQueue(null,
//                    startData.getStartTime(),
//                    startData.getDuration(),
//                    startData.getParameters(),
//                    Thread.currentThread(),
//                    "com/github/kornilova_l/flamegraph/plugin/server/trees/generate_test_data/SimpleTree",
//                    "fun5",
//                    "()V",
//                    false);
//        }
    }

    private void fun4() {
        StartData startData = new StartData(160, null);
        startData.setDuration(165);
//        if (startData.getDuration() > 1) {
//            LoggerQueue.addToQueue(null,
//                    startData.getStartTime(),
//                    startData.getDuration(),
//                    startData.getParameters(),
//                    Thread.currentThread(),
//                    "com/github/kornilova_l/flamegraph/plugin/server/trees/generate_test_data/SimpleTree",
//                    "fun4",
//                    "()V",
//                    false);
//        }
    }

    private void fun1() {
        StartData startData = new StartData(100, null);
        fun2();
        fun3();
        startData.setDuration(135);
//        if (startData.getDuration() > 1) {
//            LoggerQueue.addToQueue(null,
//                    startData.getStartTime(),
//                    startData.getDuration(),
//                    startData.getParameters(),
//                    Thread.currentThread(),
//                    "com/github/kornilova_l/flamegraph/plugin/server/trees/generate_test_data/SimpleTree",
//                    "fun1",
//                    "()V",
//                    false);
//        }
    }

    private void fun2() {
        StartData startData = new StartData(100, null);
        startData.setDuration(105);
//        if (startData.getDuration() > 1) {
//            LoggerQueue.addToQueue(null,
//                    startData.getStartTime(),
//                    startData.getDuration(),
//                    startData.getParameters(),
//                    Thread.currentThread(),
//                    "com/github/kornilova_l/flamegraph/plugin/server/trees/generate_test_data/SimpleTree",
//                    "fun2",
//                    "()V",
//                    false);
//        }
    }

    private void fun3() {
        StartData startData = new StartData(120, null);
        startData.setDuration(125);
//        if (startData.getDuration() > 1) {
//            LoggerQueue.addToQueue(null,
//                    startData.getStartTime(),
//                    startData.getDuration(),
//                    startData.getParameters(),
//                    Thread.currentThread(),
//                    "com/github/kornilova_l/flamegraph/plugin/server/trees/generate_test_data/SimpleTree",
//                    "fun3",
//                    "()V",
//                    false);
//        }
    }
}
