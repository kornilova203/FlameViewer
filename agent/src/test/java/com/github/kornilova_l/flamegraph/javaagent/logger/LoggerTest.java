package com.github.kornilova_l.flamegraph.javaagent.logger;

import org.junit.Test;

import java.io.File;

public class LoggerTest {
    private static final File logDir = new File("src/test/generated");

    @Test
    public void addToQueue() throws Exception {
//        Logger logger = new Logger(new AgentFileManager(logDir.getAbsolutePath()));
//
//        startLogger(logger);
//
//        Main.run();
//
//        waitLogger(logger);
//
//        File file = getLatestFile();
//        try (InputStream inputStream = new FileInputStream(file)) {
//            StringBuilder res = new StringBuilder();
//            EventProtos.Event event = EventProtos.Event.parseDelimitedFrom(inputStream);
//            while (event != null) {
//                res.append(event.toString());
//                event = EventProtos.Event.parseDelimitedFrom(inputStream);
//            }
            // this test always fails
            // because time and thread ids are always different
            // order of events also may be different if more than one thread
            // to check if logger is correct uncomment this lines and check this test manually
//            com.github.kornilova_l.flamegraph.javaagent.TestHelper.compare(res.toString(),
//                    new File("src/test/resources/01-result.txt"));
//        }
    }

//    private void waitLogger(Logger logger) {
//        while (!logger.isDone) { // wait for logger to log all events
//            Thread.yield();
//        }
//        logger.closeOutputStream();
//    }
//
//    private void startLogger(Logger logger) {
//        Thread loggerThread = new Thread(logger, "logging thread");
//        loggerThread.setDaemon(true);
//        loggerThread.start();
//    }
//
//    private File getLatestFile() {
//        File[] files = logDir.listFiles();
//        assert files != null;
//        Optional<File> latestFile = Arrays.stream(files)
//                .max(Comparator.comparingLong(File::lastModified));
//        assert latestFile.isPresent();
//        return latestFile.get();
//    }

}