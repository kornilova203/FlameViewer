package com.github.kornilova_l.flamegraph.proxy;

import com.github.kornilova_l.flamegraph.javaagent.logger.LoggerQueue;
import com.github.kornilova_l.flamegraph.javaagent.logger.event_data_storage.StartData;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This class is added to separate jar file
 * this jar file is added to classpath when program is profiled
 * It is used for the classes that do not have the system classloader in the chain
 */
@SuppressWarnings("unused")
public class Proxy {
    private static final String loggerQueueClassName = "com.github.kornilova_l.flamegraph.javaagent.logger.LoggerQueue";
    private static Class<?> loggerQueue = null;
    private static Method addRetVal = null;
    private static Method addException = null;

    public static StartData createStartData(long startTime, Object[] parameters) {
        return new StartData(startTime, parameters);
    }

    /**
     * {@link LoggerQueue#addToQueue(java.lang.Object, long, long, java.lang.Object[], java.lang.Thread, java.lang.String, java.lang.String, java.lang.String, boolean, java.lang.String)}
     */
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
        System.out.println("I am a proxy");
        /*
        following if block is added as a reminder to update proxy if addToQueue methods was changed
        (it cannot be compiled if not updated),
        this if block is excluded by compiler
         */
        //noinspection ConstantConditions,ConstantIfStatement
        if (false) {
            LoggerQueue.addToQueue(retVal, startTime, duration, parameters, thread, className, methodName, desc, isStatic, savedParameters);
        }
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

    /**
     * {@link LoggerQueue#addToQueue(java.lang.Throwable, boolean, long, long, java.lang.Object[], java.lang.Thread, java.lang.String, java.lang.String, java.lang.String, boolean, java.lang.String)}
     */
    public static void addToQueue(Throwable throwable,
                                  boolean saveMessage,
                                  long startTime,
                                  long duration,
                                  Object[] parameters,
                                  Thread thread,
                                  String className,
                                  String methodName,
                                  String desc,
                                  boolean isStatic,
                                  String savedParameters) {
        /*
        following if block is added as a reminder to update proxy if addToQueue methods was changed
        (it cannot be compiled if not updated),
        this if block is excluded by compiler
         */
        //noinspection ConstantConditions,ConstantIfStatement
        if (false) {
            LoggerQueue.addToQueue(throwable, saveMessage, startTime, duration, parameters, thread, className, methodName, desc, isStatic, savedParameters);
        }
        if (addException == null) {
            try {
                getLoggerQueueIfNotCached();
                addException = loggerQueue.getMethod("addToQueue",
                        Throwable.class,
                        boolean.class,
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
            addException.invoke(null, throwable, saveMessage, startTime, duration, parameters, thread, className, methodName, isStatic, desc, savedParameters);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

}
