package org.jetbrains.test;

import profiler.Profiler;

public class SimpleExample {
    private static class TestClass {
        private int a = 50;

        @Override
        public String toString() {
            return "val: " + a;
        }
    }

    public static void main(String[] args) {
        start();
    }

    public static TestClass start() {
        TestClass testClass = new TestClass();
        Profiler.log(testClass.toString());
        return testClass;
    }
    public static long returnJ() {
        long a = 12345;
        Profiler.log(String.valueOf(a));
        return a;
    }

    public static float returnF() {
        float a = (float) 12345.45;
        Profiler.log(String.valueOf(a));
        return a;
    }

    public static double returnD() {
        double a = 12345.45;
        Profiler.log(String.valueOf(a));
        return a;
    }
}
