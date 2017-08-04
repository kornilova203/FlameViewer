package com.github.kornilova_l.flamegraph.plugin.server.trees.generate_test_data;

import com.github.kornilova_l.flamegraph.javaagent.logger.LoggerQueue;

public class ExceptionCaughtAndContinue implements Runnable {
    // TODO: implement
    public static String fileName = "exception-caught";

    public static void main(String[] args) {
        TestHelper.generateFile(new ExceptionCaughtAndContinue(), fileName + ".ser");
    }

    @Override
    public void run() {
        LoggerQueue.addToQueue(Thread.currentThread(),
                10,
                "com.github.kornilova_l.flamegraph.plugin.server.trees.generate_test_data.ExceptionCaught",
                "run",
                "()V",
                false,
                null);
        try {
            fun1();
        } catch (Exception e) {
            e.printStackTrace();
        }
        LoggerQueue.addToQueue(null, Thread.currentThread(), 40);
    }

    private void fun1() throws Exception {
        LoggerQueue.addToQueue(Thread.currentThread(),
                12,
                "com.github.kornilova_l.flamegraph.plugin.server.trees.generate_test_data.ExceptionCaught",
                "fun1",
                "()V",
                false,
                null);
        fun2();
        LoggerQueue.addToQueue(null, Thread.currentThread(), 15);
    }

    private void fun2() throws Exception {
        LoggerQueue.addToQueue(Thread.currentThread(),
                14,
                "com.github.kornilova_l.flamegraph.plugin.server.trees.generate_test_data.ExceptionCaught",
                "fun2",
                "()V",
                false,
                null);
        Exception e = new RuntimeException("This exception will be caught by run()");
        LoggerQueue.addToQueue(Thread.currentThread(), e, 40);
        throw e;
    }
}
