package com.github.kornilova_l.flamegraph.plugin.server.trees.generate_test_data;

import com.github.kornilova_l.flamegraph.javaagent.logger.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;

class TestHelper {
    static Path path = Paths.get("src", "test", "resources", "out");

    static void waitLogger(Logger logger) {

        while (!logger.isDone) { // wait for logger to log all events
            Thread.yield();
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.closeOutputStream();
    }

    static Thread startLogger(Logger logger) {
        Thread loggerThread = new Thread(logger, "logging thread");
        loggerThread.setDaemon(true);
        loggerThread.start();
        System.out.println("new thread " + loggerThread.getId());
        return loggerThread;
    }

    static File getLatestFile() {
        File[] files = new File(path.toString()).listFiles();
        assert files != null;
        Optional<File> latestFile = Arrays.stream(files)
                .max(Comparator.comparingLong(File::lastModified));
        assert latestFile.isPresent();
        return latestFile.get();
    }

    static void renameFile(File file, String newName) {
        Path path = Paths.get(file.getAbsolutePath());
        try {
            Path siblingPath = path.resolveSibling(newName);
            File siblingFile = new File(siblingPath.toString());
            //noinspection RedundantIfStatement
            if (siblingFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                siblingFile.delete();
            }
            Files.move(path, siblingPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
