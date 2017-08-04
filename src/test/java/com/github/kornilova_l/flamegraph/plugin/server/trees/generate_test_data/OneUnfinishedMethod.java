package com.github.kornilova_l.flamegraph.plugin.server.trees.generate_test_data;

import com.github.kornilova_l.flamegraph.javaagent.AgentFileManager;
import com.github.kornilova_l.flamegraph.javaagent.logger.Logger;
import com.github.kornilova_l.flamegraph.javaagent.logger.LoggerQueue;

import java.io.File;

import static com.github.kornilova_l.flamegraph.plugin.server.trees.generate_test_data.TestHelper.getLatestFile;
import static com.github.kornilova_l.flamegraph.plugin.server.trees.generate_test_data.TestHelper.startLogger;
import static com.github.kornilova_l.flamegraph.plugin.server.trees.generate_test_data.TestHelper.waitLogger;

public class OneUnfinishedMethod {
    private static void start() {
        fun1();
    }

    private static void fun1() {
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

    public static void main(String[] args) {
        Logger logger = new Logger(new AgentFileManager(TestHelper.path.toString()));

        startLogger(logger);

        start();

        waitLogger(logger);

        File file = getLatestFile();
        TestHelper.renameFile(file, "one-unfinished-method.ser");
    }
}
