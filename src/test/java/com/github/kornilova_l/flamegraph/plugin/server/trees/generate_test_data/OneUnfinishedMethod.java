package com.github.kornilova_l.flamegraph.plugin.server.trees.generate_test_data;

import com.github.kornilova_l.flamegraph.javaagent.logger.LoggerQueue;

import static com.github.kornilova_l.flamegraph.plugin.server.trees.generate_test_data.TestHelper.generateFile;

public class OneUnfinishedMethod implements Runnable {
    public static void main(String[] args) {
        generateFile(new OneUnfinishedMethod(), "one-unfinished-method.ser");
    }

    public void run() {
        LoggerQueue.addToQueue(Thread.currentThread(),
                System.currentTimeMillis(),
                "com.github.kornilova_l.flamegraph.plugin.server.trees.generate_test_data.OneUnfinishedMethod",
                "fun1",
                "()V",
                true,
                null);
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
