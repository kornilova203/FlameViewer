package com.github.kornilova_l.flamegraph.javaagent.generate.test_classes;

import java.lang.reflect.InvocationTargetException;

/**
 * Sometimes it is needed to instrument system classes.
 * System classes are loaded by bootstrap and it is not possible to use
 * not system classes in their methods.
 */
public class SystemClassHasIfExpected {
    @SuppressWarnings({"RedundantCast", "unused"})
    public int method(int val) {
        try {
            Class<?> proxyClass = ClassLoader.getSystemClassLoader().loadClass("com.github.kornilova_l.flamegraph.proxy.Proxy");
            Class<?> startDataClass = ClassLoader.getSystemClassLoader().loadClass("com.github.kornilova_l.flamegraph.proxy.StartData");
            Object startData = proxyClass.getMethod("createStartData", long.class, Object[].class)
                    .invoke(null, System.currentTimeMillis(), new Object[0]);
            try {
                int res = 0;
                if (val > 0) {
                    res++;
                }
                startDataClass.getMethod("setDuration", long.class).invoke(startData, System.currentTimeMillis());
                if ((long) startDataClass.getMethod("getDuration").invoke(startData) > 1) {
                    proxyClass.getMethod("addToQueue", Object.class, long.class, long.class, Object[].class, Thread.class,
                            String.class, String.class, String.class, boolean.class, String.class)
                            .invoke(null,
                                    null,
                                    (long) startDataClass.getMethod("getStartTime").invoke(startData),
                                    (long) startDataClass.getMethod("getDuration").invoke(startData),
                                    (Object[]) startDataClass.getMethod("getParameters").invoke(startData),
                                    Thread.currentThread(),
                                    "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/SystemClassHasIf",
                                    "method",
                                    "(I)I",
                                    false,
                                    ""
                            );
                }
                return res;
            } catch (Throwable t) {
                if (!((boolean) startDataClass.getMethod("isThrownByMethod").invoke(startData))) {
                    startDataClass.getMethod("setDuration", long.class).invoke(startData, System.currentTimeMillis());
                    if ((long) startDataClass.getMethod("getDuration").invoke(startData) > 1) {
                        proxyClass.getMethod("addToQueue", Throwable.class, boolean.class, long.class, long.class,
                                Object[].class, Thread.class, String.class, String.class, String.class, boolean.class, String.class)
                                .invoke(null, // static method
                                        t,
                                        false,
                                        (long) startDataClass.getMethod("getStartTime").invoke(startData),
                                        (long) startDataClass.getMethod("getDuration").invoke(startData),
                                        (Object[]) startDataClass.getMethod("getParameters").invoke(startData),
                                        Thread.currentThread(),
                                        "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/SystemClassHasIf",
                                        "method",
                                        "(I)I",
                                        false,
                                        ""
                                );
                    }
                }
                throw t;
            }
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
