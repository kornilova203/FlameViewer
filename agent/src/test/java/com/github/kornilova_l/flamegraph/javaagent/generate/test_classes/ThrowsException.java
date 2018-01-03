package com.github.kornilova_l.flamegraph.javaagent.generate.test_classes;

public class ThrowsException {
    public static void main(String[] args) {
        throw new AssertionError("error");
    }
}
