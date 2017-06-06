package org.jetbrains.test;

import profiler.Profiler;
import profiler.State;

import java.util.ArrayList;
import java.util.Arrays;
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
        arrayList.add(3);int[] arr = new int[3];
        arr[0] = 11;
        arr[1] = 22;
        arr[2] = 33;
        simpleExample.instanceGetsParams(false, arr, 123, Pattern.compile("some.*(pattern)?"), 23);
        simpleExample.getFalse();
//        simpleExample.getArrayOfPatterns();
    }

    private static TestClass start() {
        TestClass testClass = new TestClass();
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

    private void instanceGetsParams(boolean b, int[] arr, long l, Pattern p, int a) {

    }

    private boolean getFalse() {
        return false;
    }

    private void unused(char c) {
        int[] arr = new int[3];
        arr[0] = 11;
        arr[1] = 22;
        arr[2] = 33;
//        Arrays.toString(arr);
    }

    public static void doTryCatch() {
//        try {
            System.out.println(1);
//        } catch (Exception ignored) {
//
//        }
    }

//    public Pattern[] getArrayOfPatterns() {
//        Pattern[] patterns = new Pattern[2];
//        patterns[0] = Pattern.compile("1.*");
//        patterns[1] = Pattern.compile("2.*");
//        return patterns;
//    }
}
