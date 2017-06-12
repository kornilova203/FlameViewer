package samples;

import profiler.EnterEventData;
import profiler.ExitEventData;
import profiler.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.regex.Pattern;

@SuppressWarnings({"UnnecessaryLocalVariable", "UnusedReturnValue", "SameParameterValue", "unchecked"})
public class Sample {
    private HashSet<Integer> hashSet = new HashSet<>();
    private static final ThreadLocal<Long> threadId = ThreadLocal.withInitial(() -> Thread.currentThread().getId());

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

    public void start() {
        TestClass tc = new TestClass();
        tc.doSmth();
        getsParameters(5, "hello", 12345678);


        long J = returnJ();
        Blackhole.consume(J);

        float F = returnF();
        Blackhole.consume(F);

        double D = returnD();
        Blackhole.consume(D);

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
        sample.instanceGetsParams(arrayList, false, arr, patterns, 123, Pattern.compile("some.*(pattern)?"), 23);

        sample.returnsFalse();

        returnsArrayList();

        returnsArrayListOfStrings();

        returnsArrayOfHashSets();

        returnsHashSetOfArraysOfStrings();

        HashSet<String[]> stringsHashSet = new HashSet<>();
        stringsHashSet.add(new String[] {"hello", "how", "are", "you?"});
        stringsHashSet.add(new String[] {"another", "array", "of", "strings"});
        getsHashSetOfArraysOfStrings(stringsHashSet, 1);

        doComplicatedTask();

        returns2DArrayOfStrings();

        Blackhole.consume(hashSet);

        doCondition();

        doTryCatch();

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
        Logger.queue.add(
                new EnterEventData(
                        threadId.get(),
                        System.currentTimeMillis(),
                        "samples/Sample",
                        "returnJ",
                        true,
                        null
                )
        );
        //noinspection UnnecessaryLocalVariable
        long a = 12345;
        Logger.queue.add(
                new ExitEventData(
                        threadId.get(),
                        System.currentTimeMillis(),
                        a
                )
        );
        return a;
    }

    private static float returnF() {
        //noinspection UnnecessaryLocalVariable
        float a = (float) 12345.45;
        return a;
    }

    private static double returnD() {
        double a = 12345.45;
        return a;
    }

    @SuppressWarnings("unused")
    private static Pattern[] getsParameters(int a, String s, long l) {
        long threadId = Thread.currentThread().getId();
//        Profiler.methodEnter(System.currentTimeMillis(), "samples/Sample", "getsParameters",
//                threadId, new Object[] {a, s ,l});
        int b = 23;
        Pattern[] patterns = new Pattern[10];
        patterns[5] = Pattern.compile("s0{3}me?p.*rn");
        Blackhole.consume(b);
        return patterns;
    }

    @SuppressWarnings("unused")
    private int[] instanceGetsParams(ArrayList<String> arrayList, boolean b, int[] arr, Pattern[] patterns,
                                     long l, Pattern p, int a) {
        Logger.queue.add(new EnterEventData(
                threadId.get(),
                System.currentTimeMillis(),
                "samples/Sample",
                "instanceGetsParams",
                false,
                new Object[] {arrayList, b, arr, patterns, l, p, a}));

        int[] retArr = new int[20];

        Logger.queue.add(new ExitEventData(
                threadId.get(),
                System.currentTimeMillis(),
                retArr));

        return retArr;
    }

    private boolean returnsFalse() {
        return false;
    }

    private static void doTryCatch() {
        try {
            Blackhole.consume(1);
        } catch (Exception ignored) {

        }
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

    @SuppressWarnings("unused")
    private static void getsHashSetOfArraysOfStrings(HashSet<String[]> strings, int a) {

    }

    private static String[][] returns2DArrayOfStrings() {
        String[][] strings = new String[5][2];
        strings[0][0] = "hello";
        strings[0][1] = "world";
        return strings;
    }

    private void doCondition() {
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
        for (int i = 0; i < 10000; i++) {
            hashSet.add(i * 2);
        }
    }

    @SuppressWarnings("unused")
    private void unused(Pattern[] patterns) {
//        State state = Profiler.methodEnter("desc",patterns.toString() + "some text");
//        state.methodFinish(" ");
    }

    public static void main(String[] args) throws InterruptedException {
        Sample sample = new Sample();
        sample.start();
    }
}
