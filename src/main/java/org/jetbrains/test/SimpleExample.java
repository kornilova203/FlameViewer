package org.jetbrains.test;

import profiler.Profiler;
import profiler.State;

import java.util.ArrayList;
import java.util.regex.Pattern;

public class SimpleExample {
    private static class TestClass {
        private int a = 50;

        @Override
        public String toString() {
            return "val: " + a;
        }
        public void someFun() {
            try {
                System.out.println("try");
            }
            catch (Exception ignored){

            }
        }
    }

    public static void main(String[] args) {
        start();
        getsParameters(5, "hello", 12345678);

        SimpleExample simpleExample = new SimpleExample();
        ArrayList<Integer> arrayList = new ArrayList<>();
        arrayList.add(1);
        arrayList.add(2);
        arrayList.add(3);
        simpleExample.instanceGetsParams(123, 23);
    }

    private static TestClass start() {
        TestClass testClass = new TestClass();
        Profiler.log(testClass.toString());
        return testClass;
    }
    public static long returnJ() {
        long a = 12345;
        return a;
    }

    public static float returnF() {
        float a = (float) 12345.45;
        return a;
    }

    public static double returnD() {
        State state = Profiler.methodStart("desc");
        double a = 12345.45;
        return a;
    }

    private static void getsParameters(int a, String s, long l) {
        int b = 23;
//        System.out.println(a);
//        System.out.println(s);
//        System.out.println(b);
    }

    private void instanceGetsParams(long l, int a) {

    }

    public static void doTryCatch() {
//        try {
            System.out.println(1);
//        } catch (Exception ignored) {
//
//        }
    }
}
