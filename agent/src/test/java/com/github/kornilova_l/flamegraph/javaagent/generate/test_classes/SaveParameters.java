package com.github.kornilova_l.flamegraph.javaagent.generate.test_classes;

import java.util.ArrayList;

public class SaveParameters {
    void noParams() {
        System.out.println("Hello, world!");
    }

    void oneParam(int i) {
        System.out.println("Hello, world!");
    }

    void twoParams(int i, ArrayList<String> list) {
        System.out.println("Hello, world!");
    }

    static void threeParams(boolean b, long l, String s) {
        System.out.println("Hello, world!");
    }
}
