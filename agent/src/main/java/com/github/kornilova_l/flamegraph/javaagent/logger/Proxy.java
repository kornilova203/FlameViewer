package com.github.kornilova_l.flamegraph.javaagent.logger;

import com.github.kornilova_l.flamegraph.javaagent.logger.event_data_storage.StartData;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Proxy {
    private static final String loggerQueueClassName = "com.github.kornilova_l.flamegraph.javaagent.logger.LoggerQueue";
    private static Class<?> loggerQueue = null;
    private static Method addRetVal = null;
    private static Method addException = null;

    public static StartData createStartData(long startTime, Object[] parameters) {
        return new StartData(startTime, parameters);
    }

    @SuppressWarnings("unused")
    public static void addToQueue(Object retVal,
                                  long startTime,
                                  long duration,
                                  Object[] parameters,
                                  Thread thread,
                                  String className,
                                  String methodName,
                                  String desc,
                                  boolean isStatic,
                                  String savedParameters) {
        if (addRetVal == null) {
            try {
                getLoggerQueueIfNotCached();
                addRetVal = loggerQueue.getMethod("addToQueue",
                        Object.class,
                        long.class,
                        long.class,
                        Object[].class,
                        Thread.class,
                        String.class,
                        String.class,
                        String.class,
                        boolean.class,
                        String.class);
            } catch (NoSuchMethodException | ExceptionInInitializerError | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        try {
            addRetVal.invoke(null, retVal, startTime, duration, parameters, thread, className, methodName, desc, isStatic, savedParameters);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private static void getLoggerQueueIfNotCached() throws ClassNotFoundException {
        if (loggerQueue == null) {
            loggerQueue = ClassLoader.getSystemClassLoader()
                    .loadClass(loggerQueueClassName);
        }
    }

    public static void addToQueue(Throwable throwable,
                                  long startTime,
                                  long duration,
                                  Object[] parameters,
                                  Thread thread,
                                  String className,
                                  String methodName,
                                  boolean isStatic,
                                  String desc,
                                  String savedParameters) {
        if (addException == null) {
            try {
                getLoggerQueueIfNotCached();
                addException = loggerQueue.getMethod("addToQueue",
                        Throwable.class,
                        long.class,
                        long.class,
                        Object[].class,
                        Thread.class,
                        String.class,
                        String.class,
                        boolean.class,
                        String.class,
                        String.class);
            } catch (NoSuchMethodException | ExceptionInInitializerError | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        try {
            addException.invoke(null, throwable, startTime, duration, parameters, thread, className, methodName, isStatic, desc, savedParameters);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
