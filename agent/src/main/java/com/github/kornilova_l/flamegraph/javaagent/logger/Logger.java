package com.github.kornilova_l.flamegraph.javaagent.logger;

import com.github.kornilova_l.flamegraph.javaagent.logger.event_data_storage.MethodEventData;
import com.github.kornilova_l.flamegraph.proto.EventProtos;

import java.io.*;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Thread which writes all events from loggingQueue to file
 */
public class Logger implements Runnable {
    private File file;
    private LoggerQueue loggerQueue = LoggerQueue.getInstance();
    private long lastLogTime;

    public Logger(File file) {
        this.file = file;
        System.out.println("Output file: " + file);
        lastLogTime = System.currentTimeMillis();
    }

    public void finish() {
        logEvents();
        printStatus();
    }

    private void printStatus() {
        System.out.println("Methods count: " + loggerQueue.getEventsAdded());
    }

    private void writeToFile(List<EventProtos.Event> events, OutputStream outputStream) {
        try {
            for (EventProtos.Event event : events) {
                event.writeDelimitedTo(outputStream);
            }
        } catch (IOException ignored) {
            System.out.println("Trying to write to closed stream");
        }
    }

    void printDataForHuman() {
        try (InputStream inputStream = new FileInputStream(file)) {
            EventProtos.Event event = EventProtos.Event.parseDelimitedFrom(inputStream);
            while (event != null) {
                System.out.println(event.toString());
                event = EventProtos.Event.parseDelimitedFrom(inputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                long sleepTime = getSleepTime();
                Thread.sleep(sleepTime);
                lastLogTime = System.currentTimeMillis();
                logEvents();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private long getSleepTime() {
        long delta = 1000 - (System.currentTimeMillis() - lastLogTime);
        if (delta <= 0) {
            return 0;
        }
        return delta;
    }


    private synchronized void logEvents() {
        try (OutputStream outputStream = new FileOutputStream(file, true)) {
            ConcurrentLinkedQueue<MethodEventData> queue = loggerQueue.getQueue();
            while (!queue.isEmpty()) {
                writeToFile(queue.remove().getEvents(), outputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
