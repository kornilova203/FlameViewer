package com.github.kornilova_l.flamegraph.javaagent.generate.test_classes;

import java.util.ArrayList;

public class SaveParameters {
    @SuppressWarnings("unused")
    void noParams() {
        System.out.println("Hello, world!");
    }

    @SuppressWarnings("unused")
    void oneParam(int i) {
        System.out.println("Hello, world!");
    }

    @SuppressWarnings("unused")
    void twoParams(int i, ArrayList<String> list) {
        System.out.println("Hello, world!");
    }

    @SuppressWarnings("unused")
    static void threeParams(boolean b, long l, String s) {
        System.out.println("Hello, world!");
    }
}
