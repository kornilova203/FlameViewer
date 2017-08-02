package com.github.kornilova_l.flamegraph.javaagent.logger.test_application;

import com.github.kornilova_l.flamegraph.javaagent.logger.LoggerQueue;

import java.util.LinkedList;
import java.util.List;

public class MyRunnable implements Runnable {
    @Override
    public void run() {
        fun1("hello", 1, 2);
    }

    @SuppressWarnings("SameParameterValue")
    private void fun1(String hello, int i, int i1) {
        LoggerQueue.addToQueue(
                Thread.currentThread(),
                System.currentTimeMillis(),
                "com.github.kornilova_l.flamegraph.javaagent.logger.test_application.MyRunnable",
                "fun1",
                "(Ljava/lang/String;II)V",
                false,
                new Object[]{hello, i, i1}
        );
        fun2();
        LoggerQueue.addToQueue(null, Thread.currentThread(), System.currentTimeMillis());
    }

    @SuppressWarnings("UnusedReturnValue")
    private List<String> fun2() {
        LoggerQueue.addToQueue(
                Thread.currentThread(),
                System.currentTimeMillis(),
                "com.github.kornilova_l.flamegraph.javaagent.logger.test_application.MyRunnable",
                "fun2",
                "()Ljava/util/List;",
                false,
                null
        );
        List<String> list = new LinkedList<>();
        list.add("list-item");
        LoggerQueue.addToQueue(list, Thread.currentThread(), System.currentTimeMillis());
        return list;
    }
}
