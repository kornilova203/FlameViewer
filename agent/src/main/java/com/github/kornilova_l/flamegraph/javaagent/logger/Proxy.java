package com.github.kornilova_l.flamegraph.javaagent.logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@SuppressWarnings("unused")
public class Proxy {
    private static Class<?> logger = null;
    private static Method addEnter = null;
    private static Method addExit = null;
    private static Method addException = null;

    @SuppressWarnings("unused")
    public static void addToQueue(long threadId,
                                  long startTime,
                                  String className,
                                  String methodName,
                                  String description,
                                  boolean isStatic,
                                  Object[] parameters) {
        if (addEnter == null) {
            try {
                if (logger == null) {
                    logger = ClassLoader.getSystemClassLoader()
                            .loadClass("com.github.kornilova_l.flamegraph.javaagent.logger.Logger");
                }
                addEnter = logger.getMethod("addToQueue",
                                long.class,
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
            addEnter.invoke(null, threadId, startTime, className, methodName, description, isStatic, parameters);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public static void addToQueue(Object returnValue, long threadId, long exitTime) {
        if (addExit == null) {
            try {
                if (logger == null) {
                    logger = ClassLoader.getSystemClassLoader()
                            .loadClass("com.github.kornilova_l.flamegraph.javaagent.logger.Logger");
                }
                addExit = logger.getMethod("addToQueue",
                                Object.class,
                                long.class,
                                long.class);
            } catch (ClassNotFoundException |
                    NoSuchMethodException |
                    ExceptionInInitializerError e) {
                e.printStackTrace();
            }
        }
        try {
            addExit.invoke(null, returnValue, threadId, exitTime);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    public static void addToQueue(Throwable throwable, long threadId, long exitTime) {
        if (addException == null) {
            try {
                if (logger == null) {
                    logger = ClassLoader.getSystemClassLoader()
                            .loadClass("com.github.kornilova_l.flamegraph.javaagent.logger.Logger");
                }
                addException = logger.getMethod("addToQueue",
                                Throwable.class,
                                long.class,
                                long.class);
            } catch (ClassNotFoundException |
                    NoSuchMethodException |
                    ExceptionInInitializerError e) {
                e.printStackTrace();
            }
        }
        try {
            addException.invoke(null, throwable, threadId, exitTime);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
