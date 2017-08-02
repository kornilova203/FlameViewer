package com.github.kornilova_l.flamegraph.javaagent.logger;

import com.github.kornilova_l.flamegraph.javaagent.logger.events.*;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

public class LoggerQueue {
    public static Map<String, Long> registeredThreadNames = new ConcurrentHashMap<>();
    public static Map<String, Long> registeredClassNames = new ConcurrentHashMap<>();
    public static volatile long classNamesId = 0;
    public static volatile long threadNamesId = 0;

    static final LinkedBlockingDeque<EventData> queue = new LinkedBlockingDeque<>();
    static int countEventsAdded = 0;

    public static void addToQueue(Thread thread,
                                  long startTime,
                                  String className,
                                  String methodName,
                                  String description,
                                  boolean isStatic,
                                  Object[] parameters) {
        countEventsAdded++;
        queue.add(new EnterEventData(thread.getName(), startTime, className, methodName,
                description, isStatic, parameters));
    }

    public static void addToQueue(Object returnValue, Thread thread, long exitTime) {
        countEventsAdded++;
        queue.add(new ExitEventData(returnValue, thread.getName(), exitTime));
    }

    public static void addToQueue(Thread thread, Throwable throwable, long exitTime) {
        countEventsAdded++;
        queue.add(new ExceptionEventData(throwable, thread.getName(), exitTime));
    }

}
