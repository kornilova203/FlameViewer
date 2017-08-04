package com.github.kornilova_l.flamegraph.javaagent.logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings("unused")
public class Proxy {
    private static final String loggerQueueClassName = "com.github.kornilova_l.flamegraph.javaagent.logger.LoggerQueue";
    private static Class<?> loggerQueue = null;
    private static Method addEnter = null;
    private static Method addExit = null;
    private static Method addException = null;

    @SuppressWarnings("unused")
    public static void addToQueue(Thread thread,
                                  long startTime,
                                  String className,
                                  String methodName,
                                  String description,
                                  boolean isStatic,
                                  Object[] parameters) {
        if (addEnter == null) {
            try {
                if (loggerQueue == null) {
                    loggerQueue = ClassLoader.getSystemClassLoader()
                            .loadClass(loggerQueueClassName);
                }
                addEnter = loggerQueue.getMethod("addToQueue",
                        Thread.class,
                        long.class,
                        String.class,
                        String.class,
                        String.class,
                        boolean.class,
                        Object[].class);
            } catch (ClassNotFoundException |
                    NoSuchMethodException |
                    ExceptionInInitializerError e) {
                e.printStackTrace();
            }
        }
        try {
            addEnter.invoke(null, thread, startTime, className, methodName, description, isStatic, parameters);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public static void addToQueue(Object returnValue, Thread thread, long exitTime) {
        if (addExit == null) {
            try {
                if (loggerQueue == null) {
                    loggerQueue = ClassLoader.getSystemClassLoader()
                            .loadClass(loggerQueueClassName);
                }
                addExit = loggerQueue.getMethod("addToQueue",
                        Object.class,
                        Thread.class,
                        long.class);
            } catch (ClassNotFoundException |
                    NoSuchMethodException |
                    ExceptionInInitializerError e) {
                e.printStackTrace();
            }
        }
        try {
            addExit.invoke(null, returnValue, thread, exitTime);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public static void addToQueue(Thread thread, Throwable throwable, long exitTime) {
        if (addException == null) {
            try {
                if (loggerQueue == null) {
                    loggerQueue = ClassLoader.getSystemClassLoader()
                            .loadClass(loggerQueueClassName);
                }
                addException = loggerQueue.getMethod("addToQueue",
                        Thread.class,
                        Throwable.class,
                        long.class);
            } catch (ClassNotFoundException |
                    NoSuchMethodException |
                    ExceptionInInitializerError e) {
                e.printStackTrace();
            }
        }
        try {
            addException.invoke(null, thread, throwable, exitTime);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
