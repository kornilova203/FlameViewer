package com.github.kornilova_l.flamegraph.javaagent.generate.test_classes;

import java.lang.reflect.InvocationTargetException;

/**
 * Sometimes it is needed to instrument system classes.
 * System classes are loaded by bootstrap and it is not possible to use
 * not system classes in their methods.
 */
public class SystemClassExpected {
    public void method() {
        try {
            Class<?> clazz = ClassLoader.getSystemClassLoader().loadClass("com.github.kornilova_l.flamegraph.proxy.Proxy");
            Object startData = clazz.getMethod("createStartData", long.class, Object[].class)
                    .invoke(null, System.currentTimeMillis(), new Object[0]);
            try {
                System.out.println("Hello, I am a method of System Class. " +
                        "I do not know about any other classes except system classes");
                clazz.getMethod("setDuration", long.class).invoke(startData, System.currentTimeMillis());

                if ((long) clazz.getMethod("getDuration").invoke(startData) > 1) {
                    clazz.getMethod("addToQueue", Object.class, long.class, long.class, Object[].class, Thread.class,
                            String.class, String.class, String.class, boolean.class, String.class)
                            .invoke(null,
                                    null,
                                    (long) clazz.getMethod("getStartTime").invoke(startData),
                                    (long) clazz.getMethod("getDuration").invoke(startData),
                                    (Object[]) clazz.getMethod("getParameters").invoke(startData),
                                    Thread.currentThread(),
                                    "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/SystemClass",
                                    "method",
                                    "()V",
                                    true,
                                    ""
                            );
                }
            } catch (Throwable t) {
                if (!((boolean) clazz.getMethod("isThrownByMethod").invoke(startData))) {
                    clazz.getMethod("setDuration", long.class).invoke(startData, System.currentTimeMillis());
                    if ((long) clazz.getMethod("getDuration").invoke(startData) > 1) {
                        clazz.getMethod("addToQueue", Throwable.class, boolean.class, long.class, long.class,
                                Object[].class, Thread.class, String.class, String.class, String.class, boolean.class, String.class)
                                .invoke(null,
                                        null,
                                        false,
                                        (long) clazz.getMethod("getStartTime").invoke(startData),
                                        (long) clazz.getMethod("getDuration").invoke(startData),
                                        (Object[]) clazz.getMethod("getParameters").invoke(startData),
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
