package com.github.kornilova_l.flamegraph.javaagent.generate.test_classes;

import java.lang.reflect.InvocationTargetException;

/**
 * Sometimes it is needed to instrument system classes.
 * System classes are loaded by bootstrap and it is not possible to use
 * not system classes in their methods.
 */
public class SystemClassExpected {
    @SuppressWarnings({"RedundantCast", "unused"})
    public void method() {
        try {
            Class<?> proxyClass = ClassLoader.getSystemClassLoader().loadClass("com.github.kornilova_l.flamegraph.proxy.Proxy");
            Class<?> startDataClass = ClassLoader.getSystemClassLoader().loadClass("com.github.kornilova_l.flamegraph.proxy.StartData");
            Object startData = proxyClass.getMethod("createStartData", long.class, Object[].class)
                    .invoke(null, System.currentTimeMillis(), new Object[0]);
            try {
                System.out.println("Hello, I am a method of System Class. " +
                        "I do not know about any other classes except system classes");
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
                                    "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/SystemClass",
                                    "method",
                                    "()V",
                                    true,
                                    ""
                            );
                }
            } catch (Throwable t) {
                if (!((boolean) startDataClass.getMethod("isThrownByMethod").invoke(startData))) {
                    startDataClass.getMethod("setDuration", long.class).invoke(startData, System.currentTimeMillis());
                    if ((long) startDataClass.getMethod("getDuration").invoke(startData) > 1) {
                        proxyClass.getMethod("addToQueue", Throwable.class, boolean.class, long.class, long.class,
                                Object[].class, Thread.class, String.class, String.class, String.class, boolean.class, String.class)
                                .invoke(null,
                                        null,
                                        false,
                                        (long) startDataClass.getMethod("getStartTime").invoke(startData),
                                        (long) startDataClass.getMethod("getDuration").invoke(startData),
                                        (Object[]) startDataClass.getMethod("getParameters").invoke(startData),
                                        Thread.currentThread(),
                                        "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/SystemClass",
                                        "method",
                                        "()V",
                                        true,
                                        ""
                                );
                    }
                }
                throw t;
            }
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

    }
}
