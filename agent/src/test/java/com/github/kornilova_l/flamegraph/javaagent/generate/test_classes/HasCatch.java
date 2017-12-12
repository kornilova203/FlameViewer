package com.github.kornilova_l.flamegraph.javaagent.generate.test_classes;

public class HasCatch {
    public static void main(String[] args) {
        try {
            throw new AssertionError("");
        } catch (Error throwable) {
            System.out.println("Normal");
        }
    }
}
