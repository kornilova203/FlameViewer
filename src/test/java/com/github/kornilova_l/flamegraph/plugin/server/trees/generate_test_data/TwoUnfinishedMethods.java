package com.github.kornilova_l.flamegraph.plugin.server.trees.generate_test_data;

import com.github.kornilova_l.flamegraph.javaagent.logger.LoggerQueue;

import static com.github.kornilova_l.flamegraph.plugin.server.trees.generate_test_data.TestHelper.generateFile;

public class TwoUnfinishedMethods implements Runnable {
    public static String fileName = "two-unfinished-methods";

    public static void main(String[] args) {
        generateFile(new TwoUnfinishedMethods(), fileName + ".ser");
    }

    public void run() {
        LoggerQueue.addToQueue(Thread.currentThread(),
                10,
                "com.github.kornilova_l.flamegraph.plugin.server.trees.generate_test_data.TwoUnfinishedMethods",
                "fun1",
                "()V",
                true,
                null);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        fun2();
    }

    private void fun2() {
        LoggerQueue.addToQueue(Thread.currentThread(),
                15,
                "com.github.kornilova_l.flamegraph.plugin.server.trees.generate_test_data.OneUnfinishedMethod",
                "fun2",
                "()V",
                true,
                null);
    }
}
