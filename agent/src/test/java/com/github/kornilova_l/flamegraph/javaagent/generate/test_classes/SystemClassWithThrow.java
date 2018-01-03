package com.github.kornilova_l.flamegraph.javaagent.generate.test_classes;

/**
 * Sometimes it is needed to instrument system classes.
 * System classes are loaded by bootstrap and it is not possible to use
 * not system classes in their methods.
 */
public class SystemClassWithThrow {
    @SuppressWarnings("unused")
    public void method() {
        throw new AssertionError("Something went wrong");
    }
}
