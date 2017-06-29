package com.github.kornilova_l.profiler.logger;

import java.lang.reflect.InvocationTargetException;

public class Proxy {
    private static Logger logger;

    public static Logger getLogger() {
        if (logger == null) {
            try {
                logger = (Logger) ClassLoader.getSystemClassLoader()
                        .loadClass("com.github.kornilova_l.profiler.logger.Logger")
                        .getMethod("getInstance")
                        .invoke(null);

            } catch (ClassNotFoundException |
                    IllegalAccessException |
                    NoSuchMethodException |
                    ExceptionInInitializerError |
                    InvocationTargetException e) {
                System.out.println("hello");
                e.printStackTrace();
            }
        }
        return logger;
    }
}