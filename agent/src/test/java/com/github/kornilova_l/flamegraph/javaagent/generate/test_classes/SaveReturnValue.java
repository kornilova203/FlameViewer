package com.github.kornilova_l.flamegraph.javaagent.generate.test_classes;

import java.util.ArrayList;

public class SaveReturnValue {
    @SuppressWarnings("unused")
    static ArrayList<String> returnGeneric() {
        System.out.println("Hello, world!");
        ArrayList<String> list = new ArrayList<>();
        return list;
    }

    @SuppressWarnings("unused")
    int returnInt() {
        System.out.println("Hello, world!");
        return 23;
    }

    @SuppressWarnings("unused")
    void returnVoid(int i) {
        System.out.println("Hello, world!");
    }

    @SuppressWarnings("unused")
    String returnString() {
        System.out.println("Hello, world!");
        String hello = "hello";
        return hello;
    }

    @SuppressWarnings("unused")
    long returnLong() {
        System.out.println("Hello, world!");
        return 32;
    }
}
