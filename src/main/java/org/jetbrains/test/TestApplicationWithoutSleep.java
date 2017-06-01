//package org.jetbrains.test;
//
//import java.io.FileNotFoundException;
//import java.io.PrintWriter;
//
//import static org.jetbrains.test.Profiler.registerFinish;
//import static org.jetbrains.test.Profiler.methodStart;
//
///**
// * Created by Liudmila Kornilova
// * on 04.05.17.
// *
// * Application for testing Profiler
// * It does not have sleep() calls
// * it has only one thread and determined call tree with following structure:
// * start
// *  fun1
// *      fun3
// *          fun4
// *          fun4
// *          fun5
// *              fun6
// *                  fun7
// *                      fun8
// *  fun2
// */
//
//public class TestApplicationWithoutSleep {
//    private void fun1() {
//        methodStart("");
//        fun3();
//        registerFinish();
//    }
//
//    private void fun2() {
//        methodStart("");
//        registerFinish();
//    }
//
//    private void fun3() {
//        methodStart("");
//        fun4();
//        fun4();
//        fun5();
//        registerFinish();
//    }
//
//    private void fun4() {
//        methodStart("");
//        registerFinish();
//    }
//
//    private void fun5() {
//        methodStart("");
//        fun6();
//        registerFinish();
//    }
//
//    private void fun6() {
//        methodStart("");
//        fun7();
//        registerFinish();
//    }
//
//    private void fun7() {
//        methodStart("");
//        fun8();
//        registerFinish();
//    }
//
//    private void fun8() {
//        methodStart("");
//        registerFinish();
//    }
//
//    public void start() {
//        methodStart("");
//        fun1();
//        fun2();
//        registerFinish();
//    }
//
//    public static void main(String[] args) throws InterruptedException, FileNotFoundException {
//        TestApplicationWithoutSleep ta = new TestApplicationWithoutSleep();
//        ta.start();
//        System.out.println(Profiler.getString()); // print to console
//        // export to JSON
//        PrintWriter writer = new PrintWriter("JSON/TestApplicationWithoutSleep.json");
//        writer.print(Profiler.getJson());
//        writer.close();
//    }
//}
