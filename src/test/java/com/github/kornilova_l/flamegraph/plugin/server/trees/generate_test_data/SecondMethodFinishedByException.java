//package com.github.kornilova_l.flamegraph.plugin.server.trees.generate_test_data;
//
//import com.github.kornilova_l.flamegraph.javaagent.logger.LoggerQueue;
//
//public class SecondMethodFinishedByException implements Runnable {
//    public static String fileName = "second-method-finished-by-exception";
//
//    public static void main(String[] args) {
//        TestHelper.generateSerFile(new SecondMethodFinishedByException(), fileName + ".ser");
//    }
//
//    @Override
//    public void run() {
//        LoggerQueue.addToQueue(Thread.currentThread(),
//                10,
//                "com.github.kornilova_l.flamegraph.plugin.server.trees.generate_test_data.com.github.kornilova_l.flamegraph.plugin.server.trees.generate_test_data.SecondMethodFinishedByException",
//                "run",
//                "()V",
//                false,
//                null);
//        fun1();
//        LoggerQueue.addToQueue(null, Thread.currentThread(), 20);
//    }
//
//    private void fun1() {
//        LoggerQueue.addToQueue(Thread.currentThread(), new RuntimeException("Test exception"), 15);
//        throw new RuntimeException("Test exception");
//    }
//}
