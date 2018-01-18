package com.github.kornilova_l.flamegraph.javaagent.generate.test_classes;

import com.github.kornilova_l.flamegraph.proxy.Proxy;
import com.github.kornilova_l.flamegraph.proxy.StartData;

/**
 * Created by Liudmila Kornilova
 * on 11.08.17.
 */
public class UseProxyExpected {
    public static void main(String[] args) {
        StartData startData = new StartData(System.currentTimeMillis(), null);
        try {
        System.out.println("Hello, world!");
            startData.setDuration(System.currentTimeMillis());
            if (startData.getDuration() > 1) {
                Proxy.addToQueue(null,
                        startData.getStartTime(),
                        startData.getDuration(),
                        startData.getParameters(),
                        Thread.currentThread(),
                        "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/UseProxy",
                        "main",
                        "([Ljava/lang/String;)V",
                        true,
                        "");
            }
        } catch (Throwable throwable) {
            if (!startData.isThrownByMethod()) {
                startData.setDuration(System.currentTimeMillis());
                if (startData.getDuration() > 1) {
                    Proxy.addToQueue(throwable,
                            true,
                            startData.getStartTime(),
                            startData.getDuration(),
                            startData.getParameters(),
                            Thread.currentThread(),
                            "com/github/kornilova_l/flamegraph/javaagent/generate/test_classes/UseProxy",
                            "main",
                            "([Ljava/lang/String;)V",
                            true,
                            "");
                }
            }
            throw throwable;
        }
    }
}
