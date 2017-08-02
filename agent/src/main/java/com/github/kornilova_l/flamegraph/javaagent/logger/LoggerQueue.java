package com.github.kornilova_l.flamegraph.javaagent.logger;

import com.github.kornilova_l.flamegraph.javaagent.logger.events.*;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

public class LoggerQueue {
    static final LinkedBlockingDeque<EventData> queue = new LinkedBlockingDeque<>();
    static int countEventsAdded = 0;
    private static Set<Long> registeredThreads = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static Map<String, Long> registeredClassNames = new ConcurrentHashMap<>();
    private static long classNamesId = 0;

    @SuppressWarnings("unused")
    public static void addToQueue(Thread thread,
                                  long startTime,
                                  String className,
                                  String methodName,
                                  String description,
                                  boolean isStatic,
                                  Object[] parameters) {
        countEventsAdded++;
        long threadId = thread.getId();
        registerThreadIfNew(threadId, thread);
        long classNameId = registerClassIfNew(className);
        queue.add(new EnterEventData(threadId, startTime, classNameId, methodName,
                description, isStatic, parameters));
    }

    private static long registerClassIfNew(String className) {
        Long id = registeredClassNames.get(className);
        if (id == null) {
            registeredClassNames.put(className, ++classNamesId);
            queue.add(new NewClassEventData(classNamesId, className));
            return classNamesId;
        }
        return id;
    }

    private static void registerThreadIfNew(long threadId, Thread thread) {
        if (!registeredThreads.contains(threadId)) {
            registeredThreads.add(threadId);
            queue.add(new NewThreadEventData(threadId, thread.getName()));
        }
    }

    @SuppressWarnings("unused")
    public static void addToQueue(Object returnValue, Thread thread, long exitTime) {
        countEventsAdded++;
        queue.add(new ExitEventData(returnValue, thread.getId(), exitTime));
    }

    @SuppressWarnings("unused")
    public static void addToQueue(Thread thread, Throwable throwable, long exitTime) {
        countEventsAdded++;
        queue.add(new ExceptionEventData(throwable, thread.getId(), exitTime));
    }

}
