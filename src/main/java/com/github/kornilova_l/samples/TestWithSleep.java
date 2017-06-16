package com.github.kornilova_l.samples;

/**
 * Created by Liudmila Kornilova
 * on 04.05.17.
 *
 * Application for testing profiler
 * it has only one thread and determined call tree with following structure:
 * start
 *  fun1
 *      fun3
 *          fun4
 *          fun4
 *          fun5
 *              fun6
 *  fun2
 *
 *  total sleep = 164ms
 */

public class TestWithSleep {
    private void fun1() throws InterruptedException {
        Thread.sleep(3);
        fun3();
        Thread.sleep(5);
    }

    private void fun2() throws InterruptedException {
        Thread.sleep(10);
    }

    private void fun3() throws InterruptedException {
        Thread.sleep(3);
        fun4();
        Thread.sleep(3);
        fun4();
        fun5();
    }

    private void fun4() throws InterruptedException {
        Thread.sleep(20);
    }

    private void fun5() throws InterruptedException {
        Thread.sleep(40);
        fun6();
        Thread.sleep(10);
    }

    private void fun6() throws InterruptedException {
        Thread.sleep(15);
    }

    public void start() throws InterruptedException {
        Thread.sleep(20);
        int a = 1;
        fun1();
        Thread.sleep(30);
        fun2();
        Thread.sleep(5);
    }

    public static void main(String[] args) throws InterruptedException {
        TestWithSleep ta = new TestWithSleep();
        ta.start();
    }
}
