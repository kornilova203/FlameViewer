package org.jetbrains.test;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * Created by Liudmila Kornilova
 * on 04.05.17.
 *
 * Application for testing profiler
 * It does not have sleep() calls
 * it has only one thread and determined call tree with following structure:
 * start
 *  fun1
 *      fun3
 *          fun4
 *          fun4
 *          fun5
 *              fun6
 *                  fun7
 *                      fun8
 *  fun2
 */

public class TestApplicationWithoutSleep {
    private void fun1() {
        fun3();
    }

    private void fun2() {
    }

    private void fun3() {
        fun4();
        fun4();
        fun5();
    }

    private void fun4() {
    }

    private void fun5() {
        fun6();
    }

    private void fun6() {
        fun7();
    }

    private void fun7() {
        fun8();
    }

    private void fun8() {
    }

    public void start() {
        fun1();
        fun2();
    }

    public static void main(String[] args) throws InterruptedException, FileNotFoundException {
        TestApplicationWithoutSleep ta = new TestApplicationWithoutSleep();
        ta.start();
    }
}
