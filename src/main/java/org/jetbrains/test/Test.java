package org.jetbrains.test;

import java.io.FileNotFoundException;

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

public class Test {
    private class TestClass {
        private final int val = 42;
    }

    private int fun1() {
        int a = 23;
        fun3();
        return a;
    }

    private TestClass fun2() {
        TestClass testClass = new TestClass();
        return testClass;
    }

    private TestClass fun3() {
        fun4();
        fun4();
        fun5();
        return new TestClass();
    }

    private long fun4() {
        return 1234567;
    }

    private float fun5() {
        fun6();
        return (float) 123.456;
    }

    private double fun6() {
        double a = 123.456;
        fun7();
        return a;
    }

    private boolean fun7() {
        try{
            fun8();
        } catch (Exception ignored) {

        }
        return false;
    }

    private void fun8() {
//        throw new RuntimeException("Something went wrong");
    }

    public void start() {
        fun1();
        fun2();
    }

    public static void main(String[] args) throws InterruptedException, FileNotFoundException {
        Test ta = new Test();
        ta.start();
    }
}
