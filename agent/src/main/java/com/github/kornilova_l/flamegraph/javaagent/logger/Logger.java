package com.github.kornilova_l.flamegraph.javaagent.logger;

import com.github.kornilova_l.flamegraph.javaagent.AgentFileManager;
import com.github.kornilova_l.flamegraph.javaagent.logger.events.EventData;
import com.github.kornilova_l.flamegraph.proto.EventProtos;

import java.io.*;
import java.util.List;

import static com.github.kornilova_l.flamegraph.javaagent.logger.LoggerQueue.countEventsAdded;
import static com.github.kornilova_l.flamegraph.javaagent.logger.LoggerQueue.queue;

/**
 * Thread which writes all events from loggingQueue to file
 */
public class Logger implements Runnable {
    boolean isDone = true; // changes to false when queue is enqueued
    private File file;
    private OutputStream outputStream;

    public Logger(AgentFileManager agentFileManager) {
        file = agentFileManager.createLogFile();
        System.out.println("Output file: " + file);
        OutputStream temp;
        try {
            temp = new FileOutputStream(file);
            outputStream = temp;
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Log file cannot be opened or created");
        }
    }

    void printStatus() {
        System.out.println("Events added: " + countEventsAdded);
    }

    private void writeToFile(List<EventProtos.Event> events) {
        try {
            for (EventProtos.Event event : events) {
                event.writeDelimitedTo(outputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
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

    void closeOutputStream() {
        try {
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                logEvent(queue.take());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void logEvent(EventData eventData) {
        isDone = false;
        writeToFile(eventData.getEvents());
        if (queue.size() == 0) {
            isDone = true;
        }
    }
}
