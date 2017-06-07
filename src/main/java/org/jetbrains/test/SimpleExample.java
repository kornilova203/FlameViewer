package org.jetbrains.test;

import profiler.Profiler;
import profiler.State;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Pattern;

public class SimpleExample {
    private static class TestClass {
        private int a = 50;

        @Override
        public String toString() {
            return "val: " + a;
        }
        public void someFun() {
//            try {
//                System.out.println("try");
//            }
//            catch (Exception ignored){
//
//            }
        }
    }

    public void start() {
//        returnsTestClass();
        getsParameters(5, "hello", 12345678);

        SimpleExample simpleExample = new SimpleExample();
        int[] arr = new int[3];
        arr[0] = 11;
        arr[1] = 22;
        arr[2] = 33;
        Pattern[] patterns = new Pattern[2];
        patterns[0] = Pattern.compile("1.*");
        patterns[1] = Pattern.compile("2.*");
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("some");
        arrayList.add("arrayList");
        simpleExample.instanceGetsParams(arrayList, false, arr, patterns, 123, Pattern.compile("some.*(pattern)?"), 23);
        simpleExample.getFalse();
        returnsArrayList();
        returnsArrayListOfStrings();
        returnsArrayOfHashSets();
        returnsHashSetOfArraysOfStrings();

        HashSet<String[]> hashSet = new HashSet<>();
        hashSet.add(new String[] {"hello", "how", "are", "you?"});
        hashSet.add(new String[] {"another", "array", "of", "strings"});
        getsHashSetOfArraysOfStrings(hashSet, 1);

        returns2DArrayOfStrings();
    }

    @Override
    public String toString() {
        return "I am an instance of SimpleExample";
    }

//    private static TestClass returnsTestClass() {
//        TestClass testClass = new TestClass();
//        return testClass;
//    }
    public static long returnJ() {
        long a = 12345;
        return a;
    }

    public static float returnF() {
        float a = (float) 12345.45;
        return a;
    }

    public static double returnD() {
//        State state = Profiler.methodStart("desc");
        double a = 12345.45;
        return a;
    }

    private static Pattern[] getsParameters(int a, String s, long l) {
        int b = 23;
        Pattern[] patterns = new Pattern[10];
        patterns[5] = Pattern.compile("s0{3}me?p.*rn");
        return patterns;
    }

    private int[] instanceGetsParams(ArrayList<String> arrayList, boolean b, int[] arr, Pattern[] patterns,
                                     long l, Pattern p, int a) {
        return new int[20];
    }

    private boolean getFalse() {
        return false;
    }

    public static void doTryCatch() {
//        try {
//            System.out.println(1);
//        } catch (Exception ignored) {
//
//        }
    }

    private static ArrayList<Integer> returnsArrayList() {
        ArrayList<Integer> arrayList = new ArrayList<>();
        arrayList.add(23);
        arrayList.add(123);
        return arrayList;
    }

    private static ArrayList<String> returnsArrayListOfStrings() {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("hello, ");
        arrayList.add("world!");
        return arrayList;
    }

    private static HashSet<Pattern>[] returnsArrayOfHashSets() {
        HashSet<Pattern>[] hashSets = new HashSet[7];
        hashSets[0] = new HashSet<>();
        hashSets[0].add(Pattern.compile("1.*"));
        hashSets[0].add(Pattern.compile("2.*"));
        hashSets[1] = new HashSet<>();
        return hashSets;
    }

    private static HashSet<String[]> returnsHashSetOfArraysOfStrings() {
        HashSet<String[]> hashSet = new HashSet<>();
        hashSet.add(new String[] {"hello", "how", "are", "you?"});
        hashSet.add(new String[] {"another", "array", "of", "strings"});
        return hashSet;
    }

    private static void getsHashSetOfArraysOfStrings(HashSet<String[]> strings, int a) {

    }

    private static String[][] returns2DArrayOfStrings() {
        String[][] strings = new String[5][2];
        strings[0][0] = "hello";
        strings[0][1] = "world";
        return strings;
    }

    private void unused(Pattern[] patterns) {
//        State state = Profiler.methodStart("desc",patterns.toString() + "some text");
//        state.methodFinish(" ");
    }

    public static void main(String[] args) {
        SimpleExample simpleExample = new SimpleExample();
        simpleExample.start();
    }
}
