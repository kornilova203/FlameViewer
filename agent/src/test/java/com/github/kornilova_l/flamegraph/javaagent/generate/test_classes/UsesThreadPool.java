package com.github.kornilova_l.flamegraph.javaagent.generate.test_classes;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UsesThreadPool {
    public static void main(String[] args) throws IOException, InterruptedException {
//        StartData startData = LoggerQueue.createStartData(System.currentTimeMillis(), null);
        ExecutorService service = Executors.newFixedThreadPool(3);
        for (int i = 0; i < 5; i++) {
            service.submit(() -> System.out.println("hello"));
        }
        service.shutdown();
//        startData.setDuration(System.currentTimeMillis());
//        if (startData.getDuration() > 1) {
//            LoggerQueue.addToQueue(null,
//                    startData,
//                    Thread.currentThread(),
//                    "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/UsesThreadPool",
//                    "main",
//                    "([Ljava/lang/String;)V",
//                    true);
//        }
    }
}
