package com.github.kornilova_l.flamegraph.plugin.server.trees.generate_test_data;

import com.github.kornilova_l.flamegraph.javaagent.logger.LoggerQueue;

import static com.github.kornilova_l.flamegraph.plugin.server.trees.generate_test_data.TestHelper.generateFile;

public class OneMethodFinishedByException implements Runnable {
    public static String fileName = "one-method-finished-by-exception";

    public static void main(String[] args) {
        generateFile(new OneMethodFinishedByException(), fileName + ".ser");
    }

    @Override
    public void run() {
        LoggerQueue.addToQueue(Thread.currentThread(),
                10,
                "com.github.kornilova_l.flamegraph.plugin.server.trees.generate_test_data.OneMethodFinishedByException",
                "run",
                "()V",
                false,
                null);
        LoggerQueue.addToQueue(Thread.currentThread(), new RuntimeException("Test exception"), 15);
    }
}
