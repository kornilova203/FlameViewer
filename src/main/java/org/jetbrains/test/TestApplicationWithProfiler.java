package org.jetbrains.test;

import java.io.FileWriter;
import java.io.IOException;

import static Profiler.Profiler.methodStart;

/**
 * Created by Liudmila Kornilova
 * on 04.05.17.
 *
 * Application for testing Profiler
 * it has only one thread and determined call tree with following structure:
 * start
 *  fun1
 *      fun3
 *          fun4
 *          fun4
 *          fun5
 *              fun6
 *  fun2
 */

public class TestApplicationWithProfiler {
    private void fun1() throws InterruptedException {
        methodStart();
        Thread.sleep(3);
        fun3();
        Thread.sleep(5);
//        state.methodFinish();
    }

    private void fun2() throws InterruptedException {
//        State state = methodStart();
        Thread.sleep(10);
//        state.methodFinish();
    }

    private void fun3() throws InterruptedException {
//        State state = methodStart();
        Thread.sleep(3);
        fun4();
        Thread.sleep(3);
        fun4();
        fun5();
//        state.methodFinish();
    }

    private void fun4() throws InterruptedException {
//        State state = methodStart();
        Thread.sleep(20);
//        state.methodFinish();
    }

    private void fun5() throws InterruptedException {
//        State state = methodStart();
        Thread.sleep(40);
        fun6();
        Thread.sleep(10);
//        state.methodFinish();
    }

    private void fun6() throws InterruptedException {
//        State state = methodStart();
        Thread.sleep(15);
//        state.methodFinish();
    }

    public void start() throws InterruptedException {
//        State state = methodStart();
        Thread.sleep(20);
        fun1();
        Thread.sleep(30);
        fun2();
        Thread.sleep(5);
//        state.methodFinish();
    }

    public static void main(String[] args) throws InterruptedException, IOException {
        FileWriter fileWriter = new FileWriter("out.txt");
        fileWriter.close();
        TestApplicationWithProfiler ta = new TestApplicationWithProfiler();
        ta.start();
    }
}
