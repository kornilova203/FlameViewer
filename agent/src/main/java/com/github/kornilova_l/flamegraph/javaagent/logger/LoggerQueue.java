package com.github.kornilova_l.flamegraph.javaagent.logger;

import com.github.kornilova_l.flamegraph.javaagent.logger.event_data_storage.MethodEventData;
import com.github.kornilova_l.flamegraph.javaagent.logger.event_data_storage.RetValEventData;
import com.github.kornilova_l.flamegraph.javaagent.logger.event_data_storage.StartData;
import com.github.kornilova_l.flamegraph.javaagent.logger.event_data_storage.ThrowableEventData;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

public class LoggerQueue {
    static final LinkedBlockingDeque<MethodEventData> queue = new LinkedBlockingDeque<>();
    public static Map<String, Long> registeredThreadNames = new ConcurrentHashMap<>();
    public static Map<String, Long> registeredClassNames = new ConcurrentHashMap<>();
    public static volatile long classNamesId = 0;
    public static volatile long threadNamesId = 0;
    static int countEventsAdded = 0;

    public static StartData createStartData(long startTime, Object[] parameters) {
        return new StartData(startTime, parameters);
    }

    public static void addToQueue(Object retVal,
                                  long startTime,
                                  long duration,
                                  Object[] parameters,
                                  Thread thread,
                                  String className,
                                  String methodName,
                                  String desc,
                                  boolean isStatic) {
        countEventsAdded++;
        queue.add(new RetValEventData(thread, className, startTime, duration,
                methodName, desc, isStatic, parameters, retVal));
    }

    public static void addToQueue(Throwable throwable,
                                  long startTime,
                                  long duration,
                                  Object[] parameters,
                                  Thread thread,
                                  String className,
                                  String methodName,
                                  boolean isStatic,
                                  String desc) {
        countEventsAdded++;
        queue.add(new ThrowableEventData(thread, className, startTime, duration,
                methodName, desc, isStatic, parameters, throwable));
    }
}
