package com.github.kornilova_l.samples;

import com.github.kornilova_l.profiler.logger.EnterEventData;
import com.github.kornilova_l.profiler.logger.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.regex.Pattern;

@SuppressWarnings({"UnnecessaryLocalVariable", "UnusedReturnValue", "SameParameterValue", "unchecked", "MethodMayBeStatic"})
public class Sample implements Runnable {
    private HashSet<Integer> hashSet = new HashSet<>();

    private static class TestClass {
        private int a = 50;
//        TestClass() {
//            a = 50;
//        }

        @Override
        public String toString() {
            return "val: " + a;
        }

        void doSmth() {

        }
    }

    public void run() {
        TestClass tc = new TestClass();
        tc.doSmth();
        getsParameters(5, "hello", 12345678);

        long J = returnJ();
        Blackhole.consume(J);

        float F = returnF();
        Blackhole.consume(F);

        double D = returnD();
        Blackhole.consume(D);
//        Random random = new Random(System.currentTimeMillis());
//        System.out.println("hello");
//        if (random.nextBoolean()) {
//            queue.add(new ExceptionEventData(new AssertionError("hello"), 123, 123));
//            throw new AssertionError("Something is wrong");
//        }

        Pattern[] patterns = new Pattern[2];
        patterns[0] = Pattern.compile("1.*");
        patterns[1] = Pattern.compile("2.*");
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("some");
        arrayList.add("arrayList");
        int[] arr = new int[3];
        arr[0] = 11;
        arr[1] = 22;
        arr[2] = 33;
        Sample sample = new Sample();
        sample.instanceGetsParams(arrayList, false, arr, patterns, 123,
                Pattern.compile("some.*(pattern)?"), 23, 'c', (short) 12, (byte) 1, (float) 1.2, 1.2);

        sample.returnsFalse();

        returnsArrayList();

        returnsArrayListOfStrings();

        returnsArrayOfHashSets();

        returnsHashSetOfArraysOfStrings();

        HashSet<String[]> stringsHashSet = new HashSet<>();
        stringsHashSet.add(new String[]{"hello", "how", "are", "you?"});
        stringsHashSet.add(new String[]{"another", "array", "of", "strings"});
        getsHashSetOfArraysOfStrings(stringsHashSet, 1);

        doComplicatedTask();

        returns2DArrayOfStrings();

        Blackhole.consume(hashSet);

        doCondition();

        doTryCatch();
        System.out.println("finish");
        getInt(23);
        instanceGetsI(23);

//        Thread.sleep(1000);
    }

    @Override
    public String toString() {
        return "I am an instance of Sample";
    }

//    private static TestClass returnsTestClass() {
//        return new TestClass();
//    }

    private static long returnJ() {
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        Logger.queue.add(
//                new EnterEventData(
//                        Thread.currentThread().getId(),
//                        System.currentTimeMillis(),
//                        "samples/Sample",
//                        "returnJ",
//                        true,
//                        null
//                )
//        );
        //noinspection UnnecessaryLocalVariable
        long a = 12345;
//        Logger.queue.add(
//                new ExitEventData(
//                        a,
//                        Thread.currentThread().getId(),
//                        System.currentTimeMillis()
//                )
//        );
        return a;
    }

    private static float returnF() {
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //noinspection UnnecessaryLocalVariable
        float a = (float) 12345.45;
        return a;
    }

    private static double returnD() {
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        getArrayOfObj(new Object[]{"aaa", "bbb", 23});
        double a = 12345.45;
        return a;
    }

    private static void getArrayOfObj(Object[] objects) {

    }

    @SuppressWarnings("unused")
    private static Pattern[] getsParameters(int a, String s, long l) {
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long threadId = Thread.currentThread().getId();
        int b = 23;
        Pattern[] patterns = new Pattern[10];
        patterns[5] = Pattern.compile("s0{3}me?p.*rn");
        Blackhole.consume(b);
        return patterns;
    }

    private void instanceGetsI(int i) {
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    private int[] instanceGetsParams(ArrayList<String> arrayList, boolean b, int[] arr, Pattern[] patterns,
                                     long l, Pattern p, int a, char c, short s, byte bt, float f, double d) {
//        Logger.queue.add(new EnterEventData(
//                Thread.currentThread().getId(),
//                System.currentTimeMillis(),
//                "samples/Sample",
//                "instanceGetsParams",
//                false,
//                new Object[]{
//                        this,
//                        arrayList,
//                        b,
//                        arr,
//                        patterns,
//                        l,
//                        p,
//                        a,
//                        c,
//                        s,
//                        bt,
//                        f
//                }));
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int[] retArr = new int[20];

//        Logger.queue.add(new ExitEventData(
//                Thread.currentThread().getId(),
//                System.currentTimeMillis(),
//                retArr));

        return retArr;
    }

    private boolean returnsFalse() {
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void doTryCatch() {
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            Blackhole.consume(1);
        } catch (Exception ignored) {

        }
    }

    private static ArrayList<Integer> returnsArrayList() {
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ArrayList<Integer> arrayList = new ArrayList<>();
        arrayList.add(23);
        arrayList.add(123);
        return arrayList;
    }

    private static ArrayList<String> returnsArrayListOfStrings() {
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.add("hello, ");
        arrayList.add("world!");
        return arrayList;
    }

    private static HashSet<Pattern>[] returnsArrayOfHashSets() {
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        HashSet<Pattern>[] hashSets = new HashSet[7];
        hashSets[0] = new HashSet<>();
        hashSets[0].add(Pattern.compile("1.*"));
        hashSets[0].add(Pattern.compile("2.*"));
        hashSets[1] = new HashSet<>();
        return hashSets;
    }

    private static HashSet<String[]> returnsHashSetOfArraysOfStrings() {
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        HashSet<String[]> hashSet = new HashSet<>();
        hashSet.add(new String[]{"hello", "how", "are", "you?"});
        hashSet.add(new String[]{"another", "array", "of", "strings"});
        return hashSet;
    }

    @SuppressWarnings("unused")
    private static void getsHashSetOfArraysOfStrings(HashSet<String[]> strings, int a) {
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void getInt(int a) {
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private static String[][] returns2DArrayOfStrings() {
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        String[][] strings = new String[5][2];
        strings[0][0] = "hello";
        strings[0][1] = "world";
        return strings;
    }

    private void doCondition() {
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int i = 1;
        String string1 = "hello1";
        if (hashSet.isEmpty()) {
            System.out.println("hello");
        }
        String string2 = "hello2";
        if (hashSet.size() == 1) {
            System.out.println("hello");
        }
        Blackhole.consume(string1);
        Blackhole.consume(string2);
        Blackhole.consume(i);
    }

    private void doComplicatedTask() {
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < 10000; i++) {
            hashSet.add(i * 2);
        }
    }

    @SuppressWarnings("unused")
//    private void unused(Pattern[] patterns) {
//    }

    public static void main(String[] args) throws InterruptedException {
        Thread thread1 = new Thread(new Sample());
        Thread thread2 = new Thread(new Sample());
        thread1.start();
        Thread.sleep(25);
        thread2.start();
    }
}
