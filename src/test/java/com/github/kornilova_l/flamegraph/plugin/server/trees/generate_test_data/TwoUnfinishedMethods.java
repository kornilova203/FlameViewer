package com.github.kornilova_l.flamegraph.plugin.server.trees.generate_test_data;

import com.github.kornilova_l.flamegraph.javaagent.AgentFileManager;
import com.github.kornilova_l.flamegraph.javaagent.logger.Logger;
import com.github.kornilova_l.flamegraph.javaagent.logger.LoggerQueue;

import java.io.File;

import static com.github.kornilova_l.flamegraph.plugin.server.trees.generate_test_data.TestHelper.getLatestFile;
import static com.github.kornilova_l.flamegraph.plugin.server.trees.generate_test_data.TestHelper.startLogger;
import static com.github.kornilova_l.flamegraph.plugin.server.trees.generate_test_data.TestHelper.waitLogger;

public class TwoUnfinishedMethods {
    private void start() {
        fun1();
    }

    private void fun1() {
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

    public static void main(String[] args) {
        Logger logger = new Logger(new AgentFileManager(TestHelper.path.toString()));

        startLogger(logger);

        new TwoUnfinishedMethods().start();

        waitLogger(logger);

        File file = getLatestFile();
        TestHelper.renameFile(file, "two-unfinished-methods.ser");
    }
}
