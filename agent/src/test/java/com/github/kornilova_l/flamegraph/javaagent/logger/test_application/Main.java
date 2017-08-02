package com.github.kornilova_l.flamegraph.javaagent.logger.test_application;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void run() {
        ExecutorService service = Executors.newFixedThreadPool(2);
        for (int i = 0; i < 3; i++) {
            service.submit(new MyRunnable());
        }
        service.shutdown();
        try {
            service.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
